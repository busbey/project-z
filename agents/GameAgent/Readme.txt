This agent is actually a video game!

the intent is that players will look at a display and then use this agent to play a bug on it.  each player will use a joystick.   

make all
	compiles all the java
	tells you where to put the jni libraries
	
flow:
only 1 game per workstation, runs full screen exclusive.
	make game1 - starts the frist player
	make game2 - starts the second player
	player1.sh - does the above, but as a script you can put on the desktop and make clickable (you'll have to getInfo and change the starting app)
	player2.sh - ditto, but for game 2

License:
#  Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs, Paul Ostazeski
#  
#  This program is free software: you can redistribute it and/or modify it
#  under the terms of the GNU General Public License as published by the Free
#  Software Foundation, either version 3 of the License, or (at your option)
#  any later version.
#  
#  This program is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
#  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
#  more details.
#  
#  You should have received a copy of the GNU General Public License along with
#  this program.  If not, see <http://www.gnu.org/licenses/>.
#  
