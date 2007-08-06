import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

public abstract class Agent {
    
	private DataInputStream inStream;
	private DataOutputStream outStream;
	private Socket socket;

	protected int rows, columns;

	protected State state;
	
	public static void main (String[] args) {
		if (args.length < 3) {
	    System.out.println("java Agent [hostname] [port] [name of Agent class] [other arguments]");
	    return;
		}

		String hostname = args[0];
		int port = Integer.valueOf(args[1]);
		Agent agent = null;

		try {
			Class<Agent> agentClass = Agent.class;
			Class argClass = (Class.forName(args[2]));
			if (agentClass.isAssignableFrom(argClass)) {
				agent = 
					(Agent) argClass.newInstance();
			}
			else 
				System.err.println("Could not instantiate your agent.  Make sure it extends Agent.");
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
			
		String[] agentArgs = new String[args.length - 3];
		System.arraycopy(args, 3, agentArgs, 0, agentArgs.length);
		agent.init(agentArgs);

		if (!agent.openConnection(hostname, port)) {
			return;
		}
		agent.runAgent();	
		
	}

	public Agent () {}

	public void init (String[] args) {}
	
	public boolean openConnection (String hostname, int port) {
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

			while (!socket.isInputShutdown()) {
				byte flag = inStream.readByte();
				if (flag == 0xff)
					return;
				System.out.println("flag: " + flag);
				
				byte player = inStream.readByte();
				System.out.println("player: " + (char)(player));
				
				columns = inStream.readInt();
				rows = inStream.readInt();
				System.out.println("rows: " + rows + " columns: " + columns);

				if (state == null)
					state = new State(player, rows, columns);
				
				state.setKillerBug((flag & 0x01) == 0x01);
				state.setWasKilled((flag & 0x02) == 0x02);
				state.setWasStunned((flag & 0x04) == 0x04);

				for (int i = 0; i < rows; i++) 
					for (int j = 0; j < columns; j++) 
						state.changeBoard(i, j, inStream.readByte());
				System.out.println(state.boardString());
				
				int messages = inStream.readInt();
				System.out.println("messages: " + messages);
				state.clearMessages();
				for (int i = 0; i < messages; i++) {
					byte speaker = inStream.readByte();
					byte subject = inStream.readByte();
					byte direction = inStream.readByte();
					System.out.println("message: " +
														 (char) speaker + " says " + 
														 (char) subject + " should move " +
														 Direction.lookup(direction));
					
					state.addMessage(new Message(speaker, subject, Direction.lookup(direction)));
				}
				
				respondToChange();
			}
		}
		catch(EOFException eof) {
	    System.out.println("Game has ended.");
		}
		catch (Exception e) {
	    e.printStackTrace();
		}
	}
	
	public void writeMove (Direction move) {
		System.out.println("moving: " + move);
		try {
			outStream.writeByte(move.getByte());
			outStream.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage (byte speaker, byte subject, Direction direction) {
		System.out.println("sending: " + 
											 (char) speaker + " says " + 
											 (char) subject + " should move " + 
											 direction);
		try {
			outStream.writeByte(speaker);
			outStream.writeByte(subject);
			outStream.writeByte(direction.getByte());
			outStream.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void respondToChange ();
	
}
