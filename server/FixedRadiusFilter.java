/**
 * @file filter the game board for a particular agent - only show them squares a certain distance away.
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
public class FixedRadiusFilter extends WorldFilter
{
	public static final int DEFAULT_RADIUS = 5;
	public static final boolean SQUARE_DISTANCE = true;
	public static final boolean MANHATTAN_DISTANCE = false;
	protected int radius;
	/**@brief true => square false => manhattan distance. */
	protected boolean squareOrMan = true;
	
	public FixedRadiusFilter()
	{
		this(DEFAULT_RADIUS);
	}

	public FixedRadiusFilter(int radius)
	{
		this(radius,SQUARE_DISTANCE);
	}

	public FixedRadiusFilter(int radius, boolean distanceMeasure)
	{
		this.radius = radius;
		this.squareOrMan = distanceMeasure;
	}
	
	/** @brief the default implementation doesn't change the world appearance at all.
	 */
 	public char[][] filter (char[][] state, final char agent)
	{
		return filter(state, agent, radius);
	}
	protected char[][] filter (char[][] state, final char agent, final int radius)
	{
		/* initialize these such that if they're not on the map, they can't see anything. */
		int agentX=-1*(radius+1);
		int agentY=-1*(radius+1);
		/* find agent. */
outer:	for(int i = 0; i < state.length; i++)
		{
			for(int j = 0; j < state[i].length; j++)
			{
				if(agent == state[i][j])
				{
					agentX = i;
					agentY = j;
					break outer;
				}
			}
		}
		char[][] filtered = new char[state.length][state[0].length];
		/* mask map */
		for(int i = 0; i < state.length; i++)
		{
			for(int j = 0; j < state[i].length; j++)
			{
				if(radius < (squareOrMan ? squareDistance(agentX, agentY, i, j, state.length, state[i].length):manhattenDistance(agentX,agentY,i,j,state.length, state[i].length)))
				{
					filtered[i][j] = 'X';
				}
				else
				{
					filtered[i][j] = state[i][j];
				}
			}
		}
		return filtered;
	}

	/**
	 * return the maximum of the x-axis distand and y-axis distance.
	*/
	protected int squareDistance(int x1, int y1, int x2, int y2, int height, int width)
	{
		int xdist1 = Math.abs(x2 - x1);
		int xdist2 = height - Math.abs(x1 - x2);
		int xdist = xdist1 < xdist2 ? xdist1 : xdist2;
		int ydist1 = Math.abs(y2 - y1);
		int ydist2 = width - Math.abs(y1 - y2);
		int ydist = ydist1 < ydist2 ? ydist1 : ydist2;
		return xdist > ydist ? xdist : ydist;
	}

	/**
	 * return the number of squares it takes to cover the distance, no diagonals.
	 */
	protected int manhattenDistance(int x1, int y1, int x2, int y2, int height, int width)
	{
		int xdist1 = Math.abs(x2 - x1);
		int xdist2 = height - Math.abs(x1 - x2);
		int xdist = xdist1 < xdist2 ? xdist1 : xdist2;
		int ydist1 = Math.abs(y2 - y1);
		int ydist2 = width - Math.abs(y1 - y2);
		int ydist = ydist1 < ydist2 ? ydist1 : ydist2;
		return xdist + ydist;
	}

	/**
	 * @brief change the radius.
	 */
	protected void setRadius(int radius)
	{
	 	this.radius = radius;
	}

	protected int getRadius()
	{
		return this.radius;
	}
}
