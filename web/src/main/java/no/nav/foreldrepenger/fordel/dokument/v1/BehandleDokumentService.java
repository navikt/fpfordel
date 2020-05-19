package no.nav.foreldrepenger.fordel.dokument.v1;

import static no.nav.vedtak.log.util.LoggerUtils.removeLineBreaks;

import java.time.LocalDate;
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
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.task.KlargjorForVLTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.sikkerhet.abac.AppAbacAttributtType;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.BehandleDokumentforsendelseV1;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.feil.JournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.meldinger.OppdaterOgFerdigstillJournalfoeringRequest;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
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

    static final String AVVIK_SAKSNUMMER = "FP-401245";
    static final String JOURNALPOST_MANGLER = "JournalpostId mangler";
    static final String JOURNALPOST_IKKE_INNGÅENDE = "Journalpost ikke Inngående";
    static final String ENHET_MANGLER = "EnhetId mangler";
    static final String SAKSNUMMER_UGYLDIG = "SakId (saksnummer) mangler eller er ugyldig";
    static final String BRUKER_MANGLER = "Journalpost mangler knyting til bruker - prøv igjen om et halv minutt";

    private final TilJournalføringTjeneste tilJournalføringTjeneste;
    private final KlargjørForVLTjeneste klargjørForVLTjeneste;
    private final FagsakRestKlient fagsakRestKlient;
    private final AktørConsumerMedCache aktørConsumer;
    private final ArkivTjeneste arkivTjeneste;
    private final DokumentRepository dokumentRepository;

    @Inject
    public BehandleDokumentService(TilJournalføringTjeneste tilJournalføringTjeneste,
                                   KlargjørForVLTjeneste klargjørForVLTjeneste,
                                   FagsakRestKlient fagsakRestKlient,
                                   AktørConsumerMedCache aktørConsumer,
                                   ArkivTjeneste arkivTjeneste,
                                   DokumentRepository dokumentRepository) {
        this.tilJournalføringTjeneste = tilJournalføringTjeneste;
        this.klargjørForVLTjeneste = klargjørForVLTjeneste;
        this.fagsakRestKlient = fagsakRestKlient;
        this.aktørConsumer = aktørConsumer;
        this.arkivTjeneste = arkivTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void ping() {
        LOG.debug(removeLineBreaks("ping"));
    }

    @Override
    @Transactional
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public void oppdaterOgFerdigstillJournalfoering(
            @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) OppdaterOgFerdigstillJournalfoeringRequest request)
            throws OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet,
            OppdaterOgFerdigstillJournalfoeringUgyldigInput {

        final String saksnummer = request.getSakId();
        validerSaksnummer(saksnummer);
        final String arkivId = request.getJournalpostId();
        validerArkivId(arkivId);
        final String enhetId = request.getEnhetId();
        validerEnhetId(enhetId);

        var fagsakInfomasjonDto = fagsakRestKlient.finnFagsakInfomasjon(new SaksnummerDto(saksnummer))
                .orElseThrow(() -> BehandleDokumentServiceFeil.FACTORY.finnerIkkeFagsak(saksnummer).toException());
        BehandlingTema behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(fagsakInfomasjonDto.getBehandlingstemaOffisiellKode());
        String aktørId = fagsakInfomasjonDto.getAktørId();

        var journalpost = hentJournalpost(arkivId);
        validerJournalposttype(journalpost.getJournalposttype());
        final DokumentTypeId dokumentTypeId = journalpost.getHovedtype();
        final DokumentKategori dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        BehandlingTema behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);

        BehandlingTema behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);

        validerKanJournalføres(behandlingTemaFagsak, dokumentTypeId, dokumentKategori);

        final String xml = hentDokumentSettMetadata(saksnummer, behandlingTema, aktørId, journalpost);

        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;
            if (!arkivTjeneste.oppdaterRettMangler(journalpost, aktørId, behandlingTema, brukDokumentTypeId))
                ugyldigBrukerPrøvIgjen(arkivId);
            LOG.info(removeLineBreaks("Kaller tilJournalføring")); // NOSONAR
            try {
                arkivTjeneste.ferdigstillJournalføring(journalpost.getJournalpostId(), saksnummer, enhetId);
            } catch (Exception e) {
                ugyldigBrukerPrøvIgjen(arkivId);
            }
        }

        UUID forsendelseId = getForsendelseId(journalpost.getEksternReferanseId());

        String eksternReferanseId = null;
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId))
            eksternReferanseId = journalpost.getEksternReferanseId();

        klargjørForVLTjeneste.klargjørForVL(xml, saksnummer, arkivId, dokumentTypeId, journalpost.getDatoOpprettet(),
                behandlingTema, forsendelseId, dokumentKategori, enhetId, eksternReferanseId);

        // For å unngå klonede journalposter fra Gosys - de kan komme via Kafka
        dokumentRepository.lagreJournalpostLokal(arkivId, journalpost.getKanal(), "ENDELIG", journalpost.getEksternReferanseId());
    }

    private UUID getForsendelseId(String eksternReferanseId) {
        try {
            return UUID.fromString(eksternReferanseId);
        } catch (Exception e) {
            return null;
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

    private BehandlingTema validerOgVelgBehandlingTema(BehandlingTema behandlingTemaFagsak, BehandlingTema behandlingTemaDok, DokumentTypeId dokumentTypeId) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaDok))
            return behandlingTemaFagsak;
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaFagsak))
            return behandlingTemaDok;
        if (!DokumentTypeId.erSøknadType(dokumentTypeId))
            return behandlingTemaFagsak;
        if ((BehandlingTema.gjelderForeldrepenger(behandlingTemaFagsak) && !BehandlingTema.gjelderForeldrepenger(behandlingTemaDok)) ||
                (BehandlingTema.gjelderEngangsstønad(behandlingTemaFagsak) && !BehandlingTema.gjelderEngangsstønad(behandlingTemaDok)) ||
                (BehandlingTema.gjelderSvangerskapspenger(behandlingTemaFagsak) && !BehandlingTema.gjelderSvangerskapspenger(behandlingTemaDok))) {
            throw BehandleDokumentServiceFeil.FACTORY.søknadFeilType().toException();
        }
        return BehandlingTema.ikkeSpesifikkHendelse(behandlingTemaDok) ? behandlingTemaFagsak : behandlingTemaDok;
    }

    private void validerKanJournalføres(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId,
            DokumentKategori dokumentKategori) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTema) && (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)
                || DokumentKategori.KLAGE_ELLER_ANKE.equals(dokumentKategori))) {
            throw BehandleDokumentServiceFeil.FACTORY.sakUtenAvsluttetBehandling().toException();
        }
    }

    private String hentDokumentSettMetadata(String saksnummer, BehandlingTema behandlingTema, String aktørId,
            ArkivJournalpost journalpost) {
        final String xml = journalpost.getStrukturertPayload();
        if (journalpost.getInnholderStrukturertInformasjon()) {
            // Bruker eksisterende infrastruktur for å hente ut og validere XML-data.
            // Tasktype tilfeldig valgt
            ProsessTaskData prosessTaskData = new ProsessTaskData(KlargjorForVLTask.TASKNAME);
            MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
            dataWrapper.setBehandlingTema(behandlingTema);
            dataWrapper.setSaksnummer(saksnummer);
            dataWrapper.setAktørId(aktørId);
            return validerXml(dataWrapper, behandlingTema, journalpost.getHovedtype(), xml);
        }
        return xml;
    }

    private void validerSaksnummer(String saksnummer) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (erNullEllerTom(saksnummer)) {
            UgyldigInput ugyldigInput = lagUgyldigInput(SAKSNUMMER_UGYLDIG);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private void validerArkivId(String arkivId) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (erNullEllerTom(arkivId)) {
            UgyldigInput ugyldigInput = lagUgyldigInput(JOURNALPOST_MANGLER);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private void ugyldigBrukerPrøvIgjen(String arkivId) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        LOG.warn("FPFORDEL oppdaterOgFerdigstillJournalfoering feiler for {}", arkivId);
        UgyldigInput ugyldigInput = lagUgyldigInput(BRUKER_MANGLER);
        throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
    }



    private void validerEnhetId(String enhetId) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (enhetId == null) {
            UgyldigInput ugyldigInput = lagUgyldigInput(ENHET_MANGLER);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private void validerJournalposttype(Journalposttype type) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (!Journalposttype.INNGÅENDE.equals(type)) {
            UgyldigInput ugyldigInput = lagUgyldigInput(JOURNALPOST_IKKE_INNGÅENDE);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private boolean erNullEllerTom(String s) {
        return (s == null || s.isEmpty());
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
            mottattDokument.kopierTilMottakWrapper(dataWrapper, aktørConsumer::hentAktørIdForPersonIdent);
        } catch (FunksjonellException e) {
            if (AVVIK_SAKSNUMMER.equals(e.getFeil().getKode())) {
                String logMessage = e.getFeil().getKode() + " " + e.getFeil().getFeilmelding();
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

    private void validerDokumentData(MottakMeldingDataWrapper dataWrapper, BehandlingTema behandlingTema,
            DokumentTypeId dokumentTypeId, String imType, LocalDate startDato) {
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            BehandlingTema behandlingTemaFraIM = BehandlingTema.fraTermNavn(imType);
            if (BehandlingTema.gjelderForeldrepenger(behandlingTemaFraIM)) {
                if (dataWrapper.getInntektsmeldingStartDato().isEmpty()) { // Kommer ingen vei uten startdato
                    throw BehandleDokumentServiceFeil.FACTORY.imUtenStartdato().toException();
                } else if (!BehandlingTema.gjelderForeldrepenger(behandlingTema)) { // Prøver journalføre på annen
                    // fagsak - ytelsetype
                    throw BehandleDokumentServiceFeil.FACTORY.imFeilType().toException();
                }
            } else if (!behandlingTemaFraIM.equals(behandlingTema)) {
                throw BehandleDokumentServiceFeil.FACTORY.imFeilType().toException();
            }
        }
        if (BehandlingTema.gjelderForeldrepenger(behandlingTema)
                && startDato.isBefore(KonfigVerdier.ENDRING_BEREGNING_DATO)) {
            throw BehandleDokumentServiceFeil.FACTORY.forTidligUttak().toException();
        }
    }

    private UgyldigInput lagUgyldigInput(String melding) {
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

    private interface BehandleDokumentServiceFeil extends DeklarerteFeil {

        BehandleDokumentService.BehandleDokumentServiceFeil FACTORY = FeilFactory
                .create(BehandleDokumentService.BehandleDokumentServiceFeil.class);

        @FunksjonellFeil(feilkode = "FP-963070", feilmelding = "Kan ikke journalføre på saksnummer: %s", løsningsforslag = "Journalføre dokument på annen sak i VL", logLevel = LogLevel.WARN)
        Feil finnerIkkeFagsak(String saksnummer);

        @FunksjonellFeil(feilkode = "FP-963074", feilmelding = "Klager må journalføres på sak med tidligere behandling", løsningsforslag = "Journalføre klagen på sak med avsluttet behandling", logLevel = LogLevel.WARN)
        Feil sakUtenAvsluttetBehandling();

        @FunksjonellFeil(feilkode = "FP-963075", feilmelding = "Inntektsmelding årsak samsvarer ikke med sakens type - kan ikke journalføre", løsningsforslag = "Be om ny Inntektsmelding for Foreldrepenger", logLevel = LogLevel.WARN)
        Feil imFeilType();

        @FunksjonellFeil(feilkode = "FP-963079", feilmelding = "Dokumentet samsvarer ikke med sakens type - kan ikke journalføre", løsningsforslag = "Journalfør på annen sak eller opprett ny sak", logLevel = LogLevel.WARN)
        Feil søknadFeilType();

        @FunksjonellFeil(feilkode = "FP-963076", feilmelding = "Inntektsmelding mangler startdato - kan ikke journalføre", løsningsforslag = "Be om ny Inntektsmelding med startdato", logLevel = LogLevel.WARN)
        Feil imUtenStartdato();

        @FunksjonellFeil(feilkode = "FP-963077", feilmelding = "For tidlig uttak", løsningsforslag = "Søknad om uttak med oppstart i 2018 skal journalføres mot sak i Infotrygd", logLevel = LogLevel.WARN)
        Feil forTidligUttak();
    }

}
