package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;

import java.util.List;

public record OppdaterJournalpostMedTittelDto(String journalpostTittel,
                                              List<OppdaterJournalpostMedTittelDto.DokummenterMedTitler> dokumenter) {
    public record DokummenterMedTitler(@NotNull @QueryParam("dokumentId") DokumentIdDto dokumentIdDto, @NotNull String tittel) {
    }
}
