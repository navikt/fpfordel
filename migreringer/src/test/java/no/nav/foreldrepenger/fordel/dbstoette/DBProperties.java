package no.nav.foreldrepenger.fordel.dbstoette;

public final class DBProperties {

    private String datasource;
    private String schema;
    private String defaultSchema;
    private String url;
    private String user;
    private String password;

    private String migrationScriptsFilesystemRoot;
    private String migrationScriptsClasspathRoot;

    private String versjonstabell;
    private boolean defaultDataSource;
    private boolean migrateClean;

    private DBProperties() {
    }

    private DBProperties(Builder builder) {
        this.datasource = builder.datasource;
        this.schema = builder.schema;
        this.defaultSchema = builder.defaultSchema;
        this.url = builder.url;
        this.user = builder.user;
        this.password = builder.password;
        this.migrationScriptsFilesystemRoot = builder.migrationScriptsFilesystemRoot;
        this.migrationScriptsClasspathRoot = builder.migrationScriptsClasspathRoot;
        this.versjonstabell = builder.versjonstabell;
        this.defaultDataSource = builder.defaultDataSource;
        this.migrateClean = builder.migrateClean;
    }

    String getDatasource() {
        return datasource;
    }

    public String getSchema() {
        return schema;
    }

    String getUrl() {
        return url;
    }

    String getUser() {
        return user;
    }

    String getPassword() {
        return password;
    }

    String getMigrationScriptsFilesystemRoot() {
        return migrationScriptsFilesystemRoot;
    }

    String getMigrationScriptsClasspathRoot() {
        return migrationScriptsClasspathRoot;
    }

    String getVersjonstabell() {
        return versjonstabell;
    }

    boolean isDefaultDataSource() {
        return defaultDataSource;
    }

    boolean isMigrateClean() {
        return migrateClean;
    }

    String getDefaultSchema() {
        return defaultSchema;
    }

    static class Builder {
        private String datasource;
        private String schema;
        private String defaultSchema;
        private String url;
        private String user;
        private String password;
        private String migrationScriptsFilesystemRoot;
        private String migrationScriptsClasspathRoot;
        private String versjonstabell;
        private boolean defaultDataSource;
        private boolean migrateClean;

        Builder datasource(String datasource) {
            this.datasource = datasource;
            return this;
        }

        Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        Builder defaultSchema(String defaultSchema) {
            this.defaultSchema = defaultSchema;
            return this;
        }

        Builder url(String url) {
            this.url = url;
            return this;
        }

        Builder user(String user) {
            this.user = user;
            return this;
        }

        Builder password(String password) {
            this.password = password;
            return this;
        }

        Builder migrationScriptsFilesystemRoot(String migrationScriptsFilesystemRoot) {
            this.migrationScriptsFilesystemRoot = migrationScriptsFilesystemRoot;
            return this;
        }

        Builder migrationScriptsClasspathRoot(String migrationScriptsClasspathRoot) {
            this.migrationScriptsClasspathRoot = migrationScriptsClasspathRoot;
            return this;
        }

        Builder versjonstabell(String versjonstabell) {
            this.versjonstabell = versjonstabell;
            return this;
        }

        Builder defaultDataSource(boolean defaultDataSource) {
            this.defaultDataSource = defaultDataSource;
            return this;
        }

        Builder migrateClean(boolean migrateClean) {
            this.migrateClean = migrateClean;
            return this;
        }

        DBProperties build() {
            return new DBProperties(this);
        }
    }
}
