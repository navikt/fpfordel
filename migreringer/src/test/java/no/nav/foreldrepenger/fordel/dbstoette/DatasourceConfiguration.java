package no.nav.foreldrepenger.fordel.dbstoette;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import no.nav.vedtak.felles.lokal.dbstoette.DBConnectionProperties;

public enum DatasourceConfiguration {
    UNIT_TEST, DBA;

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

    @SuppressWarnings("resource")
    public List<DBConnectionProperties> get() throws FileNotFoundException {
        String fileName = this.name().toLowerCase() + extension; // NOSONAR
        InputStream io = DatasourceConfiguration.class.getClassLoader().getResourceAsStream(fileName);
        return DBConnectionProperties.fraStream(io);
    }

    @SuppressWarnings("resource")
    public List<DBConnectionProperties> getRaw() throws FileNotFoundException {
        String fileName = this.name().toLowerCase() + extension; // NOSONAR
        InputStream io = DatasourceConfiguration.class.getClassLoader().getResourceAsStream(fileName);
        return DBConnectionProperties.rawFraStream(io);
    }
}
