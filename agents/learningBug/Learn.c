/** @file sample agent that moves randomly */
/* Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs, Paul Ostazeski, Benjamin A. Miller
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
#include <stdio.h>	/* fprintf */
#include <stdlib.h> /* rand */
#include <string.h> /* memset */

#define STORE_MOVES 5
#define DIE_PENALTY -128
#define STUN_PENALTY -16
#define POWER_GAIN 127

#define INNER_HUNTER_SET_LEFT	0x00000001
#define INNER_HUNTER_SET_RIGHT	0x00000002
#define INNER_HUNTER_SET_UP		0x00000004
#define INNER_HUNTER_SET_DOWN	0x00000008
#define INNER_POWER_SET_LEFT	0X00000010
#define INNER_POWER_SET_RIGHT	0X00000020
#define INNER_POWER_SET_UP		0X00000040
#define INNER_POWER_SET_DOWN	0X00000080
#define OUTER_HUNTER_SET_LEFT	0x00000100
#define OUTER_HUNTER_SET_RIGHT	0x00000200
#define OUTER_HUNTER_SET_UP		0x00000400
#define OUTER_HUNTER_SET_DOWN	0x00000800
#define INNER_BUG_SET_LEFT		0X00001000
#define INNER_BUG_SET_RIGHT		0X00002000
#define INNER_BUG_SET_UP		0X00004000
#define INNER_BUG_SET_DOWN		0X00008000
#define INNER_OBS_SET_LEFT		0X00010000
#define INNER_OBS_SET_RIGHT		0X00020000
#define INNER_OBS_SET_UP		0X00040000
#define INNER_OBS_SET_DOWN		0X00080000
#define OUTER_POWER_SET_LEFT	0X00100000
#define OUTER_POWER_SET_RIGHT	0X00200000
#define OUTER_POWER_SET_UP		0X00400000
#define OUTER_POWER_SET_DOWN	0X00800000
#define OUTER_BUG_SET_LEFT		0X01000000
#define OUTER_BUG_SET_RIGHT		0X02000000
#define OUTER_BUG_SET_UP		0X04000000
#define OUTER_BUG_SET_DOWN		0X08000000
#define OUTER_OBS_SET_LEFT		0X10000000
#define OUTER_OBS_SET_RIGHT		0X20000000
#define OUTER_OBS_SET_UP		0X40000000
#define OUTER_OBS_SET_DOWN		0X80000000




#define HASH_SIZE 256*1024*1024
/* our stored weights */
signed char	hashTable[HASH_SIZE];

/* the last n moves, so we know which scores to update */
int	last[STORE_MOVES];

/* did we move towards a powerup last round? */
int	powerMove = FALSE;

/** @brief handle command line args */
void 
init(int argc, char** argv)
{
	(void)argc;
	(void)argv;
	(void)memset(&(hashTable[0]), 0, HASH_SIZE * sizeof(signed char));
	(void)memset(&(last[0]), 0, STORE_MOVES * sizeof(int));
}

/** @brief clean up */
void
fini()
{
	/* no clean up. */
}

#define isInner(x,y) ((-2 < (x)) && (2 > (x)) && (-2 < (y)) && (2 > (y)))

unsigned int hash(State* state, int row, int col)
{
	unsigned int hashVal = 0;
	int rowDex,colDex;
	for(rowDex = -2; rowDex < 3; rowDex++)
	{
		for(colDex = -2; colDex < 3; colDex++)
		{
			char contents = '\0';
			int	curRow = row+rowDex;
			int curCol = col+colDex;
			if(0 == rowDex && 0 == colDex)
			{
				continue;
			}
			if(curRow < 0)
			{
				curRow = state->rows + curRow;
			}
			else if(curRow >= state->rows)
			{
				curRow -= state->rows;
			}
			if(curCol < 0)
			{
				curCol = state->cols + curCol;
			}
			else if(curCol >= state->cols)
			{
				curCol -= state->cols;
			}
			contents = state->board[curRow][curCol];
			if('1' <= contents && '9' >= contents)
			{
				if(state->bugKills)
				{
					/* treat hunters as desireable */
					if(isInner(colDex, rowDex))
					{
						if(0 > colDex)
						{
							hashVal |= INNER_POWER_SET_LEFT;
						}
						else if (0 < colDex)
						{
							hashVal |= INNER_POWER_SET_RIGHT;
						}
						if(0 > rowDex)
						{
							hashVal |= INNER_POWER_SET_UP;
						}
						else if (0 < rowDex)
						{
							hashVal |= INNER_POWER_SET_DOWN;
						}
					}
					else
					{
						if(0 > colDex)
						{
							hashVal |= OUTER_POWER_SET_LEFT;
						}
						else if (0 < colDex)
						{
							hashVal |= OUTER_POWER_SET_RIGHT;
						}
						if(0 > rowDex)
						{
							hashVal |= OUTER_POWER_SET_UP;
						}
						else if (0 < rowDex)
						{
							hashVal |= OUTER_POWER_SET_DOWN;
						}
					}
				}
				else
				{
					if(isInner(colDex, rowDex))
					{
						if(0 > colDex)
						{
							hashVal |= INNER_HUNTER_SET_LEFT;
						}
						else if (0 < colDex)
						{
							hashVal |= INNER_HUNTER_SET_RIGHT;
						}
						if(0 > rowDex)
						{
							hashVal |= INNER_HUNTER_SET_UP;
						}
						else if (0 < rowDex)
						{
							hashVal |= INNER_HUNTER_SET_DOWN;
						}
					}
					else
					{
						if(0 > colDex)
						{
							hashVal |= OUTER_HUNTER_SET_LEFT;
						}
						else if(0 < colDex)
						{
							hashVal |= OUTER_HUNTER_SET_RIGHT;
						}
						if(0 > rowDex)
						{
							hashVal |= OUTER_HUNTER_SET_UP;
						}
						else if(0 < rowDex)
						{
							hashVal |= OUTER_HUNTER_SET_DOWN;
						}
					}
				}
			}
			else if('B' <= contents && 'N' >= contents)
			{
				if(isInner(colDex, rowDex))
				{
					if(0 > colDex)
					{
						hashVal |= INNER_BUG_SET_LEFT;
					}
					else if(0 < colDex)
					{
						hashVal |= INNER_BUG_SET_RIGHT;
					}
					if(0 > rowDex)
					{
						hashVal |= INNER_BUG_SET_UP;
					}
					else if(0 < rowDex)
					{
						hashVal |= INNER_BUG_SET_DOWN;
					}
				}
				else
				{
					if(0 > colDex)
					{
						hashVal |= OUTER_BUG_SET_LEFT;
					}
					else if(0 < colDex)
					{
						hashVal |= OUTER_BUG_SET_RIGHT;
					}
					if(0 > rowDex)
					{
						hashVal |= OUTER_BUG_SET_UP;
					}
					else if(0 < rowDex)
					{
						hashVal |= OUTER_BUG_SET_DOWN;
					}
				}
			}
			else if('O' == contents)
			{
				if(isInner(colDex, rowDex))
				{
					if(0 > colDex)
					{
						hashVal |= INNER_OBS_SET_LEFT;
					}
					else if(0 < colDex)
					{
						hashVal |= INNER_OBS_SET_RIGHT;
					}
					if(0 > rowDex)
					{
						hashVal |= INNER_OBS_SET_UP;
					}
					else if(0 < rowDex)
					{
						hashVal |= INNER_OBS_SET_DOWN;
					}
				}
				else
				{
					if(0 > colDex)
					{
						hashVal |= OUTER_OBS_SET_LEFT;
					}
					else if(0 < colDex)
					{
						hashVal |= OUTER_OBS_SET_RIGHT;
					}
					if(0 > rowDex)
					{
						hashVal |= OUTER_OBS_SET_UP;
					}
					else if(0 < rowDex)
					{
						hashVal |= OUTER_OBS_SET_DOWN;
					}
				}
			}
			else if('P' == contents)
			{
				if(isInner(colDex, rowDex))
				{
					if(0 > colDex)
					{
						hashVal |= INNER_POWER_SET_LEFT;
					}
					else if(0 < colDex)
					{
						hashVal |= INNER_POWER_SET_RIGHT;
					}
					if(0 > rowDex)
					{
						hashVal |= INNER_POWER_SET_UP;
					}
					else if(0 < rowDex)
					{
						hashVal |= INNER_POWER_SET_DOWN;
					}
				}
				else
				{
					if(0 > colDex)
					{
						hashVal |= OUTER_POWER_SET_LEFT;
					}
					else if(0 < colDex)
					{
						hashVal |= OUTER_POWER_SET_RIGHT;
					}
					if(0 > rowDex)
					{
						hashVal |= OUTER_POWER_SET_UP;
					}
					else if(0 < rowDex)
					{
						hashVal |= OUTER_POWER_SET_DOWN;
					}
				}
			}
		}
	}
	return hashVal;
}

signed char lookupScore(State* state, int row, int col)
{
	int hashVal = 0;
	hashVal = hash(state, row, col);
	return hashTable[hashVal % HASH_SIZE];
}

void _updateScore(int value)
{
	int index;
	for(index = 0; index < STORE_MOVES; index++)
	{
		hashTable[last[index]] += value;
		value /= 2;
	}
}

void updateScores(State* state)
{
	if(TRUE == state->died)
	{
		_updateScore(DIE_PENALTY);
	}
	if(TRUE == state->killed)
	{
		_updateScore(POWER_GAIN);
	}
	if(TRUE == state->stunned)
	{
		_updateScore(STUN_PENALTY);
	}
	if(TRUE == state->bugKills && powerMove)
	{
		_updateScore(POWER_GAIN);
	}
}

/** @brief given a world state, pick a new action */
void respondToChange(int socket, State* newState)
{
	unsigned char MOVES[4];
	unsigned int hashes[4];
	int	powers[4];
	int numUsed = 0;
	int maxVal = -256;
	int curVal = 0;
	unsigned int curHash = 0;
	int move = 0;
	int		myRow, myCol;
	int		row, col;
	int		index;
	myRow = myCol = -1;
	updateScores(newState);
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
	/* check left, right, up and down */
	curHash = hash(newState, myRow, myCol - 1) % HASH_SIZE;
	curVal = hashTable[curHash];
	if(curVal >= maxVal)
	{
		int curCol = myCol -1;
		if(curCol < 0)
		{
			curCol = newState->cols + curCol;
		}
		if(curVal > maxVal)
		{
			numUsed = 0;
			maxVal = curVal;
		}
		MOVES[numUsed] = LEFT;
		hashes[numUsed] = curHash;
		powers[numUsed] = ('P' == newState->board[myRow][curCol]);
		numUsed++;
	}
		
	curVal = hashTable[hash(newState, myRow, myCol + 1) % HASH_SIZE];
	if(curVal >= maxVal)
	{
		int curCol = myCol + 1;
		if(curCol >= newState->cols)
		{
			curCol -= newState->cols;
		}
		if(curVal > maxVal)
		{
			numUsed = 0;
			maxVal = curVal;
		}
		MOVES[numUsed] = RIGHT;
		hashes[numUsed] = curHash;
		powers[numUsed] = ('P' == newState->board[myRow][curCol]);
		numUsed++;
	}
	curVal = hashTable[hash(newState, myRow-1, myCol) % HASH_SIZE];
	if(curVal >= maxVal)
	{
		int curRow = myRow - 1;
		if(curRow < 0)
		{
			curRow = newState->rows + curRow;
		}
		if(curVal > maxVal)
		{
			numUsed = 0;
			maxVal = curVal;
		}
		MOVES[numUsed] = UP;
		hashes[numUsed] = curHash;
		powers[numUsed] = ('P' == newState->board[curRow][myCol]);
		numUsed++;
	}
	curVal = hashTable[hash(newState, myRow+1, myCol) % HASH_SIZE];
	if(curVal >= maxVal)
	{
		int curRow = myRow + 1;
		if(curRow >= newState->rows)
		{
			curRow -= newState->rows;
		}
		if(curVal > maxVal)
		{
			numUsed = 0;
			maxVal = curVal;
		}
		MOVES[numUsed] = DOWN;
		hashes[numUsed] = curHash;
		powers[numUsed] = ('P' == newState->board[curRow][myCol]);
		numUsed++;
	}
	move = (rand())%numUsed;
	powerMove = powers[move];
	/* store our history of moves */
	for(index = STORE_MOVES - 1; index > 0; index--)
	{
		last[index] = last[index - 1];
	}
	last[0] = hashes[move];
	
	writeMoveToServer(socket, MOVES[move]);
}
