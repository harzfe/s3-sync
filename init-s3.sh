#!/bin/bash
set -e
echo "Creating default S3 bucket..."
awslocal s3 mb s3://s3sync --region eu-central-1
awslocal s3 ls
echo "S3 bucket created."