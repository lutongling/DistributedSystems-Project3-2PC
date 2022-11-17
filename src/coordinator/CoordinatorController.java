package coordinator;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import utils.KVLogger;

/**
 * This class is the Coordinator controller/app with a main method
 * It will use a hard-coded port number, letting servers connect to via RMI.
 */
public class CoordinatorController {
  private static KVLogger logger = new KVLogger("CoordinatorController");

  // In order to make test the main functionality easily,
  // we hard-coded the port number 1111 to let the servers connect to the coordinator easily
  // ***Notice that the connection between client and server is still via CLI args
  private static int portNum = 1111;

  public static void main(String[] args) {

    try {
      ICoordinator coordinator = new Coordinator(portNum);

      LocateRegistry.createRegistry(portNum);
      Naming.rebind("rmi://localhost:" + portNum + "/KeyValueCoordinator", coordinator);

      logger.logInfoMessage("Coordinator starts on port " + portNum);

    } catch (RemoteException | MalformedURLException e) {
      e.printStackTrace();
    }
  }
}
