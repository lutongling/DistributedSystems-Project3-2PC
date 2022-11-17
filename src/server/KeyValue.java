package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import coordinator.ICoordinator;
import utils.KVOperation;

/**
 * This Interface represents key-value pair operations applied to the Server.
 * It extends Remote interface to apply RMI.
 */
public interface KeyValue extends Remote {

  /**
   * Set the coordinator to the server replica.
   * It will be called when we create a new replica/server to connect with the coordinator.
   * @param coordinator the given coordinator to be set
   * @throws RemoteException
   */
  void setCoordinator(ICoordinator coordinator) throws RemoteException;

  /**
   * Return true if all the replicas are agreed on the given operation, otherwise return false.
   * This is called by the prepare method in the coordinator.
   * @param operation the given operation
   * @return true if all the replicas are agreed on the given operation, otherwise return false
   * @throws RemoteException
   */
  boolean isReplicaAgreed(KVOperation operation) throws RemoteException;

  /**
   * Perform commit by the server replica for given operation.
   * This is called by the commit method in the coordinator.
   * @param operation the given operation
   * @throws RemoteException
   */
  void serverCommit(KVOperation operation) throws RemoteException;

  /**
   * Perform abort by the server replica for given operation.
   * This is called by the abort method in the coordinator.
   * @param operation the given operation
   * @throws RemoteException
   */
  void serverAbort(KVOperation operation) throws RemoteException;

  /**
   * Insert a Key-Value pair to the storage.
   * Return true if it is agreed by all the peers, otherwise return false.
   *
   * NOTICE: Putting in an already-in-the-store key with a new value will override the old value
   * @param key the unique identifier of the key value pair to be inserted
   * @param value the value of the unique identifier key to be inserted
   * @return true if it is agreed by all the peers, otherwise return false
   * @throws RemoteException
   */
  boolean put(String key, String value) throws RemoteException;

  /**
   * Return the value of the given key, otherwise return null if the given key is not in the store
   * @param key the given key to get
   * @return the value of the given key, otherwise return null if the given key is not in the store
   * @throws RemoteException
   */
  String get(String key) throws RemoteException;

  /**
   * Return the code to specify the delete operation status.
   * For the current version:
   * return 404: key not found
   * return 200: delete successfully
   * return 500: all peers don't agree
   * @param key given key to be deleted
   * @return the corresponding code to specify the delete operation status
   * @throws RemoteException
   */
  int delete(String key) throws RemoteException;


}

