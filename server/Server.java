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

	Worker bug = null;
	Worker hunter = null;
	Worker display = null;

	World world = null;

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
		HashMap<Character, Byte> actions = new HashMap<Character, Byte>();
		HashMap<Character, DataOutputStream> clients = new HashMap<Character, DataOutputStream>();
		HashMap<Character, ChatMessage> chats = new HashMap<Character, ChatMessage>();
		
		StateWorker update = new StateWorker(state, actions, chats, clients, roundTime);
		this.world = state;
		bug = new Worker('B', new ServerSocket(bugPort), clients, actions, chats);
		hunter = new Worker('1', new ServerSocket(hunterPort), clients, actions, chats);
		display = new Worker('d', new ServerSocket(displayPort), clients, actions, chats);
		
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

	public static void main(String[] args)
	{
		boolean block = true;
		int rounds = -1;
		Server server;
		try
		{
			if(args.length > 0)
			{
				if("-b".equals(args[0]))
				{
					block = false;
					try
					{
						rounds = Integer.parseInt(args[1]);
					}
					catch(NumberFormatException ec)
					{
					}
				}
				if(!("-b".equals(args[args.length - 1])))
				{
					server = Server.fromFile(args[args.length - 1], rounds);
				}
				else
				{
					server = new Server(rounds);
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
		} catch (IOException ex)
		{
			System.err.println("Error: Couldn't create server.");
			ex.printStackTrace();
		}
	}
}
