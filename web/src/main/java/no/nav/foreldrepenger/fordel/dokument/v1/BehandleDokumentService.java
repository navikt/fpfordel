package no.nav.foreldrepenger.fordel.dokument.v1;

import static no.nav.vedtak.log.util.LoggerUtils.removeLineBreaks;

import java.time.LocalDate;
import java.util.Optional;
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
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.task.KlargjorForVLTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
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
    static final String ENHET_MANGLER = "EnhetId mangler";
    static final String SAKSNUMMER_UGYLDIG = "SakId (saksnummer) mangler eller er ugyldig";

    private final TilJournalføringTjeneste tilJournalføringTjeneste;
    private final HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste;
    private final KlargjørForVLTjeneste klargjørForVLTjeneste;
    private final FagsakRestKlient fagsakRestKlient;
    private final AktørConsumerMedCache aktørConsumer;
    private final ArkivTjeneste arkivTjeneste;

    @Inject
    public BehandleDokumentService(TilJournalføringTjeneste tilJournalføringTjeneste,
                                   HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste,
                                   KlargjørForVLTjeneste klargjørForVLTjeneste, FagsakRestKlient fagsakRestKlient,
                                   AktørConsumerMedCache aktørConsumer, ArkivTjeneste arkivTjeneste) {
        this.tilJournalføringTjeneste = tilJournalføringTjeneste;
        this.hentDataFraJoarkTjeneste = hentDataFraJoarkTjeneste;
        this.klargjørForVLTjeneste = klargjørForVLTjeneste;
        this.fagsakRestKlient = fagsakRestKlient;
        this.aktørConsumer = aktørConsumer;
        this.arkivTjeneste = arkivTjeneste;
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

        Optional<FagsakInfomasjonDto> optFagsakInfomasjonDto = fagsakRestKlient
                .finnFagsakInfomasjon(new SaksnummerDto(saksnummer));
        if (optFagsakInfomasjonDto.isEmpty()) {
            throw BehandleDokumentServiceFeil.FACTORY.finnerIkkeFagsak(saksnummer).toException();
        }

        var fagsakInfomasjonDto = optFagsakInfomasjonDto.get();

        String behandlingstemaOffisiellKode = fagsakInfomasjonDto.getBehandlingstemaOffisiellKode();
        BehandlingTema behandlingTema = BehandlingTema.fraOffisiellKode(behandlingstemaOffisiellKode);
        String aktørId = fagsakInfomasjonDto.getAktørId();

        Optional<JournalMetadata> optJournalMetadata = hentJournalMetadata(arkivId);
        final JournalMetadata journalMetadata = optJournalMetadata.get();
        final DokumentTypeId dokumentTypeIdJ = journalMetadata.getDokumentTypeId() != null
                ? journalMetadata.getDokumentTypeId()
                : DokumentTypeId.UDEFINERT;
        final DokumentTypeId dokumentTypeId = arkivTjeneste.loggSammenligningManuell(arkivId, dokumentTypeIdJ).orElse(dokumentTypeIdJ);
        final DokumentKategori dokumentKategori = journalMetadata.getDokumentKategori()
                .orElse(DokumentKategori.UDEFINERT);
        behandlingTema = HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(
                behandlingTema, dokumentTypeId);

        validerKanJournalføres(behandlingTema, dokumentTypeId, dokumentKategori);

        final String xml = hentDokumentSettMetadata(saksnummer, behandlingTema, aktørId, journalMetadata,
                dokumentTypeId);

        if (!JournalMetadata.Journaltilstand.ENDELIG.equals(journalMetadata.getJournaltilstand())) {
            LOG.info(removeLineBreaks("Kaller tilJournalføring")); // NOSONAR
            String innhold = dokumentTypeId.getTermNavn() != null ? dokumentTypeId.getTermNavn() : "Ukjent innhold";
            if (!tilJournalføringTjeneste.tilJournalføring(arkivId, saksnummer, aktørId, enhetId, innhold)) {
                validerArkivId(null);
            }
        }

        String eksternReferanseId = null;
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId))
            eksternReferanseId = journalMetadata.getKanalReferanseId();

        klargjørForVLTjeneste.klargjørForVL(xml, saksnummer, arkivId, dokumentTypeId,
                journalMetadata.getForsendelseMottattTidspunkt(),
                behandlingTema, null, dokumentKategori, journalMetadata.getJournalførendeEnhet(), eksternReferanseId); // TODO: Shekhar
                                                                                                   // forsendelseid null
    }

    private Optional<JournalMetadata> hentJournalMetadata(String arkivId)
            throws OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet {
        Optional<JournalMetadata> optJournalMetadata = hentDataFraJoarkTjeneste
                .hentHoveddokumentMetadata(arkivId);
        if (optJournalMetadata.isEmpty()) {
            JournalpostIkkeFunnet journalpostIkkeFunnet = new JournalpostIkkeFunnet();
            journalpostIkkeFunnet.setFeilmelding("Finner ikke journalpost med id " + arkivId);
            journalpostIkkeFunnet.setFeilaarsak("Finner ikke journalpost");
            throw new OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet(journalpostIkkeFunnet.getFeilmelding(),
                    journalpostIkkeFunnet);
        }
        return optJournalMetadata;
    }

    private void validerKanJournalføres(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId,
            DokumentKategori dokumentKategori) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTema) && (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)
                || DokumentKategori.KLAGE_ELLER_ANKE.equals(dokumentKategori))) {
            throw BehandleDokumentServiceFeil.FACTORY.sakUtenAvsluttetBehandling().toException();
        }
    }

    private String hentDokumentSettMetadata(String saksnummer, BehandlingTema behandlingTema, String aktørId,
            JournalMetadata journalMetadata, DokumentTypeId dokumentTypeId) {
        Optional<JournalDokument> journalDokument = hentDataFraJoarkTjeneste
                .hentStrukturertJournalDokument(journalMetadata);
        final String xml = journalDokument.map(JournalDokument::getXml).orElse(null);
        if (xml != null) {
            // Bruker eksisterende infrastruktur for å hente ut og validere XML-data.
            // Tasktype tilfeldig valgt
            ProsessTaskData prosessTaskData = new ProsessTaskData(KlargjorForVLTask.TASKNAME);
            MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
            dataWrapper.setBehandlingTema(behandlingTema);
            dataWrapper.setSaksnummer(saksnummer);
            dataWrapper.setAktørId(aktørId);
            validerXml(dataWrapper, behandlingTema, dokumentTypeId, xml);
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

    private void validerEnhetId(String enhetId) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (enhetId == null) {
            UgyldigInput ugyldigInput = lagUgyldigInput(ENHET_MANGLER);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private boolean erNullEllerTom(String s) {
        return (s == null || s.isEmpty());
    }

    private void validerXml(MottakMeldingDataWrapper dataWrapper, BehandlingTema behandlingTema,
            DokumentTypeId dokumentTypeId, String xml) {
        MottattStrukturertDokument<?> mottattDokument = MeldingXmlParser.unmarshallXml(xml);
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

        @FunksjonellFeil(feilkode = "FP-963076", feilmelding = "Inntektsmelding mangler startdato - kan ikke journalføre", løsningsforslag = "Be om ny Inntektsmelding med startdato", logLevel = LogLevel.WARN)
        Feil imUtenStartdato();

        @FunksjonellFeil(feilkode = "FP-963077", feilmelding = "For tidlig uttak", løsningsforslag = "Søknad om uttak med oppstart i 2018 skal journalføres mot sak i Infotrygd", logLevel = LogLevel.WARN)
        Feil forTidligUttak();

        @FunksjonellFeil(feilkode = "FP-424242", feilmelding = "Sak %s har åpen behandling med søknad", løsningsforslag = "Ferdigstill den åpne behandlingen før en ny søknad journalføres på saken", logLevel = LogLevel.WARN)
        Feil kanIkkeJournalføreSvpSøknadPåÅpenBehandling(String saksnummer);
    }

}
