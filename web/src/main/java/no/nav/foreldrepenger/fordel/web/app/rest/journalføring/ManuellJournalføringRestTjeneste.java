package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.fordel.web.app.rest.journalføring.ManuellJournalføringMapper.mapYtelseTypeTilDto;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilType;
import no.nav.foreldrepenger.fordel.web.app.konfig.ApiConfig;
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
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@Path(ManuellJournalføringRestTjeneste.JOURNALFOERING_PATH)
@RequestScoped
@Transactional
public class ManuellJournalføringRestTjeneste {
    private static final Environment ENV = Environment.current();

    public static final String JOURNALFOERING_PATH = "/journalfoering";
    private static final String DOKUMENT_HENT_PATH = "/dokument/hent";
    private static final String FULL_HENT_DOKUMENT_PATH =
        ENV.getProperty("context.path", "/fpfordel") + ApiConfig.API_URI + JOURNALFOERING_PATH + DOKUMENT_HENT_PATH;
    private static final String LIMIT = "50";

    private Oppgaver oppgaver;
    private PersonInformasjon pdl;
    private ArkivTjeneste arkiv;
    private Fagsak fagsak;
    private Los los;

    public ManuellJournalføringRestTjeneste() {
        // For inject
    }

    @Inject
    public ManuellJournalføringRestTjeneste(Oppgaver oppgaver, PersonInformasjon pdl, ArkivTjeneste arkiv, Fagsak fagsak, Los los) {
        this.oppgaver = oppgaver;
        this.pdl = pdl;
        this.arkiv = arkiv;
        this.fagsak = fagsak;
        this.los = los;
    }

    @GET
    @Path("/oppgaver")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(description = "Henter alle åpne journalføringsoppgaver for tema FOR og for saksbehandlers tilhørende enhet.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public List<OppgaveDto> hentÅpneOppgaverForSaksbehandler(@TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @QueryParam("ident") @NotNull @Valid SaksbehandlerIdentDto saksbehandlerIdentDto) {
        //Midlertidig for å kunne verifisere i produksjon - fjernes når verifisert ok
        if (ENV.isProd() && ("J116396".equals(KontekstHolder.getKontekst().getUid()) || "W119202".equals(KontekstHolder.getKontekst().getUid()))) {
            return oppgaver.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null , LIMIT)
                .stream()
                .filter(oppgave -> oppgave.aktoerId() != null)
                .map(this::lagOppgaveDto)
                .toList();
        }

        var tilhørendeEnheter = los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident());
        if (tilhørendeEnheter.isEmpty()) {
            throw new IllegalStateException(String.format("Det forventes at saksbehandler %s har minst en tilførende enhet. Fant ingen.", saksbehandlerIdentDto.ident()));
        }

        List<OppgaveDto> oppgaverPåSaksbehandlersEnheter = new ArrayList<>();
        for (TilhørendeEnhetDto enhet : tilhørendeEnheter) {
            try {
                oppgaverPåSaksbehandlersEnheter.addAll(oppgaver.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, enhet.enhetsnummer(), LIMIT)
                        .stream()
                        .filter(oppgave -> oppgave.aktoerId() != null)
                        .map(this::lagOppgaveDto)
                        .toList());
            } catch (Exception e) {
                throw new IllegalStateException("FPFORDEL feilet å hente åpne oppgaver for enhet " + enhet + " med melding {} ", e);
            }
        }
        return oppgaverPåSaksbehandlersEnheter;
    }

    @GET
    @Path("/oppgave/detaljer")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(description = "Henter detaljer for en gitt jornalpostId som er relevante for å kunne ferdigstille journalføring på en fagsak.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public JournalpostDetaljerDto hentJournalpostDetaljer(@TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @QueryParam("journalpostId") @NotNull @Valid JournalpostIdDto journalpostId) {
        try {
            return Optional.ofNullable(arkiv.hentArkivJournalpost(journalpostId.getJournalpostId()))
                .map(this::mapTilJournalpostDetaljerDto)
                .orElseThrow();
        } catch (NoSuchElementException ex) {
            throw new TekniskException("FORDEL-123", "Journapost " + journalpostId.getJournalpostId() + " finnes ikke i arkivet.", ex);
        }
    }

    @GET
    @Path(DOKUMENT_HENT_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
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
        return fagsak.hentBrukersSaker(new AktørIdDto(aktørId))
            .stream()
            .map(ManuellJournalføringMapper::mapSakJournalføringDto)
            .toList();
    }
    private JournalpostDetaljerDto.BrukerDto mapBruker(String aktørId) {
        if (aktørId != null) {
            var fnr = pdl.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
            var navn = pdl.hentNavn(aktørId);
            return new JournalpostDetaljerDto.BrukerDto(navn, fnr, aktørId);
        }
        return null;
    }

    private OppgaveDto lagOppgaveDto(Oppgave oppgave) {
        var trimmetBeskrivelse = ManuellJournalføringMapper.tekstFraBeskrivelse(oppgave.beskrivelse());

        return new OppgaveDto(oppgave.id(), oppgave.journalpostId(), oppgave.aktoerId(), hentPersonIdent(oppgave.aktoerId()).orElse(null), ManuellJournalføringMapper.mapTilYtelseType(oppgave.behandlingstema()),
            oppgave.fristFerdigstillelse(), ManuellJournalføringMapper.mapPrioritet(oppgave.prioritet()), oppgave.beskrivelse(), trimmetBeskrivelse, oppgave.aktivDato(),
            harJournalpostMangler(trimmetBeskrivelse, oppgave.journalpostId()), oppgave.tildeltEnhetsnr());
    }

    private boolean harJournalpostMangler(String beskrivelse, String journalpostId) {
        return beskrivelse.startsWith("Journalføring") || utledOmTittelMangler(journalpostId);
    }

    private boolean utledOmTittelMangler(String journalpostId) {
        var journalpost = arkiv.hentArkivJournalpost(journalpostId);
        return  journalpost.getOriginalJournalpost().dokumenter().stream().anyMatch(d -> (d.tittel() == null)) || (journalpost.getTittel().isEmpty() && DokumentTypeId.UDEFINERT.equals(journalpost.getHovedtype()));
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

    public record OppgaveDto(@NotNull Long id, @NotNull String journalpostId, String aktørId, String fødselsnummer, @Valid YtelseTypeDto ytelseType,
                             @NotNull LocalDate frist, OppgavePrioritet prioritet, String beskrivelse, String trimmetBeskrivelse,
                             @NotNull LocalDate opprettetDato, @NotNull boolean journalpostHarMangler, String enhetId) {

    }

    public static class EmptyAbacDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }
}
