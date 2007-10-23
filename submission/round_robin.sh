#!/bin/bash
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

# command line options.
hunters="hunters"
bugs="bugs"
maps="maps"
hunter1host="localhost"
hunter2host="localhost"
bughost="localhost"
rounds=20
usage()
{
	echo "Usage ${0##*/}
	Runs a round-robin tournament across specified hunters, bugs, and maps.
	-H	specify file with list of hunters [hunters]
	-B	specify file with list of bugs [bugs]
	-M	specify file with list of maps [maps]
	-1	host that runs first hunter team [localhost]
	-2	host that runs second hunter team [localhost]
	-b	host that runs the bug team [localhost]
	-r	number of rounds per match [20]
	-h, --help	this help screen
	" >&2
	exit 1
}
while getopts ":H:B:M:1:2:b:r" optionName; do
case "$optionName" in
H) hunters="$OPTARG";;
B) bugs="$OPTARG";;
M) maps="$OPTARG";;
1) hunter1host="$OPTARG";;
2) hunter2host="$OPTARG";;
b) bughost="$OPTARG";;
r) rounds="$OPTARG";;
\?) usage;;
*) usage;;
esac
done
serverPid=""
# properly shut down on unclean exit
cleanup()
{
	if [ "${serverPid}" != "" ]
	then
		kill -9 "${serverPid}"
	fi
	#if we have a bug, kill everything and copy output files back here
	#if we have a hunter1, kill everything and copy output files back here
	#if we have a hunter2, kill everything and copy output files back here
	echo "clean!"
	exit
}
trap "cleanup" SIGINT SIGTERM
# these "cat foo" calls should be replaced with a sequence of calls that
# randomizes the ordering of lines.
cat bugs | while read bug; do
	cat maps | while read map; do
		cat hunters | while read hunter1; do
			cat hunters | while read hunter2; do
				#make sure we haven't already done this match up.
				#create a mapping from hunter / bug teams to agents in game.
				agentList=`echo "123456789" | ./randomizeString.awk`;
				bugList=`echo "BCDEFGHIJKLMN" | ./randomizeString.awk`;
				hunter1agents=${agentList:0:2};
				hunter2agents=${agentList:2:2};
				bugagents=${bugList:6:1};
				echo "Playing on map: ${map}
Bugs are:
	team 1 (${bug}): ${bugagent}
Hunters are:
	team 1 (${hunter1}): ${hunter1agents}
	team 2 (${hunter2}): ${hunter2agents}
";
				# start the server with acls in place
				java -jar Server.jar --batch "${rounds}" --acl Displays= Bugs=${bughost}:${bugagents} Hunters=${hunter1host}:${hunter1agents},${hunter2host}:${hunter2agents} --map "${map}" >server.out 2>server.err </dev/null &
				serverPid=$!
				# remote launch the bug
				# remote launch teamA
				# remote launch teamB
				# wait for the server to end.
				wait "${serverPid}"
				serverPid=""
				# tally scores
				# prep results from server
				# cleanup
			done;
		done;
	done;
done;
# package results
# package scores
