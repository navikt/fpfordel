package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;


import java.time.LocalDate;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.mottak.klient.StatusDto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;

public record JournalpostDetaljerDto(@NotNull String journalpostId, @NotNull String tittel, @NotNull String behandlingTema, String kanal,
                                     @Valid BrukerDto bruker, @Valid AvsenderDto avsender, @Valid YtelseTypeDto ytelseType,
                                     @NotNull @Valid @Size(min = 1) Set<DokumentDto> dokumenter, @Valid Set<SakDto> fagsaker) {

    public record BrukerDto(@NotNull String navn, @NotNull String fnr, @NotNull String aktørId) {
    }

    public record AvsenderDto(@NotNull String navn, @NotNull String id) {
    }

    public record DokumentDto(@NotNull String dokumentId, @NotNull String tittel, @NotNull String lenke) {
    }

    public record SakDto(@NotNull String saksnummer, @NotNull @Valid YtelseTypeDto ytelseType, @NotNull LocalDate datoOpprettet, LocalDate sistEndret, @NotNull @Valid StatusDto status, LocalDate gjeldendeFamiliehendelseDato) {
    }

}
