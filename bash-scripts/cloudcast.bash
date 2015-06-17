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
gcloud compute instances create instance1 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance2 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance3 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance4 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance5 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance6 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance7 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance8 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance9 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance10 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance11 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance12 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance13 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance14 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance15 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance16 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance17 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance18 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance19 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance20 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance21 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance22 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance23 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance24 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance25 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance26 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance27 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance28 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance29 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance30 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance31 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance32 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance33 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance34 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance35 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance36 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance37 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance38 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance39 --image exp1-image --machine-type n1-highcpu-16
gcloud compute instances create instance40 --image exp1-image --machine-type n1-highcpu-16
sleep 30s

# Tell the broadcast instance to launch the experiment across the other instances
eval `ssh-agent`
ssh-add ~/.ssh/google_compute_engine
gcloud compute ssh --ssh-flag="-A" broadcast <<REMOTE
./broadcast.bash
REMOTE