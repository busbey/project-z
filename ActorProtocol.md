what goes on the wire for agents talking to the server.

Grammar:
```
S       -> chat | move
chat    -> speaker subject action
speaker -> actor
subject -> actor
action  -> move
actor   -> /[0-9B-N]/
move    -> 'u' | 'd' | 'l' | 'r' | 'n'
```

assuming our channel doesn't get corrupt, reading the first byte should unambiguously tell the server is this is a "I am moving" or a "I am saying" action.

The server will use the last move and chat message from a given actor when the round time has passed.  The move will be used for updating the world state, the chat message will go to all agents.