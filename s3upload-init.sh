#!/usr/bin/env sh

JAVA_OPTS="${JAVA_OPTS} -XX:ErrorFile="./hs_error.log" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof "
JAVA_OPTS="${JAVA_OPTS} -XX:OnOutOfMemoryError=/s3upload.sh -XX:OnError=/s3upload.sh "
JAVA_OPTS="${JAVA_OPTS} -Xlog:gc:./gc.log "


export JAVA_OPTS