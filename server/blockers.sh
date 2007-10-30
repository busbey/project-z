pushd $1 &> /dev/null
make hunter1go &> /dev/null < /dev/null &
make hunter2go &> /dev/null < /dev/null &
popd &> /dev/null
