import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

public class Agent {
    
    private final int HEADER_SIZE = 10;
    
    private String hostname;
    private int port;

    private DataInputStream inStream;
    private DataOutputStream outStream;
    private Socket socket;
    
    private int columns, rows, boardSize;

    private Mover mover;

    public static void main (String[] args) {
       	if (args.length < 3) {
	    System.out.println("java Agent [hostname] [port] [name of Mover class]");
	    return;
	}
	String hostname = args[0];
	int port = Integer.parseInt(args[1]);
	try {
	    Class moverClass = Class.forName(args[2]);
	    Mover moverToUse;
	    try {
		Constructor<Mover> moverConstructor = 
		    moverClass.getConstructor(String[].class);
		String[] moverArgs = new String[args.length - 3];
		System.arraycopy(args, 3, moverArgs, 0, moverArgs.length); 
		moverToUse = 
		    moverConstructor.newInstance((Object)moverArgs);
	    }
	    catch (NoSuchMethodException e) {
		moverToUse = (Mover) moverClass.newInstance();
	    }
	    Agent agent = new Agent(hostname, port, moverToUse);
	    if (!agent.openConnection())
		return;
	    agent.runAgent();	
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public Agent (String hostname, int port, Mover mover) {
	this.hostname = hostname;
	this.port = port;
	this.mover = mover;
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

    public void runAgent () {
	try {
	    /* read first state to find size of board */
	    //while (!socket.isClosed() && inStream.available() < 10) {}
	    
	    /* check for end of game (on first move? ridiculous) */
	    byte flag = inStream.readByte();
	   
	    if (flag == 0xff)
		return;

	    boolean killerBug = (flag & 0x01) == 0x01;
	    byte player = inStream.readByte();
	    
	    System.out.println("player: " + (char)(player));
	    
	    columns = inStream.readInt();
	    rows = inStream.readInt();

	    System.out.println("rows: " + rows + " columns: " + columns);
	    boardSize = columns * rows;

	    State state = new State(player, rows, columns);
	    state.setKillerBug(killerBug);
	    
	    //while (!socket.isClosed() && inStream.available() < boardSize) {}
	    
	    for (int i = 0; i < rows; i++) 
		for (int j = 0; j < columns; j++) 
		    state.changeBoard(i, j, inStream.readByte());
				    
	    Direction move = mover.respondToChange(state);
		if(Direction.NONE != move)
		{
	    	System.out.println("I am moving " + move);
			outStream.writeByte(move.getByte());
			outStream.flush();
	    }
	    
	    /* read the rest of them */
	    while (!socket.isInputShutdown()) {
		//if (inStream.available() >= (HEADER_SIZE + boardSize)) {
		    /* check for end of game */
		    flag = inStream.readByte();
		    if (flag == 0xff)
			{
				break;
			}
		    state.setKillerBug((flag & 0x01) == 0x01);
		    inStream.skip(HEADER_SIZE - 1);
		    
		    for (int i = 0; i < rows; i++) 
			for (int j = 0; j < columns; j++) 
			    state.changeBoard(i, j, inStream.readByte());
		    
		    move = mover.respondToChange(state);
		    if (Direction.NONE != move) 
			{
		    	System.out.println("I am moving " + move);
				outStream.writeByte(move.getByte());
				outStream.flush();
		    }
		//}
	    }
	}
	catch(EOFException eof)
	{
		System.out.println("Game has ended.");
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
