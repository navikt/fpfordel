package no.nav.foreldrepenger.mottak.felles.kafka;

import static no.nav.foreldrepenger.fordel.StringUtil.partialMask;

import java.util.UUID;
public record SøknadFordeltOgJournalførtHendelse(String journalpostId, UUID forsendelseId, String fnr,
            String saksnummer) {
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[journalpostId=" + journalpostId + ", forsendelseId=" + forsendelseId
                + ", fnr=" + partialMask(fnr) + ", saksnr=" + saksnummer() + "]";
    }

}
