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
