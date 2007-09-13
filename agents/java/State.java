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

	public void setKillerBug (boolean killerBug) {
		this.killerBug = killerBug;
	}

	public void setWasKilled (boolean wasKilled) {
		this.wasKilled = wasKilled;
	}

	public void setWasStunned (boolean wasStunned) {
		this.wasStunned = wasStunned;
	}
	
	public void changeBoard (int row, int column, byte type) {
		board[row][column] = type;
	}

	public String boardString () {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				buffer.append((char) board[i][j]);
			}
			if (i < (rows - 1)) 
				buffer.append("\n");
		}
		return buffer.toString();
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
