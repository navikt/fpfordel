package no.nav.foreldrepenger.mottak.task.dokumentforsendelse;

import static java.lang.String.format;
import static no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType.XML;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.UDEFINERT;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.fraOffisiellKode;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.gjelderForeldrepenger;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal.SELVBETJENING;
import static no.nav.foreldrepenger.fordel.kodeverdi.Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.AKTØR_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTKATEGORI_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_MOTTATT_TIDSPUNKT_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.SAKSNUMMER_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;
import static no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser.unmarshallXml;
import static no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil.behandlingTemaFraDokumentType;
import static no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil.utledHovedDokumentType;
import static no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil.utledKategoriFraDokumentType;
import static no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus.FPSAK;
import static no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus.GOSYS;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.felles.kafka.HendelseProdusent;
import no.nav.foreldrepenger.mottak.felles.kafka.SøknadFordeltOgJournalførtHendelse;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.OpprettetJournalpost;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.tjeneste.Destinasjon;
import no.nav.foreldrepenger.mottak.tjeneste.DestinasjonsRuter;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

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
@ApplicationScoped
@ProsessTask(value = BehandleDokumentforsendelseTask.TASKNAME, maxFailedRuns = 4, firstDelay = 10, thenDelay = 30)
public class BehandleDokumentforsendelseTask extends WrappedProsessTaskHandler {

    static final String TASKNAME = "fordeling.behandleDokumentForsendelse";

    private static final Logger LOG = LoggerFactory.getLogger(BehandleDokumentforsendelseTask.class);

    private static final TaskType TASK_GOSYS = TaskType.forProsessTaskHandler(OpprettGSakOppgaveTask.class);
    private static final TaskType TASK_FPSAK = TaskType.forProsessTaskHandler(VLKlargjørerTask.class);

    private PersonInformasjon pdl;
    private Fagsak fagsak;
    private DestinasjonsRuter ruter;
    private ArkivTjeneste arkiv;
    private DokumentRepository dokumentRepository;
    private HendelseProdusent hendelseProdusent;

    public BehandleDokumentforsendelseTask() {

    }

    @Inject
    public BehandleDokumentforsendelseTask(ProsessTaskTjeneste taskTjeneste,
                                           DestinasjonsRuter ruter,
                                           PersonInformasjon pdl,
                                           Fagsak fagsak,
                                           ArkivTjeneste arkiv,
                                           DokumentRepository dokumentRepository,
                                           HendelseProdusent hendelseProdusent) {
        super(taskTjeneste);
        this.ruter = ruter;
        this.pdl = pdl;
        this.fagsak = fagsak;
        this.arkiv = arkiv;
        this.dokumentRepository = dokumentRepository;
        this.hendelseProdusent = hendelseProdusent;
        LOG.trace("Created");
    }

    @Override
    public void precondition(MottakMeldingDataWrapper w) {
        if (w.getForsendelseId().isEmpty()) {
            throw new TekniskException("FP-941984", format("Preconditions for %s mangler %s. TaskId: %s", TASKNAME, FORSENDELSE_ID_KEY, w.getId()));
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper w) {
        if (w.getAktørId().isEmpty()) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, AKTØR_ID_KEY, w.getId()));
        }
        if (w.getBehandlingTema() == null) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, BEHANDLINGSTEMA_KEY, w.getId()));
        }
        if (TASK_FPSAK.equals(w.getProsessTaskData().taskType())) {
            postconditionJournalføringMedSak(w);
        }
        postConditionHentOgVurderVLSakOgOpprettSak(w);
    }

    private static void postconditionJournalføringMedSak(MottakMeldingDataWrapper w) {
        if (w.getSaksnummer().isEmpty()) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, SAKSNUMMER_KEY, w.getId()));
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {

        var forsendelseId = w.getForsendelseId().orElseThrow();
        var hovedDokument = dokumentRepository.hentUnikDokument(forsendelseId, true, XML);
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);

        var behandlingTema = behandlingTemaFraDokumentType(UDEFINERT,
                hovedDokument
                        .map(Dokument::getDokumentTypeId)
                        .orElse(DokumentTypeId.UDEFINERT));
        w.setBehandlingTema(behandlingTema);

        LOG.info("FPFORDEL BdTask entry bt {}", behandlingTema);

        setFellesWrapperAttributter(w, hovedDokument.orElse(null), metadata);

        var destinasjon = getDestinasjonOppdaterWrapper(w, hovedDokument, metadata);
        LOG.info("FPFORDEL BdTask destinasjon {}", destinasjon);

        w.setDokumentKategori(utledKategoriFraDokumentType(w.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)));
        destinasjon = utledSaksnummerOpprettSakVedBehov(destinasjon, w);
        w.setSaksnummer(destinasjon.saksnummer());

        var journalpost = opprettJournalpostFerdigstillHvisSaksnummer(forsendelseId, w, destinasjon.saksnummer());
        w.setArkivId(journalpost.journalpostId());
        if (!journalpost.ferdigstilt()) {
            // Det vil komme en Kafka-hendelse om noen sekunder - denne sørger for at vi
            // ikke trigger på den.
            dokumentRepository.lagreJournalpostLokal(w.getArkivId(),
                    SELVBETJENING.getKode(), "MIDLERTIDIG", forsendelseId.toString());
        }

        return utledNesteSteg(w, forsendelseId, destinasjon, journalpost.ferdigstilt());
    }

    private Destinasjon getDestinasjonOppdaterWrapper(MottakMeldingDataWrapper w, Optional<Dokument> hovedDokument, DokumentMetadata metadata) {
        if (metadata.getSaksnummer().isPresent()) {
            var saksnr = metadata.getSaksnummer().get(); // NOSONAR
            var fagsakInfoOpt = fagsak.finnFagsakInfomasjon(new SaksnummerDto(saksnr))
                    .filter(f -> erGyldigSaksnummer(w, saksnr, f, hovedDokument));
            if (fagsakInfoOpt.isEmpty()) {
                w.setSaksnummer(null); // Sendt inn på infotrygd-sak
            }
            if (hovedDokument.isEmpty()) {
                fagsakInfoOpt.ifPresent(f -> w.setBehandlingTema(fraOffisiellKode(f.getBehandlingstemaOffisiellKode())));
            }
            return fagsakInfoOpt.isPresent() ? new Destinasjon(FPSAK, saksnr) : Destinasjon.GOSYS;
        }
        return ruter.bestemDestinasjon(w);
    }

    private Destinasjon utledSaksnummerOpprettSakVedBehov(Destinasjon destinasjon, MottakMeldingDataWrapper w) {
        if (!FPSAK.equals(destinasjon.system())) {
            return Destinasjon.GOSYS;
        }
        return new Destinasjon(FPSAK,
                Optional.ofNullable(destinasjon.saksnummer())
                        .orElseGet(() -> ruter.opprettSak(w)));
    }

    private OpprettetJournalpost opprettJournalpostFerdigstillHvisSaksnummer(UUID forsendelseId, MottakMeldingDataWrapper w, String saksnummer) {
        var avsenderId = w.getAvsenderId().or(w::getAktørId)
                .orElseThrow(() -> new IllegalStateException("Hvor ble det av brukers id?"));

        if (saksnummer != null) {
            var opprettetJournalpost = arkiv.opprettJournalpost(forsendelseId, avsenderId, saksnummer);
            if (!opprettetJournalpost.ferdigstilt()) {
                LOG.info("FORDEL FORSENDELSE kunne ikke ferdigstille sak {} journalpost {} forsendelse {}", saksnummer, w.getArkivId(),
                        forsendelseId);
            }
            return opprettetJournalpost;
        }
        var referanseId = w.getRetryingTask()
                .map(s -> UUID.randomUUID())
                .orElse(forsendelseId);
        return arkiv.opprettJournalpost(forsendelseId, referanseId, avsenderId);

    }

    private MottakMeldingDataWrapper utledNesteSteg(MottakMeldingDataWrapper w, UUID forsendelseId,
            Destinasjon destinasjon, boolean ferdigstilt) {
        LOG.info("FPFORDEL BdTask exit {}", destinasjon);
        if (GOSYS.equals(destinasjon.system()) || ((destinasjon.saksnummer() != null) && !ferdigstilt)) {
            dokumentRepository.oppdaterForsendelseMedArkivId(forsendelseId, w.getArkivId(), GOSYS);
            w.setSaksnummer(null);
            return w.nesteSteg(TASK_GOSYS);
        }
        if (FPSAK.equals(destinasjon.system()) && (destinasjon.saksnummer() != null)) {
            w.setSaksnummer(destinasjon.saksnummer());
            dokumentRepository.oppdaterForsendelseMetadata(forsendelseId, w.getArkivId(), destinasjon.saksnummer(),
                    FPSAK);
            sendFordeltHendelse(forsendelseId, w);
            return w.nesteSteg(TASK_FPSAK);
        }
        throw new IllegalStateException("Ukjent system eller saksnummer mangler");

    }

    private static void postConditionHentOgVurderVLSakOgOpprettSak(MottakMeldingDataWrapper w) {
        if (TASK_FPSAK.equals(w.getProsessTaskData().taskType())
                && w.getForsendelseMottattTidspunkt().isEmpty()) {
            throw new TekniskException("FP-638068",
                    format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, FORSENDELSE_MOTTATT_TIDSPUNKT_KEY, w.getId()));
        }
        if (w.getDokumentTypeId().isEmpty()) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, DOKUMENTTYPE_ID_KEY, w.getId()));
        }
        if (w.getDokumentKategori().isEmpty()) {
            throw new TekniskException("FP-638068",
                    format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, DOKUMENTKATEGORI_ID_KEY, w.getId()));
        }
        if (DokumentTypeId.erSøknadType(w.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))
                && w.getPayloadAsString().isEmpty()) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, "payload", w.getId()));
        }
        if (!w.getHarTema()) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, TEMA_KEY, w.getId()));
        }
        if (w.getArkivId() == null) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, ARKIV_ID_KEY, w.getId()));
        }
    }

    private void setFellesWrapperAttributter(MottakMeldingDataWrapper w, Dokument dokument,
            DokumentMetadata metadata) {
        if (!w.getHarTema()) {
            w.setTema(FORELDRE_OG_SVANGERSKAPSPENGER);
        }
        w.setAktørId(metadata.getBrukerId());
        metadata.getArkivId().ifPresent(w::setArkivId);
        metadata.getSaksnummer().ifPresent(w::setSaksnummer);
        if (dokument != null) {
            w.setDokumentTypeId(dokument.getDokumentTypeId());
            w.setDokumentKategori(utledKategoriFraDokumentType(dokument.getDokumentTypeId()));
            w.setPayload(dokument.getKlartekstDokument());
            kopierOgValiderAttributterFraSøknad(w, dokument);
        } else {
            settDokumentTypeKategoriKorrigerSVP(w);
        }
        if (w.getForsendelseMottattTidspunkt().isEmpty()) {
            w.setForsendelseMottattTidspunkt(LocalDateTime.now());
        }
    }

    private boolean erGyldigSaksnummer(MottakMeldingDataWrapper w, String saksnummer, FagsakInfomasjonDto fagsakInfo, Optional<Dokument> hovedDokument) {
        if (!Objects.equals(fagsakInfo.getAktørId(), w.getAktørId().orElse(null))) {
            LOG.warn("Søkers ID samsvarer ikke med søkers ID i eksisterende sak {}", saksnummer);
            return false;
        }
        var behandlingTemaFraSak = fraOffisiellKode(fagsakInfo.getBehandlingstemaOffisiellKode());
        hovedDokument.ifPresent(h -> sjekkForMismatchMellomFagsakOgDokumentInn(w.getBehandlingTema(), behandlingTemaFraSak, h));
        return true;
    }

    // TODO: Endre når TFP-4125 er fikset i søknadSvangerskapspenger. Nå kommer
    // vedlegg som søknad og roter til mye.
    private void settDokumentTypeKategoriKorrigerSVP(MottakMeldingDataWrapper w) {
        var dokumenter = w.getForsendelseId()
                .map(dokumentRepository::hentDokumenter)
                .orElse(List.of());
        var svpSøknader = dokumenter.stream()
                .filter(d -> SØKNAD_SVANGERSKAPSPENGER.equals(d.getDokumentTypeId()))
                .toList();
        var svpStrukturert = svpSøknader.stream().anyMatch(d -> XML.equals(d.getArkivFilType()));
        if (svpSøknader.isEmpty() || svpStrukturert) {
            var doktype = utledHovedDokumentType(dokumenter.stream().map(Dokument::getDokumentTypeId).collect(Collectors.toSet()));
            w.setDokumentTypeId(doktype);
            w.setDokumentKategori(utledKategoriFraDokumentType(doktype));
        } else {
            w.setDokumentTypeId(ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG);
            w.setDokumentKategori(DokumentKategori.IKKE_TOLKBART_SKJEMA);
        }
        if (!svpSøknader.isEmpty() && !svpStrukturert) {
            svpSøknader.forEach(d -> {
                d.setDokumentTypeId(ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG);
                dokumentRepository.lagre(d);
            });
        }
    }

    private static void sjekkForMismatchMellomFagsakOgDokumentInn(BehandlingTema behandlingTema,
            BehandlingTema fagsakTema, Dokument dokument) {

        if (FORELDREPENGER_ENDRING_SØKNAD.equals(dokument.getDokumentTypeId())) {
            // Endringssøknad har ingen info om behandlingstema, slik vi kan ikke utlede
            // et spesifikt tema, så må ha løsere match. Se
            // ArkivUtil.korrigerBehandlingTemaFraDokumentType
            if (gjelderForeldrepenger(behandlingTema)) {
                return;
            }
        }
        if (!fagsakTema.equals(behandlingTema)) {
            throw new TekniskException("FP-756353",
                    format("BehandlingTema i forsendelse samsvarer ikke med BehandlingTema i eksisterende sak {%s : %s}",
                            behandlingTema.getKode(), fagsakTema.getKode()));
        }

    }

    private void kopierOgValiderAttributterFraSøknad(MottakMeldingDataWrapper nesteSteg, Dokument dokument) {
        unmarshallXml(dokument.getKlartekstDokument()).kopierTilMottakWrapper(nesteSteg, pdl::hentAktørIdForPersonIdent);
    }

    private void sendFordeltHendelse(UUID forsendelseId, MottakMeldingDataWrapper w) {
        try {
            Optional<String> fnr = w.getAktørId()
                    .flatMap(pdl::hentPersonIdentForAktørId);
            if (fnr.isEmpty()) {
                throw new TekniskException("FP-254631", format("Fant ikke personident for aktørId i task %s.  TaskId: %s", TASKNAME, w.getId()));
            }
            var hendelse = new SøknadFordeltOgJournalførtHendelse(w.getArkivId(), forsendelseId, fnr.orElse(null),
                    w.getSaksnummer().orElse(null));
            hendelseProdusent.send(hendelse, forsendelseId.toString());
        } catch (Exception e) {
            LOG.warn("fpfordel kafka hendelsepublisering feilet for forsendelse {}", forsendelseId.toString(), e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [pdl=" + pdl + ", fagsak=" + fagsak + ", ruter=" + ruter + ", arkiv=" + arkiv
                + ", dokumentRepository=" + dokumentRepository + ", hendelseProdusent=" + hendelseProdusent + "]";
    }

}
