FROM navikt/java:17-appdynamics

ENV APPD_ENABLED=true

RUN mkdir lib
RUN mkdir conf

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/urandom \
    -Duser.timezone=Europe/Oslo \
    -Dlogback.configurationFile=conf/logback.xml"

# Import vault properties
COPY .scripts/03-import-appd.sh /init-scripts/03-import-appd.sh
COPY .scripts/05-import-users.sh /init-scripts/05-import-users.sh

# Config
COPY web/target/classes/logback*.xml conf/

# Application Container (Jetty)
COPY web/target/app.jar .
COPY web/target/lib/*.jar ./

ENV TZ=Europe/Oslo
LABEL org.opencontainers.image.source=https://github.com/navikt/fpfordel
