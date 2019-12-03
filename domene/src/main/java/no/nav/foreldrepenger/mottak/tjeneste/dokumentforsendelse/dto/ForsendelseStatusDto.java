package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto;

import java.time.Duration;
import java.util.Optional;

import javax.validation.constraints.NotNull;

public class ForsendelseStatusDto {
    @NotNull
    private final ForsendelseStatus forsendelseStatus;

    private Duration pollInterval;
    private String journalpostId;
    private String saksnummer;

    public ForsendelseStatusDto(ForsendelseStatus forsendelseStatus) {
        this.forsendelseStatus = forsendelseStatus;
    }

    public ForsendelseStatus getForsendelseStatus() {
        return forsendelseStatus;
    }

    public Optional<String> getJournalpostId() {
        return Optional.ofNullable(journalpostId);
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(saksnummer);
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Optional<String> getPollInterval() {
        return pollInterval != null
                ? Optional.of(pollInterval.toString())
                : Optional.empty();
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }
}
