package no.nav.foreldrepenger.mottak.task.joark;

import static java.lang.String.format;
import static no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus.JOURNALFOERT;
import static no.nav.foreldrepenger.fordel.kodeverdi.Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser.erXmlMedKjentNamespace;
import static no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser.unmarshallXml;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
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
@ProsessTask(SendInnEndeligJournalførtTask.TASKNAME)
public class SendInnEndeligJournalførtTask extends WrappedProsessTaskHandler {

    static final String TASKNAME = "fordeling.sendInnJournalpost";

    private static final TaskType TASK_KLARGJØR = TaskType.forProsessTask(VLKlargjørerTask.class);

    private static final Logger LOG = LoggerFactory.getLogger(SendInnEndeligJournalførtTask.class);

    private PersonInformasjon pdl;
    private ArkivTjeneste arkiv;

    public SendInnEndeligJournalførtTask() {

    }

    @Inject
    public SendInnEndeligJournalførtTask(ProsessTaskTjeneste taskTjeneste, PersonInformasjon pdl, ArkivTjeneste arkiv) {
        super(taskTjeneste);
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
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {

        var journalpost = arkiv.hentArkivJournalpost(w.getArkivId());
        if (journalpost == null || w.getSaksnummer().isEmpty()) {
            throw new IllegalStateException("Finner ikke journalpost eller mangler sak");
        }

        if (journalpost.harBrevkodeCrm()) {
            var ref = Optional.ofNullable(journalpost.getEksternReferanseId()).orElse("");
            LOG.info("FPFORDEL SendInnJournalpost CRM-brevkode for journalpost {} kanal {} ref {} tilstand {} hovedtype {} alle typer {}", w.getArkivId(),
                journalpost.getKanal(), ref, journalpost.getTilstand(), journalpost.getHovedtype(), journalpost.getAlleTyper());
            return null;
        }

        if (!JOURNALFOERT.equals(journalpost.getTilstand())) {
            LOG.info("FPFORDEL SendInnJournalpost feil tilstand på journalpost {} kanal {} tema {} tilstand {} hovedtype {} alle typer {}", w.getArkivId(),
                journalpost.getKanal(), journalpost.getTema().getKode(), journalpost.getTilstand(), journalpost.getHovedtype(),
                journalpost.getAlleTyper());
            throw new IllegalStateException("Kan ikke håndtere tilstand");
        }

        var dokumentType = journalpost.getHovedtype();
        if (!DokumentTypeId.erInntektsmelding(dokumentType) && !DokumentTypeId.erSøknadType(dokumentType) && !DokumentTypeId.erKlageType(dokumentType)) {
            LOG.info("FPFORDEL SendInnJournalpost feil dokumenttype på journalpost {} kanal {} tema {} tilstand {} hovedtype {} alle typer {}", w.getArkivId(),
                journalpost.getKanal(), journalpost.getTema().getKode(), journalpost.getTilstand(), journalpost.getHovedtype(),
                journalpost.getAlleTyper());
            return null;
        }

        // Disse 2 + behandlingstema er normalt satt fra før
        w.setTema(journalpost.getTema());
        if (w.getEksternReferanseId().isEmpty()) {
            w.setEksternReferanseId(Optional.ofNullable(journalpost.getEksternReferanseId()).orElseGet(() -> UUID.randomUUID().toString()));
        }

        Optional.ofNullable(journalpost.getKanal()).ifPresent(w::setKanal);
        w.setForsendelseMottattTidspunkt(Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now));
        w.setDokumentTypeId(journalpost.getHovedtype());
        w.setBehandlingTema(ArkivUtil.behandlingTemaFraDokumentType(w.getBehandlingTema(), journalpost.getHovedtype()));
        w.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(journalpost.getHovedtype()));
        w.setAktørId(journalpost.getBrukerAktørId().orElseThrow());
        journalpost.getJournalfoerendeEnhet().ifPresent(w::setJournalførendeEnhet);
        w.setStrukturertDokument(journalpost.getInnholderStrukturertInformasjon());
        w.setInnkommendeSaksnummer(w.getSaksnummer().orElseThrow());

        if (journalpost.getInnholderStrukturertInformasjon()) {
            if (!erXmlMedKjentNamespace(journalpost.getStrukturertPayload())) {
                var jptittel = journalpost.getOriginalJournalpost().tittel();
                // kast feil for ukjent innhold som antagelig er XML (og vi kanskje bør
                // håndtere). ignorer andre
                if (!journalpost.getStrukturertPayload().isBlank() && Objects.equals('<', journalpost.getStrukturertPayload().trim().charAt(0))) {
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
                        LOG.info("FPFORDEL HentFraArkiv journalpost avvikende saksnummer i XML journalpost {} avvik {}",
                            journalpost.getJournalpostId(), vle.getFeilmelding());
                        throw new IllegalStateException("Kan ikke håndtere");
                    }
                }
            }
        }

        // Vesentlige mangler
        if (!FORELDRE_OG_SVANGERSKAPSPENGER.equals(w.getTema()) || !Journalposttype.INNGÅENDE.equals(journalpost.getJournalposttype())) {
            throw new IllegalStateException("Kan ikke håndtere");
        }
        return w.nesteSteg(TASK_KLARGJØR);

    }
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
