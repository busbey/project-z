**Project Z** is a system for testing agent-based goal seeking strategies in a simple and intuitive game world.

Project Z's main components:

  * ProblemDescription - an intro to the problem for players that describes the game world, the space of agent moves, and provides a brief implementation language agnostic game walk-through.
  * SampleServer - an implementation fo the server side of both the WorldStateProtocol and the ActorProtocol that can be used for testing by participants as well as evaluation by event organizers
  * DevKit packages - development materials that abstract away dealing with client-server communications allowing for a lower barrier-to-entry for novice participants.  Kits also contain sample agent implementations for moving randomly (ExampleRandomAgentOutput) and simple straight-line goal seeking (ExampleSmartAgentOutput). Currently available for: C with libc, Java, Ruby, Perl, and Python.  For more detail see LanguageSupport.
  * FancyDisplay - a sample display implementation for more audience-driven events designed to maximize cuteness.  uses [Danc's Miraculously Flexible Game Prototyping Tiles](http://lostgarden.com/2007/05/dancs-miraculously-flexible-game.html) art by Daniel Cook (Lostgarden.com)
  * JoystickAgent - a sample agent that allows for one of the game participants to be controlled by a human with a game input pad
  * SubmissionController - a sample back-end submission handler that runs agents against each other and reports results.