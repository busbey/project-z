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
BUG_PATTERN = [B-N]
DEFAULT_HOST = localhost
DEFAULT_PORT = 1337
all: random smart
random: Message.pm State.pm Agent.pm Random.pm 
smart: Message.pm State.pm Agent.pm Smart.pm
clean:
hunter1go: smart
	./Smart.pm $(DEFAULT_HOST) $(DEFAULT_PORT) $(BUG_PATTERN)
hunter2go: random
	./Random.pm $(DEFAULT_HOST) $(DEFAULT_PORT)
