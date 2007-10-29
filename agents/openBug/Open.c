/** @file sample agent that moves randomly */
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

#include "Agent.h"
#include <stdio.h> 		/* fprintf */
#include <stdlib.h>		/* exit, abs */
#include <sys/time.h>	/* gettimeofday */

#define LOOK_AHEAD 2500
#define ATTRACT_AMOUNT 35

/** @brief handle command line args */
void 
init(int argc, char** argv)
{
}

/** @brief clean up */
void
fini()
{
}

/** @brief if this position contains a threat */
inline int positionThreat(State* state, const int row, const int col)
{
	char area = state->board[row][col];
	switch(area)
	{
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return TRUE;
		default:
			return FALSE;
	}
}

/** @brief is this position obstructed 
		 if this is an open space 
		 and it's not adjacent to a square with an enemy
		 XXX I'd prefer to track which enemies we think
		 	are moving towards us, and only expand the influence
			of those who are.
*/
inline int positionOpen(State* state, const int row, const int col)
{
	char area = '\0';
	if(0 > row || row >= state->rows || 0 > col || col >= state->cols)
	{
		return FALSE;
	}
	area = state->board[row][col];
	switch(area)
	{
		case ' ':
		case 'P':
			if(0 <= row -1)
			{
				if(TRUE == positionThreat(state, row - 1, col))
				{
					return FALSE;
				}
			}
			if(state->rows > row + 1)
			{
				if(TRUE == positionThreat(state, row + 1, col))
				{
					return FALSE;
				}
			}
			if(0 <= col -1)
			{
				if(TRUE == positionThreat(state, row, col - 1))
				{
					return FALSE;
				}
			}
			if(state->cols > col + 1)
			{
				if(TRUE == positionThreat(state, row, col + 1))
				{
					return FALSE;
				}
			}
			return TRUE;
		default:
			return FALSE;
	}
}

/** @brief return a value for a given open square.  */
int positionValue(State* state, int row, int col)
{
	switch(state->board[row][col])
	{
		case 'P':
			return ATTRACT_AMOUNT;
		default:
			return 1;
	}
}

/** @brief calculate the straight line open area from a given square */
int openRuns(State* state, int row, int col)
{
	int openCount = 0;
	int	index;
	/* count left */
	for(index = col - 1; 0 <= index; index--)
	{
		if(FALSE == positionOpen(state, row, index))
		{
			break;
		}
		openCount += positionValue(state, row, index);
	}
	/* count right */
	for(index = col + 1; state->cols > index; index++)
	{
		if(FALSE == positionOpen(state, row, index))
		{
			break;
		}
		openCount += positionValue(state, row, index);
	}
	/* count up */
	for(index = row - 1; 0 <= index; index--)
	{
		if(FALSE == positionOpen(state, index, col))
		{
			break;
		}
		openCount += positionValue(state, index, col);
	}
	/* count down */
	for(index = row + 1; state->rows > index; index++)
	{
		if(FALSE == positionOpen(state, index, col))
		{
			break;
		}
		openCount += positionValue(state, index, col);
	}
	return openCount;
}

/** @brief given a world state, pick a new action 
	look for open areas.

	calculate the gain in length of an unobstructed straight paths
		from where we are to 
		1 space away
		2 spaces away
		3 spaces away
 */
char moveTowardsOpen(State* state)
{
	int		myRow, myCol;
	int		left[LOOK_AHEAD], right[LOOK_AHEAD], up[LOOK_AHEAD], down[LOOK_AHEAD];
	int		row, col;
	int		curOpenness = 0;
	int		index = 0;
	int		maxGain = -1;
	char	move = 'n';
	
	myRow = myCol = -1;

	/* Find out position */
	for(row = 0; row < state->rows && -1 == myRow && -1 == myCol; row++)
	{
		for(col = 0; col < state->cols && -1 == myRow && -1 == myCol; col++)
		{
			if(state->player == state->board[row][col])
			{
				myRow = row;
				myCol = col;
			}
		}
	}
	if(myRow == -1 || myCol == -1)
	{
		fprintf(stderr, "Warning: Couldn't find myself on the board.\n");
	}
	DBG_PRINT((stdout, "Current Board:\n"));
	for(row = 0; row < state->rows; row++)
	{
		for(col = 0; col < state->cols; col++)
		{
			DBG_PRINT((stdout, "%c", state->board[row][col]));
		}
		DBG_PRINT((stdout, "\n"));
	}
	curOpenness = openRuns(state, myRow, myCol);
	DBG_PRINT((stdout, "Calculating gains from %d:", curOpenness));
	/* calculate gains - left */
	DBG_PRINT((stdout, "\n\tleft "));
	for(index = 0; index < LOOK_AHEAD; index++)
	{
		const int curCol = myCol - index - 1;
		if(TRUE == positionOpen(state, myRow, curCol))
		{
			left[index] = openRuns(state, myRow, curCol);
		}
		else
		{
			break;
		}
		DBG_PRINT((stdout, " %d ", left[index]));
		if(maxGain < left[index])
		{
			maxGain = left[index];
			move = 'l';
		}
	}
	/* calculate gains - right */
	DBG_PRINT((stdout, "\n\tright "));
	for(index = 0; index < LOOK_AHEAD; index++)
	{
		const int curCol = myCol + index + 1;
		if(TRUE == positionOpen(state, myRow, curCol))
		{
			right[index] = openRuns(state, myRow, curCol);
		}
		else
		{
			break;
		}
		DBG_PRINT((stdout, " %d ", right[index]));
		if(maxGain < right[index])
		{
			maxGain = right[index];
			move = 'r';
		}
	}
	/* calculate gains - up */
	DBG_PRINT((stdout, "\n\tup "));
	for(index = 0; index < LOOK_AHEAD; index++)
	{
		const int curRow = myRow - index - 1;
		if(TRUE == positionOpen(state, curRow, myCol))
		{
			up[index] = openRuns(state, curRow, myCol);
		}
		else
		{
			break;
		}
		DBG_PRINT((stdout, " %d ", up[index]));
		if(maxGain < up[index])
		{
			maxGain = up[index];
			move = 'u';
		}
	}
	/* calculate gains - down */
	DBG_PRINT((stdout, "\n\tdown "));
	for(index = 0; index < LOOK_AHEAD; index++)
	{
		const int curRow = myRow + index + 1;
		if(TRUE == positionOpen(state, curRow, myCol))
		{
			down[index] = openRuns(state, curRow, myCol);
		}
		else
		{
			break;
		}
		DBG_PRINT((stdout, " %d ", down[index]));
		if(maxGain < down[index])
		{
			maxGain = down[index];
			move = 'd';
		}
	}
	DBG_PRINT((stdout, "\nrecommending '%c'\n", move));
	return move;
}

/** @brief given a world state, pick a new action 
 */
void respondToChange(int socket, State* newState)
{
	struct timeval	start	= {0};
	struct timeval	end		= {0};
	struct timeval	diff	= {0};
	long			round	= 0l;
	char			move	= 'n';
	
	(void)gettimeofday(&start, NULL);
	move = moveTowardsOpen(newState);
	/* Note that if we can't find ourself on the map we're going to still move towards the goal closest to the upper left hand corner. */
	writeMoveToServer(socket, move);
	(void)gettimeofday(&end, NULL);
	diff.tv_sec = end.tv_sec - start.tv_sec;
	diff.tv_usec = end.tv_usec - start.tv_usec;
	round = diff.tv_sec * 1000 + (diff.tv_usec) / 1000;
	DBG_PRINT((stderr, "round took %ld milliseconds\n", round));
}
