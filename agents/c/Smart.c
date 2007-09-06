/** @file sample agent that moves randomly */
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
		return;
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
				/* is it closer than previous goals? */
				if(-1 == goalRow && -1 == goalCol)
				{
					/* no previous goal */
					goalRow = row;
					goalCol = col;
				}
				else 
				{
					const int dist = ((row - myRow) * (row - myRow)) + ((col - myCol) * (col - myCol));
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
				move = 'l';
			}
			else
			{
				move = 'r';
			}
		}
		else
		{
			/* move in the col direction */
			if(myCol > goalCol)
			{
				move = 'u';
			}
			else
			{
				move = 'd';
			}
		}
	}
	/* Note that if we can't find ourself on the map we're going to still move towards the goal closest to the upper left hand corner. */
	writeMoveToServer(socket, move);
}
