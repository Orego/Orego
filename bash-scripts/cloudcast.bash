#!/bin/bash

# NOTE: Before starting, the latest code should be in the cloud branch on GitHub.

# Get the root persistent disk orego-instance up-to-date with the latest code from GitHub
gcloud compute instances create broadcast --disk name=orego-instance,boot=yes
# Wait for ssh demon to start on instance
sleep 30s
gcloud compute ssh --ssh-flag="-A" broadcast ./update.bash
gcloud compute instances delete broadcast --quiet

# Create an image from that disk
gcloud compute images delete exp1-image --quiet
gcloud compute images create exp1-image --source-disk orego-instance

# Create the instances
gcloud compute instances create broadcast --disk name=orego-instance,boot=yes
gcloud compute instances create instance1 instance2 instance3 instance4 --image exp1-image machine-type n1-highcpu-16
sleep 120s

# Tell the broadcast instance to launch the experiment across the other instances
eval `ssh-agent`
ssh-add ~/.ssh/google_compute_engine
gcloud compute ssh --ssh-flag="-A" broadcast <<REMOTE
./broadcast.bash
REMOTE