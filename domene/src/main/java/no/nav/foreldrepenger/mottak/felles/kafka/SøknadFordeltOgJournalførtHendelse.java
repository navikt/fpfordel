package no.nav.foreldrepenger.mottak.felles.kafka;

import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.fordel.StringUtil;

public class SøknadFordeltOgJournalførtHendelse {
    private final String journalpostId;
    private final UUID forsendelseId;
    private final String fnr;
    private final String saksnr;

    public SøknadFordeltOgJournalførtHendelse(String journalpostId, UUID forsendelseId, String fnr,
            Optional<String> saksnummer) {
        this.journalpostId = journalpostId;
        this.forsendelseId = forsendelseId;
        this.saksnr = saksnummer.isPresent() ? saksnummer.get() : null;
        this.fnr = fnr;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    public String getFnr() {
        return fnr;
    }

    public String getSaksnr() {
        return saksnr;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[journalpostId=" + journalpostId + ", forsendelseId=" + forsendelseId
                + ", fnr=" + StringUtil.partialMask(fnr) + ", saksnr=" + saksnr + "]";
    }

}
