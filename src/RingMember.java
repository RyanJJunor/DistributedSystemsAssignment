
/**
 * A node in the ring whose methods may be invoked from a non-local virtual machine.
 *
 * @author Student Number: 2522675
 */
public interface RingMember extends java.rmi.Remote {

    /**
     * Enters this instance of the class in to the critical section if the number of passes has not met the pass limit,
     * if it has, then the critical section will not be entered.
     * @param token a token object that is passed between the nodes, it counts the times passed to nodes, the pass limit
     *              and if it is to skip the 'nodeToSkip' this circulation (Advanced Feature 1).
     * @param fileName the name of the shared file (Advanced Feature 2)
     * @param extraTimeNode the id of the node that is to get extra time (Advanced Feature 4)
     * @param nodeToSkip the id of the node that is to be skipped every second pass (Advanced Feature 5)
     * @throws java.rmi.RemoteException if a problem occurs with the remote method call
     */
    public void takeToken(Token token, String fileName, int extraTimeNode, int nodeToSkip) throws java.rmi.RemoteException;

    /**
     * Calls cleanUp on the next node in the ring before unexporting itself, the next node in the ring will then repeat
     * this until all nodes have been unexported.
     * @throws java.rmi.RemoteException if a problem occurs with the remote method call
     */
    public void cleanUp() throws java.rmi.RemoteException;
}
