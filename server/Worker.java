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
				DataOutputStream outStream = new DataOutputStream(client.getOutputStream());
				final DataInputStream inStream = new DataInputStream(client.getInputStream());
				
				
				Thread actionReader = new Thread(new Runnable()
				{
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
				}, "Thread for " + agent);

				actionReader.setDaemon(true);
				actionReader.start();

				synchronized(out)
				{
					out.put(agent, outStream);
					System.err.println("state monitoring registered for " + agent);
				}
			}
			}catch(Exception ex)
			{
				System.exit(-1);
			}
		}
	}
