package no.nav.foreldrepenger.fordel.web.server.jetty;

public class JettyWebKonfigurasjon implements AppKonfigurasjon {
    private static final String CONTEXT_PATH = "/fpfordel";

    private Integer serverPort;

    public JettyWebKonfigurasjon() {
    }

    public JettyWebKonfigurasjon(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        if (serverPort == null) {
            return AppKonfigurasjon.DEFAULT_SERVER_PORT;
        }
        return serverPort;
    }

    @Override
    public String getContextPath() {
        return CONTEXT_PATH;
    }

    @Override
    public int getSslPort() {
        throw new IllegalStateException("SSL port should only be used locally");
    }

}
