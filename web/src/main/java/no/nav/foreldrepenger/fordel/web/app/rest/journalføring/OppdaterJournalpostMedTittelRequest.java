package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import java.util.List;

public record OppdaterJournalpostMedTittelRequest(@NotNull @Pattern(regexp = "^(-?[1-9]|[a-z0])[a-z0-9_:-]*$", message = "journalpostId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')") String journalpostId,
                                                  @NotNull List<OppdaterJournalpostMedTittelRequest.OppdaterDokumentRequest> dokumenter) {
    public record OppdaterDokumentRequest(@NotNull DokumentIdDto dokumentIdDto, @NotNull String tittel) {
    }
}
