package no.nav.foreldrepenger.fordel.web.app.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
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
    public Response vurderJournalpostForOpprettFagsak(@BeanParam @NotNull @Valid SjekkJournalpostRequest request) {
        List<BehandlingTema> aktive = request.getAktivesaker().stream().map(BehandlingTema::fraOffisiellKode).collect(Collectors.toList());
        return Response.ok(arkivTjeneste.kanOppretteSak(request.getJournalpostId(), BehandlingTema.fraOffisiellKode(request.getOppgitt()), aktive)).build();
    }

}
