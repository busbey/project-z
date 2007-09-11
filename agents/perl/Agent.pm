#!/usr/bin/perl
package Agent;
use strict;

use IO::Socket;
use State;
use Message;

use constant NONE => ord 'n';
use constant LEFT => ord 'l';
use constant RIGHT => ord 'r';
use constant UP => ord 'u';
use constant DOWN => ord 'd';

# construct a new Agent
sub new 
{
	my $class = shift;
	my $self = { };
	my $host = shift || "localhost";
	my $port = shift || 1337;
	printf STDERR ("Connecting to %s:%d\n", $host, $port);
	my $socket = new IO::Socket::INET(	PeerAddr => $host,
										PeerPort => $port,
										Proto	 => 'tcp');
	if($socket)
	{
		$self->{sock} = $socket;
	}
	else
	{
		printf STDERR "Problem establishing connection to server.\n";
		exit(-1);
	}
	bless $self, $class;
	return $self;
}

# send a move to the server
sub writeMoveToServer
{
	my $self = shift;
	my $move = shift;
	my $socket = $self->{sock};
	my $bytesWritten = -1;
	printf STDERR ("Telling server to perform move '%c'\n", $move);
	$move = pack "C", $move;
	print $socket $move;
}

#send a chat message to the server
sub writeMessageToServer
{
	my $self = shift;
	my $message = shift;
	my $bytesWritten = -1;
	my $socket = $self->{sock};
	printf STDERR ("Writing chat message: %c says %c should move '%c'\n", $message->sender, $message->subject, $message->move);
	my $data = undef;
	$data = pack "C[3]", [$message->sender, $message->subject, $message->move];
	print $socket $data;
}

sub getState
{
	my $self = shift;
	my $state = shift;
	return $state->readState($self->{sock});
}

sub main
{
	# make a new Agent
	my $package = shift;
	printf "Starting up $package\n";
	my $agent = ($package)->new(@ARGV);
	$agent->init(@ARGV);
	my $state = new State;
	
	STDERR->autoflush(1);
	# loop for updates until we get a game over.
	do
	{
		printf "New Round.\n";
		$state = $agent->getState($state);
		$agent->respondToChange($state);
	} while(State::FALSE == $state->gameOver );
	printf "Game Ended\n";
}

# Things to override for your implementation.

# handle command line args.
sub init
{
	# ignore everything.
}

# given a world state, pick a new action
sub respondToChange
{
	my $self = shift;
	my $state = shift;
	my $move = NONE;
	# in the default case we'll just do nothing. and say so.
	my $message = new Message($state->player, $state->player, $move);

	$self->writeMessageToServer($message);
	$self->writeMoveToServer($move);
}

# you should copy this line explicitly
__PACKAGE__->main unless caller; #invoke main unless we're a module.

