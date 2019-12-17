#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/defaultDS/username;
then
    echo "Setting DEFAULTDS_USERNAME"
    export  DEFAULTDS_USERNAME=$(cat /var/run/secrets/nais.io/defaultDS/username)
fi

if test -f /var/run/secrets/nais.io/defaultDS/password;
then
    echo "Setting DEFAULTDS_PASSWORD"
    export  DEFAULTDS_PASSWORD=$(/var/run/secrets/nais.io/defaultDS/password)
fi
echo "INIT SCRIPT XXXXXXXXXXX"
