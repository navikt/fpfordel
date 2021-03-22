package no.nav.foreldrepenger.mottak.task.dokumentforsendelse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.klient.FagsakTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.MidlJournalføringTask;
import no.nav.foreldrepenger.mottak.task.OpprettSakTask;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.Destinasjon;
import no.nav.foreldrepenger.mottak.tjeneste.VurderVLSaker;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@Dependent
@ProsessTask(BehandleDokumentforsendelseTask.TASKNAME)
public class BehandleDokumentforsendelseTask extends WrappedProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BehandleDokumentforsendelseTask.class);

    public static final String TASKNAME = "fordeling.behandleDokumentForsendelse";

    private final PersonInformasjon aktørConsumer;
    private final FagsakTjeneste fagsakRestKlient;
    private final VurderVLSaker vurderVLSaker;
    private final DokumentRepository dokumentRepository;

    @Inject
    public BehandleDokumentforsendelseTask(ProsessTaskRepository prosessTaskRepository,
            VurderVLSaker vurderVLSaker,
            PersonInformasjon aktørConsumer,
            /* @Jersey */FagsakTjeneste fagsakRestKlient,
            DokumentRepository dokumentRepository) {
        super(prosessTaskRepository);
        this.vurderVLSaker = vurderVLSaker;
        this.aktørConsumer = aktørConsumer;
        this.fagsakRestKlient = fagsakRestKlient;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getForsendelseId().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, dataWrapper.getId());
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId());
        }
        if (dataWrapper.getBehandlingTema() == null) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY, dataWrapper.getId());
        }

        if (TilJournalføringTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())) {
            postconditionJournalføring(dataWrapper);
        }

        if (TilJournalføringTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())) {
            postConditionHentOgVurderVLSakOgOpprettSak(dataWrapper);
        }
    }

    private static void postconditionJournalføring(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId());
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {

        UUID forsendelseId = dataWrapper.getForsendelseId().get();
        var hovedDokumentOpt = dokumentRepository.hentUnikDokument(forsendelseId, true,
                ArkivFilType.XML);
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);

        var behandlingTema = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT,
                hovedDokumentOpt.map(Dokument::getDokumentTypeId).orElse(DokumentTypeId.UDEFINERT));
        dataWrapper.setBehandlingTema(behandlingTema);

        LOG.info("FPFORDEL BdTask entry bt {}", behandlingTema);

        setFellesWrapperAttributter(dataWrapper, hovedDokumentOpt.orElse(null), metadata);

        Destinasjon destinasjon;


        if (metadata.getSaksnummer().isPresent()) {
            String saksnr = metadata.getSaksnummer().get(); // NOSONAR
            Optional<FagsakInfomasjonDto> fagInfoOpt = fagsakRestKlient.finnFagsakInfomasjon(new SaksnummerDto(saksnr));
            if (fagInfoOpt.isPresent()) {
                setFellesWrapperAttributterFraFagsak(dataWrapper, fagInfoOpt.get(), hovedDokumentOpt);
                destinasjon = new Destinasjon(ForsendelseStatus.FPSAK, saksnr);
            } else {
                dataWrapper.setSaksnummer(null); // Sendt inn på infotrygd-sak
                destinasjon = new Destinasjon(ForsendelseStatus.GOSYS, null);
            }
        } else {
            destinasjon = vurderVLSaker.bestemDestinasjon(dataWrapper);
        }
        LOG.info("FPFORDEL BdTask destinasjon {}", destinasjon );

        /*
         * Her kan man vurdere å journalføre lokalt - enten midlertidig journalføring av alle
         * eller endelig av de som skal til fpsak og har saksnummer
         * Nå opprettes det Sak her - det kunne vært opprettet journalpost i stedet og sendt til opprettsak
         * Det vil forenklee opprettsak + tiljournalføring dersom det finnes en midlertidig journalpost.
         * OBS: Unngå opprettjournalpost og opprettsak i samme task - hvis den ene feiler blir det mayhem for SBH
         */
        if (ForsendelseStatus.GOSYS.equals(destinasjon.system())) {
            LOG.info("FPFORDEL BdTask exit GOSYS {}", destinasjon );
            return dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
        } else {
            dataWrapper.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)));
            LOG.info("FPFORDEL BdTask exit FPSAK før opprett {}", destinasjon );
            var saksnummer = Optional.ofNullable(destinasjon.saksnummer())
                    .orElseGet(() -> vurderVLSaker.opprettSak(dataWrapper));
            dataWrapper.setSaksnummer(saksnummer);
            LOG.info("FPFORDEL BdTask exit FPSAK etter opprett {}", dataWrapper.getSaksnummer() );
            return dataWrapper.nesteSteg(TilJournalføringTask.TASKNAME);
        }
    }

    private static void postConditionHentOgVurderVLSakOgOpprettSak(MottakMeldingDataWrapper dataWrapper) {
        if (OpprettSakTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())
                && dataWrapper.getForsendelseMottattTidspunkt().isEmpty()) {
            throw MottakMeldingFeil
                    .prosesstaskPostconditionManglerProperty(TASKNAME,
                            MottakMeldingDataWrapper.FORSENDELSE_MOTTATT_TIDSPUNKT_KEY, dataWrapper.getId());
        }
        if (dataWrapper.getDokumentTypeId().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY, dataWrapper.getId());
        }
        if (dataWrapper.getDokumentKategori().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.DOKUMENTKATEGORI_ID_KEY, dataWrapper.getId());
        }
        if (DokumentTypeId.erSøknadType(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))
            && dataWrapper.getPayloadAsString().isEmpty()) {
            throw MottakMeldingFeil
                    .prosesstaskPostconditionManglerProperty(TASKNAME, "payload", dataWrapper.getId());
        }
        if (!dataWrapper.getHarTema()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.TEMA_KEY, dataWrapper.getId());
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
            dataWrapper.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(dokument.getDokumentTypeId()));
            dataWrapper.setPayload(dokument.getKlartekstDokument());
            kopierOgValiderAttributterFraSøknad(dataWrapper, dokument);
        }
        if (dataWrapper.getForsendelseMottattTidspunkt().isEmpty()) {
            dataWrapper.setForsendelseMottattTidspunkt(LocalDateTime.now());
        }
    }

    private void setFellesWrapperAttributterFraFagsak(MottakMeldingDataWrapper dataWrapper,
            FagsakInfomasjonDto fagsakInfo, Optional<Dokument> dokumentInput) {
        BehandlingTema behandlingTemaFraSak = BehandlingTema.fraOffisiellKode(
                fagsakInfo.getBehandlingstemaOffisiellKode());

        if (dokumentInput.isPresent()) {
            Dokument dokument = dokumentInput.get();
            if (!fagsakInfo.getAktørId().equals(dataWrapper.getAktørId().orElse(null))) {
                throw BehandleDokumentforsendelseFeil.aktørIdMismatch();
            }
            sjekkForMismatchMellomFagsakOgDokumentInn(dataWrapper.getBehandlingTema(), behandlingTemaFraSak, dokument);
        } else {
            // Vedlegg - mangler hoveddokument
            settDokumentTypeKategoriKorrigerSVP(dataWrapper);
            dataWrapper.setAktørId(fagsakInfo.getAktørId());
            dataWrapper.setBehandlingTema(behandlingTemaFraSak);
            dataWrapper.setForsendelseMottattTidspunkt(LocalDateTime.now());
        }
    }

    // TODO: Endre når TFP-4125 er fikset i søknadSvangerskapspenger. Nå kommer vedlegg som søknad og roter til mye.
    private void settDokumentTypeKategoriKorrigerSVP(MottakMeldingDataWrapper w) {
        var dokumenter = w.getForsendelseId().map(dokumentRepository::hentDokumenter).orElse(List.of());
        var svpSøknader = dokumenter.stream()
            .filter(d -> DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER.equals(d.getDokumentTypeId()))
            .collect(Collectors.toList());
        var svpStrukturert = svpSøknader.stream().anyMatch(d -> ArkivFilType.XML.equals(d.getArkivFilType()));
        var strukturert = dokumenter.stream().filter(d -> ArkivFilType.XML.equals(d.getArkivFilType())).findFirst();
        var brukdokument = strukturert.orElseGet(() -> dokumenter.stream().findFirst().orElseThrow());
        if (DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER.equals(brukdokument.getDokumentTypeId()) && strukturert.isEmpty()) {
            w.setDokumentTypeId(DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG);
            w.setDokumentKategori(DokumentKategori.IKKE_TOLKBART_SKJEMA);
        } else {
            var doktype = ArkivUtil.utledHovedDokumentType(dokumenter.stream().map(Dokument::getDokumentTypeId).collect(Collectors.toSet()));
            w.setDokumentTypeId(doktype);
            w.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(doktype));
        }
        if (!svpSøknader.isEmpty() && !svpStrukturert) {
            svpSøknader.forEach(d -> {
                d.setDokumentTypeId(DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG);
                dokumentRepository.lagre(d);
            });
        }
    }

    private static void sjekkForMismatchMellomFagsakOgDokumentInn(BehandlingTema behandlingTema,
            BehandlingTema fagsakTema, Dokument dokument) {

        if (DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD.equals(dokument.getDokumentTypeId())) {
            // Endringssøknad har ingen info om behandlingstema, slik vi kan ikke utlede
            // et spesifikt tema, så må ha løsere match. Se
            // ArkivUtil.korrigerBehandlingTemaFraDokumentType
            if (BehandlingTema.gjelderForeldrepenger(behandlingTema)) {
                return;
            }
        }
        if (!fagsakTema.equals(behandlingTema)) {
            throw BehandleDokumentforsendelseFeil.behandlingTemaMismatch(
                    behandlingTema.getKode(), fagsakTema.getKode());
        }

    }

    private void kopierOgValiderAttributterFraSøknad(MottakMeldingDataWrapper nesteSteg, Dokument dokument) {
        String xml = dokument.getKlartekstDokument();
        MottattStrukturertDokument<?> abstractMDto = MeldingXmlParser.unmarshallXml(xml);

        abstractMDto.kopierTilMottakWrapper(nesteSteg, aktørConsumer::hentAktørIdForPersonIdent);
    }

    static private class BehandleDokumentforsendelseFeil {

        static TekniskException aktørIdMismatch() {
            return new TekniskException("FP-758390", "Søkers ID samsvarer ikke med søkers ID i eksisterende sak");
        }

        static TekniskException behandlingTemaMismatch(String behandlingTemaforsendelse, String behandlingTemaSak) {
            return new TekniskException("FP-756353",
                    String.format("BehandlingTema i forsendelse samsvarer ikke med BehandlingTema i eksisterende sak {%s : %s}",
                            behandlingTemaforsendelse, behandlingTemaSak));

        }
    }

}
