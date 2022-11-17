package coordinator;

import java.rmi.Remote;
import java.rmi.RemoteException;

import server.KeyValue;
import utils.KVOperation;

/**
 * This Interface represents the 2PC protocol, set by a coordinator, which will let the server
 * replicas/instances to connect.
 * It will perform two phases: 1) Voting phase(prepare)  2) Decision phase(commit / abort)
 */
public interface ICoordinator extends Remote {

  /**
   * Add replicas server to the program.
   * @param replicaServer the replica given to be added.
   * @throws RemoteException
   */
  void addReplicaServer(KeyValue replicaServer) throws RemoteException;

  /**
   * Phase 1: voting phase for the 2PC protocol.
   * Return true if all the replica servers agreed, otherwise return false.
   * @param operation the given operation for the coordinator to prepare
   * @return true if all the replica servers agreed, otherwise return false
   * @throws RemoteException
   */
  boolean prepare(KVOperation operation) throws RemoteException;

  /**
   * Phase 2: case 1 - commit.
   * If the voting result in Phase 1 is all replicas agreed, then commit.
   * @param operation the given operation for the coordinator to commit
   * @throws RemoteException
   */
  void commit(KVOperation operation) throws RemoteException;

  /**
   * Phase 2: case 2 - abort.
   * If the voting result in Phase 1 is all replicas don't agree, then abort.
   * @param operation the given operation for the coordinator to abort
   * @throws RemoteException
   */
  void abort(KVOperation operation) throws RemoteException;

}
