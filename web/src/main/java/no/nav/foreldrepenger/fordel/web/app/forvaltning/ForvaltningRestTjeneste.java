package no.nav.foreldrepenger.fordel.web.app.forvaltning;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;

import java.time.DayOfWeek;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.fordel.web.app.rest.journalføring.FerdigstillJournalføringTjeneste;
import no.nav.foreldrepenger.fordel.web.app.rest.journalføring.JournalføringRestTjeneste;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.task.RekjørFeiledeTasksBatchTask;
import no.nav.foreldrepenger.mottak.task.SlettGamleTasksBatchTask;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
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
    private OppgaveRepository oppgaveRepository;
    private FerdigstillJournalføringTjeneste journalføringTjeneste;

    public ForvaltningRestTjeneste() {
        // CDI
    }

    @Inject
    public ForvaltningRestTjeneste(ProsessTaskTjeneste taskTjeneste, Fagsak fagsak, OppgaveRepository oppgaveRepository,
                                   FerdigstillJournalføringTjeneste journalføringTjeneste) {
        this.taskTjeneste = taskTjeneste;
        this.fagsak = fagsak;
        this.oppgaveRepository = oppgaveRepository;
        this.journalføringTjeneste = journalføringTjeneste;
    }

    @POST
    @Operation(description = "Setter nytt suffix for retry journalføring", tags = "Forvaltning", summary = ("Setter parametere før retry av task"), responses =

        {@ApiResponse(responseCode = "200", description = "Nytt suffix satt")})

    @Path("/retry-suffix")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
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
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
    public Response submitJournalførtInntektsmelding(@Parameter(description = "Send im til angitt sak") @NotNull @Valid SubmitJfortIMDto dto) {
        var data = taskTjeneste.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        var fra = new MottakMeldingDataWrapper(data);
        var til = fra.nesteSteg(TaskType.forProsessTask(VLKlargjørerTask.class));
        til.setSaksnummer(dto.getSaksnummerDto().saksnummer());
        til.setArkivId(dto.getJournalpostIdDto().journalpostId());
        til.setRetryingTask(VLKlargjørerTask.REINNSEND);
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(dto.getSaksnummerDto(), dto.getJournalpostIdDto()));
        taskTjeneste.lagre(til.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Send journalpost til angitt sak (ikke journalført)", tags = "Forvaltning", summary = ("Bruker eksisterende task til å sende dokument til VL"), responses = {@ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL")})
    @Path("/submit-journalforing-endelig")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
    public Response submitJournalpostEndeligKlargjor(@Parameter(description = "Send im til angitt sak") @NotNull @Valid SubmitJfortIMDto dto) {
        var data = taskTjeneste.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        data.setCallIdFraEksisterende();
        var fra = new MottakMeldingDataWrapper(data);
        if (!fra.getArkivId().equals(dto.getJournalpostIdDto().journalpostId())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        var til = fra.nesteSteg(TaskType.forProsessTask(TilJournalføringTask.class));
        til.setSaksnummer(dto.getSaksnummerDto().saksnummer());
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(dto.getSaksnummerDto(), dto.getJournalpostIdDto()));
        taskTjeneste.lagre(til.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Path("/autorun")
    @Operation(description = "Start task for å kjøre batchjobs", tags = "Forvaltning", responses = {@ApiResponse(responseCode = "200", description = "Starter batch-scheduler."), @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = false)
    public Response autoRunBatch() {
        taskTjeneste.lagre(ProsessTaskData.forProsessTask(RekjørFeiledeTasksBatchTask.class));
        taskTjeneste.lagre(ProsessTaskData.forProsessTask(SlettGamleTasksBatchTask.class));
        return Response.ok().build();
    }

    @POST
    @Path("/searchTasks")
    @Operation(description = "Søker etter journalpostId mv i taskparametre innen angitt tidsrom", tags = "Forvaltning", responses = {@ApiResponse(responseCode = "200", description = "Angitt prosesstask-id satt til status FERDIG"), @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = false)
    public Response searchTasks(@Parameter(description = "Søkefilter") @NotNull @Valid FordelSokeFilterDto dto) {
        var tasks = taskTjeneste.finnAlleMedParameterTekst(dto.getTekst(), dto.getOpprettetFraOgMed(), dto.getOpprettetTilOgMed());
        return Response.ok(tasks).build();
    }

    @POST
    @Operation(description = "Send inntektsmelding til angitt sak (allerede journalført)", tags = "Forvaltning", summary = "Bruker eksisterende task til å sende dokument til VL", responses =

        {@ApiResponse(responseCode = "200", description = "Inntektsmelding sendt til VL")})

    @Path("/fiks-arkiv-feil")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
    public Response fiksarkivFeil(@Parameter(description = "Arkivfeil") @NotNull @Valid SubmitJfortIMDto dto) {
        var data = taskTjeneste.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (data == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        var fra = new MottakMeldingDataWrapper(data);
        fra.setSaksnummer(dto.getSaksnummerDto().saksnummer());
        fra.setArkivId(dto.getJournalpostIdDto().journalpostId());
        taskTjeneste.lagre(fra.getProsessTaskData());
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Lager en ny OpprettOppgaveTask basert på tidligere + slett evt lokaloppgave", tags = "Forvaltning",
        summary = "Bruker eksisterende task til å opprette Oppgave", responses = {@ApiResponse(responseCode = "200", description = "oppgave opprettet")})
    @Path("/rerun-opprett-oppgave")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = false)
    public Response rerunOpprettOppgave(@Parameter(description = "TaskMedRef") @NotNull @Valid RetryTaskKanalrefDto dto) {
        var eksisterendeTask = taskTjeneste.finn(dto.getProsessTaskIdDto().getProsessTaskId());
        if (eksisterendeTask == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        oppgaveRepository.fjernFeilopprettetOppgave(eksisterendeTask.getPropertyValue("arkivId"));
        var nyTask = ProsessTaskData.forProsessTask(OpprettGSakOppgaveTask.class);
        nyTask.setProperties(eksisterendeTask.getProperties());
        taskTjeneste.lagre(nyTask);
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Knytter en journalpost til en ny sak ved å opprette ny journalpost", tags = "Forvaltning",
        summary = "Knytter en journalpost til en ny sak ved å opprette ny journalpost", responses = {@ApiResponse(responseCode = "200", description = "journalpost opprettet")})
    @Path("/knytt-til-annen-sak")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
    public Response knyttTilAnnenSak(@Parameter(description = "Sak og Journalpost") @NotNull @Valid JournalpostSakDto dto) {
        var journalpost = journalføringTjeneste.hentJournalpost(dto.journalpostIdDto().journalpostId());
        var response = journalføringTjeneste.knyttTilAnnenSak(journalpost, "9999", dto.saksnummerDto().saksnummer());
        return Response.ok(response.getVerdi()).build();
    }

    @POST
    @Operation(description = "Sender inn en journalpost til fpsak med angitt sak", tags = "Forvaltning",
        summary = "Sender inn en journalpost til fpsak med angitt sak", responses = {@ApiResponse(responseCode = "200", description = "sendt inn")})
    @Path("/send-inn-til-sak")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
    public Response sendInnTilSak(@Parameter(description = "Sak og Journalpost") @NotNull @Valid JournalpostSakDto dto) {
        var journalpost = journalføringTjeneste.hentJournalpost(dto.journalpostIdDto().journalpostId());
        journalføringTjeneste.sendInnPåSak(journalpost, dto.saksnummerDto().saksnummer());
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Setter den lokale oppgaven til status Feilregistrert slik at den fjernes fra oversikten.", tags = "Forvaltning",
        summary = "Fjerner lokal oppgave fra oversikten.", responses = {@ApiResponse(responseCode = "200", description = "oppgave feilregistrert")})
    @Path("/avslutt-oppgave")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = false)
    public Response feilregistrerOppgave(@TilpassetAbacAttributt(supplierClass = JournalføringRestTjeneste.JournalpostDataSupplier.class) @Parameter(description = "journalpostId") @NotNull @Valid JournalpostIdDto journalpostIdDto) {
        oppgaveRepository.avsluttOppgaveMedStatus(journalpostIdDto.journalpostId(), Status.FEILREGISTRERT);
        return Response.ok().build();
    }

    @POST
    @Operation(description = "Oppretter en lokal oppgave for journalpostId eller gjennåpner en eksisterende", tags = "Forvaltning",
        summary = ("Oppretter en lokal oppgave for journalpostId eller gjenåpner en eksisterende"),
        responses = {@ApiResponse(responseCode = "200", description = "oppgave opprettet eller oppdatert")})
    @Path("/opprett-oppgave")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT, sporingslogg = false)
    public Response opprettOppgave(@TilpassetAbacAttributt(supplierClass = JournalføringRestTjeneste.JournalpostDataSupplier.class) @Parameter(description = "journalpostId") @NotNull @Valid JournalpostIdDto journalpostIdDto) {
        var journalpostId = journalpostIdDto.journalpostId();

        var eksisterende = oppgaveRepository.hentOppgave(journalpostId);
        if (eksisterende != null) {
            eksisterende.setStatus(Status.AAPNET);
            oppgaveRepository.lagre(eksisterende);
        } else {
            var oppgave = OppgaveEntitet.builder()
                .medJournalpostId(journalpostId)
                .medStatus(Status.AAPNET)
                .medEnhet("4867")
                .medFrist(helgeJustertFrist(LocalDate.now().plusDays(1)))
                .medBeskrivelse("Journalføring")
                .build();
            oppgaveRepository.lagre(oppgave);
        }
        return Response.ok().build();
    }

    private static LocalDate helgeJustertFrist(LocalDate dato) {
        return dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue() ? dato.plusDays(
            1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue()) : dato;
    }
}
