/** @file sample agent that moves randomly */
#include "Agent.h"
#include <stdlib.h> /* rand */

/** @brief handle command line args */
void 
init(int argc, char** argv)
{
	(void)argc;
	(void)argv;
}

/** @brief given a world state, pick a new action */
void respondToChange(int socket, State* newState)
{
	const unsigned char MOVES[] = {'u','d','l','r','n'};
	int move = (rand())%sizeof(MOVES);
	ChatMessage message={0};
	(void)newState;
	writeMoveToServer(socket, MOVES[move]);
	move = (rand())%sizeof(MOVES);
	message.speaker = newState->player;
	message.subject = newState->player;
	message.action = move;
	writeChatToServer(socket, &message);
}
