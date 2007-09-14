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
#include <stdlib.h> /* rand */

/** @brief handle command line args */
void 
init(int argc, char** argv)
{
	(void)argc;
	(void)argv;
}

/** @brief clean up */
void
fini()
{
	/* no clean up. */
}

/** @brief given a world state, pick a new action */
void respondToChange(int socket, State* newState)
{
	const unsigned char MOVES[] = {UP, DOWN, LEFT, RIGHT, NONE};
	int move = (rand())%sizeof(MOVES);
	ChatMessage message={0};
	(void)newState;
	writeMoveToServer(socket, MOVES[move]);
	move = (rand())%sizeof(MOVES);
	message.speaker = newState->player;
	message.subject = newState->player;
	message.action = MOVES[move];
	writeChatToServer(socket, &message);
}
