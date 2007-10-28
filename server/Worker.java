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

	class Worker implements Runnable
	{
		ServerSocket incoming;
		HashMap<Character, ArrayList<byte[]>> out;
		HashMap<Character, Byte> in;
		HashMap<Character, ChatMessage> chats;
		boolean gameRunning = true;
		char agentStart = '\0';
		boolean onMap = true;
		HashMap<InetAddress, ArrayList<Character>> allowedAgents = null;

		/*XXX why doesn't java have tuples already? */
		ArrayList<AgentAction> clientActions = new ArrayList<AgentAction>();
		ArrayList<AgentState> clientStates = new ArrayList<AgentState>();
		ArrayList<Socket> clientSockets = new ArrayList<Socket>();
		
		Worker(char agentStart, ServerSocket incoming, HashMap<Character, ArrayList<byte[]>> out, HashMap<Character, Byte> in, HashMap<Character, ChatMessage> chats)
		{
			this(agentStart, true, incoming, out, in, chats, null);
		}

		Worker(char agentStart, boolean onMap, ServerSocket incoming, HashMap<Character, ArrayList<byte[]>> out, HashMap<Character, Byte> in, HashMap<Character, ChatMessage> chats, HashMap<InetAddress, ArrayList<Character>> allowedAgents)
		{
			this.incoming = incoming;
			this.out = out;
			this.in = in;
			this.chats = chats;
			this.agentStart = agentStart;
			this.onMap = onMap;
			this.allowedAgents = allowedAgents;
		}

		public void gameEnd()
		{
			gameRunning = false;
		}
	
		public void run()
		{
			try
			{
			while(gameRunning)
			{
				Socket client = incoming.accept();
				char agent = agentStart;
				agentStart++;
				InetAddress clientAddress =((InetSocketAddress) client.getRemoteSocketAddress()).getAddress();
				ArrayList<byte[]> states = new ArrayList<byte[]>();
				synchronized(out)
				{
					if(null != allowedAgents)
					{
						ArrayList<Character> allowed = allowedAgents.get(clientAddress);
						agent = '\0';
						if(null != allowed)
						{
							for(Character entry : allowed)
							{
								if(!out.containsKey(entry))
								{
									agent = entry;
									break;
								}
							}
						}
						if('\0' == agent)
						{
							System.err.println("\tDenying client connection.");
							/* deny this connection */
							client.close();
							continue;
						}
					}
					out.put(agent, states);
				}
				System.err.println("New connection for Agent " + agent + " from " + client.getRemoteSocketAddress().toString());
				DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
				DataInputStream inputStream = new DataInputStream(client.getInputStream());
				AgentAction clientActionThread = new AgentAction(agent, inputStream);
				AgentState clientStateThread = new AgentState(agent, outputStream, states);
				clientActions.add(clientActionThread);
				clientStates.add(clientStateThread);
				clientSockets.add(client);
				
				Thread actionReader = new Thread(clientActionThread, "Action Thread for " + agent);
				Thread stateWriter = new Thread(clientStateThread, "State Thread for " + agent);

				/* XXX hack to work around bug 33 */
				if(onMap)
				{
					synchronized(in)
					{
						in.put(agent, (byte)'n');
					}
				}
				actionReader.setDaemon(true);
				actionReader.start();
				stateWriter.setDaemon(true);
				stateWriter.start();

			}
			}catch(Exception ex)
			{
				ex.printStackTrace();
				System.exit(-1);
			}
		}

		public void close()
		{
			gameRunning = false;
			for(AgentState client : clientStates)
			{
				try
				{
					client.close();
				}
				catch(Throwable ex)
				{
				}
			}
			clientStates.clear();
			for(AgentAction client : clientActions)
			{
				try
				{
					client.close();
				}
				catch(Throwable ex)
				{
				}
			}
			clientActions.clear();
			for(Socket client : clientSockets)
			{
				try
				{
					client.close();
				}
				catch(Throwable ex)
				{
				}
			}
		}

		class AgentState implements Runnable
		{
			char agent;
			DataOutputStream outStream;
			ArrayList<byte[]> states;
			boolean running = true;

			public AgentState(char agent, DataOutputStream outStream, ArrayList<byte[]> states)
			{
				this.agent = agent;
				this.outStream = outStream;
				this.states = states;
			}

			public void run()
			{
				System.err.println("state thread started for " + agent);
				while(running)
				{
					try
					{
						/* XXX This could block should the client not read for an excessively long time.
								But in general is won't.  So if there's nothing to do, we  need to yield to avoid hogging the cpu.
						*/
						if(0 < states.size())
						{
							byte[] state;
							/* we're doing this manually rather than iterating over the list because we need the critical section
								to be small and non-blocking so the rest of the server can't get hung up by a non-responsive client
							*/
							synchronized(states)
							{
								state = states.remove(0);
							}
							outStream.write(state);
							outStream.flush();
						}
						else
						{
							Thread.yield();
							try
							{
								Thread.sleep(50);
							}
							catch(InterruptedException iex)
							{
							}
						}
					}
					catch(Exception ex)
					{
						System.err.println("Exiting state thread for " + agent + " because '" + ex.getMessage() + "'");
						running = false;
					}
				}
				synchronized(out)
				{
					out.remove(agent);
				}
			}

			public void close() throws Throwable
			{
				running = false;
				outStream.close();
			}

			protected void finalize() throws Throwable
			{
				close();
				super.finalize();
			}
		}

		class AgentAction implements Runnable
		{
			char agent;
			DataInputStream inStream;
			boolean running = true;

			public AgentAction(char agent, DataInputStream inStream)
			{
				this.agent = agent;
				this.inStream = inStream;
			}
			
			public void run()
			{
				System.err.println("action thread started for " + agent);
				while(running)
				{
					try
					{
						byte firstByte = inStream.readByte();
						/* XXX I apologize.  Java is not my friend for parsing and validating single characters. */
						switch(firstByte)
						{
							case 'l':
							case 'r':
							case 'u':
							case 'd':
							case 'n':
								/* 'i move' case */
								synchronized(in)
								{
									System.err.println("\tAdding action for " + agent);
									in.put(agent, firstByte);
								}
								break;
							default:
								/* 
									validate 'i say' case 
								   	in case of invalid message, 
								   	throw away what we have and move on. 
								 */
								if(	(World.HUNTER_MIN <= firstByte && World.HUNTER_MAX >= firstByte) ||
									(World.BUG_MIN <= firstByte && World.BUG_MAX >= firstByte))
								{
									byte speaker = firstByte;
									byte subject = inStream.readByte();
									byte action = inStream.readByte();
									if( (World.HUNTER_MIN <= subject && World.HUNTER_MAX >= subject) ||
										(World.BUG_MIN <= subject && World.BUG_MAX >= subject))
									{
										/* valid subject */
										switch(action)
										{
											case 'l':
											case 'r':
											case 'u':
											case 'd':
											case 'n':
												/* valid action */
												synchronized(chats)
												{
													System.err.println("\tAdding chat for " + agent);
													chats.put(agent, new ChatMessage(speaker, subject, action));
												}
											default:
											break;
										}
									}
								}
								break;
						}
							
					}
					catch(Exception ex)
					{
						System.err.println("Exiting agent thread for " + agent + " because '" + ex.getMessage() + "'");
						running = false;
					}
				}
			}

			public void close() throws Throwable
			{
				running = false;
				inStream.close();
			}

			protected void finalize() throws Throwable
			{
				close();
				super.finalize();
			}
	}
}
