#!/bin/bash
projz="/Users/sabusbey/project/project-z/agents/GameAgent"
pushd "${projz}"
make game1
killall make
popd
