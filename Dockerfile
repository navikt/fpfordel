FROM navikt/java:15-appdynamics

ENV APPD_ENABLED=true
ENV APP_NAME=fpfordel
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=appdynamics.adeo.no
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
        
ENV JAVA_OPTS --enable-preview

RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY web/target/classes/*.xml /app/
COPY web/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Container (Jetty)
COPY web/target/app.jar /app/
COPY web/target/lib/*.jar /app/lib/
COPY 03-export-vault-secrets.sh /init-scripts/
RUN chmod +x /init-scripts/*


# Application Start Command
COPY run-java.sh /
RUN chmod +x /run-java.sh
