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
		HashMap<Character, DataOutputStream> out;
		HashMap<Character, Byte> in;
		HashMap<Character, ChatMessage> chats;
		boolean gameRunning = true;
		char agentStart = '\0';

		ArrayList<AgentThread> clients = new ArrayList<AgentThread>();
		
		Worker(char agentStart, ServerSocket incoming, HashMap<Character, DataOutputStream> out, HashMap<Character, Byte> in, HashMap<Character, ChatMessage> chats)
		{
			this.incoming = incoming;
			this.out = out;
			this.in = in;
			this.chats = chats;
			this.agentStart = agentStart;
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
				final char agent = agentStart;
				agentStart++;
				System.err.println("New connection for Agent " + agent);
				
				AgentThread clientThread = new AgentThread(agent, client);
				clients.add(clientThread);
				Thread actionReader = new Thread(clientThread, "Thread for " + agent);

				actionReader.setDaemon(true);
				actionReader.start();

			}
			}catch(Exception ex)
			{
				ex.printStackTrace();
				System.exit(-1);
			}
		}

		public void close()
		{
			for(AgentThread client : clients)
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

		class AgentThread implements Runnable
		{
			char agent;
			DataInputStream inStream;
			DataOutputStream outStream;
			Socket client;

			public AgentThread(char agent, Socket client) throws IOException
			{
				this.agent = agent;
				outStream = new DataOutputStream(client.getOutputStream());
				inStream = new DataInputStream(client.getInputStream());
				this.client = client;
				synchronized(out)
				{
					out.put(agent, outStream);
					System.err.println("state monitoring registered for " + agent);
				}
			}
			
					public void run()
					{
						boolean running = true;
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
											in.put(agent, firstByte);
										}
										break;
									default:
										/* 
											validate 'i say' case 
										   	in case of invalid message, 
										   	throw away what we have an move on. 
										 */
										if(	('1' <= firstByte && '9' >= firstByte) ||
											('B' <= firstByte && 'N' >= firstByte))
										{
											byte speaker = firstByte;
											byte subject = inStream.readByte();
											byte action = inStream.readByte();
											if( ('1' <= subject && '9' >= subject) ||
												('B' <= subject && 'N' >= subject))
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
								running = false;
							}
						}
					}

					public void close() throws Throwable
					{
						System.err.println("Closing i/o streams for " + agent);
						client.close();
						outStream.close();
						inStream.close();
					}

					protected void finalize() throws Throwable
					{
						close();
						super.finalize();
					}
		}
	}
