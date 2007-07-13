import java.util.ArrayList;
import java.util.HashMap;

public class State {
    
    private boolean killerBug;
    private char player;
    private int rows, columns;
    private char[][] board;

    public State (char player, int rows, int columns) {
	this.player = player;
	this.rows = rows;
	this.columns = columns;
	board = new char[rows][columns];
    }

    public boolean killerBug () { return killerBug; }
    public char getPlayer () { return player; }
    public int numRows () { return rows; }
    public int numColumns () { return columns; }
    public char[][] getBoard () { return board; }

    public void setKillerBug (boolean killerBug) {
	this.killerBug = killerBug;
    }

    public void changeBoard (int row, int column, char type) {
	board[row][column] = type;
    }

    public HashMap<Character, ArrayList<Position>> sortByType () {
	HashMap<Character, ArrayList<Position>> result =
	    new HashMap<Character, ArrayList<Position>>();

	for (int i = 0; i < rows; i++) 
	    for (int j = 0; j < columns; j++) {
		char type = board[i][j];
		if (!result.containsKey(type)) 
		    result.put(type, new ArrayList<Position>());
		result.get(type).add(new Position(i, j));
	    }
	
	return result;
    }

}
