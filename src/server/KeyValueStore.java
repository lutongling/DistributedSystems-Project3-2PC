package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import coordinator.ICoordinator;
import utils.KVLogger;
import utils.KVOperation;

/**
 * This class represents a key-value storage of memory. It implements KeyValue interface.
 * It extends UnicastRemoteObject to apply RMI.
 * It is set up with using a Hashmap to store key value pairs.
 */
public class KeyValueStore extends UnicastRemoteObject implements KeyValue {
  private Map<String, String> dictionary;
  private KVLogger logger;
  private ICoordinator coordinator;

  /**
   * Must explicit throw the RemoteException in the constructor.
   * @throws RemoteException
   */
  public KeyValueStore() throws RemoteException {
    super();
    dictionary = new HashMap<>();
    logger = new KVLogger("ServerController");
  }

  @Override
  public void setCoordinator(ICoordinator coordinator) throws RemoteException {
    this.coordinator = coordinator;
  }

  @Override
  public boolean isReplicaAgreed(KVOperation operation) throws RemoteException {
    // use executor framework with Callable and Future
    ExecutorService executor = Executors.newSingleThreadExecutor();
    FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        try {
          String operationType = operation.getType();

          return operationType.equalsIgnoreCase(KVOperation.Type.DELETE.toString())
                  || operationType.equalsIgnoreCase(KVOperation.Type.PUT.toString());

        } catch (Exception e) {
            return false;
        }
      }
    });

    // future
    try {
      executor.submit(futureTask);

      logger.logInfoMessage("prepare " + operation.getType()
              + " called from coordinator for " + operation.getKey() + "-" + operation.getVal());
      // timeout for 5 seconds to handle server failures
      return futureTask.get(5, TimeUnit.SECONDS);

    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void serverCommit(KVOperation operation) throws RemoteException {
    String operationType = operation.getType();

    logger.logInfoMessage("global commit " + operation.getType()
            + " called from coordinator for " + operation.getKey() + "-" + operation.getVal());

    if(operationType.equalsIgnoreCase(KVOperation.Type.PUT.toString())) {
      dictionary.put(operation.getKey(), operation.getVal());
      logger.logInfoMessage("Successfully stored " + operation.getKey() + "-" + operation.getVal());

    } else if (operationType.equalsIgnoreCase(KVOperation.Type.DELETE.toString())){
      if(dictionary.containsKey(operation.getKey())) {
        dictionary.remove(operation.getKey());
        logger.logInfoMessage("Successfully deleted " + operation.getKey());
      }
    } else {
      // expectation: never get here
      logger.logErrorMessage("Wrong commands input. Should be handled by client-side.");
    }
  }

  @Override
  public void serverAbort(KVOperation operation) throws RemoteException {
    // should be having some logic if the program includes Transactions
    // but the current version only consider single operations without Transactions
    logger.logInfoMessage("global abort " + operation.getType()
            + " called from coordinator for " + operation.getKey() + "-" + operation.getVal());
    return;
  }

  @Override
  public boolean put(String key, String value) throws RemoteException {
    KVOperation operation = new KVOperation(KVOperation.Type.PUT, key, value);
    boolean isAllVoted = coordinator.prepare(operation);

    if(isAllVoted) {
      logger.logInfoMessage("all peers agree, initiating global commit for put!");
      logger.logInfoMessage("REQUEST - PUT; KEY => " + key + "; VALUE => " + value);
      logger.logInfoMessage("Response => code: 200;");
      coordinator.commit(operation);

      return true;

    } else {
      logger.logErrorMessage("all peers don't agree, initiating global abort for put!");
      coordinator.abort(operation);
      logger.logErrorMessage("Something went wrong while storing " + key);

      return false;
    }

  }

  @Override
  public String get(String key) {
    logger.logInfoMessage("REQUEST - GET; KEY => " + key);

    if(!dictionary.containsKey(key)) {
      logger.logWarningMessage("Response => code: 404; "
              + "message: key not found");
      return null;
    }

    String val = dictionary.get(key);
    logger.logInfoMessage("Response => code: 200; "
            + "message: " + val);

    return val;
  }

  @Override
  public int delete(String key) throws RemoteException {
    KVOperation operation = new KVOperation(KVOperation.Type.DELETE, key, null);
    boolean isAllVoted = coordinator.prepare(operation);

    if(isAllVoted) {
      logger.logInfoMessage("all peers agree, initiating global commit for delete!");
      if(!dictionary.containsKey(operation.getKey())) {
        logger.logWarningMessage("Response => code: 404; "
                + "message: key not found");
        coordinator.commit(operation);

        return 404;
      } else {
        logger.logInfoMessage("Response => code: 200; "
                + "message: Delete operation successful");
        coordinator.commit(operation);

        return 200;
      }

    } else {
      logger.logErrorMessage("all peers don't agree, initiating global abort for delete!");
      coordinator.abort(operation);
      logger.logErrorMessage("Something went wrong while deleting " + key);

      return 500;
    }
  }

}

