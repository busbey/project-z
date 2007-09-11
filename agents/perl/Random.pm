#!/usr/bin/perl
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
