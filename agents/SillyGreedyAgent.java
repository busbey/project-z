import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class SillyGreedyAgent extends Agent {
    
    private Random random;
    private char goal;

    public static void main (String[] args) {
	if (args.length < 3) {
	    System.out.println("java SillyGreedyAgent [hostname] [port] [goal]");
	    return;
	}
	String hostname = args[0];
	int port = Integer.parseInt(args[1]);
	SillyGreedyAgent agent = 
	    new SillyGreedyAgent(hostname, port, args[2].charAt(0));
	if (!agent.openConnection())
	    return;
	agent.runAgent();
    }

    public SillyGreedyAgent (String hostname, int port, char goal) {
	super(hostname, port);
	this.goal = goal;
	random = new Random();
    }
	
    public void respondToChange (State newState) {
	Direction move;
	HashMap<Character, ArrayList<Position>> sortedByType = 
	    newState.sortByType();

	Position myPosition =
	    sortedByType.get(newState.getPlayer()).get(0);

	Position goalPosition = sortedByType.get(goal).get(0);

	int verticalDistance = Math.abs(myPosition.row() - 
					goalPosition.row());
	int horizontalDistance = Math.abs(myPosition.column() - 
					  goalPosition.column());
	int totalDistance = verticalDistance + horizontalDistance;
	
	double randomMove = random.nextDouble();
	 
	if (randomMove < verticalDistance / (totalDistance + 1.)) {
	    if (myPosition.row() < goalPosition.row())
		move = Direction.DOWN;
	    else
		move = Direction.UP;
	}
	else if (randomMove < totalDistance / (totalDistance + 1.)) {
	    if (myPosition.column() < goalPosition.column())
		move = Direction.RIGHT;
	    else
		move = Direction.LEFT;
	}
	else
	    move = randomMove();

	try {
	    outStream.writeChar(move.getChar());
	    outStream.flush();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /* pick a move, yo! */
    private Direction randomMove () {
	Direction[] values = Direction.values();
	return values[random.nextInt(values.length)];
    }
    
}
