#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/defaultDSconfig/jdbc_url;
then
   export  DEFAULTDS_URL=$(cat /var/run/secrets/nais.io/defaultDSconfig/jdbc_url)
   echo "Setter DEFAULTDS_URL til $DEFAULTDS_URL"
fi

if test -f /var/run/secrets/nais.io/defaultDS/username;
then
   export  DEFAULTDS_USERNAME=$(cat /var/run/secrets/nais.io/defaultDS/username)
   echo "Setter DEFAULTDS_USERNAME til $DEFAULTDS_USERNAME"
fi

if test -f /var/run/secrets/nais.io/defaultDS/password;
then
   export  DEFAULTDS_PASSWORD=$(cat /var/run/secrets/nais.io/defaultDS/password)
   echo "Setter DEFAULTDS_PASSWORD"
fi

if test -f /var/run/secrets/nais.io/serviceuser/username;
then
   export  SYSTEMBRUKER_USERNAME=$(cat /var/run/secrets/nais.io/serviceuser/username)
   echo "Setter SYSTEMBRUKER_USERNAME til $SYSTEMBRUKER_USERNAME"
fi

if test -f /var/run/secrets/nais.io/serviceuser/password;
then
   export  SYSTEMBRUKER_PASSWORD=$(cat /var/run/secrets/nais.io/serviceuser/password)
   echo "Setter SYSTEMBRUKER_PASSWORD"
fi
