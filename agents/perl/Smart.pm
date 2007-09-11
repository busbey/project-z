#!/usr/bin/perl
package Smart;
use base qw(Agent);

__PACKAGE__->main() unless caller;

sub init
{
	$self = shift;
	#first arg, if present is a regular expression of what to chase.
	$goalreg = shift || "[B-N]";
	$self->{'goal'} = $goalreg;
	printf STDERR "Moving towards goal '%s'\n", $self->{'goal'};
}

sub respondToChange
{
	my $self = shift;
	my $state = shift;
	my $board = $state->board;
	my $move = Agent::NONE;
	my $myRow = undef;
	my $myCol = undef;
	my $goal = $self->{'goal'};
	my $goalRow = undef;
	my $goalCol = undef;
	my $goalDist = undef;
	my $row = 0;
	my $col = 0;

	for($row = 0; $row < $state->rows; $row++)
	{
		for($col=0; $col < $state->cols; $col++)
		{
			printf " %c", $board->[$row][$col];
		}
		printf "\n";
	}
	printf "\n";
	#find our player.
	for($row = 0; $row < $state->rows && (!defined($myRow)) && (!defined($myCol)); $row++)
	{
		for($col = 0; $col < $state->cols && (!defined($myRow)) && (!defined($myCol)); $col++)
		{
			if($state->player == $board->[$row][$col])
			{
				$myRow = $row;
				$myCol = $col;
			}
		}
	}

	printf "I'm at $myRow : $myCol\n";

	if((!defined($myRow)) || (!defined($myCol)))
	{
		print STDERR "Warning: Couldn't find myself on the board.\n";
		return;
	}

	printf "Finding Goal according to regex '%s'\n", $goal;
	# find the closest goal.
	for($row = 0; $row < $state->rows; $row++)
	{
		for($col = 0; $col < $state->cols; $col++)
		{
			if(chr($board->[$row][$col]) =~ /$goal/)
			{
				my $dist = 	(($row - $myRow) * ($row - $myRow)) + 
							(($col - $myCol) * ($col - $myCol));
				if(! defined $goalDist)
				{
					$goalRow = $row;
					$goalCol = $col;
					$goalDist = $dist;
				}
				else
				{
					if($dist < $goalDist)
					{
						$goalRow = $row;
						$goalCol = $col;
						$goalDist = $dist;
					}
				}
			}
		}
	}

	printf "Closest goal is in $goalRow : $goalCol\n";

	# move to the closest goal.
	if(defined($goalRow) && defined($goalCol))
	{
		my $rowDist = abs($goalRow - $myRow);
		my $colDist = abs($goalCol - $myCol);
		my $rowOrCol = rand($rowDist + $colDist);
		if($rowOrCol < $rowDist)
		{
			#move in the row direction
			if($myRow > $goalRow)
			{
				$move = Agent::UP;
			}
			else
			{
				$move = Agent::DOWN;
			}
		}
		else
		{
			#move in the col direction.
			if($myCol > $goalCol)
			{
				$move = Agent::LEFT;
			}
			else
			{
				$move = Agent::RIGHT;
			}
		}
	}

	$self->writeMoveToServer($move);
}
