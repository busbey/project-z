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
serverhost="localhost"
rounds=20
cp="rsync --archive --delete --force --timeout=10"
scp="${cp} --rsh=ssh"
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
	-s	host that runs the server [localhost]
	-r	number of rounds per match [20]
	-h, --help	this help screen
	" >&2
	exit 1
}
while getopts ":H:B:M:1:2:b:r:" optionName; do
case "$optionName" in
H) hunters="$OPTARG";;
B) bugs="$OPTARG";;
M) maps="$OPTARG";;
1) hunter1host="$OPTARG";;
2) hunter2host="$OPTARG";;
b) bughost="$OPTARG";;
r) rounds="$OPTARG";;
s) serverhost="$OPTARG";;
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
	ssh "${bughost}" "./cleanup.sh -b"
	#if we have a hunter1, kill everything and copy output files back here
	ssh "${hunter1host}" "./cleanup.sh"
	#if we have a hunter2, kill everything and copy output files back here
	ssh "${hunter2host}" "./cleanup.sh"
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
				matchstring="${bug}-${map}-${hunter1}-${hunter2}"
				matchstring=${matchstring//\//_}
				mkdir -p "results/${bug}/${matchstring}"
				mkdir -p "results/${hunter1}/${matchstring}"
				mkdir -p "results/${hunter2}/${matchstring}"
				#create a mapping from hunter / bug teams to agents in game.
				agentList=`echo "123456789" | ./randomizeString.awk`;
				bugList=`echo "BCDEFGHIJKLMN" | ./randomizeString.awk`;
				hunter1agents=${agentList:0:2};
				hunter2agents=${agentList:2:2};
				bugagents=${bugList:6:1};
				echo "Playing on map: ${map}
Bugs are:
	team 1 (${bug}): ${bugagents}
Hunters are:
	team 1 (${hunter1}): ${hunter1agents}
	team 2 (${hunter2}): ${hunter2agents}
";
				# start the server with acls in place
				java -jar Server.jar --batch "${rounds}" --acl Displays= Bugs=${bughost}:${bugagents} Hunters=${hunter1host}:${hunter1agents},${hunter2host}:${hunter2agents} --map "${map}" >server.out 2>server.err </dev/null &
				serverPid=$!
				# remote launch the bug
				ssh "${bughost}" "./runAgent.sh -b -a ${bug} -s ${serverhost}"
				# remote launch teamA
				ssh "${hunter1host}" "./runAgent.sh -a ${hunter1} -s ${serverhost}"
				# remote launch teamB
				ssh "${hunter2host}" "./runAgent.sh -a ${hunter2} -s ${serverhost}"
				# wait for the server to end.
				wait "${serverPid}"
				serverPid=""
				# tally scores
				grep "Scores: {" server.out | tail -n 1
				# prep results from server
				${cp} server.err server.out "results/${bug}/${matchstring}/"
				${cp} server.err server.out "results/${hunter1}/${matchstring}/"
				${cp} server.err server.out "results/${hunter2}/${matchstring}/"
				${scp} "${bughost}:chroot/results/bug.out chroot/results/bug.err" "results/${bug}/${matchstring}/"
				${scp} "${hunter1host}:chroot/results/hunter1.out chroot/results/hunter1.err results/hunter2.out results/hunter2.err" "results/${hunter1}/${matchstring}/"
				${scp} "${hunter2host}:chroot/results/hunter1.out chroot/results/hunter1.err results/hunter2.out results/hunter2.err" "results/${hunter2}/${matchstring}/"
				cleanup
			done;
		done;
	done;
done;
# package results
# package scores
