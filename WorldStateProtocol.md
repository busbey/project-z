serialized form:

  1. 1 byte: game flags
    * 0xFF - end of game
    * 0x01 - bug kills bug hunters (otherwise bug hunters kill bug)
    * 0x02 - you died last round
    * 0x04 - you were stunned last round
    * 0x08 - you killed an agent last round
    * 0x10 - a game round has changed
  1. 1 byte: ascii character representation (number for hunters, 'B' - 'N' for bugs, 'd' - 'z' for displays)
  1. 4 byte num entries in a world row
  1. 4 byte number of world rows
  1. num entries\*numrows byte - entries read from left to right, top to bottom starting top-left. each as an ASCII character:
    * ' ' - open space
    * 'O' - obstacle
    * 'B' to 'N' - bugs
    * 'P' - powerup
    * '0' to '9'  - hunter number
    * 'X' - unknown content
  1. 4 byte - num chat messages
  1. num chat x 3 byte - chat messages each 3 byte form:
    1. 1 byte speaker : `/[0-9B-N]/`
    1. 1 byte subject : `/[0-9B-N]/`
    1. 1 byte action  : `/[udlrn]/`