/**
 * @file server up world state and change it based on agent feedback
 */
/* Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs, Paul Ostazeski
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

import java.io.*;
import java.util.*;
import java.net.*;

public class Server
{

	public static final int DEFAULT_DISPLAY_PORT = 8668;
	public static final int DEFAULT_BUG_PORT = 7331;
	public static final int DEFAULT_HUNTER_PORT = 1337;
	public static final long DEFAULT_ROUND_TIME = 250;

	protected static final String ACL_BUG = "Bugs=";
	protected static final String ACL_HUNTER = "Hunters=";
	protected static final String ACL_DISPLAY = "Displays=";

	Worker bug = null;
	Worker hunter = null;
	Worker display = null;

	World world = null;

	StateWorker state = null;

	public void close()
	{
		bug.close();
		hunter.close();
		display.close();
		state.close();
	}
	
	public static Server fromFile(String path) throws IOException
	{
		return fromFile(path, -1);
	}

	public static Server fromFile(String path, int rounds) throws IOException
	{
		return new Server(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT, World.fromFile(path, rounds));
	}

	public Server() throws IOException
	{
		this(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT);
	}
	
	public Server(int rounds) throws IOException
	{
		this(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT, rounds);
	}

	public Server(int bugPort, int hunterPort, int displayPort) throws IOException
	{
		this(bugPort, hunterPort, displayPort, -1);
	}

	public Server(int bugPort, int hunterPort, int displayPort, int rounds) throws IOException
	{
		this(bugPort, hunterPort, displayPort, new World(80, 20, rounds));
	}
	
	public Server(int bugPort, int hunterPort, int displayPort, World state) throws IOException
	{
		this(bugPort, hunterPort, displayPort, state, DEFAULT_ROUND_TIME);
	}

	public Server(int bugPort, int hunterPort, int displayPort, World state, long roundTime) throws IOException
	{
		this(bugPort, hunterPort, displayPort, state, roundTime, null, null, null);
	}
	
	public Server(int bugPort, int hunterPort, int displayPort, World state, long roundTime, HashMap<InetAddress, ArrayList<Character>> bugAcl, HashMap<InetAddress, ArrayList<Character>> hunterAcl, HashMap<InetAddress, ArrayList<Character>> displayAcl) throws IOException
	{
		HashMap<Character, Byte> actions = new HashMap<Character, Byte>();
		HashMap<Character, ArrayList<byte[]>> clients = new HashMap<Character, ArrayList<byte[]>>();
		HashMap<Character, ChatMessage> chats = new HashMap<Character, ChatMessage>();
		
		StateWorker update = new StateWorker(state, actions, chats, clients, roundTime);
		this.world = state;
		bug = new Worker('B', true, new ServerSocket(bugPort), clients, actions, chats, bugAcl);
		hunter = new Worker('1', true, new ServerSocket(hunterPort), clients, actions, chats, hunterAcl);
		display = new Worker('d', false,  new ServerSocket(displayPort), clients, actions, chats, displayAcl);
		
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

	public boolean gameRunning()
	{
		return world.gameRunning();
	}

	public static void usage()
	{
		/* no usage statement.  let's reward the curious.*/
	}

	public static void main(String[] args)
	{
		boolean block = true;
		int rounds = -1;
		Server server = null;
		String worldFile = null;
		HashMap<InetAddress,ArrayList<Character>> bugACL = null;
		HashMap<InetAddress,ArrayList<Character>> hunterACL = null;
		HashMap<InetAddress,ArrayList<Character>> displayACL = null;
		try
		{
			if(args.length > 0)
			{
				for(int i = 0; i < (args.length -1); i++)
				{
					if("--batch".equals(args[i]))
					{
						block = false;
						try
						{
							rounds = Integer.parseInt(args[i+1]);
							i++;
						}
						catch(NumberFormatException ec)
						{
						}
					}
					else if("--acl".equals(args[i]))
					{
						int j = i + 1;
						for(; j < args.length;j++)
						{
							if(args[j].startsWith("-"))
							{
								break;
							}
							HashMap<InetAddress, ArrayList<Character>> acl = null;
							String accessList = null;
							if(args[j].startsWith(ACL_BUG))
							{
								acl = bugACL = new HashMap<InetAddress, ArrayList<Character>>();	
								accessList = args[j].substring(ACL_BUG.length());
							}
							else if(args[j].startsWith(ACL_HUNTER))
							{
								acl = hunterACL = new HashMap<InetAddress, ArrayList<Character>>();	
								accessList = args[j].substring(ACL_HUNTER.length());
							}
							else if(args[j].startsWith(ACL_DISPLAY))
							{
								acl = displayACL = new HashMap<InetAddress, ArrayList<Character>>();	
								accessList = args[j].substring(ACL_DISPLAY.length());
							}
							if(null != acl && null != accessList)
							{
								String[] hostAccesses = accessList.split(",");
								for(String host : hostAccesses)
								{
									int colon = host.lastIndexOf(":");
									if(-1 < colon)
									{
										String hostId = host.substring(0, colon);
										String agents = host.substring(colon+1);
										InetAddress address = InetAddress.getByName(hostId);
										ArrayList<Character> allowed = acl.get(address);
										if(null == allowed)
										{
											allowed = new ArrayList<Character>();
										}
										for(char agent : agents.toCharArray())
										{
											allowed.add(agent);
										}
										acl.put(address, allowed);
									}
								}
							}
						}
						i = j - 1;
					}
					else if("--map".equals(args[i]))
					{
						worldFile = args[i+1];	
					}
				}
			}
			World world = null;
			if(null != worldFile)
			{
				world = World.fromFile(worldFile, rounds);
			}
			else
			{
				world = World.random();
			}
			server = new Server(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT, world, DEFAULT_ROUND_TIME, bugACL, hunterACL, displayACL);
			if(block)
			{
				System.in.read();
				server.close();
			}
			else
			{
				while(server.gameRunning())
				{
					Thread.yield();
					try
					{
						Thread.sleep(300);
					}
					catch(InterruptedException iex)
					{
					}
				}
				server.close();
			}
		} 
		catch (IOException ex)
		{
			System.err.println("Error: Couldn't create server.");
			ex.printStackTrace();
		}
	}
}
