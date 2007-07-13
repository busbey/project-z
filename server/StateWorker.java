import java.io.*;
import java.util.*;

	class StateWorker implements Runnable
	{
	public static final byte FLAGS_EMPTY = 0x00;
	public static final byte FLAGS_GAME_END = (byte)(0xff);
	public static final byte SET_AGENT_DIED = 0x02;
	
		HashMap<Character, ObjectOutputStream> clients;
		long roundTime;
		World state;
		HashMap<Character, Byte> actions;
		HashMap<Character, Boolean> died;
		boolean gameRunning = true;
		byte flags = FLAGS_EMPTY;
		
		StateWorker(World state, HashMap<Character, Byte> actions, HashMap<Character, ObjectOutputStream> clients, long roundTime)
		{
			this.clients = clients;
			this.roundTime = roundTime;
			this.state = state;
			this.actions = actions;
			died = new HashMap<Character, Boolean>();
		}

		public void gameEnd()
		{
			gameRunning = false;
			flags = FLAGS_GAME_END;
		}

		public void updateFlags(byte newFlags)
		{
			flags = newFlags;
		}
		
		public void run()
		{
			long last = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			while(gameRunning)
			{
				end = System.currentTimeMillis();

				long toSleep = roundTime - (end - last);
				if(0 < toSleep)
				{
					try
					{
						Thread.sleep(toSleep);
					} catch (InterruptedException ex)
					{
					}
				}
				last = end;
				updateState();
				updateClients();
			}
			updateClients();
		}

		protected void updateState()
		{
			synchronized(state)
			{
				synchronized(actions)
				{
					for(Map.Entry<Character, Byte> entry : actions.entrySet())
					{
						char agent = entry.getKey();
						boolean agentDied = state.change(agent, entry.getValue());
						died.put(agent, agentDied);
					}
					actions.clear();
				}
			}
		}

		protected void updateClients()
		{
			synchronized(clients)
			{
			synchronized(state)
			{
				for(Map.Entry<Character, ObjectOutputStream> entry : clients.entrySet())
				{
					char agent = entry.getKey();
					ObjectOutputStream out = entry.getValue();
					byte agentFlags = flags;
					if(died.containsKey(agent) && died.get(agent))
					{
						agentFlags |= SET_AGENT_DIED;
					}
					try
					{
						out.writeByte(agentFlags);
						out.writeByte(agent);
						out.writeObject(state);
					}
					catch(IOException ex)
					{
						System.err.println("Warning: problem writing to agent '"+ agent+"'\n");
					}
				}
			}
			}
		}
	}
