FROM ghcr.io/navikt/fp-baseimages/java:17-appdynamics

LABEL org.opencontainers.image.source=https://github.com/navikt/fpfordel
ENV TZ=Europe/Oslo

RUN mkdir lib
RUN mkdir conf

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
    -Djava.security.egd=file:/dev/urandom \
    -Duser.timezone=Europe/Oslo \
    -Dlogback.configurationFile=conf/logback.xml"

# Config
COPY web/target/classes/logback*.xml ./conf/

# Application Container (Jetty)
COPY web/target/lib/*.jar ./lib/
COPY web/target/app.jar .
