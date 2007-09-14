#!/usr/bin/perl
#  Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs, Paul Ostazeski
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

package Message;
use strict;

# Create a new chat message
sub new
{
	my $class = shift;
	my $self = {};
	$self->{sender} = shift || undef;
	$self->{subject} = shift || undef;
	$self->{move} = shift || undef;
	bless $self, $class;
	return $self;
}

# construct a new chat message based off of reading from a file.
sub fromFile
{
	my $class = shift;
	my $file = shift;
	my $curRead = 0;
	my $bytesRead = 0;
	my $sender = undef;
	my $subject = undef;
	my $move = undef;
	my $chunk = undef;
	my $buffer = undef;
	do
	{
		$curRead = read($file, $chunk, 3 - $bytesRead);
		if(0 >= $curRead)
		{
			printf STDERR "Problem reading chat message.\n";
			exit(-1);
		}
		$buffer = $buffer . $chunk;
		$bytesRead += $curRead;
	}while(3 > $bytesRead);
	$sender, $subject, $move = unpack "C[3]", $buffer;
	
	return $class->new($sender, $subject, $move);
}

# accessors
sub sender
{
	my $self = shift;
	return $self->{sender};
}

sub subject
{
	my $self = shift;
	return $self->{subject};
}

sub move
{
	my $self = shift;
	return $self->{move};
}
# make "use Message" succeed
1;
