package no.nav.foreldrepenger.mottak.task.dokumentforsendelse;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.foreldrepenger.mottak.task.MidlJournalføringTask;
import no.nav.foreldrepenger.mottak.task.OpprettSakTask;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.FPDateUtil;

@Dependent
@ProsessTask(BehandleDokumentforsendelseTask.TASKNAME)
public class BehandleDokumentforsendelseTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.behandleDokumentForsendelse";

    private final AktørConsumer aktørConsumer;
    private final FagsakRestKlient fagsakRestKlient;
    private final DokumentRepository dokumentRepository;
    private static final LocalDate konfigVerdiStartdatoForeldrepenger = KonfigVerdier.ENDRING_BEREGNING_DATO;

    private static final Logger logger = LoggerFactory.getLogger(BehandleDokumentforsendelseTask.class);

    @Inject
    public BehandleDokumentforsendelseTask(ProsessTaskRepository prosessTaskRepository,
            AktørConsumer aktørConsumer,
            FagsakRestKlient fagsakRestKlient,
            DokumentRepository dokumentRepository) {
        super(prosessTaskRepository);
        this.aktørConsumer = aktørConsumer;
        this.fagsakRestKlient = fagsakRestKlient;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getForsendelseId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getBehandlingTema() == null) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY, dataWrapper.getId()).toException();
        }

        if (TilJournalføringTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())) {
            postconditionJournalføring(dataWrapper);
        }

        if (HentOgVurderVLSakTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType()) ||
                OpprettSakTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())) {
            postConditionHentOgVurderVLSakOgOpprettSak(dataWrapper);
        }
    }

    private void postconditionJournalføring(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {

        UUID forsendelseId = dataWrapper.getForsendelseId().get(); // NOSONAR verifisert at finnes i precondition

        Optional<Dokument> hovedDokumentOpt = dokumentRepository.hentUnikDokument(forsendelseId, true,
                ArkivFilType.XML);
        DokumentMetadata metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);

        AtomicReference<Dokument> dokument = new AtomicReference<>();
        AtomicReference<BehandlingTema> behandlingTema = new AtomicReference<>();

        hovedDokumentOpt.ifPresent(dokument::set);
        behandlingTema.set(HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(
                dataWrapper.getHarTema() ? dataWrapper.getTema() : null, BehandlingTema.UDEFINERT,
                hovedDokumentOpt.map(Dokument::getDokumentTypeId).orElse(DokumentTypeId.UDEFINERT)));
        BehandlingTema tema = behandlingTema.get();
        dataWrapper.setBehandlingTema(tema);

        setFellesWrapperAttributter(dataWrapper, dokument.get(), metadata);

        // Nytt dokument for eksisterende sak
        if (metadata.getSaksnummer().isPresent()) {
            String saksnr = metadata.getSaksnummer().get(); // NOSONAR
            Optional<FagsakInfomasjonDto> fagInfoOpt = fagsakRestKlient.finnFagsakInfomasjon(new SaksnummerDto(saksnr));
            if (fagInfoOpt.isPresent()) {
                setFellesWrapperAttributterFraFagsak(dataWrapper, fagInfoOpt.get(), hovedDokumentOpt);
                return dataWrapper.nesteSteg(TilJournalføringTask.TASKNAME);
            } else {
                dataWrapper.setSaksnummer(null); // I påvente av at Bris sender korrekt saksnummer for endringssøknader
                                                 // + at de sender GSakNr (isf Infotrygdsaksnummer)
                return dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
            }
        }

        if (BehandlingTema.gjelderSvangerskapspenger(tema)) {
            logger.info("SVP dokument med ID {} er mottatt", forsendelseId);
            return dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
        }

        if (BehandlingTema.gjelderEngangsstønad(tema)
                || (BehandlingTema.gjelderForeldrepenger(tema)
                        && !sjekkOmSøknadenKreverManuellBehandling(dataWrapper))) {
            return dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
        }
        return dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
    }

    private void postConditionHentOgVurderVLSakOgOpprettSak(MottakMeldingDataWrapper dataWrapper) {
        if (OpprettSakTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())
                && dataWrapper.getForsendelseMottattTidspunkt().isEmpty()) {
            throw MottakMeldingFeil.FACTORY
                    .prosesstaskPostconditionManglerProperty(TASKNAME,
                            MottakMeldingDataWrapper.FORSENDELSE_MOTTATT_TIDSPUNKT_KEY, dataWrapper.getId())
                    .toException();
        }
        if (dataWrapper.getDokumentTypeId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getDokumentKategori().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.DOKUMENTKATEGORI_ID_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getPayloadAsString().isEmpty()) {
            throw MottakMeldingFeil.FACTORY
                    .prosesstaskPostconditionManglerProperty(TASKNAME, "payload", dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getHarTema()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.TEMA_KEY, dataWrapper.getId()).toException();
        }
    }

    private void setFellesWrapperAttributter(MottakMeldingDataWrapper dataWrapper, Dokument dokument,
            DokumentMetadata metadata) {
        if (!dataWrapper.getHarTema()) {
            dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        }
        if (metadata != null) {
            dataWrapper.setAktørId(metadata.getBrukerId());
            metadata.getArkivId().ifPresent(dataWrapper::setArkivId);
            metadata.getSaksnummer().ifPresent(dataWrapper::setSaksnummer);
        }
        if (dokument != null) {
            dataWrapper.setDokumentTypeId(dokument.getDokumentTypeId());
            utledDokumentKategori(dokument).ifPresent(dataWrapper::setDokumentKategori);
            dataWrapper.setPayload(dokument.getKlartekstDokument());
            kopierOgValiderAttributterFraSøknad(dataWrapper, dokument);
        }
        if (dataWrapper.getForsendelseMottattTidspunkt().isEmpty()) {
            dataWrapper.setForsendelseMottattTidspunkt(FPDateUtil.nå());
        }
    }

    private void setFellesWrapperAttributterFraFagsak(MottakMeldingDataWrapper dataWrapper,
            FagsakInfomasjonDto fagsakInfo, Optional<Dokument> dokumentInput) {
        BehandlingTema behandlingTemaFraSak = BehandlingTema.fraOffisiellKode(
                fagsakInfo.getBehandlingstemaOffisiellKode());

        if (dokumentInput.isPresent()) {
            Dokument dokument = dokumentInput.get();
            if (!fagsakInfo.getAktørId().equals(dataWrapper.getAktørId().orElse(null))) {
                throw BehandleDokumentforsendelseFeil.FACTORY.aktørIdMismatch().toException();
            }
            sjekkForMismatchMellomFagsakOgDokumentInn(dataWrapper.getBehandlingTema(), behandlingTemaFraSak, dokument);
        } else {
            UUID forsendelseId = dataWrapper.getForsendelseId().get(); // NOSONAR
            Dokument dokument = dokumentRepository.hentDokumenter(forsendelseId).stream().findFirst().get();
            dataWrapper.setDokumentTypeId(dokument.getDokumentTypeId());
            utledDokumentKategori(dokument).ifPresent(dataWrapper::setDokumentKategori);
            dataWrapper.setAktørId(fagsakInfo.getAktørId());
            dataWrapper.setBehandlingTema(behandlingTemaFraSak);
            dataWrapper.setForsendelseMottattTidspunkt(FPDateUtil.nå());
        }

    }

    private void sjekkForMismatchMellomFagsakOgDokumentInn(BehandlingTema behandlingTema,
            BehandlingTema fagsakTema, Dokument dokument) {

        if (DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD.equals(dokument.getDokumentTypeId())) {
            // Endringssøknad har ingen info om behandlingstema, slik vi kan ikke utlede
            // et spesifikt tema, så må ha løsere match. Se
            // HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType
            if (BehandlingTema.gjelderForeldrepenger(behandlingTema)) {
                return;
            }
        }
        if (fagsakTema.equals(behandlingTema)) {
            return;
        }

        throw BehandleDokumentforsendelseFeil.FACTORY.behandlingTemaMismatch(
                behandlingTema.getKode(), fagsakTema.getKode()).toException();
    }

    private void kopierOgValiderAttributterFraSøknad(MottakMeldingDataWrapper nesteSteg, Dokument dokument) {
        String xml = dokument.getKlartekstDokument();
        MottattStrukturertDokument<?> abstractMDto = MeldingXmlParser.unmarshallXml(xml);

        abstractMDto.kopierTilMottakWrapper(nesteSteg, aktørConsumer::hentAktørIdForPersonIdent);
    }

    private Optional<DokumentKategori> utledDokumentKategori(Dokument dokument) {
        DokumentTypeId dti = dokument.getDokumentTypeId();
        if (DokumentTypeId.erSøknadType(dti)) {
            return Optional.of(DokumentKategori.SØKNAD);
        } else if (DokumentTypeId.KLAGE_DOKUMENT.equals(dti)) {
            return Optional.of(DokumentKategori.KLAGE_ELLER_ANKE);
        }
        return Optional.empty();
    }

    /**
     * Startdato i mottatt søknaden er før fastsatt startdato for journalføring
     * gjennom VL
     */
    private boolean sjekkOmSøknadenKreverManuellBehandling(MottakMeldingDataWrapper dataWrapper) {
        return dataWrapper.getOmsorgsovertakelsedato()
                .orElse(dataWrapper.getFørsteUttaksdag().orElse(Tid.TIDENES_BEGYNNELSE))
                .isBefore(konfigVerdiStartdatoForeldrepenger);
    }

    private interface BehandleDokumentforsendelseFeil extends DeklarerteFeil {
        BehandleDokumentforsendelseTask.BehandleDokumentforsendelseFeil FACTORY = FeilFactory.create(BehandleDokumentforsendelseTask.BehandleDokumentforsendelseFeil.class);

        @TekniskFeil(feilkode = "FP-758390", feilmelding = "Søkers ID samsvarer ikke med søkers ID i eksisterende sak", logLevel = WARN)
        Feil aktørIdMismatch();

        @TekniskFeil(feilkode = "FP-756353", feilmelding = "BehandlingTema i forsendelse samsvarer ikke med BehandlingTema i eksisterende sak {%s : %s}", logLevel = WARN)
        Feil behandlingTemaMismatch(String behandlingTemaforsendelse, String behandlingTemaSak);
    }

}
