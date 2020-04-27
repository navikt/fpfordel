package no.nav.foreldrepenger.fordel.web.app.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/vurdering")
@RequestScoped
@Transactional
public class FagsakFraJournalpostRestTjeneste {

    private ArkivTjeneste arkivTjeneste;

    public FagsakFraJournalpostRestTjeneste() {
        // CDI
    }

    @Inject
    public FagsakFraJournalpostRestTjeneste(ArkivTjeneste arkivTjeneste) {
        this.arkivTjeneste = arkivTjeneste;
    }

    @GET
    @Path("/kanopprettesak")
    @Produces(APPLICATION_JSON)
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @Operation(description = "Vurder om det bør opprettes fagsak fra journalpost", tags = "Vurdering", responses = {
            @ApiResponse(responseCode = "200", description = "Svar"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
    })
    public Response vurderJournalpostForOpprettFagsak(
            @NotNull @QueryParam("journalpostId") @Parameter(name = "journalpostId") @Valid AbacJournalpostIdDto journalpostIdDto) {
        return Response.ok(arkivTjeneste.kanOppretteSak(journalpostIdDto.getJournalpostId())).build();
    }

    public static class AbacJournalpostIdDto extends JournalpostIdDto implements AbacDto {
        public AbacJournalpostIdDto() {
            super();
        }

        public AbacJournalpostIdDto(String journalpostId) {
            super(journalpostId);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            // Er ikke tilknyttet fagsak. Settingen er å vurdere om det kan opprettes fagsak fra Gosys
            return AbacDataAttributter.opprett();
        }
    }

}
