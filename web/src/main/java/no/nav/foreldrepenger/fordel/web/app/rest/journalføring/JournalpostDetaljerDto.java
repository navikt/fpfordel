package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;


import no.nav.foreldrepenger.mottak.klient.StatusDto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public record JournalpostDetaljerDto(@NotNull String journalpostId,
                                     @NotNull String tittel,
                                     String kanal,
                                     @NotNull @Valid BrukerDto bruker,
                                     @NotNull @Valid AvsenderDto avsender,
                                     @Valid YtelseTypeDto ytelseType,
                                     @NotNull @Valid @Size(min = 1) Set<DokumentDto> dokumenter,
                                     @Valid Set<FagsakDto> fagsaker) {

    public record BrukerDto(@NotNull String navn, @NotNull String fnr, @NotNull String aktørId) {}
    public record AvsenderDto(@NotNull String navn, @NotNull String id) {}
    public record DokumentDto(@NotNull String dokumentId,
                              @NotNull String tittel,
                              @NotNull @Size(min = 1) Set<Variant> varianter,
                              @NotNull String lenke) {}
    public enum Variant { ARKIV, ORIGINAL }
    public record FagsakDto(@NotNull String saksnummer,
                            @NotNull @Valid YtelseTypeDto ytelseType,
                            @NotNull LocalDate datoOpprettet,
                            @NotNull LocalDate sistEndret,
                            @NotNull @Valid StatusDto status) {}

}
