FROM navikt/java:11-appdynamics

RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY web/target/classes/logback.xml /app/conf/
COPY web/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Container (Jetty)
COPY web/target/app.jar /app/
COPY web/target/lib/*.jar /app/lib/

# Application Start Command
COPY run-java.sh /
RUN chmod +x /run-java.sh

# Upload heapdump to s3
COPY s3upload-init.sh /init-scripts/
COPY s3upload.sh /
RUN chmod +x /s3upload.sh