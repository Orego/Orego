#!/bin/sh

source CONFIG.cfg
PORT=1099

for HOST in $NODES:
do
	echo "Opening ports on $HOST..."
	ssh root@$HOST "iptables -I INPUT -p tcp --dport $PORT -j ACCEPT"
	ssh root@$HOST "iptables -I OUTPUT -p tcp --dport $PORT -j ACCEPT"
	
	ssh root@$HOST "/etc/init.d/iptables save"
	ssh root@$HOST "/etc/init.d/iptables restart"
	
	echo "Checking if port is working (you should have RMI running on $HOST)..."
	
	nmap -p $PORT $HOST
done

