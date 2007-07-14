import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SillyGreedyAgent extends Agent {
    
    private Random random;
    private byte goal;

    public static void main (String[] args) {
	if (args.length < 3) {
	    System.out.println("java SillyGreedyAgent [hostname] [port] [goal]");
	    return;
	}
	String hostname = args[0];
	int port = Integer.parseInt(args[1]);
	SillyGreedyAgent agent = 
	    new SillyGreedyAgent(hostname, port, (byte)(args[2].charAt(0)));
	if (!agent.openConnection())
	    return;
	agent.runAgent();
    }

    public SillyGreedyAgent (String hostname, int port, byte goal) {
	super(hostname, port);
	this.goal = goal;
	random = new Random();
    }
	
    public void respondToChange (State newState) {
	Direction move;
	HashMap<Byte, ArrayList<Position>> sortedByType = 
	    newState.sortByType();

	//	for (Map.Entry<Byte, ArrayList<Position>> entry : sortedByType.entrySet()) {
	//  byte type = entry.getKey();
	//   for (Position position : entry.getValue()) {
	//	System.out.println((char)type + " " + 
	//			   position.row() + " " +
	//			   position.column());
	//    }
	//}

	Position myPosition =
	    sortedByType.get(newState.getPlayer()).get(0);

	Position goalPosition = sortedByType.get(goal).get(0);

	int verticalDistance = Math.abs(myPosition.row() - 
					goalPosition.row());
	int horizontalDistance = Math.abs(myPosition.column() - 
					  goalPosition.column());
	int totalDistance = verticalDistance + horizontalDistance;
	
	double randomMove = random.nextDouble();
	 
	if (randomMove < 0.95 * verticalDistance / totalDistance) {
	    if (myPosition.row() < goalPosition.row())
		move = Direction.DOWN;
	    else
		move = Direction.UP;
	}
	else if (randomMove < 0.95) {
	    if (myPosition.column() < goalPosition.column())
		move = Direction.RIGHT;
	    else
		move = Direction.LEFT;
	}
	else
	    move = randomMove();

	try {
	    System.out.println("The " + (char)(goal) + " is at (" +
			       goalPosition.row() + ", " + 
			       goalPosition.column() + "), I am at (" +
			       myPosition.row() + ", " +
			       myPosition.column() + ").  I am moving " +
			       move);
	    outStream.writeChar(move.getByte());
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
