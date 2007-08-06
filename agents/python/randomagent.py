#!/usr/bin/env python

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
