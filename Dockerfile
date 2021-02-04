FROM navikt/java:15-appdynamics

ENV APP_NAME=fpfordel
        
RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY web/target/classes/*.xml /app/
COPY web/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Container (Jetty)
COPY web/target/app.jar /app/
COPY web/target/lib/*.jar /app/lib/
COPY 03-export-vault-secrets.sh /init-scripts/

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0  -Dapp.name=fpfordel -Djava.security.egd=file:/dev/./urandom -D -Duser.timezone=Europe/Oslo --enable-preview "
