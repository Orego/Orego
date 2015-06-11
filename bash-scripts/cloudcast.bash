#!/bin/bash

# NOTE: Before starting, the latest code should be in the cloud branch on GitHub.

# Get the root persistent disk orego-instance up-to-date with the latest code from GitHub
gcloud compute instances create broadcast --disk name=orego-instance,boot=yes
gcloud compute ssh --ssh-flag="-A" broadcast <<REMOTE
cd Orego
git pull origin cloud
./build-all.sh
REMOTE
gcloud compute instances delete broadcast --quiet

# Create an image from that disk
gcloud compute images delete exp1-image --quiet
gcloud compute images create exp1-image --source-disk orego-instance

# Create the instances
gcloud compute instances create broadcast --disk name=orego-instance,boot=yes
gcloud compute instances create instance1 --image exp1-image --machine-type n1-highcpu-2
gcloud compute instances create instance2 --image exp1-image --machine-type n1-highcpu-2

# Tell the broadcast instance to launch the experiment across the other instances
eval `ssh-agent`
ssh-add ~/.ssh/google_compute_engine
gcloud compute ssh --ssh-flag="-A" broadcast <<REMOTE
./broadcast.bash
REMOTE