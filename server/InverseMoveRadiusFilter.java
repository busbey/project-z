/**
 * @file filter the game board for a particular agent - only show them squares a certain distance away.
 *
 *	change that distance per-agent based on them moving.
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
import java.util.*;
public class InverseMoveRadiusFilter extends FixedRadiusFilter 
{
	public static final int DEFAULT_MOVES_TO_PENALTY = 1;
	public static final int DEFAULT_RATE_OF_PENALTY = 1;
	public static final int DEFAULT_STILL_TO_GAIN = 1;
	public static final int DEFAULT_RATE_OF_GAIN = 1;
	
	protected int penaltyRate;
	protected int penaltyMoves;
	protected int gainRate;
	protected int gainMoves;

	protected HashMap<Character, MoveInfo> info = new HashMap<Character, MoveInfo>();
	
	public InverseMoveRadiusFilter(int radius, boolean distanceMeasure, int penaltyRate, int penaltyMoves, int gainRate, int gainMoves)
	{
		super(radius, distanceMeasure);
		this.penaltyRate = penaltyRate;
		this.penaltyMoves = penaltyMoves;
		this.gainRate = gainRate;
		this.gainMoves = gainMoves;
	}
	
	/** @brief the default implementation doesn't change the world appearance at all.
	 */
 	public char[][] filter (char[][] state, final char agent)
	{
		/* initialize these such that if they're not on the map, they can't see anything. */
		int agentX=-1;
		int agentY=-1;
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
		if(-1 == agentX || -1 == agentY)
		{
			return filter(state, agent, 0);
		}
			
		/* figure out movement of agent this round. */
		MoveInfo agentInfo;
		if(info.containsKey(agent))
		{
			agentInfo = info.get(agent);
			/* penalize or reward as appropriate */
			boolean moved = agentX != agentInfo.lastX || agentY != agentInfo.lastY;
			if(moved != agentInfo.moveOrStill)
			{
				agentInfo.moveOrStill = moved;
				agentInfo.roundsInCurrent = 1;
			}
			else
			{
				agentInfo.roundsInCurrent++;
			}
			if(moved)
			{
				agentInfo.lastX = agentX;
				agentInfo.lastY = agentY;
				if(0 == agentInfo.roundsInCurrent%penaltyMoves)
				{
					agentInfo.radius -= penaltyRate;
					if(0 > agentInfo.radius)
					{
						agentInfo.radius = 0;
					}
				}
			}
			else if(0 == agentInfo.roundsInCurrent%gainMoves)
			{
				agentInfo.radius += gainRate;
				if(state.length+state[0].length < agentInfo.radius)
				{
					agentInfo.radius = state.length+state[0].length;
				}
			}
			info.put(agent, agentInfo);
		}
		else
		{
			agentInfo = new MoveInfo();
			agentInfo.lastX = agentX;
			agentInfo.lastY = agentY;
			info.put(agent, agentInfo);
		}

		/* filter on the new radius. */
		return filter(state, agent, agentInfo.radius);
	}

	protected class MoveInfo
	{
		public boolean moveOrStill= false;
		public int roundsInCurrent = 0;
		public int radius = DEFAULT_RADIUS;
		public int lastX = -1;
		public int lastY = -1;
	}
}
