import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SmarterGreedyMover implements Mover {
    
    private Random random;
    private byte goal;

    private final double PROBABILITY = 0.85;

    public SmarterGreedyMover (String[] args) {
	goal = (byte)(args[0].charAt(0));
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

	int closestDistance = Integer.MAX_VALUE, verticalDistance, 
	    horizontalDistance, totalDistance;

	for (int i = 0; i < goalPositions.size(); i++) {
	    Position testPosition = goalPositions.get(i);
	    verticalDistance = Math.abs(myPosition.row() - 
					testPosition.row());
	    horizontalDistance = Math.abs(myPosition.column() - 
					  testPosition.column());
	    totalDistance = verticalDistance + horizontalDistance;
	    if (totalDistance < closestDistance) {
		closestDistance = totalDistance;
		goalPosition = testPosition;
	    }
	}

	verticalDistance = Math.abs(myPosition.row() - 
				    goalPosition.row());
	horizontalDistance = Math.abs(myPosition.column() - 
				      goalPosition.column());
	totalDistance = verticalDistance + horizontalDistance;

	double randomMove = random.nextDouble();
	 
	if (randomMove < PROBABILITY  * 
	    verticalDistance / totalDistance) {
	    if (myPosition.row() < goalPosition.row())
		move = Direction.DOWN;
	    else
		move = Direction.UP;
	}
	else if (randomMove < PROBABILITY) {
	    if (myPosition.column() < goalPosition.column())
		move = Direction.RIGHT;
	    else
		move = Direction.LEFT;
	}
	else
	    move = randomMove();
   
	System.out.println("The " + (char)(goal) + " is at (" +
			   goalPosition.row() + ", " + 
			   goalPosition.column() + "), I am at (" +
			   myPosition.row() + ", " +
			   myPosition.column() + ").");
	return move;
    }

    /* pick a move, yo! */
    private Direction randomMove () {
	Direction[] values = Direction.values();
	return values[random.nextInt(values.length)];
    }
    
}
