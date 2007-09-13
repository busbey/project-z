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

package Random;
use base qw(Agent);

__PACKAGE__->main() unless caller;

sub respondToChange
{
	my $self = shift;
	my $state = shift;
	my @moves = (Agent::NONE, Agent::LEFT, Agent::RIGHT, Agent::UP, Agent::DOWN);
	my $move = $moves[rand(scalar(@moves))];
	$self->writeMoveToServer($move);

	$move = $moves[rand(scalar(@moves))];
	my $message = new Message($state->player, $state->player, $move);
	$self->writeMessageToServer($message);
}
