#!/usr/bin/env bash

# Enables remote debugging on port 5005 if the application is running in a development cluster

if [ "$NAIS_CLUSTER_NAME" = "dev-fss" ] || [ "$NAIS_CLUSTER_NAME" = "dev-gcp" ]; then
  export JAVA_OPTS="${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
fi
