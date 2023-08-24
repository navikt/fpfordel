package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import java.util.List;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record OppdaterJournalpostMedTittelDto(String journalpostTittel,
                                              List<OppdaterJournalpostMedTittelDto.DokummenterMedTitler> dokumenter) {
    public record DokummenterMedTitler( @NotNull @Digits(integer = 18, fraction = 0) String dokumentId, @NotNull String tittel) {
    }
}
