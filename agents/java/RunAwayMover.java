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
