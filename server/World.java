/**
 * @file world representation
 *
 */
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

public class World implements Serializable
{
	public static final byte FLAGS_EMPTY = 0x00;
	public static final byte FLAGS_GAME_END = (byte)(0xff);
	public static final byte SET_AGENT_KILLED = 0x8;
	public static final byte SET_AGENT_STUN = 0x04;
	public static final byte SET_AGENT_DIED = 0x02;
	public static final byte SET_BUG_EATS = 0x1;
	public static final byte SET_ROUND_CHANGE = 0x10;
	public static final byte CLEAR_AGENT_KILLED = (byte)(0xF7);
	public static final byte CLEAR_AGENT_STUN = (byte)(0xFB);
	public static final byte CLEAR_AGENT_DIED = (byte)(0xFD);
	public static final byte CLEAR_BUG_EATS = (byte)(0xFE);
	public static final byte CLEAR_ROUND_CHANGE = (byte)(0xEF);
	
	public static final int DEFAULT_ROUNDS_TO_EAT = 40;
	
	public int SCORE_BUG_KILL = 400;
	public int SCORE_HUNTER_KILL = 100;
	public int SCORE_STUNNED = 0;
	public int SCORE_KILLED = -100;
	public int SCORE_POWERUP = 0;
	public int ROUNDS_PER_FRAME = 9;
	
	protected int roundsToEat =0;

	public static final char OBSTACLE = 'O';
	public static final char BUG_MIN = 'B';
	public static final char BUG_MAX = 'N';
	public static final char HUNTER_MIN = '0';
	public static final char HUNTER_MAX = '9';
	public static final char POWERUP = 'P';
	public static final char EMPTY = ' ';

	public static final double powerup = 0.007;	
	public static final double obstacle = 0.1;
	public static final char[] valid = {' ', 
										'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
										'J', 'K', 'L', 'M', 'N', '0', '1', '2', '3',
										'4', '5', '6', '7', '8', '9', 'O', 'P'
										};
	public static final char FRAME_SEPARATOR = '=';
	protected char[][] state = null; 

	protected char[][][] frames = null;
	protected int currentFrame = -1;

	protected int roundsToFrame = ROUNDS_PER_FRAME;

	protected boolean animate = true;
	
	protected byte flags = FLAGS_EMPTY;
	protected boolean lastRotate = false;
	protected HashMap<Character, Byte> agentFlags = new HashMap<Character, Byte>();
	protected HashMap<Character, Long> location = new HashMap<Character, Long>();
	protected HashMap<Character, Integer> score = new HashMap<Character, Integer>();

	protected int rounds = -1;

	protected ArrayList<byte[]> stuns = new ArrayList<byte[]>();
	protected ArrayList<byte[]> kills = new ArrayList<byte[]>();

	protected byte[] cache = null;
	protected byte[] headerCache = null;
	protected boolean changed = true;
	protected boolean changedHeader = true;

	protected WorldFilter filter = new WorldFilter();

	/** @brief create an uninitialized world */
	protected World()
	{
	}

	/** @brief create a randomly sized/populated world. */
	protected World(int rounds)
	{
		this((int)(Math.random()*100) + 6, (int)(Math.random()*100) + 6, rounds);
	}

	/** @brief create a blank world of a given size */
	public World(int rowSize, int numRows)
	{
		this(rowSize, numRows, -1);
	}
	public World(int cols, int rows, int rounds)
	{
		this.rounds = rounds;
		loadRandom(cols, rows);
		state[0][0] = 'B';
		state[rows - 1][cols - 1] = 'P';
		state[rows - 2][cols - 2] = '1';
		state[rows - 3][cols - 3] = '2';
		state[rows - 4][cols - 4] = '3';
		state[rows - 5][cols - 5] = '4';
		
		location.put('B', ((long) 0) | (((long)0) << 32) );
		location.put('1', ((long) rows -2) | (((long)cols-2) << 32) );
		location.put('2', ((long) rows -3) | (((long)cols-3) << 32) );
		location.put('3', ((long) rows -4) | (((long)cols-4) << 32) );
		location.put('4', ((long) rows -5) | (((long)cols-5) << 32) );
		score.put('B', 0);
		score.put('1', 0);
		score.put('2', 0);
		score.put('3', 0);
		score.put('4', 0);
	}

	protected void loadRandom()
	{
		loadRandom((int)(Math.random()*100) + 6, (int)(Math.random()*100) + 6);
	}

	protected void loadRandom(int cols, int rows)
	{
		assert(4 < rows && 4 < cols);
		state = new char[rows][cols];
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < cols; j++)
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
		changed = true;
		changedHeader = true;
	}

	public void change(char agent, byte move)
	{
		if(FLAGS_GAME_END == (flags & FLAGS_GAME_END))
		{
			System.err.println("No changes, game is over.");
			return;
		}
		if(location.containsKey(agent))
		{
			if('n' == move)
			{
				System.err.println("Taking no move for agent '"+agent+"'");
				return;
			}
			long loc = location.get(agent);
			int row = (int)(loc & (0xFFFFFFFF));
			int col = (int)(loc >>> 32);
			int newRow = row;
			int newCol = col;
			switch(move)
			{
				case 'u':
					System.err.println(agent + " moves up");
					newRow--;
					while(0 > newRow)
					{
						newRow += state.length;
					}
					break;
				case 'd':
					System.err.println(agent + " moves down");
					newRow = (newRow + 1) % state.length;
					break;
				case 'r':
					System.err.println(agent + " moves right");
					newCol = (newCol + 1) % state[row].length;
					break;
				case 'l':
					System.err.println(agent + " moves left");
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
				else
				{
					int oldScore = 0;
					if(score.containsKey(agent))
					{
						oldScore = score.get(agent);
					}
					oldScore += SCORE_POWERUP;
					score.put(agent, oldScore);
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
			if(!score.containsKey(agent))
			{
				score.put(agent, 0);
			}
			setRandomEmpty(agent);
		}
		changed = true;
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
		//System.out.println("Current World \n{" + this.toString() + "\n}");
	}

	public void kill(char target, char by)
	{
		System.out.println("Agent " + target + " killed by " + by);
		kills.add(new byte[]{(byte)by, (byte)target});
		int oldScore = 0;
		if(score.containsKey(by))
		{
			oldScore = score.get(by);
		}
		oldScore += isBug(target) ? SCORE_BUG_KILL : SCORE_HUNTER_KILL;
		score.put(by, oldScore);
		oldScore = 0;
		if(score.containsKey(target))
		{
			oldScore = score.get(target);
		}
		oldScore += SCORE_KILLED;
		score.put(target, oldScore);
		byte curFlags = FLAGS_EMPTY;
		if(agentFlags.containsKey(by))
		{
			curFlags = agentFlags.get(by);
		}
		curFlags |= SET_AGENT_KILLED;
		agentFlags.put(by, curFlags);
		if(location.containsKey(target))
		{
			long loc = location.get(target);
			int row = (int)(loc & 0xFFFFFFFF);
			int col = (int)(loc >>> 32);
			state[row][col] = EMPTY;
			curFlags = FLAGS_EMPTY;
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
		stuns.add(new byte[]{(byte)by, (byte)agent});
		/* stun */
		System.out.println("Agent " + agent + " stunned by " + by);
		int oldScore = 0;
		if(score.containsKey(agent))
		{
			oldScore = score.get(agent);
		}
		oldScore += SCORE_STUNNED;
		score.put(agent, oldScore);
		oldScore = 0;
		if(score.containsKey(by))
		{
			oldScore = score.get(by);
		}
		oldScore += SCORE_STUNNED;
		score.put(by, oldScore);
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
				return;
			}
		}
	// 	keep track if last round was a rotation round
		if (lastRotate)
		{
			flags &= CLEAR_ROUND_CHANGE;
		}
		lastRotate = (0 != (flags & SET_ROUND_CHANGE)); 
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
			curFlag &= CLEAR_AGENT_DIED & CLEAR_AGENT_STUN & CLEAR_AGENT_KILLED;
			agentFlags.put(agent, curFlag);
		}
		stuns.clear();
		kills.clear();
		if(null != frames && 1 < frames.length && animate)
		{
			roundsToFrame -= num;
			if(0 >= roundsToFrame)
			{
				currentFrame = (currentFrame+1)%frames.length;
				for(int i = 0; i < state.length; i++)
				{
					for(int j=0; j < state[i].length; j++)
					{
						if(	!isBug(state[i][j]) && 
							!isHunter(state[i][j]) &&
							(!(POWERUP == state[i][j] && EMPTY == frames[currentFrame][i][j]))
							)
						{
							state[i][j] = frames[currentFrame][i][j];
						}
					}
				}
				roundsToFrame = ROUNDS_PER_FRAME;
			}
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

	public static boolean isBug(char agent)
	{
		return BUG_MIN <=agent && BUG_MAX >= agent;
	}

	public static boolean isHunter(char agent)
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
		System.out.println("\tScores: " + score.toString());
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
		retVal.load(path);
		return retVal;
	}

	public void load(String path) throws IOException
	{
		load(new FileReader(path));
	}

	public void load(File path) throws IOException
	{
		load(new FileReader(path));
	}

	public void load(FileReader path) throws IOException
	{
		int 			rows	= 0;
		BufferedReader 	in 		= new BufferedReader(path);
		String 			curLine = in.readLine();
		char[][][]		frames	= null;
		int				curFrame = 0;
		
		Arrays.sort(valid);
		
		if(null != curLine)
		{
			frames = new char[10][10][curLine.length()];
		}
		
		while(null != curLine)
		{
			if(curFrame >= frames.length || rows >= frames[curFrame].length)
			{
				int newNumFrames = curFrame >= frames.length ? curFrame*2 : frames.length;
				int newNumRows = rows >= frames[frames.length - 1].length ? rows*2 : frames[frames.length - 1].length;
				int maxCopy = curFrame < frames.length ? (curFrame+1) : frames.length;
				char[][][] newFrame = new char[newNumFrames][newNumRows][frames[frames.length - 1][0].length];
				for(int k = 0; k < maxCopy; k++)
				{
					for(int i = 0; i < frames[k].length; i++)
					{
						for(int j=0; j < frames[k][i].length; j++)
						{
							newFrame[k][i][j] = frames[k][i][j];
						}
					}
				}
				frames = newFrame;
			}
			/* check for comments */
			if(';' == curLine.charAt(0))
			{
				try
				{
					/* check for scores */
					if(curLine.startsWith(";bug kill score:"))
					{
						SCORE_BUG_KILL = Integer.parseInt(curLine.substring(16));
						System.err.println("Setting point value for killing a bug to " + SCORE_BUG_KILL);
					}
					else if(curLine.startsWith(";hunter kill score:"))
					{
						SCORE_HUNTER_KILL = Integer.parseInt(curLine.substring(19));
						System.err.println("Setting point value for killing a hunter to " + SCORE_HUNTER_KILL);
					}
					else if(curLine.startsWith(";powerup:"))
					{
						SCORE_POWERUP = Integer.parseInt(curLine.substring(9));
						System.err.println("Setting point value for a hunter grabbing a powerup to " + SCORE_POWERUP);
					}
					else if(curLine.startsWith(";stunned:"))
					{
						SCORE_STUNNED = Integer.parseInt(curLine.substring(9));
						System.err.println("Setting point value for getting stunned to " + SCORE_STUNNED);
					}
					else if(curLine.startsWith(";killed:"))
					{
						SCORE_KILLED = Integer.parseInt(curLine.substring(8));
						System.err.println("Setting point value for getting killed to " + SCORE_KILLED);
					}
					else if(curLine.startsWith(";rounds per frame:"))
					{
						ROUNDS_PER_FRAME = Integer.parseInt(curLine.substring(18));
						System.err.println("Setting ave rounds per frame to " + ROUNDS_PER_FRAME);
					}
				}
				catch(NumberFormatException ex)
				{
					System.err.println("Error configuring score.");
					ex.printStackTrace();
				}
			}
			/* check for frame separators. */
			else if(FRAME_SEPARATOR == curLine.charAt(0))
			{
				/* */
				System.err.println("Found frame marker");
				curFrame++;
				rows = 0;
			}
			else
			{
				for(int i = 0; i < frames[curFrame][rows].length; i++)
				{
					char entry = curLine.charAt(i);
					if(0 > Arrays.binarySearch(valid, entry))
					{
						throw new IOException("invalid world state map. '"+entry+"' encountered on line " + ((curFrame)*frames[0].length + curFrame + rows) + " col " + i + "\n" + curLine);
					}
					if( (BUG_MIN <= entry && BUG_MAX >= entry) ||
						((HUNTER_MIN) <= entry && HUNTER_MAX >= entry))
					{
						location.put(entry, ((long)rows) | (((long)i) << 32));
						if(!score.containsKey(entry))
						{
							score.put(entry, 0);
						}
					}
					frames[curFrame][rows][i] = entry;
				}
				rows++;
			}
			curLine = in.readLine();
		}

		if(null == frames)
		{
			throw new RuntimeException("Couldn't read world properly from file.");
		}
		else
		{
			changed = true;
			changedHeader=true;
			this.frames = new char[curFrame+1][rows][frames[curFrame][0].length];
			System.err.println("Compacting to " + (curFrame+1) + " frames of " + rows + "x" + frames[curFrame][0].length);
			for(int k = 0; k <= curFrame; k++)
			{
				for(int i = 0; i < rows; i++)
				{
					for(int j=0; j < frames[curFrame][0].length; j++)
					{
						this.frames[k][i][j] = frames[k][i][j];
					}
				}
			}
			this.state = new char[this.frames[0].length][this.frames[0][0].length];
			for(int i = 0; i < state.length; i++)
			{
				for(int j=0; j < state[i].length; j++)
				{
					this.state[i][j] = this.frames[0][i][j];
				}
			}
			this.currentFrame = 0;
		}
		return;
	}

	public byte[] serialize()
	{
		return serialize('\0');
	}

	/** @brief serialize the game world according to a particular agent.
	 *	@param agent	this board is for this agent, '\0' means canon.
	 */
	public byte[] serialize(char agent)
	{
		/*XXX we assume uniform row lengths */
		//System.err.println("Serializing World:");
		int rowSize = 0;
		int numRows = state.length;
		if(0 < numRows)
		{
			rowSize = state[0].length;
		}
		byte[] serialized = new byte[4 + 4 + numRows * rowSize];
		//System.err.println("\tsize of row: " + rowSize);
		//System.err.println("\tnumber of rows: " + numRows);
		serializeHeader(serialized, 0);
		serializeBoard(serialized, 8, agent);
		return serialized;
	}

	/** @brief serialize info about the game board
	 */
	public byte[] serializeHeader()
	{
		byte[] buffer = new byte[8];
		serializeHeader(buffer, 0);
		return buffer;
	}
	
	public void serializeHeader(byte[] buffer, int offset)
	{
		int rowSize = 0;
		int numRows = state.length;
		if(0 < numRows)
		{
			rowSize = state[0].length;
		}
		if((changedHeader) || (null == headerCache))
		{
			if(null == headerCache)
			{
				headerCache = new byte[8];
			}
			headerCache[offset + 0] = (byte)((0xFF000000 & rowSize) >>> 24);
			headerCache[offset + 1] = (byte)((0x00FF0000 & rowSize) >>> 16);
			headerCache[offset + 2] = (byte)((0x0000FF00 & rowSize) >>> 8);
			headerCache[offset + 3] = (byte)(0x000000FF & rowSize);
			headerCache[offset + 4] = (byte)((0xFF000000 & numRows) >>> 24);
			headerCache[offset + 5] = (byte)((0x00FF0000 & numRows) >>> 16);
			headerCache[offset + 6] = (byte)((0x0000FF00 & numRows) >>> 8);
			headerCache[offset + 7] = (byte)(0x000000FF & numRows);
			changedHeader = false;
		}
		System.arraycopy(headerCache, 0, buffer, offset, headerCache.length);
	}

	/** @brief write out agent stun/kill events. */
	public byte[] serializeAgentInfo()
	{
		int numStuns = stuns.size();
		int numKills = kills.size();
		byte[] buffer = new byte[4 + 2 * numStuns + 4 + 2 * numKills];
		int offset = 0;
		buffer[offset + 0] = (byte)((0xFF000000 & numStuns) >>> 24);
		buffer[offset + 1] = (byte)((0x00FF0000 & numStuns) >>> 16);
		buffer[offset + 2] = (byte)((0x0000FF00 & numStuns) >>> 8);
		buffer[offset + 3] = (byte)(0x000000FF & numStuns);
		offset += 4;
		for(byte[] stun : stuns)
		{
			buffer[offset + 0] = stun[0];
			buffer[offset + 1] = stun[1];
			offset += 2;
		}
		buffer[offset + 0] = (byte)((0xFF000000 & numKills) >>> 24);
		buffer[offset + 1] = (byte)((0x00FF0000 & numKills) >>> 16);
		buffer[offset + 2] = (byte)((0x0000FF00 & numKills) >>> 8);
		buffer[offset + 3] = (byte)(0x000000FF & numKills);
		offset += 4;
		for(byte[] kill: kills)
		{
			buffer[offset + 0] = kill[0];
			buffer[offset + 1] = kill[1];
			offset += 2;
		}

		return buffer;
	}
	
	/** @brief write out the board for a particular agent
	 *	@param buffer	buffer to serialize to
	 *	@param start	index in buffer to start writing at
	 *	@param agent	perspective to use when rendering - '\0' means canon
	 *
	 *  writes in place to buffer
	 *
	 *	@return number of bytes used.
	 */
	public int serializeBoard(byte[] buffer, int start, char agent)
	{
		int used = -1;
		if('\0' == agent)
		{
			if(changed || null == cache)
			{
				if(null == cache || cache.length != state.length*state[0].length)
				{
					cache = new byte[state.length * state[0].length];
				}
				int index = 0;
				/*XXX we assume uniform row lengths */
				for(int i = 0; i < state.length; i++)
				{
					for(int j=0; j < state[0].length; j++, index++)
					{
						cache[index] = (byte)(state[i][j]);
					}
				}
				changed = false;
			}
			System.arraycopy(cache, 0, buffer, start, cache.length);
			used = cache.length;
		}
		else
		{
			used = 0;
			char[][] filteredState = filter.filter(state, agent);
			/*XXX we assume uniform row lengths */
			for(int i = 0; i < filteredState.length; i++)
			{
				for(int j=0; j < filteredState[0].length; j++, used++,start++)
				{
						buffer[start] = (byte)(filteredState[i][j]);
				}
			}
		}
		return used;
	}
	public byte[] serializeBoard(char agent)
	{
		byte[] board = new byte[state.length * state[0].length];
		serializeBoard(board, 0, agent);
		return board;
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

	public HashMap<Character, Integer> getScores()
	{
		HashMap<Character, Integer> returnVal = new HashMap<Character, Integer>();
		returnVal.putAll(score);
		return returnVal;
	}

	public String flagString () 
	{
		String value = null;
		if(FLAGS_GAME_END == (flags & FLAGS_GAME_END))
		{
			value = " [game over]";
		}
		else
		{
			if (SET_BUG_EATS == (flags & SET_BUG_EATS))
			{
				value = " [bug kills]";
			}
			if(SET_ROUND_CHANGE == (flags & SET_ROUND_CHANGE))
			{
				if(null == value)
				{
					value = "";
				}
				value += " [round change]";
			}
		}
		if(null == value)
		{
			value = "None.";
		}
		return value;
	}

	public int getHeight()
	{
		return state.length;
	}

	public int getWidth()
	{
		return state[0].length;
	}

	public WorldFilter getFilter()
	{
		return filter;
	}

	public void setFilter (WorldFilter newFilter) 
	{
		filter = newFilter;
	}

	public void clearFilter()
	{
		filter = new WorldFilter();
	}

	public boolean getAnimate()
	{
		return animate;
	}

	public void setAnimate(boolean anim)
	{
		animate = anim;
	}
}
