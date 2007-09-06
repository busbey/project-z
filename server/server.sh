#!/bin/sh
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
