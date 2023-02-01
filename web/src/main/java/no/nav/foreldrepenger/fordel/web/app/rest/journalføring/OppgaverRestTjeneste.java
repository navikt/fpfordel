package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.YtelseType;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.journalføring.OppgaverTjeneste;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.klient.AktørIdDto;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Dokumentvariant;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Unprotected
public class OppgaverRestTjeneste {
    private OppgaverTjeneste oppgaverTjeneste;
    private PersonInformasjon pdl;
    private ArkivTjeneste arkiv;
    private Fagsak fagsak;

    public OppgaverRestTjeneste() {
        // For inject
    }

    @Inject
    public OppgaverRestTjeneste(OppgaverTjeneste oppgaverTjeneste,
                                PersonInformasjon pdl, ArkivTjeneste arkiv, Fagsak fagsak) {
        this.oppgaverTjeneste = oppgaverTjeneste;
        this.pdl = pdl;
        this.arkiv = arkiv;
        this.fagsak = fagsak;
    }

    @GET
    @Path("/oppgaver")
    @Operation(description = "Henter alle åpne journalføringsoppgaver for tema FOR og for saksbehandlers tilhørende enhet.", tags = "Journalføring", responses = {
            @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),
            @ApiResponse(responseCode = "401", description = "Mangler token", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),
            @ApiResponse(responseCode = "403", description = "Mangler tilgang", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class)))
    })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public List<OppgaveDto> hentÅpneOppgaver() throws Exception {
        return oppgaverTjeneste.hentJournalføringsOppgaver().stream()
                .map(this::lagOppgaveDto)
                .toList();
    }

    @GET
    @Path("/detaljer/{journalpost}}")
    @Operation(description = "Henter detaljer for en gitt jornalpostId som er relevante for å kunne ferdigstille journalføring på en fagsak.", tags = "Journlanføring", responses = {
            @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),
            @ApiResponse(responseCode = "401", description = "Mangler token", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),
            @ApiResponse(responseCode = "403", description = "Mangler tilgang", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class)))
    })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public JournalpostDetaljerDto hentJournalpost(@PathParam("journalpost") String journalpostId) {
        return Optional.of(arkiv.hentArkivJournalpost(journalpostId)).map(this::mapTilJournalpostDetaljerDto).orElseThrow();
    }

    private JournalpostDetaljerDto mapTilJournalpostDetaljerDto(ArkivJournalpost journalpost) {
        return new JournalpostDetaljerDto(
                journalpost.getJournalpostId(),
                journalpost.getTittel().orElse(""),
                journalpost.getKanal(),
                journalpost.getBrukerAktørId().map(this::mapBruker).orElse(null),
                new JournalpostDetaljerDto.AvsenderDto(journalpost.getAvsenderNavn(), journalpost.getAvsenderIdent()),
                mapYtelseType(journalpost.getBehandlingstema().utledYtelseType()),
                mapDokumenter(journalpost.getOriginalJournalpost().dokumenter()),
                mapBrukersFagsaker(journalpost.getBrukerAktørId().orElse(null))
        );
    }

    private YtelseTypeDto mapYtelseType(YtelseType ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER -> YtelseTypeDto.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseTypeDto.SVANGERSKAPSPENGER;
            case ENGANGSTØNAD -> YtelseTypeDto.ENGANGSTØNAD;
            case UDEFINERT -> null;
        };
    }

    private Set<JournalpostDetaljerDto.FagsakDto> mapBrukersFagsaker(String aktørId) {
        if (aktørId != null) {
            return fagsak.hentBrukersSaker(new AktørIdDto(aktørId)).stream()
                    .map(sak -> new JournalpostDetaljerDto.FagsakDto(
                            sak.saksnummer().getSaksnummer(),
                            sak.ytelseType(),
                            sak.opprettetDato(),
                            sak.endretDato(),
                            sak.status()
                    ))
                    .collect(Collectors.toSet());
        }
        return null;
    }

    private static Set<JournalpostDetaljerDto.DokumentDto> mapDokumenter(List<DokumentInfo> dokumenter) {
        return dokumenter.stream()
                .map(dok -> new JournalpostDetaljerDto.DokumentDto(
                        dok.dokumentInfoId(),
                        dok.tittel(),
                        dok.dokumentvarianter().stream().map(it -> mapVariant(it.variantformat())).collect(Collectors.toSet()),
                        "/fordel/dokument/hent/1231212"))
                .collect(Collectors.toSet());
    }

    private static JournalpostDetaljerDto.Variant mapVariant(Dokumentvariant.Variantformat format) {
        return switch (format) {
            case ARKIV -> JournalpostDetaljerDto.Variant.ARKIV;
            case ORIGINAL -> JournalpostDetaljerDto.Variant.ORIGINAL;
            default -> null;
        };
    }

    private JournalpostDetaljerDto.BrukerDto mapBruker(String aktørId) {
        if (aktørId != null) {
            var fnr = pdl.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
            var navn = pdl.hentNavn(aktørId);
            return new JournalpostDetaljerDto.BrukerDto(navn, fnr, aktørId);
        }
        return null;
    }


    private OppgaveDto lagOppgaveDto(Oppgave oppgave) {
        return new OppgaveDto(
                oppgave.id(),
                oppgave.journalpostId(),
                oppgave.aktoerId(),
                hentPersonIdent(oppgave.aktoerId()).orElse(null),
                mapTema(oppgave.behandlingstema()),
                oppgave.fristFerdigstillelse(),
                mapPrioritet(oppgave.prioritet()),
                oppgave.beskrivelse(),
                oppgave.aktivDato(),
                harJournalpostMangler(oppgave));
    }

    private OppgavePrioritet mapPrioritet(Prioritet prioritet) {
        return switch (prioritet) {
            case HOY -> OppgavePrioritet.HØY;
            case LAV -> OppgavePrioritet.LAV;
            case NORM -> OppgavePrioritet.NORM;
        };
    }

    private Optional<String> hentPersonIdent(String aktørId) {
        if (aktørId != null) {
            return pdl.hentPersonIdentForAktørId(aktørId);
        }
        return Optional.empty();
    }

    private boolean harJournalpostMangler(Oppgave oppgave) {
        return oppgave.aktoerId() == null;
    }

    private String mapTema(String behandlingstema) {
        var behandlingTemaMappet = BehandlingTema.fraOffisiellKode(behandlingstema);
        return switch(behandlingTemaMappet) {
            case FORELDREPENGER, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FØDSEL -> "Foreldrepenger";
            case SVANGERSKAPSPENGER -> "Svangerskapspenger";
            case ENGANGSSTØNAD, ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_FØDSEL -> "Engangsstønad";
            case UDEFINERT, OMS, OMS_OMSORG, OMS_OPP, OMS_PLEIE_BARN, OMS_PLEIE_BARN_NY, OMS_PLEIE_INSTU -> "Ukjent";
        };
    }

    public record OppgaveDto(@NotNull Long id,
                             @NotNull String journalpostId,
                             String aktørId, String fødselsnummer,
                             @NotNull String ytelseType,
                             @NotNull LocalDate frist,
                             OppgavePrioritet prioritet,
                             String beskrivelse,
                             @NotNull LocalDate opprettetDato,
                             @NotNull boolean journalpostHarMangler) {

    }

    public enum OppgavePrioritet {
        HØY,
        NORM,
        LAV
    }
}
