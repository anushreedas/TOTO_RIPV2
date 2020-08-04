#!/bin/bash

# file that gets run when the docker container starts up
# @author Sam Fryer
# edited by Anushree Das

#first, start the nodejs server to receive any initial commands
node run/run.js $1 &

#then wait 10 seconds to make sure the commands came and executed
sleep 10

#finally, start the initial program
java Toto2 $1

