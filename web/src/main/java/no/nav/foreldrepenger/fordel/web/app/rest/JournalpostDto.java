package no.nav.foreldrepenger.fordel.web.app.rest;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

public record JournalpostDto(@NotNull String journalpostId,
                             @NotNull String tittel,
                             Kanal kanal,
                             @NotNull BrukerDto bruker,
                             @NotNull AvsenderDto avsender,
                             YtelseType ytelseType,
                             Set<DokumentDto> dokumenter,
                             Set<FagsakDto> fagsaker) {

    public record BrukerDto(@NotNull String navn, @NotNull String fnr, @NotNull String aktørId) {}
    public record AvsenderDto(@NotNull String navn, @NotNull String id) {}
    public enum Kanal { SELVBETJENING, ALLTIN, EESSI, EIA, HELSENETT, SKAN }
    public enum YtelseType { EN, FP, SVP }
    public record DokumentDto(@NotNull String dokumentId,
                              @NotNull String tittel,
                              @NotNull Set<Variant> varianter,
                              @NotNull String lenke) {}
    public enum Variant { ARKIV, ORIGINAL }
    public record FagsakDto(@NotNull String saksnummer,
                            @NotNull YtelseType ytelseType,
                            @NotNull LocalDate datoOpprettet,
                            @NotNull LocalDate sistEndret,
                            @NotNull FagsakStatus status) {}

    public enum FagsakStatus { OPPRETTET, UNDER_BEHANDLING, LOEPENDE, AVSLUTTET }
}
