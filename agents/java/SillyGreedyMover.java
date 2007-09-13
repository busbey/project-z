/* Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SillyGreedyMover implements Mover {
    
    private Random random;
    private byte goal;

    private final double PROBABILITY = 0.85;

    public SillyGreedyMover (String[] args) {
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

	int verticalDistance = Math.abs(myPosition.row() - 
					goalPosition.row());
	int horizontalDistance = Math.abs(myPosition.column() - 
					  goalPosition.column());
	int totalDistance = verticalDistance + horizontalDistance;
	
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
