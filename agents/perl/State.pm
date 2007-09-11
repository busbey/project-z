#!/usr/bin/perl
package State;
use strict;

use Message;

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
sub readState
{
	my $self = shift;
	my $file = shift;
	my $buffer = undef;
	my $bytesRead = 0;
	my $chunk = undef;

	$bytesRead = read($file, $buffer, 1);
	if(1 != $bytesRead)
	{
		printf STDERR "Problem reading world state.\n";
		exit(-1);
	}
	if(FLAGS_GAME_END == (FLAGS_GAME_END & $buffer))
	{
		$self->{gameOver} = TRUE;
	}
	else
	{
		$self->{gameOver} = FALSE;
		if(FLAGS_BUG_KILLS == (FLAGS_BUG_KILLS & $buffer))
		{
			$self->{bugKills} = TRUE;
		}
		if(FLAGS_AGENT_DIED == (FLAGS_AGENT_DIED & $buffer))
		{
			$self->{killed} = TRUE;
		}
		if(FLAGS_AGENT_STUN == (FLAGS_AGENT_STUN & $buffer))
		{
			$self->{stunned} = TRUE;
		}
	}
	$bytesRead = read($file, $buffer, 1);
	if(1 != $bytesRead)
	{
		printf STDERR "Problem reading agent's character.\n";
		exit(-1);
	}
	$self->{player} = unpack "C", $buffer;
	printf STDERR ("Running as Agent %c\n", $self->{player});
	$buffer = undef;
	$bytesRead = 0;
	do
	{
		my $temp = read ($file, $chunk, 4 - $bytesRead);
		if(0 >= $temp)
		{
			printf STDERR "Problem reading number of columns.\n";
			exit(-1);
		}
		$buffer = $buffer . $chunk;
		$bytesRead += $temp;
	} while(4 > $bytesRead);
	$self->{cols} = unpack("N", $buffer);	
	$buffer = undef;
	$bytesRead = 0;
	do
	{
		my $temp = read ($file, $chunk, 4 - $bytesRead);
		if(0 >= $temp)
		{
			printf STDERR "Problem reading number of rows.\n";
			exit(-1);
		}
		$buffer = $buffer . $chunk;
		$bytesRead += $temp;
	} while(4 > $bytesRead);
	$self->{rows} = unpack("N", $buffer);	
	printf STDERR ("Board is %d x %d\n",$self->{rows}, $self->{cols});
	my $row = 0;
	my $col = 0;
	my @board = ();
	for($row = 0; $row < $self->{rows}; $row++)
	{
		my @newRow = ();
		for($col = 0; $col < $self->{cols}; $col++)
		{
			$bytesRead = read($file, $buffer, 1);
			if(1 != $bytesRead)
			{
				printf STDERR "Problem reading game board";
				exit(-1);
			}
			$buffer = unpack "C", $buffer;
			push @newRow, $buffer;
		}
		push @board, \@newRow;
	}
	$self->{board} = \@board;
	$buffer = undef;
	$bytesRead = 0;
	do
	{
		my $temp = read ($file, $chunk, 4 - $bytesRead);
		if(0 >= $temp)
		{
			printf STDERR "Problem reading number of chat messages.\n";
			exit(-1);
		}
		$buffer = $buffer . $chunk;
		$bytesRead += $temp;
	} while(4 > $bytesRead);
	$self->{numMessages} = unpack("N", $buffer);
	printf STDERR ("%d chat messages this turn.\n", $self->{numMessages});
	my @messages = ();
	my $message = 0;
	for($message = 0; $message < $self->{numMessages}; $message++)
	{
		my $curMessage = Message->fromFile($file);
		push @messages, \$curMessage;
	}
	$self->{messages} = \@messages;
	
	return $self;
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

#make 'use State' succeed
1;
