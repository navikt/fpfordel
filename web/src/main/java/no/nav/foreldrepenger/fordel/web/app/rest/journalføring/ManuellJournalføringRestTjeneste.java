package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.fordel.StringUtil.isBlank;
import static no.nav.foreldrepenger.fordel.web.app.rest.journalføring.ManuellJournalføringMapper.mapYtelseTypeTilDto;
import static no.nav.foreldrepenger.fordel.web.app.rest.journalføring.ManuellJournalføringMapper.tekstFraBeskrivelse;

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
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilType;
import no.nav.foreldrepenger.fordel.web.app.konfig.ApiConfig;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.klient.AktørIdDto;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.Los;
import no.nav.foreldrepenger.mottak.klient.TilhørendeEnhetDto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@Path(ManuellJournalføringRestTjeneste.JOURNALFOERING_PATH)
@Consumes(APPLICATION_JSON)
@Transactional
@RequestScoped
public class ManuellJournalføringRestTjeneste {
    private static final Environment ENV = Environment.current();

    private static final Logger LOG = LoggerFactory.getLogger(ManuellJournalføringRestTjeneste.class);

    public static final String JOURNALFOERING_PATH = "/journalfoering";
    private static final String DOKUMENT_HENT_PATH = "/dokument/hent";
    private static final String FULL_HENT_DOKUMENT_PATH =
        ENV.getProperty("context.path", "/fpfordel") + ApiConfig.API_URI + JOURNALFOERING_PATH + DOKUMENT_HENT_PATH;
    private Journalføringsoppgave oppgaveTjeneste;
    private PersonInformasjon pdl;
    private ArkivTjeneste arkiv;
    private Fagsak fagsak;
    private Los los;

    ManuellJournalføringRestTjeneste() {
        // CDI
    }

    @Inject
    public ManuellJournalføringRestTjeneste(Journalføringsoppgave oppgaveTjeneste,
                                            PersonInformasjon pdl,
                                            ArkivTjeneste arkiv,
                                            Fagsak fagsak,
                                            Los los) {
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.pdl = pdl;
        this.arkiv = arkiv;
        this.fagsak = fagsak;
        this.los = los;
    }

    @GET
    @Path("/oppgaver")
    @Produces(APPLICATION_JSON)
    @Operation(description = "Henter alle åpne journalføringsoppgaver for tema FOR og for saksbehandlers tilhørende enhet.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public List<OppgaveDto> hentÅpneOppgaverForSaksbehandler(@TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @QueryParam("ident") @NotNull @Valid SaksbehandlerIdentDto saksbehandlerIdentDto) {
        //Midlertidig for å kunne verifisere i produksjon - fjernes når verifisert ok
        if (ENV.isProd() && ("W119202".equals(KontekstHolder.getKontekst().getUid()))) {
            var oppgaveDtoer = oppgaveTjeneste.finnÅpneOppgaverFor(null)
                .stream()
                .map(this::lagOppgaveDto)
                .toList();
            LOG.info("FPFORDEL RESTJOURNALFØRING: Henter {} oppgaver", oppgaveDtoer.size());
            return oppgaveDtoer;
        }

        var tilhørendeEnheter = los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident());
        if (tilhørendeEnheter.isEmpty()) {
            throw new IllegalStateException(
                String.format("Det forventes at saksbehandler %s har minst en tilførende enhet. Fant ingen.", saksbehandlerIdentDto.ident()));
        }
        var enheter = tilhørendeEnheter.stream().map(TilhørendeEnhetDto::enhetsnummer).collect(Collectors.toSet());
        var oppgaver = oppgaveTjeneste.finnÅpneOppgaverFor(enheter)
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
            return new HentBrukerResponseDto(pdl.hentNavn(aktørId), request.fødselsnummer());
        } catch (NoSuchElementException e) {
            throw new FunksjonellException("BRUKER-MANGLER", "Angitt bruker ikke funnet.", "Sjekk om oppgitt personnummer er riktig.", e);
        }
    }

    @POST
    @Path("/bruker/oppdater")
    @Produces(APPLICATION_JSON)
    @Operation(description = "Oppdaterer manglende bruker og så returnerer en oppdatert journalpost detaljer.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "200", description = "Bruker oppdatert"), @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
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
        }

        return Optional.ofNullable(arkiv.hentArkivJournalpost(journalpostId)).map(this::mapTilJournalpostDetaljerDto).orElseThrow();
    }

    @GET
    @Path("/oppgave/detaljer")
    @Produces(APPLICATION_JSON)
    @Operation(description = "Henter detaljer for en gitt jornalpostId som er relevante for å kunne ferdigstille journalføring på en fagsak.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public JournalpostDetaljerDto hentJournalpostDetaljer(@TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @QueryParam("journalpostId") @NotNull @Valid JournalpostIdDto journalpostId) {
        LOG.info("FPFORDEL RESTJOURNALFØRING: Henter journalpostdetaljer for journalpostId {}", journalpostId.getJournalpostId());
        try {
            var journalpostDetaljer = Optional.ofNullable(arkiv.hentArkivJournalpost(journalpostId.getJournalpostId()))
                .map(this::mapTilJournalpostDetaljerDto)
                .orElseThrow();

            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL RESTJOURNALFØRING: Journalpost-tema:{} journalpostTittel:{} antall dokumenter:{}",
                    journalpostDetaljer.behandlingTema(), journalpostDetaljer.tittel(), journalpostDetaljer.dokumenter().size());
            }
            return journalpostDetaljer;
        } catch (NoSuchElementException ex) {
            throw new TekniskException("FORDEL-123", "Journapost " + journalpostId.getJournalpostId() + " finnes ikke i arkivet.", ex);
        }
    }

    @POST
    @Path("/oppgave/reserver")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(description = "Mulighet for å reservere/avreservere en oppgave", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public Response oppgaveReserver(@TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @NotNull @Valid ReserverOppgaveDto oppgaveDto) {
        var innloggetBruker = KontekstHolder.getKontekst().getUid();
        var oppgave = oppgaveTjeneste.hentOppgaveFor(JournalpostId.fra(oppgaveDto.oppgaveId()));

        if (isBlank(oppgaveDto.reserverFor())) {
            // Avreserver
            if (innloggetBruker.equals(oppgave.tilordnetRessurs())) {
                oppgaveTjeneste.avreserverOppgaveFor(oppgave.id());
                LOG.info("Oppgave {} avreservert av {}.", oppgave.id(), innloggetBruker);
            } else {
                // Ikke mulig å avreservere for andre
                throw new TekniskException("AVRESERVER",
                    "Kan ikke avreservere en oppgave som allerede tilhører til en annen saksbehandler.");
            }
        } else {
            // Reserver
            if (isBlank(oppgave.tilordnetRessurs())) {
                oppgaveTjeneste.reserverOppgaveFor(oppgave.id(), oppgaveDto.reserverFor());
                LOG.info("Oppgave {} reservert av {}.", oppgave.id(), innloggetBruker);
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
    public Response hentDokument(@TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @QueryParam("journalpostId") @Valid JournalpostIdDto journalpostId,
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
        return new JournalpostDetaljerDto(journalpost.getJournalpostId(), journalpost.getTittel().orElse(""),
            journalpost.getBehandlingstema().getOffisiellKode(), journalpost.getKanal(),
            journalpost.getBrukerAktørId().map(this::mapBruker).orElse(null),
            new JournalpostDetaljerDto.AvsenderDto(journalpost.getAvsenderNavn(), journalpost.getAvsenderIdent()),
            mapYtelseTypeTilDto(journalpost.getBehandlingstema().utledYtelseType()),
            mapDokumenter(journalpost.getJournalpostId(), journalpost.getOriginalJournalpost().dokumenter()),
            mapBrukersFagsaker(journalpost.getBrukerAktørId().orElse(null)));
    }

    private List<JournalpostDetaljerDto.SakJournalføringDto> mapBrukersFagsaker(String aktørId) {
        if (aktørId == null) {
            return List.of();
        }
        return fagsak.hentBrukersSaker(new AktørIdDto(aktørId)).stream().map(ManuellJournalføringMapper::mapSakJournalføringDto).toList();
    }

    private JournalpostDetaljerDto.BrukerDto mapBruker(String aktørId) {
        if (aktørId == null) {
            return null;
        }
        var fnr = pdl.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
        var navn = pdl.hentNavn(aktørId);
        return new JournalpostDetaljerDto.BrukerDto(navn, fnr, aktørId);
    }

    private OppgaveDto lagOppgaveDto(Oppgave oppgave) {
        return new OppgaveDto(Long.valueOf(oppgave.id()), oppgave.id(), oppgave.aktoerId(), hentPersonIdent(oppgave.aktoerId()).orElse(null),
            mapYtelseType(oppgave.ytelseType()), oppgave.fristFerdigstillelse(), OppgavePrioritet.NORM, oppgave.beskrivelse(),
            tekstFraBeskrivelse(oppgave.beskrivelse()), oppgave.aktivDato(), oppgave.tildeltEnhetsnr(), oppgave.tilordnetRessurs());
    }

    static YtelseTypeDto mapYtelseType(YtelseType ytelseType) {
        if (null == ytelseType) {
            return null;
        }
        return switch (ytelseType) {
            case FP -> YtelseTypeDto.FORELDREPENGER;
            case SVP -> YtelseTypeDto.SVANGERSKAPSPENGER;
            case ES -> YtelseTypeDto.ENGANGSTØNAD;
        };
    }


    private Optional<String> hentPersonIdent(String aktørId) {
        if (aktørId != null) {
            return pdl.hentPersonIdentForAktørId(aktørId);
        }
        return Optional.empty();
    }

    public enum OppgavePrioritet {
        HØY,
        NORM,
        LAV
    }

    public record OppdaterBrukerDto(@NotNull String journalpostId, @NotNull String fødselsnummer) {
    }

    public record HentBrukerDto(@NotNull String fødselsnummer) {}

    public record HentBrukerResponseDto(@NotNull String navn, @NotNull String fødselsnummer) {}

    public record OppgaveDto(@NotNull Long id,
                             @NotNull String journalpostId,
                             String aktørId,
                             String fødselsnummer,
                             @Valid YtelseTypeDto ytelseType,
                             @NotNull LocalDate frist,
                             OppgavePrioritet prioritet,
                             String beskrivelse,
                             String trimmetBeskrivelse,
                             @NotNull LocalDate opprettetDato,
                             String enhetId,
                             String reservertAv) {

    }

    public record ReserverOppgaveDto(@NotNull String oppgaveId, @NotNull Integer versjon, String reserverFor) {
    }

    public static class EmptyAbacDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }

    public static class FnrDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            var dto = (OppdaterBrukerDto) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.FNR, dto.fødselsnummer());
        }
    }

    public static class HentBrukerDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            var dto = (HentBrukerDto) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.FNR, dto.fødselsnummer());
        }
    }
}
