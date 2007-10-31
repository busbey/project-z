#!/bin/bash
projz="/Users/sabusbey/project/project-z/agents/GameAgent"
pushd "${projz}"
make game2
killall make
popd
