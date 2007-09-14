#!/bin/sh
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

       home=`pwd`
       map="${home}/../worlds/tiles"
 serverLog="${home}/Server.log"
 serverPid="${home}/Server.pid"

# Start the server
if [ ! -f "Server.class" ]; then
	echo "Compiling the server..."
    javac Server.java
fi
echo -n "Starting the server.."
java Server -b "${map}" > "${serverLog}" 2>&1 &
svrPid=$!
echo ${svrPid} > "${serverPid}"

echo ". pid ${svrPid}"
popd > /dev/null 2>&1

echo "-------------------------------------------------"
echo "Press any key to terminate the server"
read key

kill ${svrPid}
rm -f "${serverPid}"
