#!/usr/bin/perl
package State;
use strict;

use IO::Socket;

use constant FALSE => 0;
use constant TRUE => 1;
use constant FLAGS_GAME_END => 0xFF;
use constant FLAGS_BUG_KILLS => 0x01;
use constant FLAGS_AGENT_DIED => 0x02;
use constant FLAGS_AGENT_STUN => 0x04;

sub new
{
	my $class = shift;
	my $self = {};
	$self->{bugKills} = FALSE;
	$self->{stunned} = FALSE;
	$self->{killed} = FALSE;
	$self->{gameOver} = FALSE;
	$self->{player} = undef;
	$self->{rows} = undef;
	$self->{cols} = undef;
	$self->{board} = undef;
	$self->{numMessages} = 0;
	$self->{messages} = undef;
	bless $self, $class;
	return $self;
}

# read a new game state from the network.
sub readState ( $socket )
{
	my $self = shift;
	my $buffer = undef;

	recv($socket, $buffer, 1, 0);
	
}

# accessors for game state

sub bugKills
{
	my $self = shift;
	return $self->{bugKills};
}

sub stunned
{
	my $self = shift;
	return $self->{stunned};
}

sub killed
{
	my $self = shift;
	return $self->{killed};
}

sub gameOver
{
	my $self = shift;
	return $self->{gameOver};
}

sub player
{
	my $self = shift;
	return $self->{player};
}

sub rows
{
	my $self = shift;
	return $self->{rows};
}

sub cols
{
	my $self = shift;
	return $self->{cols};
}

sub board
{
	my $self = shift;
	return $self->{board};
}

sub numMessages
{
	my $self = shift;
	return $self->{numMessages};
}

sub messages
{
	my $self = shift;
	return $self->{messages};
}

1;
