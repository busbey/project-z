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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class State {
    
	private boolean killerBug, wasKilled, wasStunned;
	private byte player;
	private int rows, columns;
	private byte[][] board;
	private List<Message> messages;

	private final static byte KILLER_BUG = 0x01;
	private final static byte WAS_KILLED = 0x02;
	private final static byte WAS_STUNNED = 0x04;

	public final static byte GAME_ENDED = (byte) 0xff;
	
	public State (byte player, int rows, int columns) {
		this.player = player;
		this.rows = rows;
		this.columns = columns;
		board = new byte[rows][columns];
		messages = new ArrayList<Message>();
	}
	
	public boolean killerBug () { return killerBug; }
	public boolean wasKilled () { return wasKilled; }
	public boolean wasStunned () { return wasStunned; }
	public byte getPlayer () { return player; }
	public int numRows () { return rows; }
	public int numColumns () { return columns; }
	public byte[][] getBoard () { return board; }
	public List<Message> getMessages () { return messages; }

	public void clearMessages () {
		messages.clear();
	}
	
	public void addMessage (Message message) {
		messages.add(message);
	}

	public void setFlag (int flag) {
		 killerBug = (flag & KILLER_BUG) == KILLER_BUG;
		 wasKilled = (flag & WAS_KILLED) == WAS_KILLED;
		 wasStunned = (flag & WAS_STUNNED) == WAS_STUNNED;
	}
	
	public void changeBoard (int row, int column, byte type) {
		board[row][column] = type;
	}

	public String flagString () {
		if (!(killerBug || wasKilled || wasStunned)) 
			return " None.";
		StringBuffer value = new StringBuffer();
		value.append(killerBug ? " [Bugs kill hunters]" : "");		
		value.append(wasKilled ? " [Player died last round]" : "");		
		value.append(wasStunned ? " [Player stunned last round]" : "");
		return value.toString();
	}
	
	public HashMap<Byte, ArrayList<Position>> sortByType () {
		HashMap<Byte, ArrayList<Position>> result =
	    new HashMap<Byte, ArrayList<Position>>();
		
		for (int i = 0; i < rows; i++) 
	    for (int j = 0; j < columns; j++) {
				byte type = board[i][j];
				if (!result.containsKey(type)) 
					result.put(type, new ArrayList<Position>());
				result.get(type).add(new Position(i, j));
	    }
		
		return result;
	}

}
