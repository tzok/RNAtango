#!/bin/bash

if [[ ! -d ./nginx/conf/ ]];
then
    mkdir -p ./nginx/conf
fi

if [[ ! -f ./nginx/conf/nginx.conf ]];
then
    cp ./nginx/nginx.conf ./nginx/conf/nginx.conf
fi

sudo docker-compose up --build
