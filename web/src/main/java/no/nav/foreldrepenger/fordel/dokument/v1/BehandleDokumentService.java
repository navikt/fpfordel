package no.nav.foreldrepenger.fordel.dokument.v1;

import static no.nav.vedtak.log.util.LoggerUtils.removeLineBreaks;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
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
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;
import no.nav.vedtak.felles.jpa.Transaction;
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
@WebService(wsdlLocation = "wsdl/no/nav/tjeneste/virksomhet/behandleDokumentforsendelse/v1/behandleDokumentforsendelse.wsdl", serviceName = "BehandleDokumentforsendelse_v1", portName = "BehandleDokumentforsendelse_v1Port", endpointInterface = "no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.binding.BehandleDokumentforsendelseV1")
@SoapWebService(endpoint = "/sak/behandleDokument/v1", tjenesteBeskrivelseURL = "https://confluence.adeo.no/pages/viewpage.action?pageId=220529141")
public class BehandleDokumentService implements BehandleDokumentforsendelseV1 {

    public static final Logger logger = LoggerFactory.getLogger(BehandleDokumentService.class);

    static final String AVVIK_SAKSNUMMER = "FP-401245";
    static final String JOURNALPOST_MANGLER = "JournalpostId mangler";
    static final String ENHET_MANGLER = "EnhetId mangler";
    static final String SAKSNUMMER_UGYLDIG = "SakId (saksnummer) mangler eller er ugyldig";

    private TilJournalføringTjeneste tilJournalføringTjeneste;
    private HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste;
    private KlargjørForVLTjeneste klargjørForVLTjeneste;
    private FagsakRestKlient fagsakRestKlient;
    private KodeverkRepository kodeverkRepository;
    private AktørConsumer aktørConsumer;

    @Inject
    public BehandleDokumentService(TilJournalføringTjeneste tilJournalføringTjeneste,
            HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste,
            KlargjørForVLTjeneste klargjørForVLTjeneste, FagsakRestKlient fagsakRestKlient,
            KodeverkRepository kodeverkRepository,
            AktørConsumer aktørConsumer) {
        this.tilJournalføringTjeneste = tilJournalføringTjeneste;
        this.hentDataFraJoarkTjeneste = hentDataFraJoarkTjeneste;
        this.klargjørForVLTjeneste = klargjørForVLTjeneste;
        this.fagsakRestKlient = fagsakRestKlient;
        this.kodeverkRepository = kodeverkRepository;
        this.aktørConsumer = aktørConsumer;
    }

    public BehandleDokumentService() {
        // NOSONAR: for cdi
    }

    @Override
    public void ping() {
        logger.debug(removeLineBreaks("ping")); // NOSONAR
    }

    @Override
    @Transaction
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
        if (!optFagsakInfomasjonDto.isPresent()) {
            throw BehandleDokumentServiceFeil.FACTORY.finnerIkkeFagsak(saksnummer).toException();
        }

        FagsakInfomasjonDto fagsakInfomasjonDto = optFagsakInfomasjonDto.get();

        String behandlingstemaOffisiellKode = fagsakInfomasjonDto.getBehandlingstemaOffisiellKode();
        BehandlingTema behandlingTema = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class,
                behandlingstemaOffisiellKode, BehandlingTema.UDEFINERT);
        String aktørId = fagsakInfomasjonDto.getAktørId();

        Optional<JournalMetadata<DokumentTypeId>> optJournalMetadata = hentJournalMetadata(arkivId);
        final JournalMetadata<DokumentTypeId> journalMetadata = optJournalMetadata.get();
        final DokumentTypeId dokumentTypeId = journalMetadata.getDokumentTypeId() != null
                ? kodeverkRepository.finn(DokumentTypeId.class, journalMetadata.getDokumentTypeId())
                : DokumentTypeId.UDEFINERT;
        final DokumentKategori dokumentKategori = journalMetadata.getDokumentKategori() != null
                ? kodeverkRepository.finn(DokumentKategori.class, journalMetadata.getDokumentKategori())
                : DokumentKategori.UDEFINERT;
        behandlingTema = kodeverkRepository.finn(BehandlingTema.class,
                HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(Tema.FORELDRE_OG_SVANGERSKAPSPENGER,
                        behandlingTema, dokumentTypeId));

        validerKanJournalføres(saksnummer, fagsakInfomasjonDto, behandlingTema, dokumentTypeId, dokumentKategori);

        final String xml = hentDokumentSettMetadata(saksnummer, behandlingTema, aktørId, journalMetadata,
                dokumentTypeId);

        if (!JournalMetadata.Journaltilstand.ENDELIG.equals(journalMetadata.getJournaltilstand())) {
            logger.info(removeLineBreaks("Kaller tilJournalføring")); // NOSONAR
            String innhold = dokumentTypeId.getNavn() != null ? dokumentTypeId.getNavn() : "Ukjent innhold";
            if (!tilJournalføringTjeneste.tilJournalføring(arkivId, saksnummer, aktørId, enhetId, innhold)) {
                validerArkivId(null);
            }
        }

        klargjørForVLTjeneste.klargjørForVL(xml, saksnummer, arkivId, dokumentTypeId,
                journalMetadata.getForsendelseMottattTidspunkt(),
                behandlingTema, null, dokumentKategori, journalMetadata.getJournalførendeEnhet()); // TODO: Shekhar
                                                                                                   // forsendelseid null
    }

    private Optional<JournalMetadata<DokumentTypeId>> hentJournalMetadata(String arkivId)
            throws OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet {
        Optional<JournalMetadata<DokumentTypeId>> optJournalMetadata = hentDataFraJoarkTjeneste
                .hentHoveddokumentMetadata(arkivId);
        if (!optJournalMetadata.isPresent()) {
            JournalpostIkkeFunnet journalpostIkkeFunnet = new JournalpostIkkeFunnet();
            journalpostIkkeFunnet.setFeilmelding("Finner ikke journalpost med id " + arkivId);
            journalpostIkkeFunnet.setFeilaarsak("Finner ikke journalpost");
            throw new OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet(journalpostIkkeFunnet.getFeilmelding(),
                    journalpostIkkeFunnet);
        }
        return optJournalMetadata;
    }

    private void validerKanJournalføres(String saksnummer, FagsakInfomasjonDto fagsakInfomasjonDto,
            BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, DokumentKategori dokumentKategori) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTema) && (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)
                || DokumentKategori.KLAGE_ELLER_ANKE.equals(dokumentKategori))) {
            throw BehandleDokumentServiceFeil.FACTORY.sakUtenAvsluttetBehandling().toException();
        }
    }

    private String hentDokumentSettMetadata(String saksnummer, BehandlingTema behandlingTema, String aktørId,
            JournalMetadata<DokumentTypeId> journalMetadata, DokumentTypeId dokumentTypeId) {
        Optional<JournalDokument<DokumentTypeId>> journalDokument = hentDataFraJoarkTjeneste
                .hentStrukturertJournalDokument(journalMetadata);
        final String xml = journalDokument.map(JournalDokument::getXml).orElse(null);
        if (xml != null) {
            // Bruker eksisterende infrastruktur for å hente ut og validere XML-data.
            // Tasktype tilfeldig valgt
            ProsessTaskData prosessTaskData = new ProsessTaskData(KlargjorForVLTask.TASKNAME);
            MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);
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
                && !behandlingTema.ikkeSpesifikkHendelse()) {
            dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        }
        try {
            mottattDokument.kopierTilMottakWrapper(dataWrapper, aktørConsumer::hentAktørIdForPersonIdent);
        } catch (FunksjonellException e) {
            if (AVVIK_SAKSNUMMER.equals(e.getFeil().getKode())) {
                String logMessage = e.getFeil().getKode() + " " + e.getFeil().getFeilmelding();
                logger.info(logMessage);
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
            BehandlingTema behandlingTemaFraIM = kodeverkRepository.finnForKodeverkEiersTermNavn(BehandlingTema.class,
                    imType, BehandlingTema.UDEFINERT);
            if (behandlingTemaFraIM.gjelderForeldrepenger()) {
                if (!dataWrapper.getInntektsmeldingStartDato().isPresent()) { // Kommer ingen vei uten startdato
                    throw BehandleDokumentServiceFeil.FACTORY.imUtenStartdato().toException();
                } else if (!behandlingTema.gjelderForeldrepenger()) { // Prøver journalføre på annen
                                                                      // fagsak - ytelsetype
                    throw BehandleDokumentServiceFeil.FACTORY.imFeilType().toException();
                }
            } else if (!behandlingTemaFraIM.equals(behandlingTema)) {
                throw BehandleDokumentServiceFeil.FACTORY.imFeilType().toException();
            }
        }
        if (behandlingTema.gjelderForeldrepenger()
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
}
