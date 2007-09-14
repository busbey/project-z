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
				if (flag == State.GAME_ENDED) {
					System.out.println("Game has ended...");
					return;
				}
				
				byte player = inStream.readByte();
				columns = inStream.readInt();
				rows = inStream.readInt();

				if (state == null)
					state = new State(player, rows, columns);
				
				state.setFlag(flag);

				for (int i = 0; i < rows; i++) 
					for (int j = 0; j < columns; j++) 
						state.changeBoard(i, j, inStream.readByte());
				
				System.out.println("flags:" + state.flagString());
				System.out.println("player: '" + (char)(player) + "'");
				System.out.println("rows: " + rows + " columns: " + columns);
				
				int messages = inStream.readInt();
				System.out.println("messages: " + messages);
				state.clearMessages();
				for (int i = 0; i < messages; i++) {
					byte speaker = inStream.readByte();
					byte subject = inStream.readByte();
					byte direction = inStream.readByte();
					System.out.println("message: '" +
														 (char) speaker + "' says '" + 
														 (char) subject + "' should move " +
														 Direction.lookup(direction));
					state.addMessage(new Message(speaker, subject, Direction.lookup(direction)));
				}
				
				respondToChange();
			}
		}
		catch(EOFException eof) {
	    System.out.println("Game has ended...");
		}
		catch (Exception e) {
	    e.printStackTrace();
		}
	}
	
	public void writeMove (Direction move) {
		System.out.println("moving " + move);
		try {
			outStream.writeByte(move.getByte());
			outStream.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage (byte speaker, byte subject, Direction direction) {
		System.out.println("sending: '" + 
											 (char) speaker + "' says '" + 
											 (char) subject + "' should move " + 
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
