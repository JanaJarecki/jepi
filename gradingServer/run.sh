#!/bin/bash

java -Djava.security.policy=security.policy -cp build/libs/gradingServer-all.jar evaluationbasics/RunServer $@
