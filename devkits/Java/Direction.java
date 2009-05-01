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

package com.google.code.p.project-z;

public enum Direction {
	
	NONE ('n'), 
		UP ('u'), 
		DOWN ('d'), 
		LEFT ('l'), 
		RIGHT ('r');
	
	private final byte direction;
  
	Direction (char direction) {
		this.direction = (byte) direction;
	}

	public static Direction lookup (byte toLookup) {
		switch ((char) toLookup) {
		case 'n':
			return Direction.NONE;
		case 'u':
			return Direction.UP;
		case 'd':
			return Direction.DOWN;
		case 'l':
			return Direction.LEFT;
		case 'r':
			return Direction.RIGHT;
		}
		return Direction.NONE;
	}

	public byte getByte () { return direction; }
	
}

