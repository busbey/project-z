/** 
 * @file header file for C agent example.
 */
#ifndef AGENT_H
#define AGENT_H

#define TRUE 1
#define FALSE 0

#define NONE 'n'
#define LEFT 'l'
#define RIGHT 'r'
#define UP 'u'
#define DOWN 'd'

typedef struct ChatMessage
{
	unsigned char speaker;
	unsigned char subject;
	unsigned char action;
} ChatMessage;

typedef struct State
{
	unsigned int	bugKills:1,
					stunned:1,
					killed:1,
					gameOver:1;
	unsigned char 	player;
	unsigned int 	rows;
	unsigned int 	cols;
	char**			board;
	unsigned int	numMessages;
	ChatMessage*	messages;
} State;

/** Functions you'll have to implement */

/** @brief handle command line args */
extern void init(int argc, char** argv);

/** @brief given a world state, pick a new action */
extern void respondToChange(int socket, State* newState); 

/** Functions given to you in Agent.c */

/** @brief write given move to the server */
extern void writeMoveToServer(int socket, char move);

/** @brief write given chat message to server */
extern void writeChatToServer(int socket, ChatMessage* chat);

/** @brief read the world state off of the server */
extern State* readState(int socket);

/** @brief clean up a state object */
extern void releaseState(State* state);

/** @brief set up our socket. */
extern int createSocket(char* hostname, unsigned short int port);

/** @brief program execution */
extern int main(int argc, char **argv);

#endif
