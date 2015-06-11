#!/bin/bash

# NOTE: Before running this script, create an instance using the disk orego-instance, pull the latest Orego code
# from GitHub, and build it.

gcloud compute images delete exp1-image
gcloud compute images create exp1-image --source-disk orego-instance
gcloud compute instances create broadcast --disk name=orego-instance,boot=yes
gcloud compute instances create instance1 --image exp1-image --machine-type n1-highcpu-2
gcloud compute instances create instance2 --image exp1-image --machine-type n1-highcpu-2
eval `ssh-agent`
ssh-add ~/.ssh/google_compute_engine
gcloud compute ssh --ssh-flag="-A" broadcast <<REMOTE
ssh-keygen -R instance1
ssh-keygen -R instance2
# Call the script located on the broadcast instance's disk
./broadcast.bash
REMOTE