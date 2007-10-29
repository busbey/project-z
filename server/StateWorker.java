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
			System.out.println("\tCurrent World "+ state.getWidth()+ "x" + state.getHeight() +"\n{" + state.toString() + "\n}");
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
			byte[] canonicalBoard = state.serializeBoard('\0');
			byte[] agentInfo = state.serializeAgentInfo();
			HashMap<Character, byte[]> serializedBoards = new HashMap<Character, byte[]>();
			for(Map.Entry<Character, ArrayList<byte[]>> entry : clients.entrySet())
			{
				char agent = entry.getKey();
				byte[] board = state.serializeBoard(agent);
				if(null != board)
				{
					serializedBoards.put(agent, board);
				}
			}
			byte[] boardHeader = state.serializeHeader();
			byte[] serializedChats = new byte[chats.size() * 3];
			byte[] serializedTrueChats = new byte[chats.size() * 4];
			/* manually mask and write in network order to avoid endian issues */
			byte[] numChats = new byte[4];
			numChats[0] = (byte)((0xFF000000 & chats.size()) >>> 24);
			numChats[1] = (byte)((0x00FF0000 & chats.size()) >>> 16);
			numChats[2] = (byte)((0x0000FF00 & chats.size()) >>> 8);
			numChats[3] = (byte)(0x000000FF & chats.size());
			byte[] numViews = new byte[4];
			int views = serializedBoards.size();
			numViews[0] = (byte)((0xFF000000 & views) >>> 24);
			numViews[1] = (byte)((0x00FF0000 & views) >>> 16);
			numViews[2] = (byte)((0x0000FF00 & views) >>> 8);
			numViews[3] = (byte)(0x000000FF & views);
			byte[] numScores = new byte[4];
			HashMap<Character, Integer> scores = state.getScores();
			int scoreCount = scores.size();
			numScores[0] = (byte)((0xFF000000 & scoreCount) >>> 24);
			numScores[1] = (byte)((0x00FF0000 & scoreCount) >>> 16);
			numScores[2] = (byte)((0x0000FF00 & scoreCount) >>> 8);
			numScores[3] = (byte)((0x000000FF & scoreCount));
			byte[] serializedScores = new byte[scoreCount * 5];
			int scoreIndex = 0;
			for(Map.Entry<Character, Integer> entry : scores.entrySet())
			{
				char agent = entry.getKey();
				int score = entry.getValue();
				serializedScores[scoreIndex + 0] = (byte) agent;
				serializedScores[scoreIndex + 1] = (byte)((0xFF000000 & score) >>> 24);
				serializedScores[scoreIndex + 2] = (byte)((0x00FF0000 & score) >>> 16);
				serializedScores[scoreIndex + 3] = (byte)((0x0000FF00 & score) >>> 8);
				serializedScores[scoreIndex + 4] = (byte)((0x000000FF & score));
				scoreIndex+=5;
			}
			int chatIndex = 0;
			for(Map.Entry<Character, ChatMessage> message : chats.entrySet())
			{
				char agent = message.getKey();
				message.getValue().serialize(serializedChats, chatIndex*3);
				serializedTrueChats[chatIndex*4] = (byte)(agent);
				message.getValue().serialize(serializedTrueChats, chatIndex*4 + 1);
				chatIndex++;
			}
			for(Map.Entry<Character, ArrayList<byte[]>> entry : clients.entrySet())
			{
				char agent = entry.getKey();
				ArrayList<byte[]> out = entry.getValue();
				byte[] newState = null;
				if(state.isBug(agent) || state.isHunter(agent))
				{
					byte[] board = serializedBoards.get(agent);
					newState = new byte[1 + 1 + boardHeader.length + board.length + 4 + serializedChats.length];
					newState[0] = state.flags(agent);
					newState[1] = (byte)agent;
					int offset = 2;
					System.arraycopy(boardHeader, 0, newState, offset, boardHeader.length);
					offset+=boardHeader.length;
					System.arraycopy(board, 0, newState, offset, board.length);
					offset+=board.length;
					System.arraycopy(numChats, 0, newState, offset, 4);
					offset+=4;
					System.arraycopy(serializedChats, 0, newState, offset, serializedChats.length);
					offset+=serializedChats.length;
				}
				else
				{
					newState = new byte[1 + agentInfo.length  + boardHeader.length + canonicalBoard.length + 4
											+ (serializedBoards.size() * (1 + canonicalBoard.length)) + 4 
											+ serializedTrueChats.length + numScores.length + serializedScores.length];
					newState[0] = state.flags(agent);
					int offset = 1;
					System.arraycopy(agentInfo, 0, newState, offset, agentInfo.length);
					offset += agentInfo.length;
					System.arraycopy(boardHeader, 0, newState, offset, boardHeader.length);
					offset += boardHeader.length;
					System.arraycopy(canonicalBoard, 0, newState, offset, canonicalBoard.length);
					offset += canonicalBoard.length;
					System.arraycopy(numViews, 0, newState, offset, numViews.length);
					offset += numViews.length;
					for(Map.Entry<Character, byte[]> boardEntry : serializedBoards.entrySet())
					{
						char viewer = boardEntry.getKey();
						byte[] board = boardEntry.getValue();
						newState[offset] = (byte)viewer;
						offset++;
						System.arraycopy(board, 0, newState, offset, board.length);
						offset += board.length;
					}
					System.arraycopy(numChats, 0, newState, offset, numChats.length);
					offset += numChats.length;
					System.arraycopy(serializedTrueChats, 0, newState, offset, serializedTrueChats.length);
					offset += serializedTrueChats.length;
					System.arraycopy(numScores, 0, newState, offset, numScores.length);
					offset += numScores.length;
					System.arraycopy(serializedScores, 0, newState, offset, serializedScores.length);
					offset += serializedScores.length;
				}
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
