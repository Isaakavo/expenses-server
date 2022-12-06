#!/bin/bash
clear
echo "Initializing gradle clean"
gradle clean

echo "Initializing bootJar"

gradle bootJar

echo "Starting compose"
cd src/main/docker || exit && docker-compose up --build -d