/* Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs, Paul Ostazeski
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

package com.google.code.p.project-z;

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
		
		System.out.println("Goal '" + goalRegex + "' is at (" +
											 goalPosition.row() + ", " + 
											 goalPosition.column() + "); I am at (" +
											 myPosition.row() + ", " +
											 myPosition.column() + ")");
		writeMove(move);
	}
	
	/* pick a move, yo! */
	private Direction randomMove () {
		Direction[] values = {Direction.LEFT, Direction.RIGHT,
													Direction.UP, Direction.DOWN};
		return values[random.nextInt(values.length)];
	}
	
}
