FROM ghcr.io/navikt/fp-baseimages/java:21

LABEL org.opencontainers.image.source=https://github.com/navikt/fpfordel
ENV TZ=Europe/Oslo

RUN mkdir lib
RUN mkdir conf

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/urandom \
    -Dlogback.configurationFile=conf/logback.xml"

# Config
COPY web/target/classes/logback*.xml ./conf/

# Application Container (Jetty)
COPY web/target/lib/*.jar ./lib/
COPY web/target/app.jar .
