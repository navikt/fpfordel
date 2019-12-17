FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=true
ENV APP_NAME=fpfordel

RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY web/target/classes/*.xml /app/
COPY web/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Container (Jetty)
COPY web/target/app.jar /app/
COPY web/target/lib/*.jar /app/lib/
# Application Start Command
COPY run-java.sh /
RUN chmod +x /run-java.sh
