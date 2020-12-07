package no.nav.foreldrepenger.fordel.dbstoette;

import no.nav.vedtak.util.env.Environment;

public final class DBTestUtil {
    private static final boolean isRunningUnderMaven = Environment.current().getProperty("maven.cmd.line.args") != null;

    public static boolean kjøresAvMaven() {
        return isRunningUnderMaven;
    }
}
