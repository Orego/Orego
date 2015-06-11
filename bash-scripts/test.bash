#!/bin/bash

gcloud compute instances create broadcast --disk name=orego-instance,boot=yes
gcloud compute ssh broadcast ./update
gcloud compute instances delete broadcast --quiet
