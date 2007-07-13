/**
 * @file server up world state and change it based on agent feedback
 */
import java.io.*;
import java.util.*;
import java.net.*;

public class Server
{

	public static final int DEFAULT_DISPLAY_PORT = 8668;
	public static final int DEFAULT_BUG_PORT = 7331;
	public static final int DEFAULT_HUNTER_PORT = 1337;
	public static final long DEFAULT_ROUND_TIME = 250;

	public Server() throws IOException
	{
		this(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT);
	}

	public Server(int bugPort, int hunterPort, int displayPort) throws IOException
	{
		this(bugPort, hunterPort, displayPort, World.random());
	}
	
	public Server(int bugPort, int hunterPort, int displayPort, World state) throws IOException
	{
		this(bugPort, hunterPort, displayPort, state, DEFAULT_ROUND_TIME);
	}

	public Server(int bugPort, int hunterPort, int displayPort, World state, long roundTime) throws IOException
	{
		HashMap<Character, Byte> actions = new HashMap<Character, Byte>();
		HashMap<Character, ObjectOutputStream> clients = new HashMap<Character, ObjectOutputStream>();
		StateWorker update = new StateWorker(state, actions, clients, roundTime);
		Worker bug = new Worker('B', new ServerSocket(bugPort), clients, actions);
		Worker hunter = new Worker('1', new ServerSocket(hunterPort), clients, actions);
		Worker display = new Worker('d', new ServerSocket(displayPort), clients, actions);
		Thread stateThread = new Thread(update);
		stateThread.setDaemon(true);
		stateThread.start();
		Thread bugThread = new Thread(bug);
		bugThread.setDaemon(true);
		bugThread.start();
		Thread hunterThread = new Thread(hunter);
		hunterThread.setDaemon(true);
		hunterThread.start();
		Thread displayThread = new Thread(display);
		displayThread.setDaemon(true);
		displayThread.start();
	}

	public static void main(String[] args)
	{
		try
		{
			new Server();
			System.in.read();
		} catch (IOException ex)
		{
			System.err.println("Error: Couldn't create server.");
		}
	}
}
