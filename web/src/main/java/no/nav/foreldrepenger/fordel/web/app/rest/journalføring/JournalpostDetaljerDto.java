package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JournalpostDetaljerDto(@NotNull String journalpostId,
                                     @NotNull String tittel,
                                     @NotNull String behandlingTema,
                                     String kanal,
                                     @Valid BrukerDto bruker,
                                     String tilstand,
                                     @Valid AvsenderDto avsender,
                                     String journalførendeEnhet,
                                     @Valid YtelseTypeDto ytelseType,
                                     String eksisterendeSaksnummer,
                                     @NotNull @Valid @Size(min = 1) Set<DokumentDto> dokumenter,
                                     boolean kanOppretteSak,
                                     @Valid List<SakJournalføringDto> fagsaker) {

    public record BrukerDto(@NotNull String navn, @NotNull String fnr, @NotNull String aktørId) {
    }

    public record AvsenderDto(@NotNull String navn, @NotNull String id) {
    }

    public record DokumentDto(@NotNull String dokumentId, @NotNull String tittel, @NotNull String lenke) {
    }

    public record SakJournalføringDto(@NotNull String saksnummer,
                                      @NotNull @Valid YtelseTypeDto ytelseType,
                                      @NotNull LocalDate opprettetDato,
                                      @NotNull @Valid StatusDto status,
                                      FamilieHendelseJournalføringDto familieHendelseJf,
                                      LocalDate førsteUttaksdato ) {
        record FamilieHendelseJournalføringDto(LocalDate familiehHendelseDato, @Valid FamilihendelseTypeJFDto familihendelseType) {}

        public enum StatusDto {
            @JsonProperty("OPPR")
            OPPRETTET,
            @JsonProperty("UBEH")
            UNDER_BEHANDLING,
            @JsonProperty("LOP")
            LØPENDE,
            @JsonProperty("AVSLU")
            AVSLUTTET
        }
    }


}
