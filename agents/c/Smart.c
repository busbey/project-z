/** @file sample agent that moves randomly */
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

#include "Agent.h"
#include <stdio.h> 		/* fprintf */
#include <stdlib.h>		/* exit, abs */
#include <regex.h>		/* regcomp, regexec */

regex_t	goals = {0};

/** @brief handle command line args */
void 
init(int argc, char** argv)
{
	int res;
	if(0 < argc)
	{
		/* first arg is a string regular expression of what to go after */
		res = regcomp(&goals, argv[0], REG_EXTENDED | REG_NOSUB);
	}
	else
	{
		/* default to bugs */
		res = regcomp(&goals, "[B-N]", REG_EXTENDED | REG_NOSUB );
	}
	if(0 != res)
	{
		fprintf(stderr, "Error compiling the regular expression representing Agent goals.\n");
		exit(-1);
	}
}

/** @brief given a world state, pick a new action */
void respondToChange(int socket, State* newState)
{
	int		myRow, myCol;
	int		goalRow, goalCol;
	int		oldGoalDist = -1;
	int		row, col;
	char	move = 'n';
	int		goalMatch;
	char	nextChar;
	myRow = myCol = -1;
	/* Find out position */
	for(row = 0; row < newState->rows && -1 == myRow && -1 == myCol; row++)
	{
		for(col = 0; col < newState->cols && -1 == myRow && -1 == myCol; col++)
		{
			if(newState->player == newState->board[row][col])
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
	/* Look for closest goal */
	goalRow = goalCol = -1;
	for(row = 0; row < newState->rows; row++)
	{
		for(col = 0; col < newState->cols; col++)
		{
			/* is it a goal character? */
			nextChar = newState->board[row][col+1];
			newState->board[row][col+1] = '\0';
			goalMatch = regexec(&goals, &(newState->board[row][col]), 0, NULL, 0);
			newState->board[row][col+1] = nextChar;
			if(0 == goalMatch)
			{
				const int dist = ((row - myRow) * (row - myRow)) + ((col - myCol) * (col - myCol));
				/* is it closer than previous goals? */
				if(-1 == goalRow && -1 == goalCol)
				{
					/* no previous goal */
					goalRow = row;
					goalCol = col;
					oldGoalDist = dist;
				}
				else 
				{
					if(dist < oldGoalDist)
					{
						goalRow = row;
						goalCol = col;
						oldGoalDist = dist;
					}
				}
			}
		}
	}
	/* Move in direction of closest goal */
	if(-1 != goalRow && -1 != goalCol)
	{
		const int rowDist = abs(goalRow - myRow);
		const int colDist = abs(goalCol - myCol);
		const int rowOrCol = (rand()) % (rowDist + colDist);
		if(rowOrCol < rowDist)
		{
			/* move in the row direction */
			if(myRow > goalRow)
			{
				move = UP;
			}
			else
			{
				move = DOWN;
			}
		}
		else
		{
			/* move in the col direction */
			if(myCol > goalCol)
			{
				move = LEFT;
			}
			else
			{
				move = RIGHT;
			}
		}
	}
	/* Note that if we can't find ourself on the map we're going to still move towards the goal closest to the upper left hand corner. */
	writeMoveToServer(socket, move);
}
