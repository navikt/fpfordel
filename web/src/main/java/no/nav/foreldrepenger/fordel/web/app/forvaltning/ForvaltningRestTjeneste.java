package no.nav.foreldrepenger.fordel.web.app.forvaltning;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.DRIFT;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.task.KlargjorForVLTask;
import no.nav.foreldrepenger.mottak.task.MidlJournalføringTask;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.task.VedlikeholdSchedulerTask;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskIdDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/forvaltning")
@RequestScoped
@Transaction
public class ForvaltningRestTjeneste {

    private ProsessTaskRepository prosessTaskRepository;
    private KodeverkRepository kodeverkRepository;
    FagsakRestKlient fagsakRestKlient;

    public ForvaltningRestTjeneste() {
        // CDI
    }

    @Inject
    public ForvaltningRestTjeneste(ProsessTaskRepository prosessTaskRepository, KodeverkRepository kodeverkRepository,
            FagsakRestKlient fagsakRestKlient) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.kodeverkRepository = kodeverkRepository;
        this.fagsakRestKlient = fagsakRestKlient;
    }

    @POST
    @Operation(description = "Setter nytt suffix for retry journalføring", summary = ("Setter parametere før retry av task"), responses =

    { @ApiResponse(responseCode = "200", description = "Nytt suffix satt") })

    @Path("/retry-suffix")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response setRetrySuffix(
            @Parameter(description = "Sett kanalreferanse-suffix før restart prosesstask") @NotNull @Valid RetryTaskKanalrefDto dto) {
        ProsessTaskData data = prosessTaskRepository.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setProperty(RETRY_KEY, dto.getRetrySuffix());
        prosessTaskRepository.lagre(data);
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Send inntektsmelding til angitt sak (allerede journalført)", summary = "Bruker eksisterende task til å sende dokument til VL", responses =

    { @ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL") })

    @Path("/submit-journalfort-im")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response submitJournalførtInntektsmelding(
            @Parameter(description = "Send im til angitt sak") @NotNull @Valid SubmitJfortIMDto dto) {
        ProsessTaskData data = prosessTaskRepository.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setCallIdFraEksisterende();
        MottakMeldingDataWrapper dataWrapperFra = new MottakMeldingDataWrapper(kodeverkRepository, data);
        MottakMeldingDataWrapper dataWrapperTil = dataWrapperFra.nesteSteg(KlargjorForVLTask.TASKNAME);
        dataWrapperTil.setSaksnummer(dto.getSaksnummerDto().getSaksnummer());
        dataWrapperTil.setArkivId(dto.getJournalpostIdDto().getJournalpostId());
        dataWrapperTil.setRetryingTask(KlargjorForVLTask.REINNSEND);
        fagsakRestKlient
                .knyttSakOgJournalpost(new JournalpostKnyttningDto(dto.getSaksnummerDto(), dto.getJournalpostIdDto()));
        prosessTaskRepository.lagre(dataWrapperTil.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Send journalpost til angitt sak (ikke journalført)", summary = ("Bruker eksisterende task til å sende dokument til VL"), responses = {
            @ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL") })
    @Path("/submit-journalforing-endelig")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response submitJournalpostEndeligKlargjor(
            @Parameter(description = "Send im til angitt sak") @NotNull @Valid SubmitJfortIMDto dto) {
        ProsessTaskData data = prosessTaskRepository.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setCallIdFraEksisterende();
        MottakMeldingDataWrapper dataWrapperFra = new MottakMeldingDataWrapper(kodeverkRepository, data);
        if (!dataWrapperFra.getArkivId().equals(dto.getJournalpostIdDto().getJournalpostId())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        MottakMeldingDataWrapper dataWrapperTil = dataWrapperFra.nesteSteg(TilJournalføringTask.TASKNAME);
        dataWrapperTil.setSaksnummer(dto.getSaksnummerDto().getSaksnummer());
        fagsakRestKlient
                .knyttSakOgJournalpost(new JournalpostKnyttningDto(dto.getSaksnummerDto(), dto.getJournalpostIdDto()));
        prosessTaskRepository.lagre(dataWrapperTil.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Midlertidig journalfør forsendelse", summary = ("For gsak-oppgaver opprettet uten journalpostid"), responses = {
            @ApiResponse(responseCode = "200", description = "Forsendelse til midl journalføring") })
    @Path("/midl-journalfor")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response midlJournalførForsendelse(
            @Parameter(description = "Task som skal midl journalførs") @NotNull @Valid ProsessTaskIdDto taskId) {
        ProsessTaskData data = prosessTaskRepository.finn(taskId.getProsessTaskId());
        if (data != null) {
            MottakMeldingDataWrapper dataWrapperFra = new MottakMeldingDataWrapper(kodeverkRepository, data);
            MottakMeldingDataWrapper dataWrapperTil = dataWrapperFra.nesteSteg(MidlJournalføringTask.TASKNAME);
            prosessTaskRepository.lagre(dataWrapperTil.getProsessTaskData());
        }
        return Response.ok().build();
    }

    @POST
    @Path("/autorun")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Start task for å kjøre batchjobs", responses = {
            @ApiResponse(responseCode = "200", description = "Starter batch-scheduler."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response autoRunBatch() {
        boolean eksisterende = prosessTaskRepository.finnIkkeStartet().stream()
                .map(ProsessTaskData::getTaskType)
                .anyMatch(VedlikeholdSchedulerTask.TASKTYPE::equals);
        if (!eksisterende) {
            ProsessTaskData taskData = new ProsessTaskData(VedlikeholdSchedulerTask.TASKTYPE);
            prosessTaskRepository.lagre(taskData);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/sett-task-ferdig")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Setter prosesstask til status FERDIG", responses = {
            @ApiResponse(responseCode = "200", description = "Task satt til ferdig."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response setTaskFerdig(
            @Parameter(description = "Task som skal settes ferdig") @NotNull @Valid ProsessTaskIdDto taskId) {
        ProsessTaskData data = prosessTaskRepository.finn(taskId.getProsessTaskId());
        if (data != null) {
            data.setStatus(ProsessTaskStatus.FERDIG);
            data.setSisteFeil(null);
            data.setSisteFeilKode(null);
            prosessTaskRepository.lagre(data);
        }
        return Response.ok().build();
    }
}
