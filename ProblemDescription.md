Welcome to **Project Z**, a fun and educational game designed to allow you to flex your knowledge of agent-based strategies.  Your goal is one common for most programming assignments: eliminate as many bugs as possible.  Like any development project, bugs show up over time even if you squash them.  Unlike most other cases, these bugs will try to run away - except for when they're trying to get you!

# Goal #

In this game, you'll design a pair of bug hunters that will move around the game world attempting to kill bugs.  Sometimes, bugs will get a powerup that turns them deadly.  Crashing your programs was one thing, now they'll hunt you down!  During this critical section, you should try your best to avoid them.

Don't forget that you're not a solo act!  This hunt takes place in an extreme program, so your pair will need to work together to take care of the mounting investation.  Your team is not alone in the game world.  While the competing team won't directly hurt your hunters, they can slow you down.  After all, both teams can't get credit for destroying the same bug.

# How the World Looks #

Every quarter of a second, each of your hunters will get an update on how the game is going.  This update will include which hunter you are, the current game world, a set of flags that represent various conditions, and a set of messages sent in the previous round.

Players are represented in the world map with the numbers 0-9.  The world state will include which of these numbers corresponds with the currently running hunter.  Note that the world state will not explicitly tell you which other player on the map is your companion; you'll have to figure that out for yourself!

The game board is represented by a _m_ by _n_ rectangle of differently typed tiles.  Tiles are represented by ASCII characters:
  * the letters B-N represent bugs
  * the numbers 0-9 represent hunters
  * the letter O represents an impassable barrier
  * the letter P represents a powerup for a bug
  * the character " " (space) represents an empty square
  * the character X represents a square with unknown contents

The flags will tell you important information about the game in general as well as information specific to your hunter.  These flags include:
  * whether the game is over
  * whether the bug can eat hunters
  * whether you were eaten last round
  * whether you were stunned last round

The messages you receive, if any, will contain information of the form "[hunter/bug] says [hunter/bug] should move [direction](direction.md)."  Note that these messages are (possible malevolent) suggestions from other players, and do not need to be heeded.

# Rules of Hunting #

After receiving this round's world state, you will have until the next message to decide what to do.  Each round you must decide in which direction to move, and whether you'd like to send a message to the other hunters.  You may move up, down, left, or right from your current position, or you may elect not to move.  These moves will be subject to the following rules:
  * a move into an obstacle will be ignored
  * when two hunters elect to move into the same space, only one will end up in that space (chosen arbitrarily), and both will be stunned from the impact and remain unable to move for one turn
  * a move into a powerup will remove the powerup from the board
  * a move into a bug will kill the bug, unless it can eat you, in which case you'll be eaten
  * a move off the edge of the board will wrap around to the other side

If you choose to send a message, it may instruct any agent on the board (including yourself) to move in any direction.  In addition, you may choose to attribute this message to any agent on the board.  Of course, an honest player like you would only send messages as yourself, right? ;)

You may respond any number of times you wish (including zero), but only the last move and message received will be used.  If you do not send a move, you will not move.  If you send a message, it will be broadcast to all players (including yourself) with the next world state.

When a bug or a hunter is killed, scores will be awarded to the killer and the victim will reappear in a random empty square on the board.

# Sample Walkthrough #

For the sake of example, let us run through a small game on a six-by-six board.  Let's assume in this example that your two hunters will not collaborate with each other.  For the sake of brevity, this walkthrough will only last four rounds.

After your agent connects to the game server, it receives its first world state:
```
Player: 1
Flags: None
World:

OOOOOO
O B  O
O O  O
O1 2 O
O 3 4O
OOOOOO

Messages: None
```

What to do?  You're hunter '1' and you want to squash that bug 'B'.  You notice that the B can't eat you, so you decide to go after him.  Up seems to be a reasonable choice here, so you instruct the server to move your hunter up.  Because you're not colluding with your other hunter, you choose not to send a message this round.

The next round appears:
```
Player: 1
Flags: None
World:

OOOOOO
OB   O
O1O2 O
O    O
O  34O
OOOOOO

Messages: 
2 says 3 should move left
3 says 3 should move right
```

We appear to be closer to our goal.  The bug is within reach!  Analyzing the other players, it appears that 3 and 4 might have attempted to move into the same space.  Alternatively, 4 might have elected not to move last turn.  Notice that we're not told when other players are stunned.

We move up and receive the next round:

```
Player: 1
Flags: None
World:

OOOOOO
O1B2 O
O O  O
O  34O
O    O
OOOOOO

Messages: 
2 says 1 should move down
B says 2 should move down         
```

The bug got away, and now we notice that 2 might be a malicious agent.  But we don't have to do what he says, so we chose to move right.  Assuming 2 is rational, one of us is going to get that bug!  The fourth round appears:
```
Player: 1
Flags: Stunned last round
World:

OOOOOO
O 12 O
O O3 O
O    O
O B 4O
OOOOOO

Messages: None
```

We got the bug!  Unfortunately, we were also stunned by 2.  The bug reappeared in a random place, and the seemingly confused player 4 now seems to have an advantage.  Because we were stunned, we do not bother sending a move this round.  With an evil grin, however, we decide to send a message to 4, telling it to move away from the bug.

Finally, the last round appears:
```
Player: 1
Flags: Game over
World:

OOOOOO
O 12 O
O O  O
O  3 O
O B4 O
OOOOOO

Messages: 1 says 4 should move up
```

And with that move, the game is over.

# Making your own solution #

You will be provided development kits in five languages: C, Java, Python, Perl, and Ruby.  Each kit provides wrapper code that handles talking to the server as well as the basic tools required to test your own solution.  There will also be two sample hunters provided: one that always moves in a random direction, and another one that tries a slightly smarter approach--moving in a straight line toward the bug.

Making your own hunter is simple.  Each round you will be passed a structure representing the current world state, and respond to the change by deciding on moves and any message you would like to send.  The code in each development kit contains simple instructions for how to do this in way appropriate for your language of choice.

Each development kit also includes a GNU Makefile that includes several important targets:
  * "all," which should be the default, that handles any precomputation necessary.  For compiled languages, this will include any required compilation.  For interpreted languages, this target will by default do nothing, but may be used instead to precompute any required models, etc. you might need.
  * "clean," which should clean up any files created by "all"
  * "hunter1go," which handles running your first hunter
  * "hunter2go," which handles running your second hunter

To help debug your solutions, each development kit provides a game server.  More details, including how to run this server, may be found in the "README.txt" file included in each development kit.

When you have designed a solution you would like to run against other entrants, you assemble a submission package that includes your code and an appropriately modified Makefile.  Make sure your Makefile implements the "all," "hunter1go," and "hunter2go" targets as described above.

These files should be placed in a directory and archived (using tar, zip, etc.).  You may then upload this file to a submission server to run against other submitted agents.
