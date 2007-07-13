import java.io.*;
import java.util.*;
import java.net.*;

	class Worker implements Runnable
	{
		ServerSocket incoming;
		HashMap<Character, ObjectOutputStream> out;
		HashMap<Character, Byte> in;
		boolean gameRunning = true;
		char agentStart = '\0';
		
		Worker(char agentStart, ServerSocket incoming, HashMap<Character, ObjectOutputStream> out, HashMap<Character, Byte> in)
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
				final char agent = agentStart;
				agentStart++;
				Socket client = incoming.accept();
				ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
				final DataInputStream inStream = new DataInputStream(client.getInputStream());
				
				
				Thread actionReader = new Thread(new Runnable()
				{
					public void run()
					{
						boolean running = true;
						while(running)
						{
							try
							{
								byte action = inStream.readByte();
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
				}
			}
			}catch(Exception ex)
			{
				System.exit(-1);
			}
		}
	}
