package no.nav.foreldrepenger.fordel.web.app.forvaltning;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.web.app.rest.DokumentforsendelseRestTjeneste;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.task.RekjørFeiledeTasksBatchTask;
import no.nav.foreldrepenger.mottak.task.SlettForsendelseTask;
import no.nav.foreldrepenger.mottak.task.SlettGamleTasksBatchTask;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.task.dokumentforsendelse.BehandleDokumentforsendelseTask;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseIdDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskDataBuilder;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path("/forvaltning")
@RequestScoped
@Transactional
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class ForvaltningRestTjeneste {

    private ProsessTaskTjeneste taskTjeneste;
    private Fagsak fagsak;

    public ForvaltningRestTjeneste() {
        // CDI
    }

    @Inject
    public ForvaltningRestTjeneste(ProsessTaskTjeneste taskTjeneste, Fagsak fagsak) {
        this.taskTjeneste = taskTjeneste;
        this.fagsak = fagsak;
    }

    @POST
    @Operation(description = "Setter nytt suffix for retry journalføring", tags = "Forvaltning", summary = ("Setter parametere før retry av task"), responses =

        {@ApiResponse(responseCode = "200", description = "Nytt suffix satt")})

    @Path("/retry-suffix")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT)
    public Response setRetrySuffix(@Parameter(description = "Sett kanalreferanse-suffix før restart prosesstask") @NotNull @Valid RetryTaskKanalrefDto dto) {
        var data = taskTjeneste.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setProperty(RETRY_KEY, dto.getRetrySuffix());
        taskTjeneste.lagre(data);
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Send inntektsmelding til angitt sak (allerede journalført)", tags = "Forvaltning", summary = "Bruker eksisterende task til å sende dokument til VL", responses =

        {@ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL")})

    @Path("/submit-journalfort-im")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT)
    public Response submitJournalførtInntektsmelding(@Parameter(description = "Send im til angitt sak") @NotNull @Valid SubmitJfortIMDto dto) {
        var data = taskTjeneste.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setCallIdFraEksisterende();
        var fra = new MottakMeldingDataWrapper(data);
        var til = fra.nesteSteg(TaskType.forProsessTask(VLKlargjørerTask.class));
        til.setSaksnummer(dto.getSaksnummerDto().getSaksnummer());
        til.setArkivId(dto.getJournalpostIdDto().getJournalpostId());
        til.setRetryingTask(VLKlargjørerTask.REINNSEND);
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(dto.getSaksnummerDto(), dto.getJournalpostIdDto()));
        taskTjeneste.lagre(til.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Send journalpost til angitt sak (ikke journalført)", tags = "Forvaltning", summary = ("Bruker eksisterende task til å sende dokument til VL"), responses = {@ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL")})
    @Path("/submit-journalforing-endelig")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT)
    public Response submitJournalpostEndeligKlargjor(@Parameter(description = "Send im til angitt sak") @NotNull @Valid SubmitJfortIMDto dto) {
        var data = taskTjeneste.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setCallIdFraEksisterende();
        var fra = new MottakMeldingDataWrapper(data);
        if (!fra.getArkivId().equals(dto.getJournalpostIdDto().getJournalpostId())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        var til = fra.nesteSteg(TaskType.forProsessTask(TilJournalføringTask.class));
        til.setSaksnummer(dto.getSaksnummerDto().getSaksnummer());
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(dto.getSaksnummerDto(), dto.getJournalpostIdDto()));
        taskTjeneste.lagre(til.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Path("/autorun")
    @Operation(description = "Start task for å kjøre batchjobs", tags = "Forvaltning", responses = {@ApiResponse(responseCode = "200", description = "Starter batch-scheduler."), @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT)
    public Response autoRunBatch() {
        taskTjeneste.lagre(ProsessTaskData.forProsessTask(RekjørFeiledeTasksBatchTask.class));
        taskTjeneste.lagre(ProsessTaskData.forProsessTask(SlettGamleTasksBatchTask.class));
        return Response.ok().build();
    }

    @POST
    @Path("/searchTasks")
    @Operation(description = "Søker etter journalpostId mv i taskparametre innen angitt tidsrom", tags = "Forvaltning", responses = {@ApiResponse(responseCode = "200", description = "Angitt prosesstask-id satt til status FERDIG"), @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT)
    public Response searchTasks(@Parameter(description = "Søkefilter") @NotNull @Valid FordelSokeFilterDto dto) {
        var tasks = taskTjeneste.finnAlleMedParameterTekst(dto.getTekst(), dto.getOpprettetFraOgMed(), dto.getOpprettetTilOgMed());
        return Response.ok(tasks).build();
    }

    @POST
    @Path("/taskForBehandleForsendelse")
    @Operation(description = "Behandler forsendelse som ikke er plukket opp", tags = "Forvaltning", responses = {@ApiResponse(responseCode = "200", description = "Opprettet prosesstask"), @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT)
    public Response taskForBehandleForsendelse(@TilpassetAbacAttributt(supplierClass = DokumentforsendelseRestTjeneste.ForsendelseAbacDataSupplier.class) @NotNull @QueryParam("forsendelseId") @Parameter(name = "forsendelseId") @Valid ForsendelseIdDto forsendelseIdDto) {
        var builder = ProsessTaskDataBuilder.forProsessTask(BehandleDokumentforsendelseTask.class)
            .medCallId(forsendelseIdDto.forsendelseId().toString())
            .medProperty(FORSENDELSE_ID_KEY, forsendelseIdDto.forsendelseId().toString());

        taskTjeneste.lagre(builder.build());

        return Response.ok().build();
    }

    @POST
    @Path("/taskForSlettForsendelse")
    @Operation(description = "Sletter forsendelse som ikke skal behandles videre", tags = "Forvaltning", responses = {@ApiResponse(responseCode = "200", description = "Opprettet prosesstask"), @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT)
    public Response taskForSlettForsendelse(@TilpassetAbacAttributt(supplierClass = DokumentforsendelseRestTjeneste.ForsendelseAbacDataSupplier.class) @NotNull @QueryParam("forsendelseId") @Parameter(name = "forsendelseId") @Valid ForsendelseIdDto forsendelseIdDto) {

        var builder = ProsessTaskDataBuilder.forProsessTask(SlettForsendelseTask.class)
            .medCallId(forsendelseIdDto.forsendelseId().toString())
            .medProperty(FORSENDELSE_ID_KEY, forsendelseIdDto.forsendelseId().toString())
            .medProperty(SlettForsendelseTask.FORCE_SLETT_KEY, "forvaltning");

        taskTjeneste.lagre(builder.build());

        return Response.ok().build();
    }

    @POST
    @Operation(description = "Send inntektsmelding til angitt sak (allerede journalført)", tags = "Forvaltning", summary = "Bruker eksisterende task til å sende dokument til VL", responses =

        {@ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL")})

    @Path("/fiks-arkiv-feil")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT)
    public Response fiksarkivFeil(@Parameter(description = "Arkivfeil") @NotNull @Valid SubmitJfortIMDto dto) {
        var data = taskTjeneste.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setCallIdFraEksisterende();
        var fra = new MottakMeldingDataWrapper(data);
        fra.setSaksnummer(dto.getSaksnummerDto().getSaksnummer());
        fra.setArkivId(dto.getJournalpostIdDto().getJournalpostId());
        taskTjeneste.lagre(fra.getProsessTaskData());
        return Response.ok().build();
    }

}
