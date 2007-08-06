import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SmarterAgent extends Agent {
  
	private Random random;
	private String goalRegex;
	
	public void init (String[] args) {
		goalRegex = args[0];
		random = new Random();
	}
	
	public void respondToChange () {
		Direction move;
		HashMap<Byte, ArrayList<Position>> sortedByType = 
	    state.sortByType();
		
		ArrayList<Position> myPositions =
	    sortedByType.get(state.getPlayer());
		
		ArrayList<Position> goalPositions = new ArrayList<Position>();
		for (byte type : sortedByType.keySet()) 
	    if ((new String(new byte[] {type})).matches(goalRegex)) 
				goalPositions.addAll(sortedByType.get(type));
		
		if (myPositions == null || goalPositions == null || goalPositions.size() == 0) {
	    writeMove(Direction.NONE);
			return;
		}
		
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
		
		if (randomMove < verticalDistance / totalDistance) {
	    if (myPosition.row() < goalPosition.row())
				move = Direction.DOWN;
	    else
				move = Direction.UP;
		}
		else {
	    if (myPosition.column() < goalPosition.column())
				move = Direction.RIGHT;
	    else
				move = Direction.LEFT;
		}
		
		System.out.println("The " + goalRegex + " is at (" +
											 goalPosition.row() + ", " + 
											 goalPosition.column() + "), I am at (" +
											 myPosition.row() + ", " +
											 myPosition.column() + ").");
		writeMove(move);
	}
	
	/* pick a move, yo! */
	private Direction randomMove () {
		Direction[] values = {Direction.LEFT, Direction.RIGHT,
													Direction.UP, Direction.DOWN};
		return values[random.nextInt(values.length)];
	}
	
}
