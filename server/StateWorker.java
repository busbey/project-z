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

	class StateWorker implements Runnable
	{
		HashMap<Character, DataOutputStream> clients;
		long roundTime;
		World state;
		HashMap<Character, Byte> actions;
		HashMap<Character, ChatMessage> chats;
		
		StateWorker(World state, HashMap<Character, Byte> actions, HashMap<Character, ChatMessage> chats, HashMap<Character, DataOutputStream> clients, long roundTime)
		{
			this.clients = clients;
			this.roundTime = roundTime;
			this.state = state;
			this.actions = actions;
			this.chats = chats;
		}

		public void run()
		{
			long last = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			while(state.gameRunning())
			{
				end = System.currentTimeMillis();
				//if(end%5 == 0)
				//{
				//	int rounds = state.getRounds();
				//if(0 < rounds)
				//{
					//System.err.println("Rounds remaining: " + rounds);
				//}
				//	HashMap<Character, Long> score = state.getScores();
				//	if(0 < score.size())
				//	{
				//		System.err.println("Current Scores: " + state.getScores().toString());
				//	}
				//	System.err.println("World \n{" + state.toString() + "\n}");
				//}
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
					System.out.println("Rounds remaining: " + state.getRounds());
					state.roundsPassed(1);
					for(Map.Entry<Character, Byte> entry : actions.entrySet())
					{
						char agent = entry.getKey();
						state.change(agent, entry.getValue());
					}
					actions.clear();
					System.out.println("Flags:" + state.flagString());
					System.out.println("Current World \n{" + state.toString() + "\n}");
				}
			}
		}

		protected void updateClients()
		{
			synchronized(clients)
			{
			synchronized(state)
			{
			synchronized(chats)
			{
				for(Map.Entry<Character, DataOutputStream> entry : clients.entrySet())
				{
					char agent = entry.getKey();
					DataOutputStream out = entry.getValue();
					byte agentFlags = state.flags(agent);
					try
					{
						byte agentVal = (byte)agent;
			//			System.err.println("---Starting Agent '"+ (char)(agentVal)+"' (0x"+Integer.toHexString(agentVal)+")");
			//			System.err.println("Writing flags: 0x" + Integer.toHexString(agentFlags));
						out.writeByte(agentFlags);
						out.writeByte((byte)agent);
				//		System.err.println("Starting World");
						state.serialize(out);
						out.writeInt(chats.size());
						for(Map.Entry<Character, ChatMessage> message : chats.entrySet())
						{
							message.getValue().serialize(out);
						}
						out.flush();
					}
					catch(IOException ex)
					{
						if(state.isBug(agent) || state.isHunter(agent))
						{
							System.err.println("Error: problem writing to agent '"+ agent+"'\n");
							System.err.println("World at Death {" + state.toString() + "\n}");
					/*
							System.exit(-1);
					*/
						}
					}
				}
				chats.clear();
			}
			}
			}
		}

		public void close()
		{
			state.end();
			updateClients();
		}
	}
