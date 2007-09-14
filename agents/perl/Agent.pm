#!/usr/bin/perl
#  Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs
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
my %MOVES = (	ord 'n' => "NONE", 
				ord 'l' => "LEFT",
				ord 'r' => "RIGHT",
				ord 'u' => "UP",
				ord 'd' => "DOWN");

# construct a new Agent
sub new 
{
	my $class = shift;
	my $self = { };
	my $argRef = shift;

        use Getopt::Std;
        my %opt;
        my $opt_string = "h:p:";
        getopts( $opt_string, \%opt );
        usage() unless $opt{h} and $opt{p};

        my $host = $opt{h};
        my $port = $opt{p};

	#printf STDERR ("Connecting to %s:%d\n", $host, $port);
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
	printf STDERR ("moving %s\n", $MOVES{$move});
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
	printf STDERR ("sending '%c' says '%c' should move %s\n", $message->sender, $message->subject, $MOVES{$message->move});
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
	my $argref = \@ARGV;
	my $agent = ($package)->new($argref);
	$agent->init(@$argref);
	my $state = new State;
	
	STDERR->autoflush(1);
	# loop for updates until we get a game over.
	do
	{
		$state = $agent->getState($state);
		$agent->respondToChange($state);
		printf STDERR "\n";
	} while(State::FALSE == $state->gameOver );
	printf "Game has ended...\n";
}

# Things to override for your implementation.

# usage statment
sub usage
{
   die "Usage: $0 -h host -p port [args *]\n";
}

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

