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
#include <regex.h>		/* regcomp, regexec */

#define LOOK_AHEAD 3

char*	openDesc = "";
regex_t	openArea = {0};
char*	deathDesc = "";
regex_t death = {0};

/** @brief handle command line args */
void 
init(int argc, char** argv)
{
	int res;
	if(1 < argc)
	{
		/* first arg is a string regular expression of what to go after */
		openDesc = argv[0];
		/* second arg is a string regular expression of what to avoid */
		deathDesc = argv[1];
	}
	else
	{
		/* default to powerups  and hunters */
		openDesc = "[ P]";
		deathDesc = "[1-9]";
	}
	res = regcomp(&openArea, openDesc, REG_EXTENDED | REG_NOSUB);
	res = regcomp(&death, deathDesc, REG_EXTENDED | REG_NOSUB);

	if(0 != res)
	{
		fprintf(stderr, "Error compiling the regular expression representing Agent goals.\n");
		exit(-1);
	}
	DBG_PRINT((stdout, "Starting up looking for large areas of '%s' with lethal '%s'\n", openDesc, deathDesc));
}

/** @brief clean up */
void
fini()
{
	regfree(&openArea);
	regfree(&death);
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
	if(0 > row || row >= state->rows || 0 > col || col >= state->cols)
	{
		return FALSE;
	}
	if(	(0 == regexec(&openArea, &(state->board[row][col]), 0, NULL, 0)) &&
		(0 >= row - 1 || 0 != regexec(&death, &(state->board[row-1][col]), 0, NULL, 0) ) &&
		(state->rows >= row + 1 || 0 != regexec(&death, &(state->board[row+1][col]), 0, NULL, 0) ) &&
		(0 >= col - 1 || 0 != regexec(&death, &(state->board[row][col-1]), 0, NULL, 0) ) &&
		(state->cols >= col + 1 || 0 != regexec(&death, &(state->board[row][col+1]), 0, NULL, 0) )
	)
	{
		return TRUE;
	}
	else
	{
		return FALSE;
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
		openCount++;
	}
	/* count right */
	for(index = col + 1; state->cols > index; index++)
	{
		if(FALSE == positionOpen(state, row, index))
		{
			break;
		}
		openCount++;
	}
	/* count up */
	for(index = row - 1; 0 <= index; index--)
	{
		if(FALSE == positionOpen(state, index, col))
		{
			break;
		}
		openCount++;
	}
	/* count down */
	for(index = row + 1; state->rows > index; index++)
	{
		if(FALSE == positionOpen(state, index, col))
		{
			break;
		}
		openCount++;
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
	curOpenness = openRuns(state, myRow, myCol);
	DBG_PRINT((stdout, "Calculating gains from %d:", curOpenness));
	/* calculate gains - left */
	DBG_PRINT((stdout, "\n\tleft "));
	for(index = 0; index < LOOK_AHEAD; index++)
	{
		const int curCol = myCol - index - 1;
		if(TRUE == positionOpen(state, myRow, curCol))
		{
			left[index] = openRuns(state, myRow, curCol) - curOpenness;
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
			right[index] = openRuns(state, myRow, curCol) - curOpenness;
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
			up[index] = openRuns(state, curRow, myCol) - curOpenness;
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
			down[index] = openRuns(state, curRow, myCol) - curOpenness;
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
	char	move = 'n';
	
	move = moveTowardsOpen(newState);
	/* Note that if we can't find ourself on the map we're going to still move towards the goal closest to the upper left hand corner. */
	writeMoveToServer(socket, move);
}
