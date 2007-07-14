import java.util.ArrayList;
import java.util.HashMap;

public class State {
    
    private boolean killerBug;
    private byte player;
    private int rows, columns;
    private byte[][] board;

    public State (byte player, int rows, int columns) {
	this.player = player;
	this.rows = rows;
	this.columns = columns;
	board = new byte[rows][columns];
    }

    public boolean killerBug () { return killerBug; }
    public byte getPlayer () { return player; }
    public int numRows () { return rows; }
    public int numColumns () { return columns; }
    public byte[][] getBoard () { return board; }

    public void setKillerBug (boolean killerBug) {
	this.killerBug = killerBug;
    }

    public void changeBoard (int row, int column, byte type) {
	board[row][column] = type;
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
