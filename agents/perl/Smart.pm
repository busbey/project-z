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

package Smart;
use base qw(Agent);

__PACKAGE__->main() unless caller;

# usage statment
sub usage
{
   die "Usage: $0 host port [regex for goal]\n\tex: $0 localhost 1337 [B-N]\n";
}

#handle args
sub init
{
	$self = shift;
	#first arg, if present is a regular expression of what to chase.
	$goalreg = shift @ARGV || "[B-N]";
	$self->{'goal'} = $goalreg;
	#printf STDERR "Moving towards goal '%s'\n", $self->{'goal'};
}

# move towards the closest goal
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

	if((!defined($myRow)) || (!defined($myCol)))
	{
		print STDERR "Warning: Couldn't find myself on the board.\n";
		return;
	}

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

	printf ("Goal '%s' is at (%d, %d); I am at (%d, %d)\n", $goal, $goalRow, $goalCol, $myRow, $myCol);

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
