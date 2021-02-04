FROM navikt/java:15-appdynamics

ENV APPD_ENABLED=true
ENV APP_NAME=fpfordel
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=appdynamics.adeo.no
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
        

RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY web/target/classes/*.xml /app/
COPY web/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Container (Jetty)
COPY web/target/app.jar /app/
COPY web/target/lib/*.jar /app/lib/
COPY 03-export-vault-secrets.sh /init-scripts/

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Europe/Oslo --enable-preview "
