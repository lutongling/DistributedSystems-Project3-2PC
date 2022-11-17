package coordinator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import server.KeyValue;
import utils.KVLogger;
import utils.KVOperation;

/**
 * This class represents a coordinator. It implements ICoordinator interface.
 * It extends UnicastRemoteObject to apply RMI to be connected by the replica servers.
 * It maintains the connected server instances/replicas by using a List.
 */
public class Coordinator extends UnicastRemoteObject implements ICoordinator {
  private List<KeyValue> replicas;
  private KVLogger logger;
  private int port;
  private int votes;

  public Coordinator(int portNum) throws RemoteException {
    super();
    replicas = new ArrayList<>();
    logger = new KVLogger("CoordinatorController");
    port = portNum;
    votes = 0;

  }

  @Override
  public void addReplicaServer(KeyValue replicaServer) throws RemoteException {
    replicas.add(replicaServer);
  }

  @Override
  public synchronized boolean prepare(KVOperation operation) throws RemoteException {
    votes = 0;

    // thread safe container
    Vector<Thread> threads = new Vector<>();

    for(int i = 0; i < replicas.size(); i++) {
      logger.logInfoMessage("initiating prepare for server: " + i);
      KeyValue currentServer = replicas.get(i);

      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          logger.logInfoMessage("Thread-ID: " + Thread.currentThread().getId());
          try {
            if(currentServer.isReplicaAgreed(operation)) {
              votes++;
            }
          } catch (RemoteException e) {
            logger.logErrorMessage("Something went wrong while preparing for all servers");
            e.printStackTrace();
          }
        }
      });

      threads.add(t);
      t.start();
    }

    // make sure all threads done
    for(Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return votes == replicas.size();
  }

  @Override
  public synchronized void commit(KVOperation operation) throws RemoteException {
    for (KeyValue currentServer : replicas) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            currentServer.serverCommit(operation);
          } catch (RemoteException e) {
            logger.logErrorMessage("Something went wrong while committing");
          }
        }
      }).start();
    }

    logger.logInfoMessage("initiating global commit!");

  }

  @Override
  public synchronized void abort(KVOperation operation) throws RemoteException {
    for (KeyValue currentServer : replicas) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            currentServer.serverAbort(operation);
          } catch (RemoteException e) {
            logger.logErrorMessage("Something went wrong while aborting");
          }
        }
      }).start();
    }

    logger.logInfoMessage("initiating global abort!");
  }
}
