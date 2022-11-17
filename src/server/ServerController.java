package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import coordinator.ICoordinator;
import utils.KVLogger;

/**
 * This class is the Server controller/app with a main method to take 1 command line argument:
 * 1. port number
 * It will then create a service object reference and rebind it to remote control by giving it a
 * specific name. Then the reference can be accessed by the client.
 *
 * It will also use the hard-coded port number of the coordinator to connect to the 2PC protocol.
 */
public class ServerController {
  private static KVLogger logger = new KVLogger("ServerController");

  public static void main(String[] args) {
    if (args.length != 1 || !args[0].matches("\\d+")) {
      logger.logErrorMessage("Please enter in valid format: java ServerController <Port Number>");
      System.exit(1);
    }

    // valid CLI args: a port number
    int portNum = Integer.parseInt(args[0]);

    try {
      KeyValue kv = new KeyValueStore();

      // add replica and set coordinator for this server --- connect to coordinator
      ICoordinator coordinator = (ICoordinator) Naming.lookup("rmi://localhost:1111/KeyValueCoordinator");
      coordinator.addReplicaServer(kv);
      kv.setCoordinator(coordinator);

      LocateRegistry.createRegistry(portNum);
      Naming.rebind("rmi://localhost:" + portNum + "/KeyValueService", kv);

      logger.logInfoMessage("Server starts on port " + portNum);

    } catch (RemoteException | MalformedURLException | NotBoundException e) {
      logger.logErrorMessage(e.getMessage());
    }

  }
}

