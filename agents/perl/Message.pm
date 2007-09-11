#!/usr/bin/perl
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
			printf stderr "Problem reading chat message.\n";
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
