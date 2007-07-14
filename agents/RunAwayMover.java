import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RunAwayMover implements Mover {
    
    private Random random;
    private byte goal;

    private SmarterGreedyMover smarterGreedyMover;

    private final double PROBABILITY = 0.85;

    public RunAwayMover (String[] args) {
	goal = (byte)(args[0].charAt(0));
	random = new Random();
	smarterGreedyMover = new SmarterGreedyMover(args);
    }
	
    public Direction respondToChange (State newState) {
	Direction smartMove = smarterGreedyMover.respondToChange(newState);
	
	switch (smartMove) {
	case UP:
	    return Direction.DOWN;
	case DOWN:
	    return Direction.UP;
	case LEFT:
	    return Direction.RIGHT;
	}

	return Direction.LEFT;
    }

}
