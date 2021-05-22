package no.nav.foreldrepenger.fordel.web.app.forvaltning;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.web.server.abac.BeskyttetRessursAttributt;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.task.VedlikeholdSchedulerTask;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/forvaltning")
@RequestScoped
@Transactional
@Unprotected // Endres til Protected når DokumentforsendelseRestTjeneste gjør det samme
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class ForvaltningRestTjeneste {

    private ProsessTaskRepository repo;
    private Fagsak fagsak;

    public ForvaltningRestTjeneste() {
        // CDI
    }

    @Inject
    public ForvaltningRestTjeneste(ProsessTaskRepository prosessTaskRepository, Fagsak fagsak) {
        this.repo = prosessTaskRepository;
        this.fagsak = fagsak;
    }

    @POST
    @Operation(description = "Setter nytt suffix for retry journalføring", tags = "Forvaltning", summary = ("Setter parametere før retry av task"), responses =

    { @ApiResponse(responseCode = "200", description = "Nytt suffix satt") })

    @Path("/retry-suffix")
    @BeskyttetRessurs(action = CREATE, resource = BeskyttetRessursAttributt.DRIFT)
    public Response setRetrySuffix(
            @Parameter(description = "Sett kanalreferanse-suffix før restart prosesstask") @NotNull @Valid RetryTaskKanalrefDto dto) {
        var data = repo.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setProperty(RETRY_KEY, dto.getRetrySuffix());
        repo.lagre(data);
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Send inntektsmelding til angitt sak (allerede journalført)", tags = "Forvaltning", summary = "Bruker eksisterende task til å sende dokument til VL", responses =

    { @ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL") })

    @Path("/submit-journalfort-im")
    @BeskyttetRessurs(action = CREATE, resource = BeskyttetRessursAttributt.DRIFT)
    public Response submitJournalførtInntektsmelding(
            @Parameter(description = "Send im til angitt sak") @NotNull @Valid SubmitJfortIMDto dto) {
        var data = repo.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setCallIdFraEksisterende();
        var fra = new MottakMeldingDataWrapper(data);
        var til = fra.nesteSteg(VLKlargjørerTask.TASKNAME);
        til.setSaksnummer(dto.getSaksnummerDto().getSaksnummer());
        til.setArkivId(dto.getJournalpostIdDto().getJournalpostId());
        til.setRetryingTask(VLKlargjørerTask.REINNSEND);
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(dto.getSaksnummerDto(), dto.getJournalpostIdDto()));
        repo.lagre(til.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Send journalpost til angitt sak (ikke journalført)", tags = "Forvaltning", summary = ("Bruker eksisterende task til å sende dokument til VL"), responses = {
            @ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL") })
    @Path("/submit-journalforing-endelig")
    @BeskyttetRessurs(action = CREATE, resource = BeskyttetRessursAttributt.DRIFT)
    public Response submitJournalpostEndeligKlargjor(
            @Parameter(description = "Send im til angitt sak") @NotNull @Valid SubmitJfortIMDto dto) {
        var data = repo.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setCallIdFraEksisterende();
        var fra = new MottakMeldingDataWrapper(data);
        if (!fra.getArkivId().equals(dto.getJournalpostIdDto().getJournalpostId())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        var til = fra.nesteSteg(TilJournalføringTask.TASKNAME);
        til.setSaksnummer(dto.getSaksnummerDto().getSaksnummer());
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(dto.getSaksnummerDto(), dto.getJournalpostIdDto()));
        repo.lagre(til.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Path("/autorun")
    @Operation(description = "Start task for å kjøre batchjobs", tags = "Forvaltning", responses = {
            @ApiResponse(responseCode = "200", description = "Starter batch-scheduler."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = CREATE, resource = BeskyttetRessursAttributt.DRIFT)
    public Response autoRunBatch() {
        boolean eksisterende = repo.finnIkkeStartet()
                .stream()
                .map(ProsessTaskData::getTaskType)
                .anyMatch(VedlikeholdSchedulerTask.TASKTYPE::equals);
        if (!eksisterende) {
            repo.lagre(new ProsessTaskData(VedlikeholdSchedulerTask.TASKTYPE));
        }
        return Response.ok().build();
    }

    @POST
    @Path("/retryAlleTasks")
    @Operation(description = "Restarter alle prosesstask med status FEILET.", summary = "Dette endepunktet vil tvinge feilede tasks til å trigge ett forsøk uavhengig av maks antall forsøk", tags = "Forvaltning", responses = {
            @ApiResponse(responseCode = "200", description = "Response med liste av prosesstasks som restartes"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")
    })
    @BeskyttetRessurs(action = CREATE, resource = BeskyttetRessursAttributt.DRIFT)
    public Response retryAlleProsessTasks() {

        var ptdList = this.repo.finnAlle(ProsessTaskStatus.FEILET);
        if (ptdList.isEmpty()) {
            return Response.ok().build();
        }
        VedlikeholdSchedulerTask.resetTilStatusKlar(ptdList,
                tasktype -> repo.finnProsessTaskType(tasktype).map(ProsessTaskTypeInfo::getMaksForsøk).orElse(1));
        ptdList.forEach(repo::lagre);
        return Response.ok().build();
    }

    @POST
    @Path("/setTaskFerdig")
    @Operation(description = "Setter feilet prosesstask med angitt prosesstask-id til FERDIG (kjøres ikke)", tags = "Forvaltning", responses = {
            @ApiResponse(responseCode = "200", description = "Angitt prosesstask-id satt til status FERDIG"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")
    })
    @BeskyttetRessurs(action = CREATE, resource = BeskyttetRessursAttributt.DRIFT)
    public Response setFeiletTaskFerdig(@Parameter(description = "Prosesstask-id for feilet prosesstask") @NotNull @Valid RetryTaskKanalrefDto dto) {
        var taskData = repo.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if ((taskData == null) || !taskData.getStatus().getDbKode().equals(dto.getRetrySuffix())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        taskData.setStatus(ProsessTaskStatus.KJOERT);
        taskData.setSisteFeil(null);
        taskData.setSisteFeilKode(null);
        repo.lagre(taskData);
        return Response.ok().build();
    }

}
