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

	Worker bug=null;
	Worker hunter=null;
	Worker display=null;

	RotatingWorld world=null;

	StateWorker state=null;

	public void close()
	{
		bug.close();
		hunter.close();
		display.close();
		state.close();
	}

	/* crete an uninitialized Server. */
	public Server()
	{
	}
	
	public Server(int bugPort, int hunterPort, int displayPort, RotatingWorld state, long roundTime, HashMap<InetAddress, ArrayList<Character>> bugAcl, HashMap<InetAddress, ArrayList<Character>> hunterAcl, HashMap<InetAddress, ArrayList<Character>> displayAcl) throws IOException
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
		int changeEvery = -1;
		boolean clean = false;
		int fogRadius = -1;
		boolean fogDist = FixedRadiusFilter.SQUARE_DISTANCE;
		boolean fixedOrFlex = true;
		ArrayList<File> maps = new ArrayList<File>();
		HashMap<InetAddress,ArrayList<Character>> bugACL = null;
		HashMap<InetAddress,ArrayList<Character>> hunterACL = null;
		HashMap<InetAddress,ArrayList<Character>> displayACL = null;
		try
		{
			if(args.length > 0)
			{
				for(int i = 0; i < (args.length); i++)
				{
					System.err.println(args[i]);
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
						ArrayList<File> toCheck = new ArrayList<File>();
						toCheck.add(new File(args[i+1]));
						i++;
						do
						{
							File file = toCheck.remove(0);
							if(file.exists() && file.canRead() && ! file.isHidden())
							{
								if(file.isFile())
								{
									System.err.println("Adding map for " + file);
									maps.add(file);
								}
								else if(file.isDirectory())
								{
									File[] files = file.listFiles();
									if(null != files)
									{
										/* good thing arrays aren't collections */
										for(File temp : files)
										{
											toCheck.add(temp);
										}
									}
								}
							}
						} while (false == toCheck.isEmpty());
					}
					else if("--change".equals(args[i]))
					{
						try
						{
							changeEvery = Integer.parseInt(args[i+1]);
						}
						catch(RuntimeException re)
						{
						}
					}
					else if("--clean".equals(args[i]))
					{
						clean = true;
					}
					else if("--fog".equals(args[i]))
					{
						System.err.println("Enabling Fog Of War...");
						fogRadius = 5;
						if(i+1 < args.length && "flex".equals(args[i+1]))
						{
							fixedOrFlex = false;
							i++;
						}
						if(i+1 < args.length && "square".equals(args[i+1]))
						{
							fogDist=FixedRadiusFilter.SQUARE_DISTANCE;
							i++;
						}
						else if(i+1 < args.length && "manhattan".equals(args[i+1]))
						{
							fogDist=FixedRadiusFilter.MANHATTAN_DISTANCE;
							i++;
						}
						try
						{
							fogRadius = Integer.parseInt(args[i+1]);
							i++;
						}
						catch(RuntimeException re)
						{
							System.err.println ("No fog distance defined. defaulting to 5 squares.");
						}
					}
				}
			}
			final RotatingWorld world;
			if(maps.isEmpty())
			{
				world = new RotatingWorld(rounds);
			}
			else
			{
				world = new RotatingWorld(maps, rounds);
			}
			if(0 < fogRadius)
			{
				if(fixedOrFlex)
				{
					world.setFilter(new FixedRadiusFilter(fogRadius, fogDist));
				}
				else
				{
					/* Set up a filter that removes 1 square of sight every other round moving, and puts back 2 per round standing still. */
					world.setFilter(new InverseMoveRadiusFilter(fogRadius, fogDist, 1, 2, 2, 1));
				}
			}
			server = new Server(DEFAULT_BUG_PORT, DEFAULT_HUNTER_PORT, DEFAULT_DISPLAY_PORT, world, DEFAULT_ROUND_TIME, bugACL, hunterACL, displayACL);
			/* change maps?  */
			Timer boardChanger = new Timer("Board Changer", true);
			if(-1 < changeEvery)
			{
				final boolean _clean = clean;
				TimerTask change = new TimerTask()
				{
					public void run()
					{
						synchronized(world)
						{
							try
							{
								System.err.println("rotating world... " + (_clean ? "reseting scores":"keeping scores"));
								world.rotate(_clean);
							}
							catch(IOException ioex)
							{
								ioex.printStackTrace();
								System.exit(-1);
							}
						}
					}
				};
				boardChanger.scheduleAtFixedRate(change, changeEvery*1000l, changeEvery*1000l);
			}
			if(block)
			{
				System.in.read();
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
			}
			server.close();
			boardChanger.cancel();
		} 
		catch (IOException ex)
		{
			System.err.println("Error: Couldn't create server.");
			ex.printStackTrace();
		}
	}
}
