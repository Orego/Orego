#!/bin/bash

gcloud compute instances create broadcast --disk name=orego-instance,boot=yes
gcloud compute ssh --ssh-flag="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" broadcast ./update
gcloud compute instances delete broadcast --quiet
