package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import server.KeyValue;
import utils.KVLogger;

/**
 * This class is the Client controller/app with a main method to take 2 command line arguments:
 * 1. port number 2. host name / IP address
 * to connect to the server by looking up the object reference in the remote control by knowing the
 * specific service name.
 */
public class ClientController {
  private static KVLogger logger = new KVLogger("ClientController");

  public static void main(String[] args) {
    // input args contain a port number and a host name
    if (args == null || args.length != 2) {
      logger.logErrorMessage("Please enter in valid format: java ClientController <Port Number> <Host Name>");
      System.exit(1);
    }

    try {
      int portNum = Integer.parseInt(args[0]);
      String hostName = args[1];

      KeyValue kv = (KeyValue) Naming.lookup("rmi://" + hostName + ":" + portNum + "/KeyValueService");

      // run prepopulate hard-coded operations before prompting user to input
      List<String> preOperations = prepopulateOperations();

      // run prepopulate hard-coded operations before prompting user to input
      for (String str : preOperations) {
        handleCMD(str, hostName, portNum, kv);
      }

      System.out.println();

      // Then prompt client to enter cmd
      BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

      while (true) {
        System.out.println("Enter the operation to be performed:");
        System.out.println("PUT <key> <value>");
        System.out.println("GET <key>");
        System.out.println("DELETE <key>");

        String str = userInput.readLine();

        // handle the put/get/delete commands
        String cmd = handleCMD(str, hostName, portNum, kv);

        // handle the exit command and other invalid commands
        if (cmd.equalsIgnoreCase("exit")) {
          break;
        } else if (!(cmd.equalsIgnoreCase("put")
                || cmd.equalsIgnoreCase("get")
                || cmd.equalsIgnoreCase("delete"))) {
          printInvalid("", str);
        }

      }

    } catch (NumberFormatException e) {
      logger.logErrorMessage("Please enter a pure number for port number --- " + e.getMessage());
    } catch (MalformedURLException | NotBoundException | RemoteException e) {
      // catch the exceptions for lookup and remote
      e.printStackTrace();
    } catch (IOException e) {
      // catch the I/O Exception
      logger.logErrorMessage(e.getMessage());
    }
  }

  /**
   * The helper method for handling commands start with put/get/delete, to avoid code duplication.
   * Should print out corresponding log messages with host name and port number information.
   * Return the command, which is the first element of the string separated by space.
   *
   * @param str      the given cmd as a string
   * @param hostName the host name
   * @param portNum  the port number
   * @param kv       the remote object reference
   * @return the command, which is the first element of the string separated by space
   * @throws RemoteException
   */
  private static String handleCMD(String str, String hostName, int portNum, KeyValue kv) throws RemoteException {
    String[] strings = str.split("\\s+");
    String cmd = strings[0];

    if (cmd.equalsIgnoreCase("put")) {
      if (strings.length != 3) {
        printInvalid("PUT", str);
        return cmd;
      }

      logger.logInfoMessage("; HOST: " + hostName + "; "
              + "PORT_NO: " + portNum + "; "
              + "REQUEST - PUT; "
              + "KEY: " + strings[1] + "; "
              + "VALUE: " + strings[2]);

      boolean isPut = kv.put(strings[1], strings[2]);
      if (isPut) {
        logger.logInfoMessage("; RESPONSE - Put operation successful");
      } else {
        logger.logWarningMessage("; RESPONSE - Something went wrong while storing " + strings[1]);
      }

    } else if (cmd.equalsIgnoreCase("get")) {
      if (strings.length != 2) {
        printInvalid("GET", str);
        return cmd;
      }

      logger.logInfoMessage("; HOST: " + hostName + "; "
              + "PORT_NO: " + portNum + "; "
              + "REQUEST - GET; "
              + "KEY: " + strings[1] + "; ");

      String val = kv.get(strings[1]);
      if (val != null) {
        logger.logInfoMessage("; RESPONSE - " + val);
      } else {
        logger.logWarningMessage("; RESPONSE - key not found");
      }

    } else if (cmd.equalsIgnoreCase("delete")) {
      if (strings.length != 2) {
        printInvalid("DELETE", str);
        return cmd;
      }

      logger.logInfoMessage("; HOST: " + hostName + "; "
              + "PORT_NO: " + portNum + "; "
              + "REQUEST - DELETE; "
              + "KEY: " + strings[1] + "; ");

      int deleteCode = kv.delete(strings[1]);
      if (deleteCode == 200) {
        logger.logInfoMessage("; RESPONSE - Delete operation successful");
      } else if (deleteCode == 404){
        logger.logWarningMessage("; RESPONSE - key not found");
      } else {
        logger.logErrorMessage("Something went wrong while deleting " + strings[1]);
      }

    }

    return cmd;
  }

  /**
   * Helper method for print invalid request log messages, to avoid code duplication.
   *
   * @param type request type
   * @param str  request content
   */
  private static void printInvalid(String type, String str) {
    if (type.equals(""))
      logger.logWarningMessage("Invalid request received => " + str);
    else
      logger.logWarningMessage(String.format("Invalid %s request received => " + str, type));
  }

  /**
   * Return a list of strings that represents hard-coded prepopulate PUT/GET/DELETE operations
   * to test before prompting user to give inputs.
   *
   * @return a list of strings that represents hard-coded prepopulate PUT/GET/DELETE operations
   */
  private static List<String> prepopulateOperations() {
    List<String> preOperations = new ArrayList<>();
    preOperations.add("put firstKey firstVal");
    preOperations.add("get firstKey");
    preOperations.add("delete firstKey");
    preOperations.add("put secondKey secondVal");
    preOperations.add("get secondKey");
    preOperations.add("delete secondKey");
    preOperations.add("put thirdKey thirdVal");
    preOperations.add("get thirdKey");
    preOperations.add("put thirdKey anotherThirdVal");
    preOperations.add("get thirdKey");
    preOperations.add("delete thirdKey");
    preOperations.add("put fourthKey fourthVal");
    preOperations.add("get fourthKey");
    preOperations.add("delete fourthKey");
    preOperations.add("put fifthKey fifthVal");
    preOperations.add("delete fifthKey");

    return preOperations;
  }

}

