#!/bin/sh
cd /opt/rnatango/engine
mvn spring-boot:run
#mvn clean package
#java -jar /opt/rnatango/engine/target/rnatango-engine-1.0.0-RELEASE.jar 
while :
do
    sleep 10
done
