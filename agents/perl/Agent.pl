#!/usr/bin/perl
package Agent;
use strict;

use IO::Socket;
use State;
use Message;

# construct a new Agent
sub new 
{
	my $class = shift;
	my $self = { };
	my $host = shift || "localhost";
	my $port = shift || 1337;
	my $socket = new IO::Socket::INET(	PeerAddr => $host,
										PeerPort => $port,
										Proto	 => 'tcp');
	if($socket)
	{
		$self->{sock} = $socket;
	}
	else
	{
		printf stderr "Problem establishing connection to server.\n";
		exit(-1);
	}
	init @_;
	bless $self, $class;
	return $self;
}

# send a move to the server
sub writeMoveToServer
{
	my $self = shift;
	my $move = shift;
	my $bytesWritten = -1;
	printf (stderr, "Telling server to perform move '%c'\n", $move);
	$bytesWritten = write($self->{sock}, $move, 1);
	if(1 != $bytesWritten)
	{
		printf stderr "Problem writing move to server.\n";
		exit(-1);
	}
}

#send a chat message to the server
sub writeMessageToServer
{
	my $self = shift;
	my $message = shift;
	my $bytesWritten = -1;
	printf (stderr, "Writing chat message: %c says %c should move '%c'\n", $message->sender, $message->subject, $message->move);
	$bytesWritten = write($self->{sock}, $message->sender, 1);
	if(1 != $bytesWritten)
	{
		printf stderr "Problem writing chat message to server.\n";
		exit(-1);
	}
	$bytesWritten = write($self->{sock}, $message->subject, 1);
	if(1 != $bytesWritten)
	{
		printf stderr "Problem writing chat message to server.\n";
		exit(-1);
	}
	$bytesWritten = write($self->{sock}, $message->move, 1);
	if(1 != $bytesWritten)
	{
		printf stderr "Problem writing chat message to server.\n";
		exit(-1);
	}
}

sub getState
{
	$self = shift;
	$state = shift;
	return $state->readState($self->{sock});
}

sub main
{
	# make a new Agent
	my $package = shift;
	my $agent = new $package;
	my $state = new State;
	
	# loop for updates until we get a game over.
	do
	{
		$state = $agent->getState($state);
		$agent->respondToChange($state);
	} while(! $state->gameOver )
	printf "Game Ended";
}

# Things to override for your implementation.

# you should copy this line explicitly
__PACKAGE__->main unless caller; #invoke main unless we're a module.

# handle command line args.
sub init
{
	# ignore everything.
}

# given a world state, pick a new action
sub respondToChnage
{
	$self = shift;
	$state = shift;
	# in the default case we'll just do nothing. and say so.
	$message = new Message($state->agent, $state->agent, 'n');
	$self->writeMessageToServer($message);
	$self->writeMoveToServer('n');
}


