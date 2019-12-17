#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/defaultDS/username;
then
    export  DEFAULTDS_USERNAME=$(cat /var/run/secrets/nais.io/defaultDS/username)
   echo "Setting DEFAULTDS_USERNAME to $DEFAULTDS_USERNAME"
    
fi

if test -f /var/run/secrets/nais.io/defaultDS/password;
then
    export  DEFAULTDS_PASSWORD=$(cat /var/run/secrets/nais.io/defaultDS/password)
    echo "Setting DEFAULTDS_PASSWORD to $DEFAULTDS_PASSWORD"
fi
echo "INIT SCRIPT DONE"
