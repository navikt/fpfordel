package no.nav.foreldrepenger.fordel.kodeverdi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JournalstatusTest {

    @Test
    void sjekker_er_endelig_tilstand() {
        assertTrue(Journalstatus.FEILREGISTRERT.erEndelig());
        assertTrue(Journalstatus.JOURNALFOERT.erEndelig());
        assertTrue(Journalstatus.FERDIGSTILT.erEndelig());
        assertTrue(Journalstatus.EKSPEDERT.erEndelig());
    }

    @Test
    void sjekker_er_ikke_endelig_tilstand() {
        assertFalse(Journalstatus.MOTTATT.erEndelig());
        assertFalse(Journalstatus.UTGAAR.erEndelig());
        assertFalse(Journalstatus.UKJENT.erEndelig());
        assertFalse(Journalstatus.UDEFINERT.erEndelig());
    }
}
