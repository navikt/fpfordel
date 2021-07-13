package no.nav.foreldrepenger.fordel.dokument.v1;

import static no.nav.vedtak.log.util.LoggerUtils.removeLineBreaks;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.fordel.web.server.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.fordel.web.server.abac.BeskyttetRessursAttributt;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.BehandleDokumentforsendelseV1;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.feil.JournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.meldinger.OppdaterOgFerdigstillJournalfoeringRequest;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.integrasjon.sak.v1.SakClient;
import no.nav.vedtak.felles.integrasjon.sak.v1.SakJson;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

/**
 * Webservice for å oppdatere og ferdigstille journalføring. For så å klargjøre
 * og sende over saken til videre behandling i VL.
 */

@Dependent
@WebService(wsdlLocation = "wsdl/no/nav/tjeneste/virksomhet/behandleDokumentforsendelse/v1/behandleDokumentforsendelse.wsdl", serviceName = "BehandleDokumentforsendelse_v1", portName = "BehandleDokumentforsendelse_v1Port", endpointInterface = "no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.BehandleDokumentforsendelseV1")
@SoapWebService(endpoint = "/sak/behandleDokument/v1", tjenesteBeskrivelseURL = "https://confluence.adeo.no/pages/viewpage.action?pageId=220529141")
public class BehandleDokumentService implements BehandleDokumentforsendelseV1 {

    public static final Logger LOG = LoggerFactory.getLogger(BehandleDokumentService.class);

    static final String JOURNALPOST_MANGLER = "JournalpostId mangler";
    static final String JOURNALPOST_IKKE_INNGÅENDE = "Journalpost ikke Inngående";
    static final String ENHET_MANGLER = "EnhetId mangler";
    static final String SAKSNUMMER_UGYLDIG = "SakId (saksnummer) mangler eller er ugyldig";
    static final String BRUKER_MANGLER = "Journalpost mangler knyting til bruker - prøv igjen om et halv minutt";

    private final VLKlargjører klargjører;
    private final Fagsak fagsak;
    private final PersonInformasjon pdl;
    private final ArkivTjeneste arkivTjeneste;
    private final DokumentRepository dokumentRepository;
    private final SakClient sakClient;

    @Inject
    public BehandleDokumentService(VLKlargjører klargjører,
            Fagsak fagsak,
            @Jersey SakClient sakClient,
            PersonInformasjon pdl,
            ArkivTjeneste arkivTjeneste,
            DokumentRepository dokumentRepository) {
        this.klargjører = klargjører;
        this.fagsak = fagsak;
        this.pdl = pdl;
        this.arkivTjeneste = arkivTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.sakClient = sakClient;
    }

    @Override
    public void ping() {
        LOG.debug(removeLineBreaks("ping"));
    }

    @Override
    @Transactional
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = BeskyttetRessursAttributt.FAGSAK)
    public void oppdaterOgFerdigstillJournalfoering(
            @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) OppdaterOgFerdigstillJournalfoeringRequest request)
            throws OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet,
            OppdaterOgFerdigstillJournalfoeringUgyldigInput {

        final var saksnrFraRequest = request.getSakId();
        validerSaksnummer(saksnrFraRequest);
        validerArkivId(request.getJournalpostId());
        validerEnhetId(request.getEnhetId());

        final var journalpost = hentJournalpost(request.getJournalpostId());

        var fagsakInformasjon = fagsak.finnFagsakInfomasjon(new SaksnummerDto(saksnrFraRequest))
            .filter(f -> journalpost.getBrukerAktørId().isEmpty() || Objects.equals(f.getAktørId(), journalpost.getBrukerAktørId().get()));
        String saksnummer;
        if (fagsakInformasjon.isPresent()) {
            saksnummer = saksnrFraRequest;
        } else {
            try {
                final var funnetSaksnummer = Optional.ofNullable(sakClient.hentSakId(saksnrFraRequest)).map(SakJson::getFagsakNr).orElseThrow();
                LOG.info("FPFORDEL GOSYS slår opp fagsak {} finner {}", saksnrFraRequest, funnetSaksnummer);
                saksnummer = funnetSaksnummer;
            } catch (Exception e1) {
                throw BehandleDokumentServiceFeil.finnerIkkeFagsak(saksnrFraRequest);
            }
        }
        // Gosys sender alltid arkivsaksnummer- dvs sak.id
        final var fagsakInfomasjonDto = fagsak.finnFagsakInfomasjon(new SaksnummerDto(saksnummer))
            .orElseThrow(() -> BehandleDokumentServiceFeil.finnerIkkeFagsak(saksnummer));
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
                ugyldigBrukerPrøvIgjen(request.getJournalpostId(), null);
            }
            LOG.info(removeLineBreaks("Kaller tilJournalføring")); // NOSONAR
            try {
                arkivTjeneste.oppdaterMedSak(journalpost.getJournalpostId(), saksnummer, fagsakInfoAktørId);
                arkivTjeneste.ferdigstillJournalføring(journalpost.getJournalpostId(), request.getEnhetId());
            } catch (Exception e) {
                ugyldigBrukerPrøvIgjen(request.getJournalpostId(), e);
            }
        }

        final Optional<UUID> forsendelseId = asUUID(journalpost.getEksternReferanseId());

        String eksternReferanseId = null;
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            eksternReferanseId = journalpost.getEksternReferanseId() != null ? journalpost.getEksternReferanseId()
                    : arkivTjeneste.hentEksternReferanseId(journalpost.getOriginalJournalpost()).orElse(null);
        }

        klargjører.klargjør(xml, saksnummer, request.getJournalpostId(), dokumentTypeId, journalpost.getDatoOpprettet(),
                behandlingTema, forsendelseId.orElse(null), dokumentKategori, request.getEnhetId(), eksternReferanseId);

        // For å unngå klonede journalposter fra Gosys - de kan komme via Kafka
        dokumentRepository.lagreJournalpostLokal(request.getJournalpostId(), journalpost.getKanal(), "ENDELIG", journalpost.getEksternReferanseId());
    }

    private static Optional<UUID> asUUID(String eksternReferanseId) {
        try {
            return Optional.of(UUID.fromString(eksternReferanseId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private ArkivJournalpost hentJournalpost(String arkivId) throws OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet {
        try {
            return arkivTjeneste.hentArkivJournalpost(arkivId);
        } catch (Exception e) {
            JournalpostIkkeFunnet journalpostIkkeFunnet = new JournalpostIkkeFunnet();
            journalpostIkkeFunnet.setFeilmelding("Finner ikke journalpost med id " + arkivId);
            journalpostIkkeFunnet.setFeilaarsak("Finner ikke journalpost");
            throw new OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet(journalpostIkkeFunnet.getFeilmelding(),
                    journalpostIkkeFunnet);
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
            throw BehandleDokumentServiceFeil.sakUtenAvsluttetBehandling();
        }
    }

    private String hentDokumentSettMetadata(String saksnummer, BehandlingTema behandlingTema, String aktørId,
            ArkivJournalpost journalpost) {
        final String xml = journalpost.getStrukturertPayload();
        if (journalpost.getInnholderStrukturertInformasjon()) {
            // Bruker eksisterende infrastruktur for å hente ut og validere XML-data.
            // Tasktype tilfeldig valgt
            ProsessTaskData prosessTaskData = new ProsessTaskData(VLKlargjørerTask.TASKNAME);
            MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
            dataWrapper.setBehandlingTema(behandlingTema);
            dataWrapper.setSaksnummer(saksnummer);
            dataWrapper.setAktørId(aktørId);
            return validerXml(dataWrapper, behandlingTema, journalpost.getHovedtype(), xml);
        }
        return xml;
    }

    private static void validerSaksnummer(String saksnummer) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (erNullEllerTom(saksnummer)) {
            UgyldigInput ugyldigInput = lagUgyldigInput(SAKSNUMMER_UGYLDIG);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private static void validerArkivId(String arkivId) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (erNullEllerTom(arkivId)) {
            UgyldigInput ugyldigInput = lagUgyldigInput(JOURNALPOST_MANGLER);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private static void ugyldigBrukerPrøvIgjen(String arkivId, Exception e) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (e != null) {
            LOG.warn("FPFORDEL oppdaterOgFerdigstillJournalfoering feiler for {}", arkivId, e);
        }
        UgyldigInput ugyldigInput = lagUgyldigInput(BRUKER_MANGLER);
        throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
    }

    private static void validerEnhetId(String enhetId) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (enhetId == null) {
            UgyldigInput ugyldigInput = lagUgyldigInput(ENHET_MANGLER);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private static void validerJournalposttype(Journalposttype type) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (!Journalposttype.INNGÅENDE.equals(type)) {
            UgyldigInput ugyldigInput = lagUgyldigInput(JOURNALPOST_IKKE_INNGÅENDE);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
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
            LOG.info("Journalpost med type {} er strukturert men er ikke gyldig XML", dokumentTypeId);
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
                LOG.info(logMessage);
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

    /*
     * Midlertidig håndtering mens Gosys fikser koden som identifiserer saksnummer.
     * Redusere kompleksitet og risk ved prodsetting. Rekursjon med stopp
     */

    private static UgyldigInput lagUgyldigInput(String melding) {
        UgyldigInput faultInfo = new UgyldigInput();
        faultInfo.setFeilmelding(melding);
        faultInfo.setFeilaarsak("Ugyldig input");
        return faultInfo;
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            OppdaterOgFerdigstillJournalfoeringRequest req = (OppdaterOgFerdigstillJournalfoeringRequest) obj;
            var attributter = AbacDataAttributter.opprett()
                    .leggTil(AppAbacAttributtType.SAKSNUMMER, req.getSakId());
            if (req.getJournalpostId() != null) {
                attributter.leggTil(AppAbacAttributtType.JOURNALPOST_ID, req.getJournalpostId());
            }
            return attributter;
        }
    }

    private static class BehandleDokumentServiceFeil {

        private BehandleDokumentServiceFeil() {

        }

        static FunksjonellException finnerIkkeFagsak(String saksnummer) {
            return new FunksjonellException("FP-963070", String.format("Kan ikke journalføre på saksnummer: %s", saksnummer),
                    "Journalføre dokument på annen sak i VL");
        }

        static FunksjonellException sakUtenAvsluttetBehandling() {
            return new FunksjonellException("FP-963074", "Klager må journalføres på sak med tidligere behandling",
                    "Journalføre klagen på sak med avsluttet behandling");
        }

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

}
