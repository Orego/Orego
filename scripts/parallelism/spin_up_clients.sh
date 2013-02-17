#!/bin/bash

# simple script to run the RMI registry and the number of instances requested for live integration testing
# helpful guide: http://www.symkat.com/understanding-job-control-in-bash

OREGO_CP=../../bin
HOSTNAME=localhost
SERVER_COMMANDS=server.command


echo "Init clients $1....."

for i in `seq 1 $1`;
do
	echo "Starting client $i..."

	java -ea -Xmx2048M -cp $OREGO_CP orego.cluster.ClusterTreeSearcher $HOSTNAME > client_$i.log &

	sleep 1s
done

jobs
