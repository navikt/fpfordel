FROM navikt/java:17-appdynamics

ENV APPD_NAME=fpfordel
ENV APPD_ENABLED=true
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=appdynamics.adeo.no
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
ENV TZ=Europe/Oslo

RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY fordel-web/target/classes/*.xml /app/

# Application Container (Jetty)
COPY fordel-web/target/app.jar /app/
COPY fordel-web/target/lib/*.jar /app/lib/
COPY export-vault.sh /init-scripts/

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0  -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Europe/Oslo "
