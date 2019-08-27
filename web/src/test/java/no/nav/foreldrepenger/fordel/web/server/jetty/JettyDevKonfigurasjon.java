package no.nav.foreldrepenger.fordel.web.server.jetty;

public class JettyDevKonfigurasjon extends JettyWebKonfigurasjon {
    private static final int SSL_SERVER_PORT = DEFAULT_SERVER_PORT - 1;

    @Override
    public int getSslPort() {
        return SSL_SERVER_PORT;
    }

}
