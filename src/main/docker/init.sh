#!/bin/bash
clear
echo "Initializing gradle clean"
gradle clean
echo "Initializing bootJar"
gradle bootJar

echo "Starting copy"
cp build/libs/expensesapp-server-0.0.1-SNAPSHOT.jar .

echo "Starting compose"
cd src/main/docker || exit && docker-compose up -d