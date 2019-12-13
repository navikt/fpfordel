FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=true

RUN mkdir lib
RUN mkdir conf
RUN mkdir webapp
RUN dir web/target/classes


# Config
COPY web/target/classes/jetty/jaspi-conf.xml conf/

# Application Container (Jetty)
COPY web/target/app.jar .
COPY web/target/lib/*.jar ./

ENV JAVA_OPTS="-Xmx1024m -Xms128m \
    -Djava.security.egd=file:/dev/urandom 
