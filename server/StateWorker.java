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
		HashMap<Character, ArrayList<byte[]>> clients;
		long roundTime;
		World state;
		HashMap<Character, Byte> actions;
		HashMap<Character, ChatMessage> chats;
		
		StateWorker(World state, HashMap<Character, Byte> actions, HashMap<Character, ChatMessage> chats, HashMap<Character, ArrayList<byte[]>> clients, long roundTime)
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

		/* XXX Note that this no longer writes directly to client sockets in order to avoid
			blocking in the face of an unresponsive client.  !But! this means that we will
			purposefully start consuming memory instead.  So a hostile agent could attempt to
			exhaust our memory.  This should only be a problem if we're in forever mode.
		*/
		protected void updateClients()
		{
			ArrayList<Character> toRemove = new ArrayList<Character>();
			System.err.println("\tSerializing state for clients");
			byte[] serializedState = state.serialize();
			byte[] serializedChats = new byte[chats.size() * 3];
			/* manually mask and write in network order to avoid endian issues */
			byte[] numChats = new byte[4];
			numChats[0] = (byte)((0xFF000000 & chats.size()) >>> 24);
			numChats[1] = (byte)((0x00FF0000 & chats.size()) >>> 16);
			numChats[2] = (byte)((0x0000FF00 & chats.size()) >>> 8);
			numChats[3] = (byte)(0x000000FF & chats.size());
			int chatIndex = 0;
			for(Map.Entry<Character, ChatMessage> message : chats.entrySet())
			{
				message.getValue().serialize(serializedChats, chatIndex);
				chatIndex++;
			}
			for(Map.Entry<Character, ArrayList<byte[]>> entry : clients.entrySet())
			{
				char agent = entry.getKey();
				ArrayList<byte[]> out = entry.getValue();
				byte[] newState = new byte[1 + 1 + serializedState.length + 4 + serializedChats.length];
				newState[0] = state.flags(agent);
				newState[1] = (byte)agent;
				System.arraycopy(serializedState, 0, newState, 2, serializedState.length);
				System.arraycopy(numChats, 0, newState, 2 + serializedState.length, 4);
				System.arraycopy(serializedChats, 0, newState, 2 + serializedState.length + 4, serializedChats.length);
				synchronized(out)
				{
					out.add(newState);
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
