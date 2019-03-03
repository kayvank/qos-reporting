#!/bin/bash
##########################################
## run the docker image generated from packaging this project
## must provide image name
## dependency: requires ./.envrc pointing to a suitable env
##########################################

if [[ $# -lt 1 ]]; then 
  echo "usage is $0 dockeriamge" 
  exit -1
fi
##for i in `docker ps -a | grep 'qos' | awk '{print $1}'`;do docker rm -f $i;done
##for i in `docker images | grep 'qos' | awk '{print $3}'`;do docker rmi -f  $i;done

 docker run  \
   -p 9000:9000 \
   -e API_KEY=${DATADOG_API_KEY} \
   -e AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} \
   -e AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} \
   -e JDBC_URL=${JDBC_URL} \
   -e JDBC_USER=${PRD_JDBC_USER} \
   -e JDBC_PASSWORD=${JDBC_PASSWORD} \
   -e DD_API_KEY=${DD_API_KEY} \
   -e STATSD_TAG_ENV="local,qos-r-02" \
  $1
