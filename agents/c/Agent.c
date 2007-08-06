/**
 * @file implementation of agent example for C
 */

#include "Agent.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <stdlib.h>

#define FLAGS_GAME_END 0xFF
#define FLAGS_BUG_KILLS 0x01
#define FLAGS_AGENT_DIED 0x02
#define FLAGS_AGENT_STUN 0x04

/** @brief set up our socket. */
int
create_socket(char* hostname, unsigned short int port)
{
	struct hostent* host;
	struct sockaddr_in address = {0};
	int socket = -1;

	if(NULL == hostname)
	{
		hostname = "localhost.";
	}

	address.sin_family = AF_INET;
	address.sin_port = htons(port);

	host = gethostbyname(hostname);
	if(NULL == host)
	{
		herror("Could not resolve server name");
		exit(-1);
	}

	address.sin_addr = *((struct in_addr *)(host->h_addr));

	socket = socket(PF_INET, SOCK_STREAM, 0);
	if(-1 == socket)
	{
		fprintf(stderr, "Failed to create socket.\n");
		exit(-1);
	}

	if(-1 == connect(socket, (struct sockaddr *)(&address), sizeof(address)))
	{
		fprintf(stderr, "Failed to connect socket to server.");
		exit(-1);
	}
	return socket;
}

/** @brief write given move to the server */
void writeMoveToServer(int socket, char move)
{
	ssize_t bytesWritten = -1;
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
		if(NULL != board)
		{
			int index;
			for(index = 0; index < numRows; index++)
			{
				if(NULL != board[index])
				{
					free(board[index]);
				}
			}
			free(board);
		}
		if(NULL != state->messages)
		{
			free(messages);
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
		unsigned char byte,
		unsigned int  integer,
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
		if((FLAGS_BUG_KILLS & buf.byte) == FLAGS_BUG_KILLS)
		{
			state->bugKills = TRUE;
		}
		if((FLAGS_AGENT_DIED & buf.byte) == FLAGS_AGENT_DIED)
		{
			state->killed = TRUE;
		}
		if((FLAGS_AGENT_STUN & buf.byte) == FLAGS_AGENT_STUN)
		{
			state->stunned = TRUE;
		}
	}
	bytesRead = read(socket, &(buf.byte), 1);
	if(1 != bytesRead)
	{
		fprintf(stderr, "Error reading agent's character.\n");
		exit(-1);
	}
	state->player = buf.byte;
	bytesRead = read(socket, &(buf.integer), 4);
	if(4 != bytesRead)
	{
		fprintf(stderr, "Error reading number of columns\n");
		exit(-1);
	}
	buf.integer = ntohl(buf.integer);
	state->cols = buf.integer;
	bytesRead = read(socket, &(buf.integer), 4);
	if(4 != bytesRead)
	{
		fprintf(stderr, "Error reading number of rows\n");
		exit(-1);
	}
	buf.integer = ntohl(buf.integer);
	state->rows = buf.integer;
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
		bytesRead = read(socket, state->board[index], state->cols);
		if(state->cols != bytesRead)
		{
			fprintf(stderr, "Error reading in the game board.\n");
		}
	}
	bytesRead = read(socket, &(buf.integer), 4);
	if(4 != bytesRead)
	{
		fprintf(stderr, "Error reading number of chat messages\n");
		exit(-1);
	}
	buf.integer = ntohl(buf.integer);
	state->numChats = buf.integer;
	if(0 < state->numChats)
	{
		state->chats = malloc(state->numChats * sizeof(ChatMessage));
		if(NULL == state->chats)
		{
			fprintf(stderr, "Error allocating memory for chats");
			exit(-1);
		}
		memset(state->chats, 0, state->numChats*sizeof(ChatMessage));
		for(index = 0; index < state->numChats; index++)
		{
			bytesRead = read(socket, &(buf.byte),1); 
			if(1 != bytesRead)
			{
				fprintf(stderr, "Error reading chat message.\n");
				exit(-1);
			}
			(state->chats[index]).speaker = buf.byte;
			bytesRead = read(socket, &(buf.byte),1); 
			if(1 != bytesRead)
			{
				fprintf(stderr, "Error reading chat message.\n");
				exit(-1);
			}
			(state->chats[index]).subject = buf.byte;
			bytesRead = read(socket, &(buf.byte),1); 
			if(1 != bytesRead)
			{
				fprintf(stderr, "Error reading chat message.\n");
				exit(-1);
			}
			(state->chats[index]).action = buf.byte;
		}
	}
	return state;
}

static
void usage()
{
	fprintf(stdout, "usage: agent [-h host] [-p port] [args*]\n");
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
		if(argv[0][0] == '-')
		{
			if(argv[0][1] == 'h')
			{
				host = argv[1];
				argsUsed+=2;
				if(3 < argc)
				{
					if(argv[2][0] == '-' && argv[2][1] == 'p')
					{
						port = strtol(argv[3], NULL, 0);
						argsUsed+=2;
					}
				}
			}
			else if(argv[0][1] == 'p')
			{
				port = strtol(argv[1], NULL, 0);
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
		releaseState(state);
	}
	close(socket);
	fprintf(stdout, "Game has ended.\n");
}
