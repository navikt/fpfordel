package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.felles.kafka.HendelseProdusent;
import no.nav.foreldrepenger.mottak.felles.kafka.SøknadFordeltOgJournalførtHendelse;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProssessTask som utleder journalføringsbehov og forsøker rette opp disse.
 * </p>
 */
@Dependent
@ProsessTask(TilJournalføringTask.TASKNAME)
public class TilJournalføringTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.tilJournalforing";

    private static final Logger LOG = LoggerFactory.getLogger(TilJournalføringTask.class);
    private static final String AUTOMATISK_ENHET = "9999";

    private final ArkivTjeneste arkivTjeneste;
    private final DokumentRepository dokumentRepository;
    private final PersonInformasjon aktør;
    private final HendelseProdusent hendelseProdusent;

    @Inject
    public TilJournalføringTask(ProsessTaskRepository prosessTaskRepository,
            ArkivTjeneste arkivTjeneste,
            HendelseProdusent hendelseProdusent,
            DokumentRepository dokumentRepository,
            PersonInformasjon aktørConsumer) {
        super(prosessTaskRepository);
        this.arkivTjeneste = arkivTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.aktør = aktørConsumer;
        this.hendelseProdusent = hendelseProdusent;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()).toException();
        }
    }

    @Transactional
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {
        Optional<String> fnr = w.getAktørId().flatMap(aktør::hentPersonIdentForAktørId);
        if (fnr.isEmpty()) {
            throw MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, w.getId()).toException();
        }
        var saksnummer = w.getSaksnummer().orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler saksnummer"));
        if (w.getArkivId() == null) {
            // Dokument fra selvbetjening, ikke journalført ennå.
            if (!opprettJournalpostFerdigstill(w, saksnummer)) {
                LOG.info("FORDEL OPPRETT/FERDIG feilet for {} forsendelse {}", saksnummer, w.getForsendelseId());
                return w.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            }
        } else {
            // Annet dokument fra dokumentmottak (scanning, altinn). Kan skippe
            // unntakshåndtering. Bør feile.
            try {
                if (w.getInnkommendeSaksnummer().isEmpty()) {
                    arkivTjeneste.oppdaterMedSak(w.getArkivId(), saksnummer);
                } else {
                    LOG.info("FORDEL OPPRETT/FERDIG presatt saksnummer {} for journalpost {}", w.getInnkommendeSaksnummer().get(), w.getArkivId());
                }
                arkivTjeneste.ferdigstillJournalføring(w.getArkivId(), w.getJournalførendeEnhet().orElse(AUTOMATISK_ENHET));
            } catch (Exception e) {
                MottakMeldingFeil.FACTORY.feilJournalTilstandForventetTilstandEndelig().log(LOG);
                return w.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            }
        }
        w.getForsendelseId().ifPresent(id -> {
            dokumentRepository.oppdaterForsendelseMetadata(id, w.getArkivId(), saksnummer,
                    ForsendelseStatus.PENDING);
            try {
                hendelseProdusent.send(
                        new SøknadFordeltOgJournalførtHendelse(w.getArkivId(), id, fnr.get(), saksnummer),
                        id.toString());
            } catch (Exception e) {
                LOG.warn("fpfordel kafka hendelsepublisering feilet for forsendelse {}", id.toString(), e);
            }
        });
        return w.nesteSteg(KlargjorForVLTask.TASKNAME);
    }

    private boolean opprettJournalpostFerdigstill(MottakMeldingDataWrapper w, String saksnummer) {
        UUID forsendelseId = w.getForsendelseId().orElseThrow(IllegalStateException::new);
        var avsenderId = w.getAvsenderId()
                .orElseGet(() -> w.getAktørId().orElseThrow(() -> new IllegalStateException("Hvor ble det av brukers id?")));

        var opprettetJournalpost = arkivTjeneste.opprettJournalpost(forsendelseId, avsenderId, saksnummer);
        w.setArkivId(opprettetJournalpost.journalpostId());

        if (!opprettetJournalpost.ferdigstilt()) {
            LOG.info("FORDEL OPPRETT/FERDIG ikke ferdig for {} forsendelse {}", w.getArkivId(), forsendelseId);
            MottakMeldingFeil.FACTORY.feilJournalTilstandForventetTilstandEndelig().log(LOG);
            dokumentRepository.oppdaterForsendelseMedArkivId(forsendelseId, w.getArkivId(), ForsendelseStatus.GOSYS);
            // Det kommer en midlertidig-hendelse på Kafka om et par sekunder. Unngå å
            // reagere på den.
            dokumentRepository.lagreJournalpostLokal(w.getArkivId(), MottakKanal.SELVBETJENING.getKode(), "MIDLERTIDIG", forsendelseId.toString());
        }
        return opprettetJournalpost.ferdigstilt();
    }
}
