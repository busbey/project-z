import java.io.*;
import java.util.*;

	class StateWorker implements Runnable
	{
		HashMap<Character, DataOutputStream> clients;
		long roundTime;
		World state;
		HashMap<Character, Byte> actions;
		
		StateWorker(World state, HashMap<Character, Byte> actions, HashMap<Character, DataOutputStream> clients, long roundTime)
		{
			this.clients = clients;
			this.roundTime = roundTime;
			this.state = state;
			this.actions = actions;
		}

		public void run()
		{
			long last = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			while(state.gameRunning())
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
					state.roundsPassed(1);
					for(Map.Entry<Character, Byte> entry : actions.entrySet())
					{
						char agent = entry.getKey();
						state.change(agent, entry.getValue());
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
				for(Map.Entry<Character, DataOutputStream> entry : clients.entrySet())
				{
					char agent = entry.getKey();
					DataOutputStream out = entry.getValue();
					byte agentFlags = state.flags(agent);
					try
					{
						byte agentVal = (byte)agent;
				//		System.err.println("---Starting Agent '"+ (char)(agentVal)+"' (0x"+Integer.toHexString(agentVal)+")");
				//		System.err.println("Writing flags: 0x" + Integer.toHexString(agentFlags));
						out.writeByte(agentFlags);
						out.writeByte((byte)agent);
				//		System.err.println("Starting World");
						state.serialize(out);
					}
					catch(IOException ex)
					{
						if(state.isBug(agent) || state.isHunter(agent))
						{
							System.err.println("Warning: problem writing to agent '"+ agent+"'\n");
						}
					}
				}
			}
			}
		}
	}
