#!/usr/bin/env bash

while true
do
    curl -s -I http://localhost:9082/api/movies
	sleep 1
done