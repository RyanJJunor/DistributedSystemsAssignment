import java.io.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.util.*;

/**
 * A critical section thread that the node enters into in order to alter the shared file before being released and then
 * the next node is given the token.
 *
 * @author Student Number: 2522675
 */
public class CriticalSection extends Thread {

    private String this_id;
    private String this_host;
    private String next_id;
    private String next_host;

    private Token token;
    private String fileName;
    private int extraTimeNode;
    private int nodeToSkip;

    private RingMember rMember;

    private Random rand;

    /**
     * The critical section thread is created and the data concerning the current and next node is initialised along
     * with other information concerning the node network.
     *
     * @param t_host        the host address of this node
     * @param t_id          the id of this node
     * @param n_host        the host address of the next node in the ring
     * @param n_id          the id of the next node in the ring
     * @param token         a token object that is passed between the nodes, it counts the times passed to nodes, the pass limit
     *                      and if it is to skip the 'nodeToSkip' this circulation.
     * @param fileName      the name of the shared file (Advanced Feature 2)
     * @param extraTimeNode the id of the node that is to get extra time (Advanced Feature 4)
     * @param nodeToSkip    the id of the node that is to be skipped every second pass (Advanced Feature 5)
     */
    public CriticalSection(String t_host, String t_id, String n_host, String n_id, Token token, String fileName,
                           int extraTimeNode, int nodeToSkip) {

        this_host = t_host;
        this_id = t_id;
        next_host = n_host;
        next_id = n_id;

        this.token = token;
        this.fileName = fileName;
        this.extraTimeNode = extraTimeNode;
        this.nodeToSkip = nodeToSkip;

        rand = new Random();
    }

    /**
     * Checks to see if the current node needs to be skipped (Advanced Feature 5), if so, the token is passed again. If not, it carries on and
     * checks if the node required extra time (Advanced Feature 4). Then the shared file is written to and the next node
     * Ring Member is found and calls takeToken();.
     */
    public void run() {

        int currentId;

        currentId = Integer.parseInt(this_id);

        /*If the current node is the one the user would like to skip every 2nd visit and it is the second visit, pass the
        token on to the next node and flip the toSkip variable in the token (Advanced Feature 5)*/

        if ((currentId == nodeToSkip) && (token.getToSkip())) {

            System.out.println("Node skipped!");
            System.out.println("-------------\n");

            token.changeToSkip();

            try {
                rMember = (RingMember) Naming.lookup("rmi://" + next_host + "/node" + next_id);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }

            try {
                rMember.takeToken(token, fileName, extraTimeNode, nodeToSkip);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else {

            System.out.println("Token received: entering critical section");
            token.passToken();

            if (currentId == nodeToSkip)
                token.changeToSkip();

            //Sleep to symbolise critical section duration

            //If this node is the one that is to receive extra time, provide longer sleep (Advanced Feature 4)
            if ((Integer.parseInt(this_id) % extraTimeNode) == 0) {
                System.out.println("Node receiving extra time.");
                try {
                    Thread.sleep(rand.nextInt(1000) + 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {

                try {
                    Thread.sleep(rand.nextInt(500) + 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


            //Write timestamp (date) to file

            try {
                System.out.println("Writing to file: " + fileName + ".txt");
                Date timestmp = new Date();
                String timestamp = timestmp.toString();
                //Next create fileWriter - true means writer *appends*
                FileWriter fileWriter = new FileWriter(fileName + ".txt", true);
                //Create PrintWriter - true = flush buffer on each println
                PrintWriter printWriter = new PrintWriter(fileWriter, true);
                //println means adds a newline (as distinct from print)
                printWriter.println("Record from ring node on host "
                        + this_host + ", port number " + this_id + ", is " + timestamp + " Token count value is: "
                        + token.getObjectsPassedTo());
                printWriter.close();
                fileWriter.close();
            } catch (java.io.IOException e) {

                System.out.println("Error writing to file: " + e);
            }


            System.out.println("Look up RMI registry with: rmi://" + next_host + "/node" + next_id);

            //Get remote reference to next ring element, and pass token on ...
            try {
                rMember = (RingMember) Naming.lookup("rmi://" + next_host + "/node" + next_id);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
            System.out.println("Received token count value is: " + token.getObjectsPassedTo());

            try {
                rMember.takeToken(token, fileName, extraTimeNode, nodeToSkip);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            System.out.println("Token released: exiting critical section");
            System.out.println("----------------------------------------\n");

        }
    }
}