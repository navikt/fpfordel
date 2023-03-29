package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import javax.validation.constraints.NotNull;

import java.util.List;

public record OppdaterJournalpostMedTittelDto(String journalpostTittel,
                                              List<OppdaterJournalpostMedTittelDto.DokummenterMedTitler> dokumenter) {
    public record DokummenterMedTitler(@NotNull DokumentIdDto dokumentIdDto, @NotNull String tittel) {
    }
}
