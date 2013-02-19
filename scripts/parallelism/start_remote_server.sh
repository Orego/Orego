#!/bin/sh

# simple script to run the RMI registry and the number of instances requested for live integration testing
# helpful guide: http://www.symkat.com/understanding-job-control-in-bash

echo "Reading in configuration..."

source CONFIG.cfg

echo "Tearing down old services..."
ssh $HOSTNAME killall -9 rmiregistry
ssh $HOSTNAME killall -9 java

echo "Starting RMI registry...."
# start RMI in the background
nohup ssh $HOSTNAME rmiregistry&

echo "Spinning up server to listen at $HOSTNAME"

java -ea -Xmx2048M -cp  $OREGO_CP -Djava.rmi.server.hostname=$HOSTNAME orego.ui.Orego player=ClusterPlayer threads=2 msec=2000
