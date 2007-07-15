import java.io.*;
import java.util.*;
import java.net.*;

	class Worker implements Runnable
	{
		ServerSocket incoming;
		HashMap<Character, DataOutputStream> out;
		HashMap<Character, Byte> in;
		boolean gameRunning = true;
		char agentStart = '\0';

		ArrayList<AgentThread> clients = new ArrayList<AgentThread>();
		
		Worker(char agentStart, ServerSocket incoming, HashMap<Character, DataOutputStream> out, HashMap<Character, Byte> in)
		{
			this.incoming = incoming;
			this.out = out;
			this.in = in;
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
								byte action = inStream.readByte();
								/*
								switch(action)
								{
									case 'l':
									case 'r':
									case 'u':
									case 'd':
										System.err.println("Not Warning: read valid action '"+action+"' from agent " + agent);
										break;
									default:
										System.err.println("Warning: invalid action '"+action+"' read from agent " + agent);
										break;
								}
								*/
								synchronized(in)
								{
									in.put(agent, action);
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
