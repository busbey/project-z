maps are animated from a series of frames.  non-animated maps are just a degenerate case where there is a single frame.

maps can have multiple single-line comments.

several tunable parameters are also set via comments.

maps may not change size between frames.  map rows must be uniform in size.

map files are:
```
 S                -> line{numRows*numFrames + numComments}
  line            -> comment
                     frame delimiter
                     map row
  comment         -> ';' + command text
  comment text    -> command
                     plain text
  command         -> 'bug kill score:' + int    // points for killing bug
                     'hunter kill score:' + int // points for killing hunter
                     'powerup:' + int           // points for getting powerup
                     'stunned:' + int           // points for being stunned
                     'killed:' + int            // points for being killed
                     'rounds per frame:' + int  // rounds to wait between animation frames
  frame delimiter -> /={rowLen}/
  map row         -> /[0-9B-NOP]{rowLen}/

```

example:
```
;demo map
OOOOOOO  OOOOOO  OOOOOOOOO
O    O  O    O  O        O
O     O  O  O  O         O
O      O  OO  O          O
; comments in the middle are fine
O        OOOO            O
O       OOOOOOOOO        O
O      O         O       O
OOOOOOO  OOOOOO  OOOOOOOOO
==========================
OOOOOOO  OOOOOO  OOOOOOOOO
O    O  O    O  O        O
O     O  O  O  O         O
O      O  OO  O          O
O       OOOOOO           O
O       OOOOOOOOO        O
O      O         O       O
OOOOOOO  OOOOOO  OOOOOOOOO
;rounds per frame:10
```