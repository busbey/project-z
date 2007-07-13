import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public abstract class Agent {
    
    protected enum Direction {
	UP ('u'),
	DOWN ('d'),
	LEFT ('l'),
	RIGHT ('r');
	
	private final char direction;

	Direction (char direction) {
	    this.direction = direction;
	}

	public char getChar () { return direction; }
    }

    protected static final int HEADER_SIZE = 10;
    
    protected DataInputStream inStream;
    protected DataOutputStream outStream;
    protected Socket socket;
    protected int columns, rows, boardSize;

    private String hostname;
    private int port;

    public Agent (String hostname, int port) {
	this.hostname = hostname;
	this.port = port;
    }

    public boolean openConnection () {
	boolean success = true;
	InetAddress[] addresses;
	try {
	    addresses = InetAddress.getAllByName(hostname);
	    socket = new Socket(addresses[0], port);
	}
	catch (UnknownHostException e) {
	    System.out.println("No IP address could be found for " +
			       hostname + "!");
	    e.printStackTrace();
	    success = false;
	}
	catch (IOException e) {
	    System.out.println("I/O exception while creating socket!");
	    e.printStackTrace();
	    success = false;
	}
	catch (SecurityException e) {
	    e.printStackTrace();
	    success = false;
	}
	
	try {
	    inStream = new DataInputStream(socket.getInputStream());
	    outStream = new DataOutputStream(socket.getOutputStream());
	}
	catch (IOException e) {
	    System.out.println("I/O exception while getting streams!");
	    e.printStackTrace();
	    success = false;
	}
	return success;
    }

    public abstract void respondToChange (State newState);

    public void runAgent () {
	try {
	    /* read first state to find size of board */
	    while (!socket.isClosed() && inStream.available() < 10) {}
	    
	    /* check for end of game (on first move? ridiculous) */
	    char flag = inStream.readChar();
	    if (flag == 0xff)
		return;

	    boolean killerBug = (flag == 0x01);
	    char player = inStream.readChar();
	    columns = inStream.readInt();
	    rows = inStream.readInt();
	    boardSize = columns * rows;

	    State state = new State(player, rows, columns);
	    state.setKillerBug(killerBug);
	    
	    while (!socket.isClosed() && inStream.available() < boardSize) {}
	    
	    for (int i = 0; i < rows; i++) 
		for (int j = 0; j < columns; j++) 
		    state.changeBoard(i, j, inStream.readChar());
				    
	    respondToChange(state);

	    /* read the rest of them */
	    while (!socket.isClosed()) {
		if (inStream.available() >= 
		    (RandomAgent.HEADER_SIZE + boardSize)) {
		    /* check for end of game */
		    flag = inStream.readChar();
		    if (flag == 0xff)
			return;
		    state.setKillerBug(flag == 0x01);
		    inStream.skip(RandomAgent.HEADER_SIZE - 1);
		    
		    for (int i = 0; i < rows; i++) 
			for (int j = 0; j < columns; j++) 
			    state.changeBoard(i, j, inStream.readChar());
		    
		    respondToChange(state);
		}
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
