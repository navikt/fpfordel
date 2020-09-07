package no.nav.foreldrepenger.fordel.dbstoette;

import java.io.InputStream;
import java.util.List;

import no.nav.vedtak.felles.lokal.dbstoette.DBConnectionProperties;

public enum DatasourceConfiguration {
    UNIT_TEST,
    DBA;

    private String extension;

    DatasourceConfiguration() {
        this(null);
    }

    DatasourceConfiguration(String extension) {
        if (extension != null) {
            this.extension = extension;
        } else {
            this.extension = ".json";
        }
    }

    public List<DBConnectionProperties> get() {
        String fileName = this.name().toLowerCase() + extension; // NOSONAR
        InputStream io = DatasourceConfiguration.class.getClassLoader().getResourceAsStream(fileName);
        return DBConnectionProperties.fraStream(io);
    }

    public List<DBConnectionProperties> getRaw() {
        String fileName = this.name().toLowerCase() + extension; // NOSONAR
        InputStream io = DatasourceConfiguration.class.getClassLoader().getResourceAsStream(fileName);
        return DBConnectionProperties.rawFraStream(io);
    }
}
