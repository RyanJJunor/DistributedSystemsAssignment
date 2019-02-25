import java.net.UnknownHostException;
import java.rmi.*;
import java.net.*;
import java.io.*;
import java.rmi.ConnectException;


/**
 * The manager which injects the first token in to the ring of nodes
 *
 * @author Student Number: 2522675
 */
public class RingManager {

    private RingMember rMember;

    /**
     * The constructor that is invoked to clean up the ring nodes
     *
     * @param ring_node_host the host address for the first node to clean up
     * @param ring_node_id the id for the first node to clean up
     * @throws RemoteException if a problem occurs with the remote method call
     */
    public RingManager(String ring_node_host, String ring_node_id) throws RemoteException {

        System.out.println("Cleaning!");

        try {
            //Returns the next node in the ring
            rMember = (RingMember) Naming.lookup("rmi://" + ring_node_host + "/node" + ring_node_id);
        } catch (NotBoundException e) {
            System.out.println("Ensure that the nodes are running and that the URL matches that of a node");
            System.exit(1);
        } catch (MalformedURLException e) {
            System.out.println("Please check that the URL you are looking up is correct.");
            System.exit(1);
        }catch (ConnectException ce){
            System.out.println("Cannot connect");
            System.exit(1);
        }

        try {
            //calls clean up on the next node in the ring
            rMember.cleanUp();
        }catch (ConnectException ce){
            System.out.println("Please ensure that the nodes are running!");
            System.exit(1);
        }catch (NullPointerException npe){
            //This exception will already have an error message output above
        }

        System.out.println("All cleaned up!");
    }

    /**
     * The constructor for injecting the first token
     * @param ring_node_host the host address for the first node to have the token injected
     * @param ring_node_id the id for the first node to have the token injected
     * @param fileName the name that the user has chosen for the shared file (Advanced Feature 2)
     * @param passLimit the maximum amount of passes that the user has chosen to allow (Advanced Feature 3)
     * @param extraTimeNode the id of the node that is to get extra time (Advanced Feature 4)
     * @param nodeToSkip the id of the node that is to be skipped every second pass (Advanced Feature 5)
     * @throws RemoteException
     */
    public RingManager(String ring_node_host, String ring_node_id, String fileName, int passLimit, int extraTimeNode,
                       int nodeToSkip) throws RemoteException {

        //Creates the token object to be passed between nodes (Advanced Feature 1).
        Token token = new Token(passLimit);

        try {
            System.out.println("Ring manager host is " + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("Ring element host is " + ring_node_host);
        System.out.println("Clearing " + fileName + ".txt file");

        //Create fileWriter and clear file
        try {
            FileWriter fileWriter = new FileWriter(fileName + ".txt", false);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Please check your file name is valid and doesn't include illegal characters: / ? < > \\ : * | \"");
            System.exit(1);
        }

        //Get remote reference to ring element/node and inject token by calling takeToken()
        System.out.println("Connecting to Node");

        try {
            rMember = (RingMember) Naming.lookup("rmi://" + ring_node_host + "/node" + ring_node_id);
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            System.out.println("Ensure that you have started RMI Registry");
        }

        try {
            rMember.takeToken(token, fileName, extraTimeNode, nodeToSkip);
        } catch (ConnectException ce) {
            System.out.println("Remote Object not found!\nPlease run the nodes!");
        }catch (NullPointerException npe){

        }

    }


    public static void main(String argv[]) throws RemoteException {

        if(argv.length==2){

            //This instance of RingManager will clean up the ring (Advanced Feature 6).
            RingManager clean = new RingManager(argv[0], argv[1]);

        }else {

            int passLimit = 0;
            int extraTimeNode = 0;
            int nodeToSkip = 0;

            try {
                try {
                    passLimit = Integer.parseInt(argv[3]);
                } catch (NumberFormatException e) {
                    System.out.println("Argument 4 not valid, please enter an integer.");
                }

                try {
                    extraTimeNode = Integer.parseInt(argv[4]);
                } catch (NumberFormatException e) {
                    System.out.println("Argument 5 not valid, please enter an integer.");
                }

                try {
                    nodeToSkip = Integer.parseInt(argv[5]);
                } catch (NumberFormatException e) {
                    System.out.println("Argument 6 not valid, please enter an integer.");
                }

                //Instantiate RingManager with parameters
                RingManager rm = new RingManager(argv[0], argv[1], argv[2], passLimit, extraTimeNode, nodeToSkip);
            }catch (ArrayIndexOutOfBoundsException e){
                System.out.println("Please check you have the correct number of parameters and that your file name is " +
                        "valid! ");
            }
        }

    }
}  