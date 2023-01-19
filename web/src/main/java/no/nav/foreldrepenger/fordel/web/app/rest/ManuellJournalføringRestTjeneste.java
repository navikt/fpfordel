package no.nav.foreldrepenger.fordel.web.app.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.manuellJournalføring.ManuellJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class ManuellJournalføringRestTjeneste {
    private final ManuellJournalføringTjeneste manuellJournalføringTjeneste;
    private final PersonInformasjon pdl;


    @Inject
    public ManuellJournalføringRestTjeneste(ManuellJournalføringTjeneste manuellJournalføringTjeneste,
                                            PersonInformasjon pdl) {
        this.manuellJournalføringTjeneste = manuellJournalføringTjeneste;
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
        var oppgaver = manuellJournalføringTjeneste.hentJournalføringsOppgaver();
        List<OppgaveDto> journalføringOppgaver = new ArrayList<>();

        if(oppgaver.isEmpty()) {
            return Collections.emptyList();
        }
        oppgaver.forEach(oppgave -> journalføringOppgaver.add(lagOppgaveDto(oppgave)));
        return journalføringOppgaver;
    }

    private OppgaveDto lagOppgaveDto(Oppgave oppgave) {
        return new OppgaveDto(oppgave.id(), oppgave.journalpostId(), hentPersonIdent(oppgave.aktoerId()).orElse(null),
                mapTema(oppgave.behandlingstema()), oppgave.fristFerdigstillelse(), mapPrioritet(oppgave.prioritet()), oppgave.beskrivelse(),
                oppgave.opprettetTidspunkt(), harJournalpostMangler(oppgave));
    }

    private OppgavePrioritet mapPrioritet(Prioritet prioritet) {
        return switch (prioritet) {
            case HOY -> OppgavePrioritet.HOY;
            case LAV -> OppgavePrioritet.LAV;
            case NORM -> OppgavePrioritet.NORM;
        };
    }

    private Optional<String> hentPersonIdent(String aktørId) {
        if (!aktørId.isEmpty()) {
            return pdl.hentPersonIdentForAktørId(aktørId);
        }
        return Optional.empty();
    }

    private boolean harJournalpostMangler(Oppgave oppgave) {
        return oppgave.aktoerId().isEmpty();
    }

    private String mapTema(String behandlingstema) {
        var behandlingTemaMappet = BehandlingTema.fraOffisiellKode(behandlingstema);
        return switch(behandlingTemaMappet) {
            case FORELDREPENGER, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FØDSEL -> "Foreldrepenger";
            case SVANGERSKAPSPENGER -> "Svangerskapspenger";
            case ENGANGSSTØNAD, ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_FØDSEL -> "Engangsstønad";
            case UDEFINERT, OMS, OMS_OMSORG, OMS_OPP, OMS_PLEIE_BARN, OMS_PLEIE_BARN_NY, OMS_PLEIE_INSTU -> "Ukjent"; //skal vi da sette jornalpostHarMangler == true?
        };
    }
}
