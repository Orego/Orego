#!/bin/bash

# simple script to run the RMI registry and the number of instances requested for live integration testing
# helpful guide: http://www.symkat.com/understanding-job-control-in-bash

echo "Reading config..."
source CONFIG.cfg


echo "Starting remote clients clients $1....."

for NODE in $NODES;
do
	echo "Starting client $NODE..."

	nohup ssh $NODE java -ea -Xmx2048M -cp $OREGO_CP orego.cluster.ClusterTreeSearcher $HOSTNAME &> client_$i.log &

	sleep 1s
done

jobs
