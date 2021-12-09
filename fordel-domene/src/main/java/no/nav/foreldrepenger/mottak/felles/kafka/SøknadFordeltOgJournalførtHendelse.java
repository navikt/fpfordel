package no.nav.foreldrepenger.mottak.felles.kafka;

import java.util.UUID;
public record SøknadFordeltOgJournalførtHendelse(String journalpostId, UUID forsendelseId, String fnr,
            String saksnummer) {
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[journalpostId=" + journalpostId + ", forsendelseId=" + forsendelseId + ", saksnr=" + saksnummer() + "]";
    }

}
