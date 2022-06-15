package no.nav.foreldrepenger.mottak.task.joark;

import static java.lang.String.format;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.fraTermNavn;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.gjelderForeldrepenger;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.gjelderSvangerskapspenger;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.UDEFINERT;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.erInntektsmelding;
import static no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus.MOTTATT;
import static no.nav.foreldrepenger.fordel.kodeverdi.Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.konfig.KonfigVerdier.ENDRING_BEREGNING_DATO;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.AKTØR_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.SAKSNUMMER_KEY;
import static no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser.erXmlMedKjentNamespace;
import static no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser.unmarshallXml;
import static no.nav.vedtak.konfig.Tid.TIDENES_BEGYNNELSE;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsInfo;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.DestinasjonsRuter;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

/**
 * <p>
 * ProssessTask som håndterer uthenting av saksinformasjon fra Journalarkivet(joark).
 * Henter journalpost / metadata og original (XML) dokument og finner destinasjon for journalposten
 * </p>
 */
@ApplicationScoped
@ProsessTask(HentDataFraJoarkTask.TASKNAME)
public class HentDataFraJoarkTask extends WrappedProsessTaskHandler {

    static final String TASKNAME = "fordeling.hentFraJoark";

    private static final TaskType TASK_GOSYS = TaskType.forProsessTask(OpprettGSakOppgaveTask.class);
    private static final TaskType TASK_JOURNALFØR = TaskType.forProsessTask(TilJournalføringTask.class);

    private static final Logger LOG = LoggerFactory.getLogger(HentDataFraJoarkTask.class);

    private PersonInformasjon pdl;
    private ArkivTjeneste arkiv;
    private DestinasjonsRuter vurderVLSaker;

    public HentDataFraJoarkTask() {

    }

    @Inject
    public HentDataFraJoarkTask(ProsessTaskTjeneste taskTjeneste,
                                DestinasjonsRuter vurderVLSaker,
                                PersonInformasjon pdl,
                                ArkivTjeneste arkiv) {
        super(taskTjeneste);
        this.vurderVLSaker = vurderVLSaker;
        this.pdl = pdl;
        this.arkiv = arkiv;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper w) {
        if (w.getArkivId() == null || w.getArkivId().isEmpty()) {
            throw new TekniskException("FP-941984", format("Preconditions for %s mangler %s. TaskId: %s", TASKNAME, ARKIV_ID_KEY, w.getId()));
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper w) {
        if (!TASK_GOSYS.equals(w.getProsessTaskData().taskType()) && w.getAktørId().isEmpty()) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, AKTØR_ID_KEY, w.getId()));
        }
        if (TASK_JOURNALFØR.equals(w.getProsessTaskData().taskType()) && w.getSaksnummer().isEmpty()) {
            throw new TekniskException("FP-638068", format("Postconditions for %s mangler %s. TaskId: %s", TASKNAME, SAKSNUMMER_KEY, w.getId()));
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {

        var journalpost = arkiv.hentArkivJournalpost(w.getArkivId());

        if (journalpost.harBrevkodeCrm()) {
            var ref = Optional.ofNullable(journalpost.getEksternReferanseId()).orElse("");
            LOG.info("FPFORDEL HentFraArkiv CRM-brevkode for journalpost {} kanal {} ref {} tilstand {} hovedtype {} alle typer {}",
                w.getArkivId(), journalpost.getKanal(), ref, journalpost.getTilstand(),
                journalpost.getHovedtype(), journalpost.getAlleTyper());
            if (!MOTTATT.equals(journalpost.getTilstand())) {
                return null;
            }
        }

        if (!MOTTATT.equals(journalpost.getTilstand())) {
            LOG.info("FPFORDEL HentFraArkiv feil tilstand på journalpost {} kanal {} tema {} tilstand {} hovedtype {} alle typer {}",
                w.getArkivId(), journalpost.getKanal(), journalpost.getTema().getKode(), journalpost.getTilstand(),
                journalpost.getHovedtype(), journalpost.getAlleTyper());
            return null;
        }

        // Disse 2 + behandlingstema er normalt satt fra før
        w.setTema(journalpost.getTema());

        if (w.getEksternReferanseId().isEmpty()) {
            w.setEksternReferanseId(journalpost.getEksternReferanseId());
        }

        w.setForsendelseMottattTidspunkt(Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now));
        w.setDokumentTypeId(journalpost.getHovedtype());
        w.setBehandlingTema(ArkivUtil.behandlingTemaFraDokumentType(w.getBehandlingTema(), journalpost.getHovedtype()));
        w.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(journalpost.getHovedtype()));
        finnAktørId(journalpost).ifPresent(w::setAktørId);
        journalpost.getJournalfoerendeEnhet().ifPresent(w::setJournalførendeEnhet);
        w.setStrukturertDokument(journalpost.getInnholderStrukturertInformasjon());
        journalpost.getSaksnummer().ifPresent(s -> {
            LOG.info("FORDEL HentFraArkiv presatt saksnummer {} for journalpost {}", s, w.getArkivId());
            w.setSaksnummer(s);
            w.setInnkommendeSaksnummer(s);
        });

        if (journalpost.getInnholderStrukturertInformasjon()) {
            if (!erXmlMedKjentNamespace(journalpost.getStrukturertPayload())) {
                var jptittel = journalpost.getOriginalJournalpost().tittel();
                // kast feil for ukjent innhold som antagelig er XML (og vi kanskje bør
                // håndtere). ignorer andre
                if (!journalpost.getStrukturertPayload().isBlank() &&
                        Objects.equals('<', journalpost.getStrukturertPayload().trim().charAt(0))) {
                    var doktittel = journalpost.getOriginalJournalpost().dokumenter().get(0).tittel();
                    var prefix = journalpost.getStrukturertPayload().substring(0, Math.min(40, journalpost.getStrukturertPayload().length()));
                    LOG.warn("FPFORDEL journalpost med ukjent xml innhold {} {} {}", jptittel, doktittel, prefix);
                } else {
                    LOG.info("FPFORDEL journalpost med non-xml strukturert innhold {}", jptittel);
                }
            } else {
                try {
                    var mottattDokument = unmarshallXml(journalpost.getStrukturertPayload());
                    mottattDokument.kopierTilMottakWrapper(w, pdl::hentAktørIdForPersonIdent);
                    w.setPayload(journalpost.getStrukturertPayload());
                } catch (VLException vle) {
                    // Mottatt journalpost har annet saksnummer enn den i endringssøknaden....
                    // Skyldes spesiell bruk av Gosys. Lag oppgave i dette tilfelle, godta i BehandleDokumentService
                    if ("FP-401245".equals(vle.getKode())) {
                        w.setSaksnummer(null);
                        LOG.info("FPFORDEL HentFraArkiv journalpost avvikende saksnummer i XML journalpost {} avvik {}",
                                journalpost.getJournalpostId(), vle.getFeilmelding());
                        return w.nesteSteg(TASK_GOSYS);
                    }
                }
            }
        }

        // Egen rute for rene ankedokument - journalføringsoppgave Klageinstans
        if (journalpost.getAlleTyper().contains(DokumentTypeId.KLAGE_DOKUMENT)
                && journalpost.getSaksnummer().isEmpty()
                && (journalpost.getTittel().filter(t -> t.equalsIgnoreCase("anke")).isPresent() ||
                    ArkivTjeneste.harBrevKode(journalpost.getOriginalJournalpost(), NAVSkjema.SKJEMA_KLAGE_A_DOKUMENT))) {
            LOG.info("FPFORDEL HentFraArkiv ankedokument til KA journalpost {} kanal {} dokumenttype {}",
                    journalpost.getJournalpostId(), journalpost.getKanal(), journalpost.getHovedtype());
            w.setJournalførendeEnhet(EnhetsInfo.NK_ENHET_ID);
            return w.nesteSteg(TASK_GOSYS);
        }

        // Journalposter uten kanalreferanse er vanligvis slike som er "klonet" av SBH
        // og forsøkt journalført fra Gosys.
        // Håndteres manuelt hvis de kommer helt hit - mulig de skal slippes videre
        if (w.getEksternReferanseId().isEmpty() && w.getInnkommendeSaksnummer().isEmpty()
                && !MottakKanal.SELVBETJENING.getKode().equals(journalpost.getKanal())) {
            LOG.info("FPFORDEL HentFraArkiv journalpost uten kanalreferanse journalpost {} kanal {} dokumenttype {}",
                    journalpost.getJournalpostId(), journalpost.getKanal(), journalpost.getHovedtype());
            return w.nesteSteg(TASK_GOSYS);
        }
        // Vesentlige mangler
        if (!FORELDRE_OG_SVANGERSKAPSPENGER.equals(w.getTema())) {
            LOG.info("FPFORDEL HentFraArkiv feil tema for journalpost {} kanal {} tema {}",
                    w.getArkivId(), journalpost.getKanal(), journalpost.getTema().getKode());
            return w.nesteSteg(TASK_GOSYS);
        }
        if (w.getAktørId().isEmpty()) {
            var avsender = journalpost.getAvsenderIdent() == null ? "ikke satt"
                    : pdl.hentAktørIdForPersonIdent(journalpost.getAvsenderIdent()).orElse("finnes ikke");
            LOG.info("FPFORDEL HentFraArkiv manglende bruker for journalpost {} kanal {} type {} avsender {}",
                    w.getArkivId(), journalpost.getKanal(), journalpost.getHovedtype(), avsender);
            return w.nesteSteg(TASK_GOSYS);
        }
        if (UDEFINERT.equals(journalpost.getHovedtype())) {
            LOG.info("FPFORDEL HentFraArkiv udefinert dokumenttype journalpost {} kanal {} tittel {}",
                    w.getArkivId(), journalpost.getKanal(), journalpost.getTittel());
            return w.nesteSteg(TASK_GOSYS);
        }

        LOG.info("FPFORDEL INNGÅENDE journalpost {} kanal {} tilstand {} hovedtype {} alle typer {}",
                w.getArkivId(), journalpost.getKanal(), journalpost.getTilstand(),
                journalpost.getHovedtype(), journalpost.getAlleTyper());

        if (erInntektsmelding(journalpost.getHovedtype())) {
            oppdaterInntektsmelding(w);
            if (kreverInntektsmeldingManuellVurdering(w)) {
                LOG.info("FPFORDEL HentFraArkiv inntektsmelding til manuell murdering journalpost {}", journalpost.getJournalpostId());
                return w.nesteSteg(TASK_GOSYS);
            }
        } else if (!arkiv.oppdaterRettMangler(journalpost, w.getAktørId().get(), w.getBehandlingTema(),
                w.getDokumentTypeId().orElse(UDEFINERT))) {
            LOG.info("FPFORDEL HentFraArkiv kunne ikke rette opp mangler journalpost {} kanal {} hovedtype {} alle typer {}",
                    w.getArkivId(), journalpost.getKanal(), journalpost.getHovedtype(), journalpost.getAlleTyper());
            return w.nesteSteg(TASK_GOSYS);
        }

        var destinasjon = vurderVLSaker.bestemDestinasjon(w);
        LOG.info("FPFORDEL HentFraArkiv destinasjon {} journalpost {} kanal {} dokumenttype {} saksnummer {}",
                destinasjon, w.getArkivId(), journalpost.getKanal(), journalpost.getHovedtype(), journalpost.getSaksnummer());
        if (ForsendelseStatus.GOSYS.equals(destinasjon.system())) {
            LOG.info("FPFORDEL HentFraArkiv destinasjon GOSYS journalpost {} kanal {} dokumenttype {}",
                    journalpost.getJournalpostId(), journalpost.getKanal(), journalpost.getHovedtype());
            return w.nesteSteg(TASK_GOSYS);
        } else {
            if (destinasjon.saksnummer() == null && !vurderVLSaker.kanOppretteSak(w)) {
                LOG.info("FPFORDEL HentFraArkiv kan ikke opprette sak - til GOSYS journalpost {} kanal {} dokumenttype {}",
                        journalpost.getJournalpostId(), journalpost.getKanal(), journalpost.getHovedtype());
                return w.nesteSteg(TASK_GOSYS);
            }
            var saksnummer = Optional.ofNullable(destinasjon.saksnummer())
                    .orElseGet(() -> vurderVLSaker.opprettSak(w));
            w.setSaksnummer(saksnummer);
            return w.nesteSteg(TASK_JOURNALFØR);
        }
    }

    private void oppdaterInntektsmelding(MottakMeldingDataWrapper w) {
        Optional<String> imYtelse = w.getInntektsmeldingYtelse();
        if (imYtelse.isEmpty()) {
            throw new TekniskException("FP-429673", "Mangler Ytelse på Innteksmelding");
        }
        var behandlingTemaFraIM = fraTermNavn(imYtelse.get());

        // Mangler alltid bruker
        arkiv.oppdaterBehandlingstemaBruker(w.getArkivId(), INNTEKTSMELDING,
                behandlingTemaFraIM.getOffisiellKode(),
                w.getAktørId().orElseThrow(() -> new IllegalStateException("Utviklerfeil: aktørid skal være satt")));

        w.setBehandlingTema(behandlingTemaFraIM);
    }

    private boolean kreverInntektsmeldingManuellVurdering(MottakMeldingDataWrapper w) {
        if (gjelderForeldrepenger(w.getBehandlingTema())) {
            return kreverStartdatoForInntektsmeldingenManuellBehandling(w);
        }
        if (gjelderSvangerskapspenger(w.getBehandlingTema())) {
            return sjekkOmInntektsmeldingGjelderMann(w);
        }
        return true;
    }

    private boolean sjekkOmInntektsmeldingGjelderMann(MottakMeldingDataWrapper w) {
        String aktørId = w.getAktørId().orElseThrow(() -> new IllegalStateException("Utviklerfeil"));
        String fnrBruker = pdl.hentPersonIdentForAktørId(aktørId)
                .orElseThrow(() -> new TekniskException("FP-254631",
                        format("Fant ikke personident for aktørId i task %s.  TaskId: %s", TASKNAME, w.getId())));
        return (Character.getNumericValue(fnrBruker.charAt(8)) % 2) != 0;
    }

    private static boolean kreverStartdatoForInntektsmeldingenManuellBehandling(MottakMeldingDataWrapper dataWrapper) {
        return dataWrapper.getInntektsmeldingStartDato()
                .orElse(TIDENES_BEGYNNELSE).isBefore(ENDRING_BEREGNING_DATO);
    }

    private Optional<String> finnAktørId(ArkivJournalpost journalpost) {
        if (journalpost.getBrukerAktørId().isPresent()) {
            return journalpost.getBrukerAktørId();
        }
        if (journalpost.getAvsenderIdent() != null && journalpost.getOriginalJournalpost().avsenderMottaker().getIdHvisFNR().isPresent()) {
            LOG.info("FPFORDEL HentFraArkiv journalpost uten bruker med FNR-avsender journalpost {} kanal {} tittel {}",
                    journalpost.getJournalpostId(), journalpost.getKanal(), journalpost.getTittel());
            // return
            // aktørConsumer.hentAktørIdForPersonIdent(journalpost.getAvsenderIdent());
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [pdl=" + pdl + ", arkiv=" + arkiv + ", vurderVLSaker=" + vurderVLSaker + "]";
    }

}