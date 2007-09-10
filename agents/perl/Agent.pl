#!/usr/bin/perl
package Agent;
use strict;

use Socket;

# construct a new Agent
sub new 
{
	my $class = @_;
	my $self = { _socket = undef };
	bless $self, $class;
	return $self;
}

# set up our socket connection
sub connect
{
}

# send a move to the server
sub writeMoveToServer
{
}

# Things to override for your implementation.

# handle command line args.
sub init
{
}

# given a world state, pick a new action
sub respondToChnage
{
}
