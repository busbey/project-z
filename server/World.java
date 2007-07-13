/**
 * @file world representation
 *
 */

import java.io.*;
import java.util.*;

public class World implements Serializable
{

	protected char[][] state = null; 

	/** @brief create an uninitialized world */
	protected World()
	{
	}

	/** @brief create a blank world of a given size */
	public World(int rowSize, int numRows)
	{
		state = new char[rowSize][numRows];
		for(int i = 0; i < numRows; i++)
		{
			for(int j = 0; j < rowSize; j++)
			{
				state[i][j] = ' ';
			}
		}
	}

	public boolean change(char agent, byte move)
	{
	
		return false;
	}

	public char get(int column, int row)
	{
		return state[row][column];
	}
	
	public void put(int column, int row, char value)
	{
		state[row][column] = value;
	}

	/** @brief read in an ascii file that shows an initial world state */
	public static World fromFile(String path) throws IOException
	{
		World retVal = new World();
		int rows	= 0;
		BufferedReader in = new BufferedReader(new FileReader(path));
		String curLine = in.readLine();
		char[] valid = {' ', 'b', '1', '2', '3', '4', 'O'};
		
		Arrays.sort(valid);
		
		if(null != curLine)
		{
			retVal.state = new char[10][curLine.length()];
		}
		
		while(null != curLine)
		{
			if(rows > retVal.state.length)
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
				retVal.state[rows][i] = entry;
			}
			curLine = in.readLine();
		}

		if(null == retVal.state)
		{
			retVal.state = new char[0][0];
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
		return new World(8,8);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeInt(state[0].length);
		out.writeInt(state.length);
		for(int i = 0; i < state.length; i++)
		{
			for(int j=0; j < state[i].length; j++)
			{
				out.writeByte(state[i][j]);
			}
		}
	}
}
