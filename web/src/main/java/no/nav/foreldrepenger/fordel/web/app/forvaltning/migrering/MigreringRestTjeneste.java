package no.nav.foreldrepenger.fordel.web.app.forvaltning.migrering;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path("/forvaltning/migrering")
@RequestScoped
@Transactional
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class MigreringRestTjeneste {

    private DokumentRepository dokumentRepository;
    private OppgaveRepository oppgaveRepository;

    private Validator validator;

    public MigreringRestTjeneste() {
        // CDI
    }

    @Inject
    public MigreringRestTjeneste(DokumentRepository dokumentRepository, OppgaveRepository oppgaveRepository) {
        this.dokumentRepository = dokumentRepository;
        this.oppgaveRepository = oppgaveRepository;
        @SuppressWarnings("resource") var factory = Validation.buildDefaultValidatorFactory();
        // hibernate validator implementations er thread-safe, trenger ikke close
        validator = factory.getValidator();
    }

    @GET
    @Operation(description = "Leser ut oppgaver som skal migreres", tags = "Forvaltning",
        summary = ("Leser ut oppgaver som skal migreres"),
        responses = {@ApiResponse(responseCode = "200", description = "Oppgaver")})
    @Path("/lesOppgaver")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT)
    public Response lesOppgaver() {
        var oppgaver = oppgaveRepository.hentAlleÅpneOppgaver().stream()
            .map(MigreringMapper::tilOppgaveDto)
            .toList();
        var respons = new MigreringOppgaveDto(oppgaver);
        var violations = validator.validate(respons);
        if (!violations.isEmpty()) {
            var allErrors = violations.stream().map(it -> it.getPropertyPath().toString() + " :: " + it.getMessage()).toList();
            throw new IllegalArgumentException("Valideringsfeil; " + allErrors);
        }
        return Response.ok(respons).build();
    }

    @POST
    @Operation(description = "Sammenligner oppgaver som skal migreres", tags = "Forvaltning",
        summary = ("Sammenligner oppgaver som skal migreres"),
        responses = {@ApiResponse(responseCode = "200", description = "Oppgaver")})
    @Path("/sammenlignOppgaver")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT)
    public Response sammenlignOppgaver(@TilpassetAbacAttributt(supplierClass = MigreringAbacSupplier.class)
                                       @NotNull @Parameter(name = "oppgaver") @Valid MigreringOppgaveDto oppgaver) {
        var rmap = oppgaver.oppgaver().stream()
            .map(MigreringMapper::fraOppgaveDto)
            .collect(Collectors.toList());
        var lokale = oppgaveRepository.hentAlleÅpneOppgaver().stream()
            .map(MigreringMapper::tilOppgaveDto)
            .collect(Collectors.toSet());
        var remote = new HashSet<>(oppgaver.oppgaver());
        return
            lokale.size() == remote.size() && lokale.containsAll(remote) ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Operation(description = "Lagrer oppgaver som skal migreres", tags = "Forvaltning",
        summary = ("Lagre oppgaver som skal migreres"),
        responses = {@ApiResponse(responseCode = "200", description = "Oppgaver")})
    @Path("/lagreOppgaver")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT)
    public Response lagreOppgaver(@TilpassetAbacAttributt(supplierClass = MigreringAbacSupplier.class)
                                  @NotNull @Parameter(name = "oppgaver") @Valid MigreringOppgaveDto oppgaver) {
        oppgaver.oppgaver().stream()
            .map(MigreringMapper::fraOppgaveDto)
            .forEach(oppgaveRepository::lagre);
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Lagre lokale journalposter som skal migreres", tags = "Forvaltning",
        summary = ("Lagre lokale journalposter som skal migreres"),
        responses = {@ApiResponse(responseCode = "200", description = "Journalposter")})
    @Path("/lagreJournal")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT)
    public Response lagreJournal(@TilpassetAbacAttributt(supplierClass = MigreringAbacSupplier.class)
                                 @NotNull @Parameter(name = "journalposter") @Valid MigreringJournalpostDto journalposter) {
        journalposter.journalposter().stream()
            .map(MigreringMapper::fraJournalpostDto)
            .forEach(dokumentRepository::lagre);
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Sammenligne lokale journalposter som skal migreres", tags = "Forvaltning",
        summary = ("Sammenligne lokale journalposter som skal migreres"),
        responses = {@ApiResponse(responseCode = "200", description = "Journalposter")})
    @Path("/sammenlignJournal")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT)
    public Response sammenlignJournal(@TilpassetAbacAttributt(supplierClass = MigreringAbacSupplier.class)
                                      @NotNull @Parameter(name = "journalposter") @Valid MigreringJournalpostDto journalposter) {
        var rmap = journalposter.journalposter().stream()
            .map(MigreringMapper::fraJournalpostDto)
            .collect(Collectors.toList());
        var lokale = dokumentRepository.hentAlleJournalposter().stream()
            .map(MigreringMapper::tilJournalpostDto)
            .collect(Collectors.toSet());
        var remote = new HashSet<>(journalposter.journalposter());
        return
            lokale.size() == remote.size() && lokale.containsAll(remote) ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Operation(description = "Leser ut lokale journalposter som skal migreres", tags = "Forvaltning",
        summary = ("Leser ut lokale journalposter som skal migreres"),
        responses = {@ApiResponse(responseCode = "200", description = "Journalposter")})
    @Path("/lesJournal")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT)
    public Response lesJournal() {
        var journalposter = dokumentRepository.hentAlleJournalposter().stream()
            .map(MigreringMapper::tilJournalpostDto)
            .toList();
        var respons = new MigreringJournalpostDto(journalposter);
        var violations = validator.validate(respons);
        if (!violations.isEmpty()) {
            var allErrors = violations.stream().map(it -> it.getPropertyPath().toString() + " :: " + it.getMessage()).toList();
            throw new IllegalArgumentException("Valideringsfeil; " + allErrors);
        }
        return Response.ok(respons).build();
    }


    public static class MigreringAbacSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }
}
