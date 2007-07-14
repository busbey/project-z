import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RunAwayAgent extends Agent {
    
    private Random random;
    private byte goal;

    private final double PROBABILITY = 0.85;

    public static void main (String[] args) {
	if (args.length < 3) {
	    System.out.println("java RunAwayAgent [hostname] [port] [goal]");
	    return;
	}
	String hostname = args[0];
	int port = Integer.parseInt(args[1]);
	RunAwayAgent agent = 
	    new RunAwayAgent(hostname, port, (byte)(args[2].charAt(0)));
	if (!agent.openConnection())
	    return;
	agent.runAgent();
    }

    public RunAwayAgent (String hostname, int port, byte goal) {
	super(hostname, port);
	this.goal = goal;
	random = new Random();
    }
	
    public Direction respondToChange (State newState) {
	Direction move;
	HashMap<Byte, ArrayList<Position>> sortedByType = 
	    newState.sortByType();

	ArrayList<Position> myPositions =
	    sortedByType.get(newState.getPlayer());
	ArrayList<Position> goalPositions =
	    sortedByType.get(goal);

	if (myPositions == null || goalPositions == null)
	    return randomMove();

	Position myPosition = myPositions.get(0);	
	Position goalPosition = goalPositions.get(0);

	int closestIndex = 0, closestDistance = 0,
	    verticalDistance, horizontalDistance;
	for (int i = 0; i < goalPositions.size(); i++) {
	    Position testPosition = goalPositions.get(i);
	    verticalDistance = Math.abs(myPosition.row() - 
					testPosition.row());
	    horizontalDistance = Math.abs(myPosition.column() - 
					  testPosition.column());
	    if ((verticalDistance + horizontalDistance) < closestDistance) {
		closestIndex = i;
	       goalPosition = testPosition;
	    }
	}

	verticalDistance = Math.abs(myPosition.row() - 
				    goalPosition.row());
	horizontalDistance = Math.abs(myPosition.column() - 
				      goalPosition.column());
	int totalDistance = verticalDistance + horizontalDistance;

	double randomMove = random.nextDouble();
	 
	if (randomMove < PROBABILITY  * 
	    verticalDistance / totalDistance) {
	    if (myPosition.row() < goalPosition.row())
		move = Direction.UP;
	    else
		move = Direction.DOWN;
	}
	else if (randomMove < PROBABILITY) {
	    if (myPosition.column() < goalPosition.column())
		move = Direction.LEFT;
	    else
		move = Direction.RIGHT;
	}
	else
	    move = randomMove();
   
	System.out.println("The " + (char)(goal) + " is at (" +
			   goalPosition.row() + ", " + 
			   goalPosition.column() + "), I am at (" +
			   myPosition.row() + ", " +
			   myPosition.column() + ").  I am moving " +
			   move);
	return move;
    }

    /* pick a move, yo! */
    private Direction randomMove () {
	Direction[] values = Direction.values();
	return values[random.nextInt(values.length)];
    }
    
}
