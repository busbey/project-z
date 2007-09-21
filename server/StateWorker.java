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
				long timeForLastRound = end - last;
				long toSleep = roundTime - timeForLastRound;
				System.err.println("\tRound took " + timeForLastRound + "ms");
				if(0 < toSleep)
				{
					try
					{
						Thread.sleep(toSleep);
					} 
					catch (InterruptedException ex)
					{
						System.err.println("Who dares wake me from my slumber?");
					}
				}
				last = System.currentTimeMillis();
				
				/* Java's way of expressing synchronized blocks is clunky. */
				synchronized(state)
				{
					synchronized(actions)
					{
						synchronized(clients)
						{
							synchronized(chats)
							{
								System.err.println("");
								System.err.println("Round processing begins:");
								updateState();
								updateClients();
							}
						}
					}
				}
			}
		}

		protected void updateState()
		{
			if(-1 != state.getRounds())
			{
				System.out.println("\tRounds remaining: " + state.getRounds());
			}
			System.err.println("\tUpdating internal state.");
			state.roundsPassed(1);
			for(Map.Entry<Character, Byte> entry : actions.entrySet())
			{
				char agent = entry.getKey();
				state.change(agent, entry.getValue());
			}
			System.err.println("\tClearing out " + actions.size() + " actions.");
			actions.clear();
			System.out.println("\tFlags:" + state.flagString());
			System.out.println("\tCurrent World \n{" + state.toString() + "\n}");
			System.out.println("\tScores: " + state.getScores().toString());
		}

		protected void updateClients()
		{
			ArrayList<Character> toRemove = new ArrayList<Character>();
			System.err.println("\tSending state to clients");
			for(Map.Entry<Character, DataOutputStream> entry : clients.entrySet())
			{
				char agent = entry.getKey();
				DataOutputStream out = entry.getValue();
				byte agentFlags = state.flags(agent);
				try
				{
					byte agentVal = (byte)agent;
					out.writeByte(agentFlags);
					out.writeByte((byte)agent);
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
						System.err.println("Warning: problem writing to agent '"+ agent+"'\n");
					}
					toRemove.add(agent);
				}
			}
			for(char agent : toRemove)
			{
				System.err.println("Removing '" + agent + "'" );
				DataOutputStream connection = clients.remove(agent);
				try
				{
					connection.close();
				}
				catch(IOException ex)
				{
					/* why does close throw an exception anyways? */
				}
			}
			System.err.println("\tClearing out " + chats.size() + " chats.");
			chats.clear();
		}

		public void close()
		{
			synchronized(state)
			{
				synchronized(clients)
				{
					state.end();
					updateClients();
					clients.clear();
				}
			}
		}
	}
