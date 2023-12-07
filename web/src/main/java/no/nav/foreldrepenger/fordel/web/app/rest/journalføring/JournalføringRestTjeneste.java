package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.fordel.StringUtil.isBlank;
import static no.nav.foreldrepenger.fordel.web.app.rest.journalføring.ManuellJournalføringMapper.mapYtelseTypeTilDto;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilType;
import no.nav.foreldrepenger.fordel.web.app.konfig.ApiConfig;
import no.nav.foreldrepenger.fordel.web.server.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.klient.AktørIdDto;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Sak;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@Path(JournalføringRestTjeneste.JOURNALFOERING_PATH)
@Consumes(APPLICATION_JSON)
@Transactional
@RequestScoped
public class JournalføringRestTjeneste {
    private static final Environment ENV = Environment.current();

    private static final Logger LOG = LoggerFactory.getLogger(JournalføringRestTjeneste.class);

    public static final String JOURNALFOERING_PATH = "/journalfoering";
    private static final String DOKUMENT_HENT_PATH = "/dokument/hent";
    private static final String FULL_HENT_DOKUMENT_PATH =
        ENV.getProperty("context.path", "/fpfordel") + ApiConfig.API_URI + JOURNALFOERING_PATH + DOKUMENT_HENT_PATH;
    private Journalføringsoppgave oppgaveTjeneste;
    private PersonInformasjon pdl;
    private ArkivTjeneste arkiv;
    private Fagsak fagsak;

    JournalføringRestTjeneste() {
        // CDI
    }

    @Inject
    public JournalføringRestTjeneste(Journalføringsoppgave oppgaveTjeneste,
                                     PersonInformasjon pdl,
                                     ArkivTjeneste arkiv,
                                     Fagsak fagsak) {
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.pdl = pdl;
        this.arkiv = arkiv;
        this.fagsak = fagsak;
    }

    @GET
    @Path("/oppgaver")
    @Produces(APPLICATION_JSON)
    @Operation(description = "Henter alle åpne journalføringsoppgaver for tema FOR.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.APPLIKASJON)
    public List<OppgaveDto> hentÅpneOppgaver() {
        var oppgaver = oppgaveTjeneste.finnÅpneOppgaverFiltrert()
            .stream()
            .map(this::lagOppgaveDto)
            .toList();
        LOG.info("FPFORDEL RESTJOURNALFØRING: Henter {} oppgaver", oppgaver.size());
        return oppgaver;
    }

    @POST
    @Path("/bruker/hent")
    @Produces(APPLICATION_JSON)
    @Operation(description = "Hent bruker navn og etternavn", tags = "Manuell journalføring", responses = { @ApiResponse(responseCode = "200", description = "Bruker hentet"), @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public HentBrukerResponseDto hentBruker(@Parameter(description = "Trenger FNR/DNR til å kunne innhente en bruker.")
                                                 @NotNull @Valid @TilpassetAbacAttributt(supplierClass = HentBrukerDataSupplier.class) HentBrukerDto request) {
        Objects.requireNonNull(request.fødselsnummer(), "FNR/DNR må være satt.");
        try {
            var aktørId = pdl.hentAktørIdForPersonIdent(request.fødselsnummer()).orElseThrow();
            return new HentBrukerResponseDto(pdl.hentNavn(BehandlingTema.FORELDREPENGER, aktørId), request.fødselsnummer());
        } catch (NoSuchElementException e) {
            throw new TekniskException("BRUKER-MANGLER", "Angitt bruker ikke funnet. Sjekk om oppgitt personnummer er riktig.", e);
        }
    }

    @POST
    @Path("/bruker/oppdater")
    @Produces(APPLICATION_JSON)
    @Operation(description = "Oppdaterer manglende bruker og så returnerer en oppdatert journalpost detaljer.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "200", description = "Bruker oppdatert"), @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    public JournalpostDetaljerDto oppdaterBruker(@Parameter(description = "Trenger journalpostId, og FNR/DNR til å kunne oppdatere dokumentet.") @NotNull @Valid @TilpassetAbacAttributt(supplierClass = FnrDataSupplier.class) OppdaterBrukerDto request) {
        Objects.requireNonNull(request.journalpostId(), "JournalpostId må være satt.");
        Objects.requireNonNull(request.fødselsnummer(), "FNR/DNR må være satt.");

        var journalpost = arkiv.hentArkivJournalpost(request.journalpostId()).getOriginalJournalpost();
        var journalpostId = journalpost.journalpostId();

        if ((journalpost.bruker() == null) || (journalpost.bruker().id() == null)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL oppdaterer manglende bruker for {}", journalpostId);
            }
            arkiv.oppdaterJournalpostBruker(journalpostId, request.fødselsnummer());

            oppgaveTjeneste.hentLokalOppgaveFor(JournalpostId.fra(journalpostId))
                .ifPresent(oppgave -> oppgaveTjeneste.oppdaterBruker(oppgave, request.fødselsnummer()));
        }

        return Optional.ofNullable(arkiv.hentArkivJournalpost(journalpostId)).map(this::mapTilJournalpostDetaljerDto).orElseThrow();
    }

    @GET
    @Path("/oppgave/detaljer")
    @Produces(APPLICATION_JSON)
    @Operation(description = "Henter detaljer for en gitt jornalpostId som er relevante for å kunne ferdigstille journalføring på en fagsak.",
        tags = "Manuell journalføring",
        responses = {
            @ApiResponse(responseCode = "200", description = "Fant journalpost", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JournalpostDetaljerDto.class))),
            @ApiResponse(responseCode = "204", description = "Fant ikke journalpost"),
            @ApiResponse(responseCode = "500", description = "Feil i request"),
        })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public Response hentJournalpostDetaljer(@TilpassetAbacAttributt(supplierClass = JournalpostDataSupplier.class) @QueryParam("journalpostId") @NotNull @Valid JournalpostIdDto journalpostId) {
        LOG.info("FPFORDEL RESTJOURNALFØRING: Henter journalpostdetaljer for journalpostId {}", journalpostId.getJournalpostId());
        try {
            var journalpostDetaljer = Optional.ofNullable(arkiv.hentArkivJournalpost(journalpostId.getJournalpostId()))
                .map(this::mapTilJournalpostDetaljerDto)
                .orElseThrow();

            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL RESTJOURNALFØRING: Journalpost-tema:{} journalpostTittel:{} antall dokumenter:{}",
                    journalpostDetaljer.behandlingTema(), journalpostDetaljer.tittel(), journalpostDetaljer.dokumenter().size());
            }
            return Response.ok().entity(journalpostDetaljer).build();
        } catch (TekniskException|NoSuchElementException ex) {
            if (ex instanceof NoSuchElementException || ex.getMessage().contains("Fant ikke journalpost i fagarkivet")) {
                return Response.noContent().build();
            } else if (ex.getMessage().contains("Saksbehandler har ikke tilgang til ressurs på grunn av journalposten sin status")) {
                return Response.noContent().build();
            }
            throw new TekniskException("FORDEL-123", "Journapost " + journalpostId.getJournalpostId() + " finnes ikke i arkivet.", ex);
        }
    }

    @POST
    @Path("/oppgave/tilgosys")
    @Produces(APPLICATION_JSON)
    @Operation(description = "Flytter evt lokal oppgave til Gosys for å utføre avanserte funksjoner.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    public Response flyttOppgaveTilGosys(@TilpassetAbacAttributt(supplierClass = JournalpostDataSupplier.class) @NotNull @Valid JournalpostIdDto journalpostId) {
        LOG.info("FPFORDEL TILGOSYS: Flytter journalpostId {} til Gosys", journalpostId.getJournalpostId());
        try {
            oppgaveTjeneste.flyttLokalOppgaveTilGosys(JournalpostId.fra(journalpostId.getJournalpostId()));
            return Response.ok().build();
        } catch (NoSuchElementException ex) {
            throw new TekniskException("FORDEL-123", "Journalpost " + journalpostId.getJournalpostId() + " kunne ikke flyttes til Gosys.", ex);
        }
    }

    @POST
    @Path("/oppgave/reserver")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(description = "Mulighet for å reservere/avreservere en oppgave", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public Response oppgaveReserver(@TilpassetAbacAttributt(supplierClass = ReserverOppgaveDataSupplier.class) @NotNull @Valid ReserverOppgaveDto reserverOppgaveDto) {
        var innloggetBruker = KontekstHolder.getKontekst().getUid();
        var oppgave = oppgaveTjeneste.hentOppgaveFor(JournalpostId.fra(reserverOppgaveDto.journalpostId()));

        if (isBlank(reserverOppgaveDto.reserverFor())) {
            // Avreserver
            if (innloggetBruker.equals(oppgave.tilordnetRessurs())) {
                oppgaveTjeneste.avreserverOppgaveFor(oppgave);
                LOG.info("Oppgave {} avreservert av {}.", oppgave.journalpostId(), innloggetBruker);
            } else {
                // Ikke mulig å avreservere for andre
                throw new TekniskException("AVRESERVER",
                    "Kan ikke avreservere en oppgave som allerede tilhører til en annen saksbehandler.");
            }
        } else {
            // Reserver
            if (isBlank(oppgave.tilordnetRessurs())) {
                oppgaveTjeneste.reserverOppgaveFor(oppgave, reserverOppgaveDto.reserverFor());
                LOG.info("Oppgave {} reservert av {}.", oppgave.journalpostId(), innloggetBruker);
            }
            else {
                throw new TekniskException("RESERVER",
                    "Det er ikke mulig å reservere en oppgave som allerede tilhører til en annen saksbehandler.");
            }
        }
        return Response.ok().build();
    }

    @GET
    @Path(DOKUMENT_HENT_PATH)
    @Operation(description = "Søk etter dokument på JOARK-identifikatorene journalpostId og dokumentId", summary = ("Retunerer dokument som er tilknyttet journalpost og dokumentId."), tags = "Manuell journalføring")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public Response hentDokument(@TilpassetAbacAttributt(supplierClass = JournalpostDataSupplier.class) @QueryParam("journalpostId") @Valid JournalpostIdDto journalpostId,
                                 @TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @QueryParam("dokumentId") @Valid DokumentIdDto dokumentId) {
        var journalpost = journalpostId.getJournalpostId();
        var dokument = dokumentId.dokumentId();
        try {
            var responseBuilder = Response.ok(new ByteArrayInputStream(arkiv.hentDokumet(journalpost, dokument)));
            responseBuilder.type("application/pdf");
            responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
            return responseBuilder.build();
        } catch (Exception e) {
            var feilmelding = String.format("Dokument ikke funnet for journalpost= %s dokument= %s", journalpost, dokument);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new FeilDto(feilmelding, FeilType.TOMT_RESULTAT_FEIL))
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }

    private static Set<JournalpostDetaljerDto.DokumentDto> mapDokumenter(String journalpostId, List<DokumentInfo> dokumenter) {
        return dokumenter.stream()
            .map(dok -> new JournalpostDetaljerDto.DokumentDto(dok.dokumentInfoId(), dok.tittel(),
                String.format("%s?journalpostId=%s&dokumentId=%s", FULL_HENT_DOKUMENT_PATH, journalpostId, dok.dokumentInfoId())))
            .collect(Collectors.toSet());
    }

    JournalpostDetaljerDto mapTilJournalpostDetaljerDto(ArkivJournalpost journalpost) {
        var eksisterendeSaksnummer = journalpost.getOriginalJournalpost() != null && journalpost.getOriginalJournalpost().sak() != null &&
            Sak.Sakstype.FAGSAK.equals(journalpost.getOriginalJournalpost().sak().sakstype()) &&
            "FS36".equals(journalpost.getOriginalJournalpost().sak().fagsaksystem()) ? journalpost.getSaksnummer().orElse(null) : null;
        return new JournalpostDetaljerDto(
            journalpost.getJournalpostId(), journalpost.getTittel().orElse(""), journalpost.getBehandlingstema().getOffisiellKode(),
            journalpost.getKanal(), mapBruker(journalpost), journalpost.getTilstand().name(),
            new JournalpostDetaljerDto.AvsenderDto(journalpost.getAvsenderNavn(), journalpost.getAvsenderIdent()),
            journalpost.getJournalfoerendeEnhet().orElse(null),
            mapYtelseTypeTilDto(journalpost.getBehandlingstema().utledYtelseType()), eksisterendeSaksnummer,
            mapDokumenter(journalpost.getJournalpostId(), journalpost.getOriginalJournalpost().dokumenter()),
            mapBrukersFagsaker(journalpost.getBrukerAktørId().orElse(null)));
    }

    private List<JournalpostDetaljerDto.SakJournalføringDto> mapBrukersFagsaker(String aktørId) {
        if (aktørId == null) {
            return List.of();
        }
        return fagsak.hentBrukersSaker(new AktørIdDto(aktørId)).stream().map(ManuellJournalføringMapper::mapSakJournalføringDto).toList();
    }

    private JournalpostDetaljerDto.BrukerDto mapBruker(ArkivJournalpost journalpost) {
        var aktørId = journalpost.getBrukerAktørId().orElse(null);
        if (aktørId == null) {
            return null;
        }
        var fnr = pdl.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
        var navn = pdl.hentNavn(journalpost.getBehandlingstema(), aktørId);
        return new JournalpostDetaljerDto.BrukerDto(navn, fnr, aktørId);
    }

    private OppgaveDto lagOppgaveDto(Oppgave oppgave) {
        return new OppgaveDto(
            oppgave.journalpostId(),
            oppgave.aktørId(),
            hentPersonIdent(oppgave).orElse(null),
            mapYtelseType(oppgave),
            oppgave.fristFerdigstillelse(),
            oppgave.beskrivelse(),
            oppgave.aktivDato(),
            oppgave.tildeltEnhetsnr(),
            oppgave.tilordnetRessurs(),
            mapKilde(oppgave));
    }

    static YtelseTypeDto mapYtelseType(Oppgave oppgave) {
        if (oppgave == null || oppgave.ytelseType() == null) {
            return null;
        }
        return switch (oppgave.ytelseType()) {
            case FP -> YtelseTypeDto.FORELDREPENGER;
            case SVP -> YtelseTypeDto.SVANGERSKAPSPENGER;
            case ES -> YtelseTypeDto.ENGANGSTØNAD;
        };
    }

    static OppgaveKilde mapKilde(Oppgave oppgave) {
        if (oppgave == null || oppgave.kilde() == null) {
            return null;
        }
        return switch (oppgave.kilde()) {
            case LOKAL -> OppgaveKilde.LOKAL;
            case GOSYS -> OppgaveKilde.GOSYS;
        };
    }


    private Optional<String> hentPersonIdent(Oppgave oppgave) {
        if (oppgave != null && oppgave.aktørId() != null) {
            return pdl.hentPersonIdentForAktørId(oppgave.aktørId());
        }
        return Optional.empty();
    }

    public enum OppgaveKilde { LOKAL, GOSYS }

    public record OppdaterBrukerDto(@NotNull String journalpostId, @NotNull String fødselsnummer) {
    }

    public record HentBrukerDto(@NotNull String fødselsnummer) {}

    public record HentBrukerResponseDto(@NotNull String navn, @NotNull String fødselsnummer) {}

    public record OppgaveDto(@NotNull String journalpostId,
                             String aktørId,
                             String fødselsnummer,
                             @Valid YtelseTypeDto ytelseType,
                             @NotNull LocalDate frist,
                             String beskrivelse,
                             @NotNull LocalDate opprettetDato,
                             String enhetId,
                             String reservertAv,
                             OppgaveKilde kilde) {

    }

    public record ReserverOppgaveDto(@NotNull String journalpostId, String reserverFor) {
    }

    public static class EmptyAbacDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }

    public static class ReserverOppgaveDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            var dto = (ReserverOppgaveDto) obj;
            return AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.JOURNALPOST_ID, JournalpostId.fra(dto.journalpostId()));
        }
    }

    public static class JournalpostDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            var dto = (JournalpostIdDto) obj;
            return AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.JOURNALPOST_ID, JournalpostId.fra(dto.getJournalpostId()));
        }
    }

    public static class FnrDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            var dto = (OppdaterBrukerDto) obj;
            return AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.FNR, dto.fødselsnummer());
        }
    }

    public static class HentBrukerDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            var dto = (HentBrukerDto) obj;
            return AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.FNR, dto.fødselsnummer());
        }
    }
}
