/**
 * @file world representation that can rotate through different maps.
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

public class RotatingWorld extends World
{
	protected ArrayList<File> boards = new ArrayList<File>();
	protected int	curBoard = 0;
    
	public RotatingWorld()
	{
	}
	
	public RotatingWorld(int rounds)
	{
		super(rounds);
	}
	
	public RotatingWorld(ArrayList<File> boards, int rounds) throws IOException
	{
		this(boards, 0, rounds);
	}
	
	public RotatingWorld(ArrayList<File> boards, int firstBoard, int rounds) throws IOException
	{
		super(rounds);
		this.boards.addAll(boards);
		loadBoard(firstBoard,true);
	}

	/** XXX like most of hte world related functions, call ties without a lock on the object is bad */
	public void rotate(boolean reset) throws IOException
	{
		loadBoard((curBoard+1)%boards.size(), reset);
	}

	/** XXX like most of hte world related functions, call ties without a lock on the object is bad */
	public void loadBoard(int boardNum, boolean reset) throws IOException
	{
		if(reset)
		{
			flags |= SET_ROUND_CHANGE;
			score.clear();
			agentFlags.clear();
		}
		Set<Character> agents= location.keySet();
		load(boards.get(boardNum));
		location.clear();
		/* need to make sure locations are valid */
		for(Character where : agents)
		{
			setRandomEmpty(where);
		}
		curBoard = boardNum;
	}

	public File[] getMaps()
	{
		return boards.toArray(new File[0]);
	}
}
