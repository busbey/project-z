public interface Mover {
    
    /* oh by the way you need to have a constructor that takes in
       a String[] argument.  when your mover is called you get the
       remaining command line arguments passed to you */

    public Direction respondToChange (State newState);

}
