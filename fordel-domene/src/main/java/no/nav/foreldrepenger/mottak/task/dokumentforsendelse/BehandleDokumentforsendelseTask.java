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
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.felles.kafka.HendelseProdusent;
import no.nav.foreldrepenger.mottak.felles.kafka.SøknadFordeltOgJournalførtHendelse;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.OpprettetJournalpost;
import no.nav.foreldrepenger.mottak.klient.FagsakTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.KlargjorForVLTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.Destinasjon;
import no.nav.foreldrepenger.mottak.tjeneste.VurderVLSaker;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/*
 * Hovedgangen i tasken
 * - hent data fra forsendelse metadata + dokument og populer wrapper
 * - finn destinasjon (GOSYS/FPSAK/FPSAKsnummer) for forsendelsen (utledes i fpsak)
 * - opprett sak i VL dersom destinasjon FPSAK uten angitt saksnummer
 * - Journalfør forsendelse og få journalpostId - GOSYS = midlertidig, FPSAK = endelig
 * - Send hendelse til historikk og oppdater destinasjon i forsendelsen
 * - Neste steg: Gosys eller Innsending til VL
 *
 * OBS: minimer risiko for exception etter journalføring - vasnskelig å rydde
 */
@Dependent
@ProsessTask(BehandleDokumentforsendelseTask.TASKNAME)
public class BehandleDokumentforsendelseTask extends WrappedProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BehandleDokumentforsendelseTask.class);

    public static final String TASKNAME = "fordeling.behandleDokumentForsendelse";

    private final PersonInformasjon aktørConsumer;
    private final FagsakTjeneste fagsakRestKlient;
    private final VurderVLSaker vurderVLSaker;
    private final ArkivTjeneste arkivTjeneste;
    private final DokumentRepository dokumentRepository;
    private final HendelseProdusent hendelseProdusent;

    @Inject
    public BehandleDokumentforsendelseTask(ProsessTaskRepository prosessTaskRepository,
            VurderVLSaker vurderVLSaker,
            PersonInformasjon aktørConsumer,
            /* @Jersey */FagsakTjeneste fagsakRestKlient,
            ArkivTjeneste arkivTjeneste,
            DokumentRepository dokumentRepository,
            HendelseProdusent hendelseProdusent) {
        super(prosessTaskRepository);
        this.vurderVLSaker = vurderVLSaker;
        this.aktørConsumer = aktørConsumer;
        this.fagsakRestKlient = fagsakRestKlient;
        this.arkivTjeneste = arkivTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.hendelseProdusent = hendelseProdusent;
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

        if (KlargjorForVLTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())) {
            postconditionJournalføringMedSak(dataWrapper);
        }

        postConditionHentOgVurderVLSakOgOpprettSak(dataWrapper);
    }

    private static void postconditionJournalføringMedSak(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId());
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {

        UUID forsendelseId = dataWrapper.getForsendelseId().orElseThrow();
        var hovedDokumentOpt = dokumentRepository.hentUnikDokument(forsendelseId, true,
                ArkivFilType.XML);
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);

        var behandlingTema = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT,
                hovedDokumentOpt.map(Dokument::getDokumentTypeId).orElse(DokumentTypeId.UDEFINERT));
        dataWrapper.setBehandlingTema(behandlingTema);

        LOG.info("FPFORDEL BdTask entry bt {}", behandlingTema);

        setFellesWrapperAttributter(dataWrapper, hovedDokumentOpt.orElse(null), metadata);

        var destinasjon = getDestinasjonOppdaterWrapper(dataWrapper, hovedDokumentOpt, metadata);
        LOG.info("FPFORDEL BdTask destinasjon {}", destinasjon );

        dataWrapper.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)));
        destinasjon = utledSaksnummerOpprettSakVedBehov(destinasjon, dataWrapper);
        dataWrapper.setSaksnummer(destinasjon.saksnummer());

        var journalpost = opprettJournalpostFerdigstillHvisSaksnummer(forsendelseId, dataWrapper, destinasjon.saksnummer());
        dataWrapper.setArkivId(journalpost.journalpostId());
        if (!journalpost.ferdigstilt()) {
            // Det vil komme en Kafka-hendelse om noen sekunder - denne sørger for at vi ikke trigger på den.
            dokumentRepository.lagreJournalpostLokal(dataWrapper.getArkivId(),
                    MottakKanal.SELVBETJENING.getKode(), "MIDLERTIDIG", forsendelseId.toString());
        }

        return utledNesteSteg(dataWrapper, forsendelseId, destinasjon, journalpost.ferdigstilt());
    }

    private Destinasjon getDestinasjonOppdaterWrapper(MottakMeldingDataWrapper dataWrapper, Optional<Dokument> hovedDokumentOpt, DokumentMetadata metadata) {
        if (metadata.getSaksnummer().isPresent()) {
            String saksnr = metadata.getSaksnummer().get(); // NOSONAR
            Optional<FagsakInfomasjonDto> fagInfoOpt = fagsakRestKlient.finnFagsakInfomasjon(new SaksnummerDto(saksnr));
            if (fagInfoOpt.isPresent()) {
                setFellesWrapperAttributterFraFagsak(dataWrapper, fagInfoOpt.get(), hovedDokumentOpt);
                return new Destinasjon(ForsendelseStatus.FPSAK, saksnr);
            } else {
                if (hovedDokumentOpt.isEmpty()) {
                    settDokumentTypeKategoriKorrigerSVP(dataWrapper);
                }
                dataWrapper.setSaksnummer(null); // Sendt inn på infotrygd-sak
                return Destinasjon.GOSYS;
            }
        } else {
            return vurderVLSaker.bestemDestinasjon(dataWrapper);
        }
    }

    private Destinasjon utledSaksnummerOpprettSakVedBehov(Destinasjon destinasjon, MottakMeldingDataWrapper w) {
        if (!ForsendelseStatus.FPSAK.equals(destinasjon.system())) return Destinasjon.GOSYS;
        return new Destinasjon(ForsendelseStatus.FPSAK,
                Optional.ofNullable(destinasjon.saksnummer()).orElseGet(() -> vurderVLSaker.opprettSak(w)));
    }

    private OpprettetJournalpost opprettJournalpostFerdigstillHvisSaksnummer(UUID forsendelseId, MottakMeldingDataWrapper w, String saksnummer) {
        var avsenderId = w.getAvsenderId().or(w::getAktørId)
                .orElseThrow(() -> new IllegalStateException("Hvor ble det av brukers id?"));

        OpprettetJournalpost opprettetJournalpost;
        if (saksnummer != null) {
            opprettetJournalpost = arkivTjeneste.opprettJournalpost(forsendelseId, avsenderId, saksnummer);
            if (!opprettetJournalpost.ferdigstilt()) {
                LOG.info("FORDEL FORSENDELSE kunne ikke ferdigstille sak {} journalpost {} forsendelse {}", saksnummer, w.getArkivId(), forsendelseId);
            }
            return opprettetJournalpost;
        } else {
            var referanseId = w.getRetryingTask().map(s -> UUID.randomUUID()).orElse(forsendelseId);
            return arkivTjeneste.opprettJournalpost(forsendelseId, referanseId, avsenderId);
        }
    }

    private MottakMeldingDataWrapper utledNesteSteg(MottakMeldingDataWrapper dataWrapper, UUID forsendelseId,
                                                    Destinasjon destinasjon, boolean ferdigstilt) {
        LOG.info("FPFORDEL BdTask exit {}", destinasjon);
        if (ForsendelseStatus.GOSYS.equals(destinasjon.system()) || (destinasjon.saksnummer() != null && !ferdigstilt)) {
            dokumentRepository.oppdaterForsendelseMedArkivId(forsendelseId, dataWrapper.getArkivId(), ForsendelseStatus.GOSYS);
            dataWrapper.setSaksnummer(null);
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        } else if (ForsendelseStatus.FPSAK.equals(destinasjon.system()) && destinasjon.saksnummer() != null) {
            dataWrapper.setSaksnummer(destinasjon.saksnummer());
            dokumentRepository.oppdaterForsendelseMetadata(forsendelseId, dataWrapper.getArkivId(), destinasjon.saksnummer(), ForsendelseStatus.FPSAK);
            sendFordeltHendelse(forsendelseId, dataWrapper);
            return dataWrapper.nesteSteg(KlargjorForVLTask.TASKNAME);
        } else {
            throw new IllegalStateException("Ukjent system eller saksnummer mangler");
        }
    }

    private static void postConditionHentOgVurderVLSakOgOpprettSak(MottakMeldingDataWrapper dataWrapper) {
        if (KlargjorForVLTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())
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
        if (dataWrapper.getArkivId() == null) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.ARKIV_ID_KEY, dataWrapper.getId());
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
        if (svpSøknader.isEmpty() || svpStrukturert) {
            var doktype = ArkivUtil.utledHovedDokumentType(dokumenter.stream().map(Dokument::getDokumentTypeId).collect(Collectors.toSet()));
            w.setDokumentTypeId(doktype);
            w.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(doktype));
        } else {
            w.setDokumentTypeId(DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG);
            w.setDokumentKategori(DokumentKategori.IKKE_TOLKBART_SKJEMA);
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

    private void sendFordeltHendelse(UUID forsendelseId, MottakMeldingDataWrapper w) {
        try {
            Optional<String> fnr = w.getAktørId().flatMap(aktørConsumer::hentPersonIdentForAktørId);
            if (fnr.isEmpty()) {
                throw MottakMeldingFeil.fantIkkePersonidentForAktørId(TASKNAME, w.getId());
            }
            var hendelse = new SøknadFordeltOgJournalførtHendelse(w.getArkivId(), forsendelseId, fnr.get(), Optional.of(w.getSaksnummer().orElseThrow()));
            hendelseProdusent.send(hendelse, forsendelseId.toString());
        } catch (Exception e) {
            LOG.warn("fpfordel kafka hendelsepublisering feilet for forsendelse {}", forsendelseId.toString(), e);
        }
    }

}
