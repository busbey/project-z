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

	Worker bug = null;
	Worker hunter = null;
	Worker display = null;

	StateWorker state = null;

	public void close()
	{
		state.close();
		bug.close();
		hunter.close();
		display.close();
	}

	public static Server fromFile(String path) throws IOException
	{
		return new Server(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT, World.fromFile(path));
	}

	public Server() throws IOException
	{
		this(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT);
	}

	public Server(int bugPort, int hunterPort, int displayPort) throws IOException
	{
		this(bugPort, hunterPort, displayPort, new World(80, 20));
	}
	
	public Server(int bugPort, int hunterPort, int displayPort, World state) throws IOException
	{
		this(bugPort, hunterPort, displayPort, state, DEFAULT_ROUND_TIME);
	}

	public Server(int bugPort, int hunterPort, int displayPort, World state, long roundTime) throws IOException
	{
		HashMap<Character, Byte> actions = new HashMap<Character, Byte>();
		HashMap<Character, DataOutputStream> clients = new HashMap<Character, DataOutputStream>();
		StateWorker update = new StateWorker(state, actions, clients, roundTime);
		bug = new Worker('B', new ServerSocket(bugPort), clients, actions);
		hunter = new Worker('1', new ServerSocket(hunterPort), clients, actions);
		display = new Worker('d', new ServerSocket(displayPort), clients, actions);
		System.err.println("Starting state monitor thread.");
		this.state = update;
		Thread stateThread = new Thread(update);
		stateThread.setDaemon(true);
		stateThread.start();
		System.err.println("Starting bug port thread.");
		Thread bugThread = new Thread(bug);
		bugThread.setDaemon(true);
		bugThread.start();
		System.err.println("Starting hunter port thread.");
		Thread hunterThread = new Thread(hunter);
		hunterThread.setDaemon(true);
		hunterThread.start();
		System.err.println("Starting display port thread.");
		Thread displayThread = new Thread(display);
		displayThread.setDaemon(true);
		displayThread.start();
	}

	public static void main(String[] args)
	{
		boolean block = true;
		Server server;
		try
		{
			if(args.length > 0)
			{
				if("-b".equals(args[0]))
				{
					block = false;
				}
				if(!("-b".equals(args[args.length - 1])))
				{
					server = Server.fromFile(args[args.length - 1]);
				}
				else
				{
					server = new Server();
				}
			}
			else
			{
				server = new Server();
			}
			if(block)
			{
				System.in.read();
				server.close();
			}
			else
			{
				while(true);
			}
		} catch (IOException ex)
		{
			System.err.println("Error: Couldn't create server.");
			ex.printStackTrace();
		}
	}
}
