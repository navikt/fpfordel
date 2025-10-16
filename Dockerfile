FROM ghcr.io/navikt/fp-baseimages/chainguard:jre-25
LABEL org.opencontainers.image.source=https://github.com/navikt/fpfordel

# Config
COPY web/target/classes/logback*.xml ./conf/

# Application Container (Jetty)
COPY web/target/lib/*.jar ./lib/
COPY web/target/app.jar .
