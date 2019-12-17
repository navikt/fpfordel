#!/usr/bin/env bash

if test -f /secrets/oracle/dbconfig/jdbc_url;
then
    echo "Setting DEFAULTDS_URL"
    export  DEFAULTDS_URL=$(cat /secrets/oracle/dbconfig/jdbc_url)
fi

if test -f /secrets/oracle/credentials/username;
then
    echo "Setting DEFAULTDS_USERNAME"
    export  DEFAULTDS_USERNAME=$(cat /secrets/oracle/credentials/username)
fi

if test -f /secrets/oracle/credentials/password;
then
    echo "Setting DEFAUTLDS_PASSWORD"
    export  DEFAULTDS_PASSWORD=$(cat /secrets/oracle/credentials/password)
fi
