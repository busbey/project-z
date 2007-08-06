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

class Agent:
    headerSize = 10;

    def __init__ (self, host, port):
        self.host = host
        self.port = port
        self.openSocket()
        self.handleCommunications()
        sys.exit(0)

    def openSocket (self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect( (self.host, int(self.port)) )

    def handleCommunications (self):
        flag = socket.ntohl(struct.unpack("B", self.sock.recv(1))[0])
        if (flag == 0xff):
            self.socket.shutdown()
            self.socket.close()
            return 
        player = self.sock.recv(1)
        columns = socket.ntohl(struct.unpack("L", self.sock.recv(4))[0])
        rows = socket.ntohl(struct.unpack("L", self.sock.recv(4))[0])
        self.state = State(player, rows, columns)
        self.state.killerBug = ((flag & 0x01) == 0x01)
        for i in range(0, rows - 1):
            for j in range(0, columns - 1):
                self.state.changeBoard(i, j, self.sock.recv(1))
        
        print self.state.board
        messages = socket.ntohl(struct.unpack("L", self.sock.recv(4))[0])
        self.state.emptyMessages()
        for i in range(1, messages):
            speaker = self.sock.recv(1)
            subject = self.sock.recv(1)
            action = self.sock.recv(1)
            self.state.addMessage(speaker, subject, action)

        self.respondToChange()

        while True:
            flag = socket.ntohl(struct.unpack("B", self.sock.recv(1))[0])
            if (flag == 0xff):
                self.socket.shutdown()
                self.socket.close()
                return 
            self.sock.recv(Agent.headerSize - 1)
            self.state.killerBug = ((flag & 0x01) == 0x01)
            for i in range(0, rows - 1):
                for j in range(0, columns - 1):
                    self.state.changeBoard(i, j, self.sock.recv(1))
            
            messages = socket.ntohl(struct.unpack("L", self.sock.recv(4))[0])
            self.state.emptyMessages()
            for i in range(1, messages):
                speaker = self.sock.recv(1)
                subject = self.sock.recv(1)
                action = self.sock.recv(1)
                self.state.addMessage(speaker, subject, action)

            self.respondToChange()

    def writeMove (self, move):
        print move
        self.sock.send(move)

    def sendMessage (self, speaker, subject, action)
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
        for i in range(1, self.rows * self.columns):
            self.board.append('')
        self.emptyMessages()
    
    def changeBoard (self, row, column, type):
        self.board[row * self.rows + column] = type

    def emptyMessages (self):
        self.messages = []

    def addMessage (self, speaker, subject, action):
        self.messages.append( (speaker, subject, action) )

if __name__ == '__main__':
    main(sys.argv[1:])
