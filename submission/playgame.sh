#!/bin/bash

java server/Server ./map > results &
pid=$!

pushd bug
make buggo > /dev/null 2>&1 &
popd

pushd firsthunters
make hunter1go > /dev/null 2>&1 &
make hunter2go > /dev/null 2>&1 &
popd

pushd secondhunters
make hunter1go > /dev/null 2>&1 &
make hunter2go > /dev/null 2>&1 &
popd

while [ 2 -eq `ps -p $pid | wc -l | xargs expr` ]
do
done

