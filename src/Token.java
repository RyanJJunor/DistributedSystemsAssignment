import java.io.Serializable;

/**
 * The token object that is passed between the nodes and keeps track of how many it has been passed between
 * (Advanced Feature 1). Also holds the maximum amount of passes decided by the user (Advanced Feature 3) and also
 * contains a variable that dictates if the node that the user wishes to skip every second visit is to be skipped this
 * visit (Advanced Feature 5)
 *
 * @author Student Number: 2522675
 */
public class Token implements Serializable {

    private int objectsPassedTo = 0;
    private int passLimit;
    private boolean toSkip = false;

    /**
     * Creates an instance of Token with a limit of how many times it can be passed.
     * @param passLimit the limit of how many times it can be passed (Advanced Feature 3).
     */
    public Token(int passLimit) {

        this.passLimit = passLimit;

    }

    /**
     * Increments the tokens counter variable by 1
     */
    public void passToken() {
        objectsPassedTo++;
    }

    /**
     *
     * @return the amount of times the token has being passed
     */
    public int getObjectsPassedTo() {
        return objectsPassedTo;
    }

    /**
     *
     * @return the limit to how many times the token can be passed (Advanced Feature 3).
     */
    public int getPassLimit(){
        return passLimit;
    }

    /**
     *
     * @return whether this is the second pass of the node to be skipped and the node is to be skipped
     */
    public boolean getToSkip(){
        return toSkip;
    }

    /**
     * Alters the variable that contains whether the next visit to the node that is to be skipped every second visit or not.
     */
    public void changeToSkip(){
            toSkip = !toSkip;

        }
    }

