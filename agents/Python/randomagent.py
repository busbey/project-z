#!/usr/bin/env python
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


import agent
import random
import sys
import getopt

def main (argv):
    host = ""
    port = 0
    
    try:
        opts, args = getopt.getopt(argv, "h:p:", ["host=", "port="])
    except getopt.GetoptError:
        print "randomagent.py [-h|--host=] host [-p|--port=] port"
        sys.exit(1)
        
    for opt, arg in opts:
        if opt in ("-h", "--host"):
            host = arg
        elif opt in ("-p", "--port"):
            port = arg

    agent = RandomAgent(host, port)

class RandomAgent(agent.Agent):
    moves = ['u', 'd', 'l', 'r', 'n']
    
    def respondToChange (self):
        self.writeMove(random.choice(RandomAgent.moves))
        self.sendMessage(self.state.player, self.state.player, random.choice(RandomAgent.moves))

if __name__ == '__main__':
    main(sys.argv[1:])
