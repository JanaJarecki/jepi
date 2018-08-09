#!/bin/bash

CALL_SITE=$(PWD)
SCRIPT_SITE="$(dirname "$0")"

cd ${SCRIPT_SITE}
java -Djava.security.policy=${CALL_SITE}/security.policy -cp build/libs/gradingServer-all.jar evaluationbasics/RunServer $@
