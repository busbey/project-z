/**
 * Interface for Movers.  By default, the Mover will be constructed using
 * the empty constructor.  If your Mover requires command-line arguments,
 * create a constructor that takes a String[] argument.  That constructor
 * will be called with any remaining arguments given in the call to 
 * Agent. 
 */

public interface Mover {

    public void respondToChange (State newState);

}
