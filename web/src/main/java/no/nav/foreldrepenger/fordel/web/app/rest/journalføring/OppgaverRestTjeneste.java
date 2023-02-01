package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.journalføring.OppgaverTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Unprotected
public class OppgaverRestTjeneste {
    private OppgaverTjeneste oppgaverTjeneste;
    private PersonInformasjon pdl;

    public OppgaverRestTjeneste() {
        // For inject
    }

    @Inject
    public OppgaverRestTjeneste(OppgaverTjeneste oppgaverTjeneste,
                                PersonInformasjon pdl) {
        this.oppgaverTjeneste = oppgaverTjeneste;
        this.pdl = pdl;
    }

    @GET
    @Path("/oppgaver")
    @Operation(description = "Henter alle åpne journalføringsoppgaver for tema FOR og for saksbehandlers tilhørende enhet.", tags = "GOSYS", responses = {
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

    private OppgaveDto lagOppgaveDto(Oppgave oppgave) {
        return new OppgaveDto(oppgave.id(), oppgave.journalpostId(), oppgave.aktoerId(), hentPersonIdent(oppgave.aktoerId()).orElse(null),
                mapTema(oppgave.behandlingstema()), oppgave.fristFerdigstillelse(), mapPrioritet(oppgave.prioritet()), oppgave.beskrivelse(),
                oppgave.aktivDato(), harJournalpostMangler(oppgave));
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
