package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.*;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.fordel.web.server.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.journalføring.ManuellOpprettSakValidator;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.OpprettSakV2Dto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.foreldrepenger.typer.JournalpostId;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static no.nav.foreldrepenger.mapper.YtelseTypeMapper.mapFraDto;
import static no.nav.foreldrepenger.mapper.YtelseTypeMapper.mapTilDto;

/**
 * Enkelt REST tjeneste for å oppdatere og ferdigstille journalføring på dokumenter som kunne ikke
 * journalføres automatisk på fpsak saker. Brukes for å klargjøre og sende over saken til videre behandling i VL.
 * Gir mulighet å opprette saken i fpsak og så journalføre dokumentet på den nye saken.
 */
@Path("/sak")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Transactional
@Unprotected // midlertidig, fram til vi skrur over
public class FerdigstillJournalføringRestTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillJournalføringRestTjeneste.class);
    static final String JOURNALPOST_IKKE_INNGÅENDE = "Journalpost ikke Inngående";
    static final String BRUKER_MANGLER = "Journalpost mangler knyting til bruker - prøv igjen om et halv minutt";

    private VLKlargjører klargjører;
    private Fagsak fagsak;
    private PersonInformasjon pdl;
    private ArkivTjeneste arkivTjeneste;
    private DokumentRepository dokumentRepository;

    protected FerdigstillJournalføringRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public FerdigstillJournalføringRestTjeneste(VLKlargjører klargjører,
                                                Fagsak fagsak,
                                                PersonInformasjon pdl,
                                                ArkivTjeneste arkivTjeneste,
                                                DokumentRepository dokumentRepository) {
        this.klargjører = klargjører;
        this.fagsak = fagsak;
        this.pdl = pdl;
        this.arkivTjeneste = arkivTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    @POST
    @Path("/ferdigstill")
    @Operation(description = "For å ferdigstille journalføring. Det opprettes en ny fagsak om saksnummer ikke sendes.", tags = "Manuell journalføring", responses = {
            @ApiResponse(responseCode = "200", description = "Journalføring ferdigstillt"),
            @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),
    })
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    public void oppdaterOgFerdigstillJournalfoering(
        @Parameter(description = "Trenger journalpostId, saksnummer og enhet til ferdigstille en journalføring. " +
                "Om saksnummer ikke foreligger må ytelse type og aktørId oppgis for å opprette en ny sak.")
        @NotNull @Valid @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) FerdigstillJournalføringRestTjeneste.FerdigstillRequest request) {

        String saksnummer;

        var saksnummerFraRequest = Optional.ofNullable(request.saksnummer());

        validerJournalpostId(request.journalpostId());
        validerEnhetId(request.enhetId());

        var journalpostId = new JournalpostId(request.journalpostId());
        if (saksnummerFraRequest.isEmpty()) {
            var opprettSak = Optional.ofNullable(request.opprettSak())
                    .orElseThrow(() -> new TekniskException("FP-32354", "OpprettSakDto kan ikke være null ved opprettelse av en sak."));

            var ytelseType = mapFraDto(opprettSak.ytelseType());
            var aktørId = new AktørId(opprettSak.aktørId());

            new ManuellOpprettSakValidator(arkivTjeneste, fagsak)
                    .validerKonsistensMedSak(journalpostId, ytelseType, aktørId);

            saksnummer = fagsak.opprettSak(new OpprettSakV2Dto(journalpostId.getVerdi(), mapTilDto(ytelseType), aktørId.getId())).getSaksnummer();
        } else {
            saksnummer = saksnummerFraRequest.get();
        }

        // ikke nyttig lenger
        validerSaksnummer(saksnummer);

        final var journalpost = hentJournalpost(request.journalpostId());

        // Finn sak i fpsak med samme aktør
        final var fagsakFraRequestSomTrefferRettAktør =
                hentFagsakInfo(saksnummer)
                        .filter(rettAktør(journalpost.getBrukerAktørId()));

        FagsakInfomasjonDto fagsakInfomasjonDto;
        if (fagsakFraRequestSomTrefferRettAktør.isPresent()) {
            LOG.info("FPFORDEL JOURNALFØRING Fant en FP-sak med saksnummer {} som har rett aktør", saksnummerFraRequest);
            fagsakInfomasjonDto = fagsakFraRequestSomTrefferRettAktør.get();
        } else {
            throw new FunksjonellException("FP-963070", "Kan ikke journalføre på saksnummer: " + saksnummer,
                    "Journalføre dokument på annen sak i VL");
        }

        final BehandlingTema behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(fagsakInfomasjonDto.getBehandlingstemaOffisiellKode());
        final String fagsakInfoAktørId = fagsakInfomasjonDto.getAktørId();

        validerJournalposttype(journalpost.getJournalposttype());
        final DokumentTypeId dokumentTypeId = journalpost.getHovedtype();
        final DokumentKategori dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        final BehandlingTema behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);

        final BehandlingTema behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);

        validerKanJournalføres(behandlingTemaFagsak, dokumentTypeId, dokumentKategori);

        final String xml = hentDokumentSettMetadata(saksnummer, behandlingTema, fagsakInfoAktørId, journalpost);

        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;
            if (!arkivTjeneste.oppdaterRettMangler(journalpost, fagsakInfoAktørId, behandlingTema, brukDokumentTypeId)) {
                ugyldigBrukerPrøvIgjen(request.journalpostId(), null);
            }
            try {
                arkivTjeneste.settTilleggsOpplysninger(journalpost, brukDokumentTypeId);
            } catch (Exception e) {
                LOG.info("FPFORDEL JOURNALFØRING Feil ved setting av tilleggsopplysninger for journalpostId {}", journalpost.getJournalpostId());
            }
            LOG.info("FPFORDEL JOURNALFØRING Kaller tilJournalføring"); // NOSONAR
            try {
                arkivTjeneste.oppdaterMedSak(journalpost.getJournalpostId(), saksnummer, fagsakInfoAktørId);
                arkivTjeneste.ferdigstillJournalføring(journalpost.getJournalpostId(), request.enhetId());
            } catch (Exception e) {
                ugyldigBrukerPrøvIgjen(request.journalpostId(), e);
            }
        }

        final Optional<UUID> forsendelseId = asUUID(journalpost.getEksternReferanseId());

        String eksternReferanseId = null;
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            eksternReferanseId = journalpost.getEksternReferanseId() != null ? journalpost.getEksternReferanseId()
                    : arkivTjeneste.hentEksternReferanseId(journalpost.getOriginalJournalpost()).orElse(null);
        }

        var mottattTidspunkt = Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now);
        klargjører.klargjør(xml, saksnummer, request.journalpostId(), dokumentTypeId, mottattTidspunkt,
                behandlingTema, forsendelseId.orElse(null), dokumentKategori, request.enhetId(), eksternReferanseId);

        // For å unngå klonede journalposter fra GOSYS - de kan komme via Kafka
        dokumentRepository.lagreJournalpostLokal(request.journalpostId(), journalpost.getKanal(), "ENDELIG", journalpost.getEksternReferanseId());
    }

    private Optional<FagsakInfomasjonDto> hentFagsakInfo(String saksnummerFraArkiv) {
        return fagsak.finnFagsakInfomasjon(new SaksnummerDto(saksnummerFraArkiv));
    }

    private static Predicate<FagsakInfomasjonDto> rettAktør(Optional<String> brukerAktørId) {
        return f -> brukerAktørId.isEmpty() || Objects.equals(f.getAktørId(), brukerAktørId.get());
    }

    private static Optional<UUID> asUUID(String eksternReferanseId) {
        try {
            return Optional.of(UUID.fromString(eksternReferanseId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private ArkivJournalpost hentJournalpost(String arkivId) {
        try {
            return arkivTjeneste.hentArkivJournalpost(arkivId);
        } catch (Exception e) {
            LOG.warn("FORDEL WS fikk feil fra hentjournalpost: ", e);
            throw new TekniskException("FP-15676", lagUgyldigInputMelding("Journalpost", "Finner ikke journalpost med id " + arkivId));
        }
    }

    private static BehandlingTema validerOgVelgBehandlingTema(BehandlingTema behandlingTemaFagsak, BehandlingTema behandlingTemaDok,
                                                              DokumentTypeId dokumentTypeId) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaDok)) {
            return behandlingTemaFagsak;
        }
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaFagsak)) {
            return behandlingTemaDok;
        }
        if (!DokumentTypeId.erSøknadType(dokumentTypeId)) {
            return behandlingTemaFagsak;
        }
        if ((BehandlingTema.gjelderForeldrepenger(behandlingTemaFagsak) && !BehandlingTema.gjelderForeldrepenger(behandlingTemaDok)) ||
                (BehandlingTema.gjelderEngangsstønad(behandlingTemaFagsak) && !BehandlingTema.gjelderEngangsstønad(behandlingTemaDok)) ||
                (BehandlingTema.gjelderSvangerskapspenger(behandlingTemaFagsak) && !BehandlingTema.gjelderSvangerskapspenger(behandlingTemaDok))) {
            throw BehandleDokumentServiceFeil.søknadFeilType();
        }
        return BehandlingTema.ikkeSpesifikkHendelse(behandlingTemaDok) ? behandlingTemaFagsak : behandlingTemaDok;
    }

    private static void validerKanJournalføres(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId,
                                               DokumentKategori dokumentKategori) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTema) && (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)
                || DokumentKategori.KLAGE_ELLER_ANKE.equals(dokumentKategori))) {
            throw new FunksjonellException("FP-963074", "Klager må journalføres på sak med tidligere behandling",
                    "Journalføre klagen på sak med avsluttet behandling");
        }
    }

    private String hentDokumentSettMetadata(String saksnummer, BehandlingTema behandlingTema, String aktørId,
                                            ArkivJournalpost journalpost) {
        final String xml = journalpost.getStrukturertPayload();
        if (journalpost.getInnholderStrukturertInformasjon()) {
            // Bruker eksisterende infrastruktur for å hente ut og validere XML-data.
            // Tasktype tilfeldig valgt
            ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(VLKlargjørerTask.class);
            MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
            dataWrapper.setBehandlingTema(behandlingTema);
            dataWrapper.setSaksnummer(saksnummer);
            dataWrapper.setAktørId(aktørId);
            return validerXml(dataWrapper, behandlingTema, journalpost.getHovedtype(), xml);
        }
        return xml;
    }

    private static void validerSaksnummer(String saksnummer) {
        if (erNullEllerTom(saksnummer)) {
            throw new TekniskException("FP-15677", lagUgyldigInputMelding("Saksnummer", saksnummer));
        }
    }

    private static void validerJournalpostId(String journalpostId) {
        if (erNullEllerTom(journalpostId)) {
            throw new TekniskException("FP-15678", lagUgyldigInputMelding("ArkivId", journalpostId));
        }
    }

    private static void ugyldigBrukerPrøvIgjen(String arkivId, Exception e) {
        if (e != null) {
            LOG.warn("FPFORDEL JOURNALFØRING oppdaterOgFerdigstillJournalfoering feiler for {}", arkivId, e);
        }
        throw new TekniskException("FP-15678", lagUgyldigInputMelding("Bruker", BRUKER_MANGLER));
    }

    private static void validerEnhetId(String enhetId) {
        if (enhetId == null) {
            throw new TekniskException("FP-15679", lagUgyldigInputMelding("EnhetId", enhetId));
        }
    }

    private static void validerJournalposttype(Journalposttype type) {
        if (!Journalposttype.INNGÅENDE.equals(type)) {
            throw new TekniskException("FP-15680", lagUgyldigInputMelding("JournalpostType", JOURNALPOST_IKKE_INNGÅENDE));
        }
    }

    private static boolean erNullEllerTom(String s) {
        return ((s == null) || s.isEmpty());
    }

    private String validerXml(MottakMeldingDataWrapper dataWrapper, BehandlingTema behandlingTema,
                              DokumentTypeId dokumentTypeId, String xml) {
        MottattStrukturertDokument<?> mottattDokument;
        try {
            mottattDokument = MeldingXmlParser.unmarshallXml(xml);
        } catch (Exception e) {
            LOG.info("FPFORDEL JOURNALFØRING Journalpost med type {} er strukturert men er ikke gyldig XML", dokumentTypeId);
            return null;
        }
        if (DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD.equals(dokumentTypeId)
                && !BehandlingTema.ikkeSpesifikkHendelse(behandlingTema)) {
            dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        }
        try {
            mottattDokument.kopierTilMottakWrapper(dataWrapper, pdl::hentAktørIdForPersonIdent);
        } catch (FunksjonellException e) {
            // Her er det "greit" - da har man bestemt seg, men kan lage rot i saken.
            if ("FP-401245".equals(e.getKode())) {
                String logMessage = e.getMessage();
                LOG.info("FPFORDEL JOURNALFØRING {}", logMessage);
            } else {
                throw e;
            }
        }
        String imType = dataWrapper.getInntektsmeldingYtelse().orElse(null);
        LocalDate startDato = dataWrapper.getOmsorgsovertakelsedato()
                .orElse(dataWrapper.getFørsteUttaksdag().orElse(Tid.TIDENES_ENDE));
        validerDokumentData(dataWrapper, behandlingTema, dokumentTypeId, imType, startDato);
        return xml;
    }

    private static void validerDokumentData(MottakMeldingDataWrapper dataWrapper, BehandlingTema behandlingTema,
                                            DokumentTypeId dokumentTypeId, String imType, LocalDate startDato) {
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            BehandlingTema behandlingTemaFraIM = BehandlingTema.fraTermNavn(imType);
            if (BehandlingTema.gjelderForeldrepenger(behandlingTemaFraIM)) {
                if (dataWrapper.getInntektsmeldingStartDato().isEmpty()) { // Kommer ingen vei uten startdato
                    throw BehandleDokumentServiceFeil.imUtenStartdato();
                } else if (!BehandlingTema.gjelderForeldrepenger(behandlingTema)) { // Prøver journalføre på annen
                    // fagsak - ytelsetype
                    throw BehandleDokumentServiceFeil.imFeilType();
                }
            } else if (!behandlingTemaFraIM.equals(behandlingTema)) {
                throw BehandleDokumentServiceFeil.imFeilType();
            }
        }
        if (BehandlingTema.gjelderForeldrepenger(behandlingTema)
                && startDato.isBefore(KonfigVerdier.ENDRING_BEREGNING_DATO)) {
            throw BehandleDokumentServiceFeil.forTidligUttak();
        }
    }

    private static String lagUgyldigInputMelding(String feltnavn, String verdi) {
        return String.format("Ugyldig input: %s med verdi: %s er ugyldig input.", feltnavn, verdi);
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (FerdigstillRequest) obj;
            var opprett = AbacDataAttributter.opprett();
            if (req.opprettSak() != null) {
                opprett.leggTil(AppAbacAttributtType.AKTØR_ID, req.opprettSak().aktørId());
            }
            return opprett;
        }
    }

    private static class BehandleDokumentServiceFeil {

        private BehandleDokumentServiceFeil() {}

        static FunksjonellException imFeilType() {
            return new FunksjonellException("FP-963075", "Inntektsmelding årsak samsvarer ikke med sakens type - kan ikke journalføre",
                    "Be om ny Inntektsmelding for Foreldrepenger");
        }

        static FunksjonellException søknadFeilType() {
            return new FunksjonellException("FP-963079", "Dokumentet samsvarer ikke med sakens type - kan ikke journalføre",
                    "Journalfør på annen sak eller opprett ny sak");
        }

        static FunksjonellException imUtenStartdato() {
            return new FunksjonellException("FP-963076", "Inntektsmelding mangler startdato - kan ikke journalføre",
                    "Be om ny Inntektsmelding med startdato");
        }

        static FunksjonellException forTidligUttak() {
            return new FunksjonellException("FP-963077", "For tidlig uttak",
                    "Søknad om uttak med oppstart i 2018 skal journalføres mot sak i Infotrygd");
        }
    }

    record OpprettSakDto(
            @NotNull YtelseTypeDto ytelseType,
            @NotNull @Pattern(regexp = "^\\d{13}$", message = "aktørId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')") String aktørId) {
    }

    record FerdigstillRequest(
            @NotNull @Pattern(regexp = "^(-?[1-9]|[a-z0])[a-z0-9_:-]*$", message = "journalpostId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')") String journalpostId,
            @NotNull String enhetId,
            @Size(max = 11) @Pattern(regexp = "^[0-9_\\-]*$") String saksnummer,
            @Valid OpprettSakDto opprettSak) {
    }
}
