#!/usr/bin/env python

import sys
import getopt
import socket
import struct

def main (argv):
    host = ""
    port = 0
    
    try:
        opts, args = getopt.getopt(argv, "h:p:", ["host=", "port="])
    except getopt.GetoptError:
        print "agent.py [-h|--host=] host [-p|--port=] port"
        sys.exit(1)
        
    for opt, arg in opts:
        if opt in ("-h", "--host"):
            host = arg
        elif opt in ("-p", "--port"):
            port = arg

    agent = Agent(host, port)

class Agent(object):
    def __init__ (self, host, port):
        self.host = host
        self.port = port
        self.state = None
        self.openSocket()
        self.handleCommunications()
        sys.exit(0)

    def openSocket (self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect( (self.host, int(self.port)) )

    def handleCommunications (self):
        while True:
            flag = struct.unpack("B", self.sock.recv(1))[0]
            if (flag == 0xff):
                self.socket.shutdown()
                self.socket.close()
                return 
            print 'flag: %d' % flag
            
            player = self.sock.recv(1)
            print 'player: ' + player

            columns = self.readInteger()
            print 'columns: %d' % columns
            
            rows = self.readInteger()
            print 'rows: %d' % rows
            
            if (self.state == None):
                self.state = State(player, rows, columns)
            self.state.killerBug = ((flag & 0x01) == 0x01)
            for i in range(0, rows):
                for j in range(0, columns):
                    self.state.changeBoard(i, j, self.sock.recv(1))
            print 'board: ' + str(self.state.board)
                        
            messages = self.readInteger()
            print 'messages: %d' % messages 
            
            self.state.emptyMessages()
            for i in range(0, messages):
                speaker = self.sock.recv(1)
                subject = self.sock.recv(1)
                action = self.sock.recv(1)
                print 'message: ' + speaker + ' says ' + subject + ' should move ' + action
                self.state.addMessage(speaker, subject, action)

            self.respondToChange()

    def readInteger (self):
        read = []
        while (len(read) < 4):
            read.append(self.sock.recv(1))
        return socket.ntohl(struct.unpack("L", ''.join(read))[0])

    def writeMove (self, move):
        print 'moving: ' + move
        self.sock.send(move)

    def sendMessage (self, speaker, subject, action):
        print 'sending: ' + speaker + ' says ' + subject + ' should move ' + action
        self.sock.send(speaker)
        self.sock.send(subject)
        self.sock.send(action)
    
    def respondToChange (self):
        self.writeMove('n')

class State:
    def __init__ (self, player, rows, columns):
        self.killerBug = False
        self.player = player
        self.rows = rows
        self.columns = columns
        self.board = []
        for i in range(0, self.rows * self.columns):
            self.board.append('')
        self.emptyMessages()
    
    def changeBoard (self, row, column, type):
        self.board[row * self.columns + column] = type

    def readBoard (self, row, column):
        return self.board[row * self.columns + column]

    def emptyMessages (self):
        self.messages = []

    def addMessage (self, speaker, subject, action):
        self.messages.append( (speaker, subject, action) )

if __name__ == '__main__':
    main(sys.argv[1:])
