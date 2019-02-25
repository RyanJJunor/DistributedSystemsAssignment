import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;

/**
 * A RingMemberImpl instance is a node connected in a ring of other RingMemberImpl instances in which a token is passed
 * through.
 *
 * @author Student Number: 2522675
 */
public class RingMemberImpl extends java.rmi.server.UnicastRemoteObject implements RingMember {

    private String next_id;
    private String next_host;
    private String this_id;
    private String this_host;
    private RingMember rMember;
    private boolean finished = false;

    /**
     * Creates a RingMemberImpl instance with the parameters supplied
     *
     * @param t_node the host address of this node
     * @param t_id the id of this node
     * @param n_node the host address of the next node in the ring
     * @param n_id the id of the next node in the ring
     * @throws RemoteException if a problem occurs with the remote method call
     */
    public RingMemberImpl(String t_node, String t_id, String n_node, String n_id) throws RemoteException {
        this_host = t_node;
        this_id = t_id;
        next_host = n_node;
        next_id = n_id;
    }

    /**
     * Enters this instance of the class in to the critical section if the number of passes has not met the pass limit,
     * if it has, then the critical section will not be entered.
     * @param token a token object that is passed between the nodes, it counts the times passed to nodes, the pass limit
     *              and if it is to skip the 'nodeToSkip' this circulation.
     * @param fileName the name of the shared file (Advanced Feature 2)
     * @param extraTimeNode the id of the node that is to get extra time (Advanced Feature 4)
     * @param nodeToSkip the id of the node that is to be skipped every second pass (Advanced Feature 5)
     * @throws RemoteException if a problem occurs with the remote method call
     */
    public synchronized void takeToken(Token token, String fileName, int extraTimeNode, int nodeToSkip) throws RemoteException {

        System.out.println("Entered method: " + Thread.currentThread().getStackTrace()[1].getMethodName() + " : " +
                Thread.currentThread().getStackTrace()[1].getClassName());

        //Check to see if the token has reached it's pass limit (Advanced Feature 3)
        if (token.getObjectsPassedTo() != token.getPassLimit()) {
            //start critical section by instantiating and starting critical section thread
            CriticalSection cs = new CriticalSection(this_host, this_id, next_host, next_id, token, fileName,
                    extraTimeNode, nodeToSkip);
            cs.start();

        } else {
            System.out.println("Circulation finished!");
        }

        System.out.println("Exiting method: " + Thread.currentThread().getStackTrace()[1].getMethodName() + " : " +
                Thread.currentThread().getStackTrace()[1].getClassName());
    }

    /**
     * Calls cleanUp on the next node in the ring which will then call cleanUp on the node after that, this will continue
     * until it returns to the original node, then each node will unbind and unexport itself
     *
     * @throws RemoteException if a problem occurs with the remote method call
     */
    public void cleanUp() throws RemoteException {

        System.out.println("Enter cleanUp");

        if (!finished) {

        try {
            rMember = (RingMember) Naming.lookup("rmi://" + next_host + "/node" + next_id);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }

        //This ensures that the first time this is called it calls cleanUp in the next node of the ring

            finished = true;
            rMember.cleanUp();
        }

        try {
            //Unbinds the name from its object
            Naming.unbind("rmi://" + this_host + "/node" + this_id);
        } catch (NotBoundException | MalformedURLException e) {
            /*This will produce an exception as when it gets to the last node in the ring, it will try to unbind the
            first node again.*/
        }
        try {
            //Unexport the object
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException nsoe) {
            /*This will produce an exception as when it gets to the last node in the ring, it will try to unexport the
            first node again.*/
        }

        System.out.println("Exit cleanUp");

    }

    public static void main(String argv[]) throws RemoteException {

        String hostName = null;

        System.setSecurityManager(new SecurityManager());

        //instantiate RingMemberImpl class with appropriate parameters
        RingMemberImpl ringImpl = new RingMemberImpl(argv[0], argv[1], argv[2], argv[3]);

        System.out.println("Host " + argv[1]);

        //Gets the host name for informative output messages
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("Ring member's hostname is " + hostName);
        System.out.println("Ring member " + hostName + " is binding to RMI Registry");

        //Register object with RMI registry
        try {
            Naming.rebind("rmi://" + argv[0] + "/node" + argv[1], ringImpl);
        } catch (ConnectException ce) {
            System.out.println("Ensure you have started the RMI Registry");
        } catch (AccessControlException ace) {
            System.out.println("Please check the policy file");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        System.out.println("Ring member " + hostName + " is bound with the RMI Registry");

    }


}