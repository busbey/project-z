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
killChildren()
{
	for child in `ps -o ppid,pid | grep "$1 " | cut -d " " -f 2`
	do
		kill -9 child
		killChildren child
	done
}
cp="rsync --archive --delete --force --timeout=10"
usage()
{
	echo "Usage ${0##*/}
		cleanup from having an agent run.
	-b	cleanup a bug [cleanup hunters]
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
pid=`cat "${agentType}.pid"`
kill -9 pid
killChildren pid
${cp} "common/" "chroot/"
