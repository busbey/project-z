/**
 * @file implementation of agent example for C
 */
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
#include <sys/socket.h>	/* socket, connect */
#include <netinet/in.h> /* sockaddr_in */
#include <netdb.h> 		/* gethostinfo */
#include <unistd.h> 	/* read write */
#include <arpa/inet.h> 	/* ntohl */
#include <stdlib.h> 	/* strtol */
#include <stdio.h> 		/* fprintf */
#include <string.h> 	/* memset */

#define FLAGS_GAME_END 0xFF
#define FLAGS_BUG_KILLS 0x01
#define FLAGS_AGENT_DIED 0x02
#define FLAGS_AGENT_STUN 0x04
#define FLAGS_NONE 0x00

/** @brief set up our socket. */
int
createSocket(char* hostname, unsigned short int port)
{
	struct hostent* host;
	struct sockaddr_in address = {0};
	int socketfd = -1;

	if(NULL == hostname)
	{
		hostname = "localhost.";
	}

	address.sin_family = AF_INET;
	address.sin_port = htons(port);

	host = gethostbyname(hostname);
	if(NULL == host)
	{
		herror("Could not resolve server name\n");
		exit(-1);
	}

	address.sin_addr = *((struct in_addr *)(host->h_addr));

	socketfd = socket(PF_INET, SOCK_STREAM, 0);
	if(-1 == socketfd)
	{
		fprintf(stderr, "Failed to create socket.\n");
		exit(-1);
	}

	if(-1 == connect(socketfd, (struct sockaddr *)(&address), sizeof(address)))
	{
		fprintf(stderr, "Failed to connect socket to server.\n");
		exit(-1);
	}
	return socketfd;
}

/** @brief give a descriptive string for a move 
 *	returned strings are literals and need not be freed.	
 */
char* moveToStr(char move)
{
	char* moveDesc = "NONE";
	switch(move)
	{
		case UP:
			moveDesc = "UP";
			break;
		case DOWN:
			moveDesc = "DOWN";
			break;
		case LEFT:
			moveDesc = "LEFT";
			break;
		case RIGHT:
			moveDesc = "RIGHT";
			break;
		default:
			break;
	}
	return moveDesc;
}

/** @brief write given move to the server */
void writeMoveToServer(int socket, char move)
{
	ssize_t bytesWritten = -1;
	DBG_PRINT((stdout, "moving %s\n", moveToStr(move)));
	bytesWritten = write(socket, &(move), 1);
	if(1 != bytesWritten)
	{
		fprintf(stderr, "Error writing move to server.\n");
		exit(-1);
	}
}

/** @brief write given chat message to server */
void writeChatToServer(int socket, ChatMessage* chat)
{
	ssize_t bytesWritten = -1;
	DBG_PRINT((stdout, "sending '%c' says '%c' should move %s\n",chat->speaker, chat->subject, moveToStr(chat->action)));
	bytesWritten = write(socket, &(chat->speaker), 1);
	if(1 != bytesWritten)
	{
		fprintf(stderr, "Error writing chat message to server.\n");
		exit(-1);
	}
	bytesWritten = write(socket, &(chat->subject), 1);
	if(1 != bytesWritten)
	{
		fprintf(stderr, "Error writing chat message to server.\n");
		exit(-1);
	}
	bytesWritten = write(socket, &(chat->action), 1);
	if(1 != bytesWritten)
	{
		fprintf(stderr, "Error writing chat message to server.\n");
		exit(-1);
	}
}

/** @brief clean up a state object */
void releaseState(State* state)
{
	if(NULL != state)
	{
		if(NULL != state->board)
		{
			int index;
			for(index = 0; index < state->rows; index++)
			{
				if(NULL != state->board[index])
				{
					free(state->board[index]);
				}
			}
			free(state->board);
		}
		if(NULL != state->messages)
		{
			free(state->messages);
		}
		free(state);
	}
}

/** @brief read the world state off of the server */
State* 
readState(int socket)
{
	union 
	{
		unsigned char byte;
		unsigned char intbytes[4];
		unsigned int  integer;
	}				buf = {0};
	ssize_t			bytesRead = -1;
	unsigned int	index = 0;
	State* 			state = malloc(sizeof(*state));
	memset(state, 0, sizeof(*state));
	
	bytesRead = read(socket, (&(buf.byte)), 1);
	if(1 != bytesRead)
	{
		fprintf(stderr, "Error reading game state flags.\n");
		exit(-1);
	}
	if((FLAGS_GAME_END & (buf.byte)) == FLAGS_GAME_END)
	{
		state->gameOver = TRUE;
	}
	else
	{
		DBG_PRINT((stderr, "flags:"));
		if((FLAGS_BUG_KILLS & buf.byte) == FLAGS_BUG_KILLS)
		{
			state->bugKills = TRUE;
			DBG_PRINT((stderr, " [Bugs kill hunters]"));
		}
		if((FLAGS_AGENT_DIED & buf.byte) == FLAGS_AGENT_DIED)
		{
			state->killed = TRUE;
			DBG_PRINT((stderr, " [Player died last round]"));
		}
		if((FLAGS_AGENT_STUN & buf.byte) == FLAGS_AGENT_STUN)
		{
			state->stunned = TRUE;
			DBG_PRINT((stderr, " [Player stunned last round]"));
		}
		if(buf.byte == FLAGS_NONE)
		{
			DBG_PRINT((stderr, " None"));
		}
		DBG_PRINT((stderr, "\n"));
	}
	bytesRead = read(socket, &(buf.byte), 1);
	if(1 != bytesRead)
	{
		fprintf(stderr, "Error reading agent's character.\n");
		exit(-1);
	}
	DBG_PRINT((stderr, "player: '%c'\n", buf.byte));
	state->player = buf.byte;
	bytesRead = read(socket, &(buf.intbytes[0]), 4);
	if(0 >= bytesRead)
	{
		fprintf(stderr, "Error reading number of columns.\n");
		exit(-1);
	}
	while(4 > bytesRead)
	{
		int result = read(socket, &(buf.intbytes[bytesRead]), 4 - bytesRead);
		if(0 >= result)
		{
			fprintf(stderr, "Error reading number of columns.\n");
			exit(-1);
		}
		bytesRead += result;
	}
	buf.integer = ntohl(buf.integer);
	state->cols = buf.integer;
	bytesRead = read(socket, &(buf.integer), 4);
	if(0 >= bytesRead)
	{
		fprintf(stderr, "Error reading number of rows.\n");
		exit(-1);
	}
	while(4 > bytesRead)
	{
		int result = read(socket, &(buf.intbytes[bytesRead]), 4 - bytesRead);
		if(0 >= result)
		{
			fprintf(stderr, "Error reading number of rows.\n");
			exit(-1);
		}
		bytesRead += result;
	}
	buf.integer = ntohl(buf.integer);
	state->rows = buf.integer;
	DBG_PRINT((stderr, "rows: %d columns: %d\n", state->rows, state->cols));
	state->board = malloc(state->rows * sizeof(*(state->board)));
	if(NULL == state->board)
	{
		fprintf(stderr, "Error allocating memory for game board.\n");
		exit(-1);
	}
	memset(state->board, 0, state->rows * sizeof(*(state->board)));
	for(index = 0; index < state->rows; index++)
	{
		state->board[index] = malloc(state->cols * sizeof(**(state->board)));
		if(NULL == state->board[index])
		{
			fprintf(stderr, "Error allocating memory for game board row.\n");
			exit(-1);
		}
		memset(state->board[index], 0, state->cols * sizeof(**(state->board)));
		bytesRead = read(socket, &(state->board[index][0]), state->cols);
		if(0 >= bytesRead)
		{
			fprintf(stderr, "Error reading row %d of game board.\n", index);
			exit(-1);
		}
		while(state->cols > bytesRead)
		{
			int result = read(socket, &(state->board[index][bytesRead]), state->cols - bytesRead);
			if(0 >= result)
			{
				fprintf(stderr, "Error reading row %d of game board.\n", index);
				exit(-1);
			}
			bytesRead += result;
		}
		if(state->cols != bytesRead)
		{
			fprintf(stderr, "Error reading in the game board.\n");
		}
	}
	bytesRead = read(socket, &(buf.integer), 4);
	if(0 >= bytesRead)
	{
		fprintf(stderr, "Error reading number of chat messages.\n");
		exit(-1);
	}
	while(4 > bytesRead)
	{
		int result = read(socket, &(buf.intbytes[bytesRead]), 4 - bytesRead);
		if(0 >= result)
		{
			fprintf(stderr, "Error reading number of chat messages.\n");
			exit(-1);
		}
		bytesRead += result;
	}
	buf.integer = ntohl(buf.integer);
	state->numMessages = buf.integer;
	DBG_PRINT((stderr, "messages: %d\n", state->numMessages));
	if(0 < state->numMessages)
	{
		state->messages = malloc(state->numMessages * sizeof(ChatMessage));
		if(NULL == state->messages)
		{
			fprintf(stderr, "Error allocating memory for chats\n");
			exit(-1);
		}
		memset(state->messages, 0, state->numMessages * sizeof(ChatMessage));
		for(index = 0; index < state->numMessages; index++)
		{
			bytesRead = read(socket, &(buf.byte),1); 
			if(1 != bytesRead)
			{
				fprintf(stderr, "Error reading chat message.\n");
				exit(-1);
			}
			(state->messages[index]).speaker = buf.byte;
			bytesRead = read(socket, &(buf.byte),1); 
			if(1 != bytesRead)
			{
				fprintf(stderr, "Error reading chat message.\n");
				exit(-1);
			}
			(state->messages[index]).subject = buf.byte;
			bytesRead = read(socket, &(buf.byte),1); 
			if(1 != bytesRead)
			{
				fprintf(stderr, "Error reading chat message.\n");
				exit(-1);
			}
			(state->messages[index]).action = buf.byte;
			DBG_PRINT((stderr, "message: '%c' says '%c' should move %s\n", 
						(state->messages[index]).speaker,
						(state->messages[index]).subject,
						moveToStr((state->messages[index]).action)));
		}
	}
	return state;
}

static
void usage(char* name)
{
	fprintf(stdout, "usage: %s [-h host] [-p port] [args*]\n", name);
}

/** @brief program execution */
int 
main(int argc, char **argv)
{
	int	running = TRUE;
	int argsUsed = 0;
	int socket  = -1;
	char* host = "localhost";
	unsigned short int port = 1337;

	if(1 < argc)
	{
		if((argv[1][0] == '-' && argv[1][1] == '?') ||
			(0 == strncasecmp("--help", argv[1], 7)))
		{
				usage(argv[0]);
				exit(0);
		}
	}
	argsUsed++;
	if(2 < argc)
	{
		if(argv[1][0] == '-')
		{
			if(argv[1][1] == 'h')
			{
				host = argv[2];
				argsUsed+=2;
				if(3 < argc)
				{
					if(argv[3][0] == '-' && argv[3][1] == 'p')
					{
						port = strtol(argv[4], NULL, 0);
						argsUsed+=2;
					}
				}
			}
			else if(argv[1][1] == 'p')
			{
				port = strtol(argv[2], NULL, 0);
				argsUsed+=2;
			}
		}
	}
	if(0 < argc - argsUsed)
	{
		init(argc - argsUsed, &(argv[argsUsed]));
	}
	else
	{
		init(0, NULL);
	}
	socket = createSocket(host, port);
	while(running)
	{
		State* state = readState(socket);
		if(TRUE == state->gameOver)
		{
			running = FALSE;
		}
		respondToChange(socket, state);
		DBG_PRINT((stderr, "\n"));
		releaseState(state);
	}
	fini();
	close(socket);
	fprintf(stdout, "Game has ended...\n");
	return (0);
}
