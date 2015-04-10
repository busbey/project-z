serialized form:

  1. 1 byte: game flags
    * 0xFF - end of game
    * 0x01 - bug kills bug hunters (otherwise bug hunters kill bug)
    * 0x10 - a game round has changed
  1. 4 byte num of stuns last round
  1. num stuns x 2 bytes
    1. 1 byte caused stun : `/[0-9B-N]/`
    1. 1 byte subject of stun : `/[0-9B-N]/`
  1. 4 byte num of kills last round
  1. num kills x 2 bytes
    1. 1 byte killer : `/[0-9B-N]/`
    1. 1 byte killed : `/[0-9B-N]/`
  1. canonical world view
    1. 4 byte num entries in a world row
    1. 4 byte number of world rows
    1. num entries x numrows byte - entries read from left to right, top to bottom starting top-left. each as an ASCII character:
      * ' ' - open space
      * 'O' - obstacle
      * 'B' to 'N' - bugs
      * 'P' - powerup
      * '0' to '9'  - hunter number
  1. 4 byte num of per-agent world views
  1. num per-agent x (1 + canonical num entries x canonical numrows) bytes
    1. 1 byte agent this view is for : `/[1-9B-N]/`
    1. canonical num entries x canonical numrows bytes - entries read left to right, top to bottom starting top-left.  each as an ASCII character:
      * ' ' - open space
      * 'O' - obstacle
      * 'B' to 'N' - bugs
      * 'P' - powerup
      * '0' to '9'  - hunter number
      * 'X' - unknown content
  1. 4 byte - num chat messages
  1. num chat x 4 byte - chat messages each 4 byte form:
    1. 1 byte sender  : `/[0-9B-N/`
    1. 1 byte speaker : `/[0-9B-N]/`
    1. 1 byte subject : `/[0-9B-N]/`
    1. 1 byte action  : `/[udlrn]/`
  1. 4 byte - num scores
  1. num scores x 5 byte - scores, each of form:
    1. 1 byte agent : `/[0-9B-N]/`
    1. 4 byte score