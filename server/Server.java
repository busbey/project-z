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
	protected World state;
	protected HashMap<InetAddress,ArrayList<Socket>> hunters;
	protected HashMap<InetAddress,ArrayList<Socket>> bugs;
	protected HashMap<InetAddress,ArrayList<Socket>> displays;

	public Server()
	{
		this(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT);
	}

	public Server(int bugPort, int hunterPort, int displayPort)
	{
		this(bugPort, hunterPort, displayPort, World.random());
	}

	public Server(int bugPort, int hunterPort, int displayPort, World starting)
	{
		state = starting;

		
	}
}
