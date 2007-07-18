#!/bin/sh
       home=`pwd`
       map="${home}/worlds/tiles"
 serverLog="${home}/Server.log"
 serverPid="${home}/Server.pid"
displayLog="${home}/Display.log"

# Start the server
pushd server > /dev/null 2>&1
if [ ! -f "Server.class" ]; then
	echo "Compiling the server..."
    javac Server.java
fi
echo -n "Starting the server.."
java Server -b "${map}" > "${serverLog}" 2>&1 &
svrPid=$!
echo ${svrPid} > "${serverPid}"

sleep 5
# Give the server a little time to start...
while [ `grep 'Starting display ' "${serverLog}" > /dev/null` ]; do
	echo -n "."
	sleep 2
done
echo ". pid ${svrPid}"
popd > /dev/null 2>&1

# Start the display
pushd display > /dev/null 2>&1
echo -n "Starting the display..."
./display.rb > "${displayLog}" 2>&1 &
dspPid=$!

while [ ! -s "${displayLog}" ]; do
	echo -n "."
	sleep 1
done
echo ". pid ${dspPid}"
popd > /dev/null 2>&1

# Attach agents
pushd agents > /dev/null 2>&1
if [ ! -f "Agent.class" ]; then
	echo "Compiling agents..."
    javac Agent.java
    javac SmartBugMover.java
    javac SmartHunterMover.java
fi

for idx in 1 ; do
	echo "Starting bug agent ${idx}..."
    java Agent localhost 7331 SmartBugMover P '[1-9]'	\
			> Bug${idx}.log 2>&1 &
			#> /dev/null 2>&1 &	
done

for idx in 1 2 3 4; do
	echo "Starting hunter agent ${idx}..."
    java Agent localhost 1337 SmartHunterMover '[B-N]'	\
			> Hunter${idx}.log 2>&1 &
			#> /dev/null 2>&1 &	
done
popd > /dev/null 2>&1


echo "-------------------------------------------------"
echo "Press any key to terminate the server and display"
read key

kill ${dspPid}
kill ${svrPid}
rm -f "${serverPid}"
