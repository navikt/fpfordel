package no.nav.foreldrepenger.fordel.web.app.util;

public final class Environment {

    private final Cluster cluster;
    private final Namespace namespace;

    private Environment(Cluster cluster, Namespace namespace) {
        this.cluster = cluster;
        this.namespace = namespace;
    }

    private static Environment of(Cluster cluster, Namespace namespace) {
        return new Environment(cluster, namespace);
    }

    public static Environment current() {
        return Environment.of(Cluster.current(), Namespace.current());
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public boolean isProd() {
        return cluster.isProd();
    }

    public String clusterName() {
        return cluster.clusterName();
    }

    public String namespace() {
        return namespace.getNamespace();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[cluster=" + cluster + ", namespace=" + namespace + "]";
    }

}
