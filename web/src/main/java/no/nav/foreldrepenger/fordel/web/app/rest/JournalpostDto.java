package no.nav.foreldrepenger.fordel.web.app.rest;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

record JournalpostDto(@NotNull String journalpostId,
                      @NotNull String tittel,
                      Kanal kanal,
                      @NotNull BrukerDto bruker,
                      @NotNull AvsenderDto avsender,
                      YtelseType ytelseType,
                      Set<DokumentDto> dokumenter,
                      Set<FagsakDto> fagsaker) {

    record BrukerDto(@NotNull String navn, @NotNull String fnr, @NotNull String aktørId) {}
    record AvsenderDto(@NotNull String navn, @NotNull String id) {}
    enum Kanal { SELVBETJENING, ALLTIN, EESSI, EIA, HELSENETT, SKAN }
    enum YtelseType { EN, FP, SVP }
    record DokumentDto(@NotNull String dokumentId,
                       @NotNull String tittel,
                       @NotNull Set<Variant> varianter,
                       @NotNull String lenke) {}
    enum Variant { ARKIV, ORIGINAL }
    record FagsakDto(@NotNull String saksnummer,
                     @NotNull YtelseType ytelseType,
                     @NotNull LocalDate datoOpprettet,
                     @NotNull LocalDate sistEndret,
                     @NotNull FagsakStatus status) {}

    enum FagsakStatus { OPPRETTET, UNDER_BEHANDLING, LOEPENDE, AVSLUTTET }
}
