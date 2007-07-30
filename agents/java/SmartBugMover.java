public class SmartBugMover implements Mover {
    
    private SmarterGreedyMover goalMover, eatingMover;;

    private final double PROBABILITY = 0.7;

    public SmartBugMover (String[] args) {
	goalMover = new SmarterGreedyMover(new String[] {args[0]});
	eatingMover = new SmarterGreedyMover(new String[] {args[1]});
    }
	
    public Direction respondToChange (State newState) {
	if (newState.killerBug())
	    return eatingMover.respondToChange(newState);
	return goalMover.respondToChange(newState);
    }

}
