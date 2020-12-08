package no.nav.foreldrepenger.fordel.web.server.jetty;

import javax.sql.DataSource;

final class DBConnProp {

    private static final String MIGRATIONS_LOCATION = "classpath:/db/migration/";

    private final DataSource datasource;
    private final String migrationScripts;
    private final String dsName;

    public DBConnProp(DataSource datasource, String dsName) {
        this.datasource = datasource;
        this.dsName = dsName;
        this.migrationScripts = MIGRATIONS_LOCATION + dsName;
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public String getLocations() {
        return migrationScripts;
    }

    @Override
    public String toString() {
        return "DBConnProp{" + "datasource=" + datasource + ", migrationScripts='" + migrationScripts + '\''
            + ", dsName='" + dsName + '\'' + '}';
    }
}
