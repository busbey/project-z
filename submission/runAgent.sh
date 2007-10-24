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
agentType="hunter"
serverhost="localhost"
agent=""

cp="rsync --archive --delete --force --timeout=10"
usage()
{
	echo "Usage ${0##*/}
	Run an agent.  either bug or hunter.  in the background. 
		create info needed to cleanup
	-b	run a bug [run two hunters]
	-s	name of the machine running the server [localhost]
	-a	name of the agent to use (required)
	-h, --help	this help screen
	" >&2
	exit 1
}
while getopts ":bs:a:" optionName; do
case "$optionName" in
b) agentType="bug";;
s) serverhost="$OPTARG";;
a) agent="$OPTARG";;
\?) usage;;
*) usage;;
esac
done
if [ "" = "${agent}" ]
then
	usage
	exit 1
fi
port=1337
if [ "bug" = "${agentType}" ]
then
	port=7331
fi
sshPid=""
ssh -NR "${port}:${serverhost}:${port}" localhost &
sshPid=$!
echo "${sshPid}" > ssh.pid
${cp} "${agent}/" "chroot/${agentType}"
cd chroot/
if [ "bug" = "${agentType}" ]
then
	chroot chroot/ ./runBug.sh &
	bugPid=$!
	echo "${bugPid}" > bug.pid
else
	chroot chroot/ ./runHunters.sh &
	hunterPid=$!
	echo "${hunterPid}" > hunter.pid
fi
popd
