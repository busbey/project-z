/**
 * @file world representation
 *
 */
/* Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs
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

public class World implements Serializable
{
	public static final byte FLAGS_EMPTY = 0x00;
	public static final byte FLAGS_GAME_END = (byte)(0xff);
	public static final byte SET_AGENT_STUN = 0x04;
	public static final byte SET_AGENT_DIED = 0x02;
	public static final byte SET_BUG_EATS = 0x1;
	public static final byte CLEAR_AGENT_STUN = (byte)(0xFB);
	public static final byte CLEAR_AGENT_DIED = (byte)(0xFD);
	public static final byte CLEAR_BUG_EATS = (byte)(0xFE);
	
	public static final int DEFAULT_ROUNDS_TO_EAT = 100;
	
	public static final long SCORE_BUG_KILL = 400;
	public static final long SCORE_HUNTER_KILL = 100;
	
	protected int roundsToEat =0;

	public static final char OBSTACLE = 'O';
	public static final char BUG_MIN = 'B';
	public static final char BUG_MAX = 'N';
	public static final char HUNTER_MIN = '1';
	public static final char HUNTER_MAX = '9';
	public static final char POWERUP = 'P';
	public static final char EMPTY = ' ';

	public static final double powerup = 0.007;	
	public static final double obstacle = 0.1;
	public static final char[] valid = {' ', 'B', 'C', '1', '2', '3', '4', 'O', 'P'};
	protected char[][] state = null; 
	protected byte flags = FLAGS_EMPTY;
	HashMap<Character, Byte> agentFlags = new HashMap<Character, Byte>();
	HashMap<Character, Long> location = new HashMap<Character, Long>();
	HashMap<Character, Long> score = new HashMap<Character, Long>();

	protected int rounds = -1;

	/** @brief create an uninitialized world */
	protected World()
	{
	}

	protected World(int rounds)
	{
		this.rounds = rounds;
	}

	/** @brief create a blank world of a given size */
	public World(int rowSize, int numRows)
	{
		this(rowSize, numRows, -1);
	}
	public World(int rowSize, int numRows, int rounds)
	{
		assert(4 < numRows && 4 < rowSize);
		state = new char[numRows][rowSize];
		for(int i = 0; i < numRows; i++)
		{
			for(int j = 0; j < rowSize; j++)
			{
				double type = Math.random();
				if(obstacle > type)
				{
					state[i][j] = 'O';
				}
				else
				{
					state[i][j] = ' ';
				}
			}
		}
		state[0][0] = 'B';
		state[numRows - 1][rowSize - 1] = 'P';
		state[numRows - 2][rowSize - 2] = '1';
		state[numRows - 3][rowSize - 3] = '2';
		state[numRows - 4][rowSize - 4] = '3';
		state[numRows - 5][rowSize - 5] = '4';
		
		location.put('B', ((long) 0) | (((long)0) << 32) );
		location.put('1', ((long) numRows -2) | (((long)rowSize-2) << 32) );
		location.put('2', ((long) numRows -3) | (((long)rowSize-3) << 32) );
		location.put('3', ((long) numRows -4) | (((long)rowSize-4) << 32) );
		location.put('4', ((long) numRows -5) | (((long)rowSize-5) << 32) );
		this.rounds = rounds;
	}

	public void change(char agent, byte move)
	{
		if('n' == move)
		{
			return;
		}
		if(location.containsKey(agent))
		{
			long loc = location.get(agent);
			int row = (int)(loc & (0xFFFFFFFF));
			int col = (int)(loc >>> 32);
			int newRow = row;
			int newCol = col;
			switch(move)
			{
				case 'u':
					newRow--;
					while(0 > newRow)
					{
						newRow += state.length;
					}
					break;
				case 'd':
					newRow = (newRow + 1) % state.length;
					break;
				case 'r':
					newCol = (newCol + 1) % state[row].length;
					break;
				case 'l':
					newCol--;
					while(0 > newCol)
					{
						newCol += state[row].length;
					}
					break;
				default:
				break;
			}
			char target = state[newRow][newCol];

			if(target == agent)
			{
				System.err.println("----------------------------------------------------");
				System.err.println("Warning: target and agent are the same. move was '"+move+"'");
				System.err.println(" agent " + agent + "["+row+","+col+"] -- target " + target + "["+newRow+","+newCol+"]");
				System.err.println("World {"+this.toString()+"\n}");
				System.err.println("----------------------------------------------------");
			}
			if(EMPTY == target)
			{
				//System.err.println("Target square is empty");
				move(row, col, newRow, newCol);
			}
			else if(POWERUP == target)
			{
				//System.err.println("Agent " + agent + " has consumed powerup at [ " + row + " , " + col + " ]");
				if(isBug(agent))
				{
					bugEats();
				}
				move(row, col, newRow, newCol);
			}
			else if(OBSTACLE != target)
			{
				if(	(isHunter(agent) && isHunter(target)) ||
					(isBug(agent) && isBug(target)) )
				{
					stun(target, agent);
				}
				else
				{
					boolean bugLoses = (0 == (flags & SET_BUG_EATS));
					if(isHunter(agent))
					{
						if(bugLoses)
						{
							//System.err.println("Hunter " + agent + " kills Bug " + target);
							kill(target, agent);
							move(row, col, newRow, newCol);
							setRandomEmpty(target);
						}
						else
						{
							//System.err.println("Hunter " + agent + " runs into Bug " + target + " and dies");
							kill(agent, target);
							setRandomEmpty(agent);
						}
					}
					else if(isBug(agent))
					{
						if(bugLoses)
						{
							//System.err.println("Bug " + agent + " runs into Hunter " + target + " and dies");
							kill(agent, target);
							setRandomEmpty(agent);
						}
						else
						{
							//System.err.println("Bug " + agent + " kills Hunter " + target);
							kill(target, agent);
							move(row, col, newRow, newCol);
							setRandomEmpty(target);
						}
					}
				}
			}
		}
		else
		{
			System.err.println("New Agent " + agent + " first appears.");
			setRandomEmpty(agent);
		}
	}

	public void bugEats()
	{
		flags |= SET_BUG_EATS;
		roundsToEat = DEFAULT_ROUNDS_TO_EAT;
	}

	public void move(int fromRow, int fromCol, int toRow, int toCol)
	{
		char agent = state[fromRow][fromCol];
		//System.err.println("Moving " +agent+ " [ "+fromRow+" , "+fromCol+" ] =>  "+state[toRow][toCol]+" [ " + toRow +" , " + toCol + " ]");
		location.put(agent, ( ((long)toRow) | (((long)toCol) << 32)));
		//System.err.println("Previous World \n{" + this.toString() + "\n}");
		state[toRow][toCol] = state[fromRow][fromCol];
		state[fromRow][fromCol] = EMPTY;
		//System.err.println("Current World \n{" + this.toString() + "\n}");
	}

	public void kill(char target, char by)
	{
		System.err.println("Agent " + target + " killed by " + by);
		long oldScore = 0l;
		if(score.containsKey(by))
		{
			oldScore = score.get(by);
		}
		oldScore += isBug(target) ? SCORE_BUG_KILL : SCORE_HUNTER_KILL;
		score.put(by, oldScore);
		if(location.containsKey(target))
		{
			long loc = location.get(target);
			int row = (int)(loc & 0xFFFFFFFF);
			int col = (int)(loc >>> 32);
			state[row][col] = EMPTY;
			byte curFlags = FLAGS_EMPTY;
			if(agentFlags.containsKey(target))
			{
				curFlags = agentFlags.get(target);
			}
			curFlags |= SET_AGENT_DIED;
			agentFlags.put(target, curFlags);
		}
		else
		{
			System.err.println("Warning: target " + target + " killed but has no location.");
		}
	}

	public void stun(char agent, char by)
	{
		/* stun */
		System.err.println("Agent " + agent + " stunned by " + by);
		byte curFlag = FLAGS_EMPTY;
		if(agentFlags.containsKey(agent))
		{
			curFlag = agentFlags.get(agent);
		}
		curFlag |= SET_AGENT_STUN;
		agentFlags.put(agent, curFlag);
		curFlag = FLAGS_EMPTY;
		if(agentFlags.containsKey(by))
		{
			curFlag = agentFlags.get(by);
		}
		curFlag |= SET_AGENT_STUN;
		agentFlags.put(by, curFlag);
	}

	/** XXX this is not thread safe, on the assumption
			that the game world will be in one thread.
	*/
	public void roundsPassed(int num)
	{
		if(FLAGS_GAME_END == flags)
		{
			return;
		}
		if(-1 != rounds)
		{
			rounds -= num;
			if(0 >= rounds)
			{
				end();
			}
		}
		if(0 != (flags & SET_BUG_EATS))
		{
			roundsToEat -= num;
			if(0 >= roundsToEat)
			{
				flags &= CLEAR_BUG_EATS;
				setRandomEmpty(POWERUP);
			}
		}
		else if(powerup > Math.random() )
		{
			setRandomEmpty(POWERUP);
		}
		for(Character agent : agentFlags.keySet())
		{
			byte curFlag = agentFlags.get(agent);
			curFlag &= CLEAR_AGENT_DIED & CLEAR_AGENT_STUN;
			agentFlags.put(agent, curFlag);
		}

	}

	public void setRandomEmpty(char target)
	{

		int row = (int)(Math.random() * (state.length-1));
		int col = (int)(Math.random() * (state[row].length-1));
		while(EMPTY != state[row][col] && POWERUP != state[row][col])
		{
			row = (int)(Math.random() * (state.length-1));
			col = (int)(Math.random() * (state[row].length-1));
		}
		if(POWERUP == state[row][col] && isBug(target))
		{
			bugEats();
		}
		//System.err.println("Spawning " + target + " [ "+row+" , "+col+" ]");
		//System.err.println("Previous World \n{" + this.toString() + "\n}");
		state[row][col] = target;
		//System.err.println("Current World \n{" + this.toString() + "\n}");
		location.put(target, ( ((long) row) | (((long) col) << 32) ));
	}

	public boolean isBug(char agent)
	{
		return BUG_MIN <=agent && BUG_MAX >= agent;
	}

	public boolean isHunter(char agent)
	{
		return HUNTER_MIN <= agent && HUNTER_MAX >= agent;
	}

	public byte flags(char agent)
	{
		byte ret = flags;
		if(agentFlags.containsKey(agent))
		{
			ret |= agentFlags.get(agent);
		}
		return ret;
	}

	public boolean gameRunning()
	{
		return FLAGS_GAME_END != (FLAGS_GAME_END & flags);
	}

	public void end()
	{
		System.err.println("Ending game...");
		System.err.println("\tScores: " + score.toString());
		flags = FLAGS_GAME_END;
	}

	public char get(int column, int row)
	{
		return state[row][column];
	}
	
	public void put(int column, int row, char value)
	{
		//System.err.println("Setting "+ value +" [ "+row+" , "+column + " ]");
		state[row][column] = value;
	}

	/** @brief read in an ascii file that shows an initial world state */
	public static World fromFile(String path) throws IOException
	{
		return fromFile(path, -1);
	}

	public static World fromFile(String path, int rounds) throws IOException
	{
		World retVal = new World(rounds);
		int rows	= 0;
		BufferedReader in = new BufferedReader(new FileReader(path));
		String curLine = in.readLine();
		
		Arrays.sort(valid);
		
		if(null != curLine)
		{
			retVal.state = new char[10][curLine.length()];
		}
		
		while(null != curLine)
		{
			if(rows >= retVal.state.length)
			{
				char[][] state = new char[retVal.state.length*2][retVal.state[0].length];
				for(int i = 0; i < retVal.state.length; i++)
				{
					for(int j=0; j < retVal.state[i].length; j++)
					{
						state[i][j] = retVal.state[i][j];
					}
				}
				retVal.state = state;
			}
			for(int i = 0; i < retVal.state[rows].length; i++)
			{
				char entry = curLine.charAt(i);
				if(0 > Arrays.binarySearch(valid, entry))
				{
					throw new IOException("invalid world state map.");
				}
				if( (BUG_MIN <= entry && BUG_MAX >= entry) ||
					((HUNTER_MIN) <= entry && HUNTER_MAX >= entry))
				{
					retVal.location.put(entry, ((long)rows) | (((long)i) << 32));
				}
				retVal.state[rows][i] = entry;
			}
			rows++;
			curLine = in.readLine();
		}

		if(null == retVal.state)
		{
			throw new RuntimeException("Couldn't read world properly from file.");
		}
		else
		{
			char[][] state = new char[rows][retVal.state[0].length];
			for(int i = 0; i < state.length; i++)
			{
				for(int j=0; j < state[i].length; j++)
				{
					state[i][j] = retVal.state[i][j];
				}
			}
			retVal.state = state;
		}
		return retVal;
	}

	public static World random()
	{
	    int numRows = (int)(Math.random()*100) + 6;
		int rowSize = (int)(Math.random()*100) + 6;
		World returnVal = new World(rowSize, numRows);
		
		return returnVal;
	}

	public void serialize(java.io.DataOutputStream out) throws IOException
	{
		//System.err.println("Serializing World:");
		int rowSize = 0;
		int numRows = state.length;
		if(0 < numRows)
		{
			rowSize = state[0].length;
		}
		//System.err.println("\tsize of row: " + rowSize);
		//System.err.println("\tnumber of rows: " + numRows);
		out.writeInt(rowSize);
		out.writeInt(numRows);
		for(int i = 0; i < state.length; i++)
		{
			//System.err.println("\t writing out row " + i);
			for(int j=0; j < state[i].length; j++)
			{
				//System.err.println("\t\twriting out entry " + j);
				out.writeByte(state[i][j]);
			}
		}
	}
	
	public String toString()
	{
		StringBuffer ret= new StringBuffer();
		for(int i = 0 ; i < state.length; i++)
		{
			ret.append("\n|");
			for(int j = 0; j < state[i].length; j++)
			{
				if(' ' != state[i][j])
				{
					ret.append(state[i][j]);
				}
				else
				{
					ret.append('_');
				}
			}
		}
		return ret.toString();
	}

	public int getRounds()
	{
		return rounds;
	}

	public HashMap<Character, Long> getScores()
	{
		HashMap<Character, Long> returnVal = new HashMap<Character, Long>();
		returnVal.putAll(score);
		return returnVal;
	}
}
