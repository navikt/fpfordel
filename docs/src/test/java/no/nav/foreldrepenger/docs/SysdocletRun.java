package no.nav.foreldrepenger.docs;

import org.junit.Test;

/** Sparker i gang via unit test, pga classpath (fikk ikke riktig med exec-maven-plugin). */
public class SysdocletRun {

    @Test
    public void run_sysdoclet() throws Exception {
        Sysdoclet.main(new String[] { "..", "./generated" });
    }
}
