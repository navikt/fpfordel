package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
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
    public static final String JOURNALMANGLER_EXCEPTION_KODE = "FP-453958";

    private static final Logger LOG = LoggerFactory.getLogger(TilJournalføringTask.class);

    private final TilJournalføringTjeneste journalføring;
    private final EnhetsTjeneste enhetsidTjeneste;
    private final DokumentRepository dokumentRepository;
    private final AktørConsumerMedCache aktør;

    @Inject
    public TilJournalføringTask(ProsessTaskRepository prosessTaskRepository,
            TilJournalføringTjeneste journalføringTjeneste,
            EnhetsTjeneste enhetsidTjeneste,
            DokumentRepository dokumentRepository,
            AktørConsumerMedCache aktørConsumer) {
        super(prosessTaskRepository);
        this.journalføring = journalføringTjeneste;
        this.enhetsidTjeneste = enhetsidTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.aktør = aktørConsumer;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getSaksnummer().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()).toException();
        }
    }

    @Transactional
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        Optional<String> fnr = aktør.hentPersonIdentForAktørId(dataWrapper.getAktørId().get());// NOSONAR
        if (fnr.isEmpty()) {
            throw MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException();
        }
        String enhetsId = enhetsidTjeneste.hentFordelingEnhetId(dataWrapper.getTema(), dataWrapper.getBehandlingTema(),
                dataWrapper.getJournalførendeEnhet(), fnr);
        if (dataWrapper.getArkivId() == null) {
            UUID forsendelseId = dataWrapper.getForsendelseId().orElseThrow(IllegalStateException::new);
            Boolean forsøkEndeligJF = true;
            DokumentforsendelseResponse response = journalføring.journalførDokumentforsendelse(forsendelseId,
                    dataWrapper.getSaksnummer(), dataWrapper.getAvsenderId(), forsøkEndeligJF,
                    dataWrapper.getRetryingTask());
            dataWrapper.setArkivId(response.getJournalpostId());
            // Hvis endelig journalføring feiler (fx pga doktype annet), send til manuell
            // journalføring (journalpost er opprettet).
            if (!JournalTilstand.ENDELIG_JOURNALFØRT.equals(response.getJournalTilstand())) {
                MottakMeldingFeil.FACTORY.feilJournalTilstandForventetTilstandEndelig(response.getJournalTilstand())
                        .log(LOG);
                dokumentRepository.oppdaterForseldelseMedArkivId(forsendelseId, dataWrapper.getArkivId(),
                        ForsendelseStatus.GOSYS);
                return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            }
        } else {
            String innhold = dataWrapper.getDokumentTypeId().map(DokumentTypeId::getTermNavn).orElse("Ukjent innhold");
            try {
                if (!journalføring.tilJournalføring(dataWrapper.getArkivId(), dataWrapper.getSaksnummer().get(),
                        dataWrapper.getAktørId().get(), enhetsId, innhold)) {
                    return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
                }
            } catch (IntegrasjonException e) {
                if (JOURNALMANGLER_EXCEPTION_KODE.equals(e.getFeil().getKode())) {
                    String logMessage = e.getFeil().getKode() + " " + e.getFeil().getFeilmelding();
                    LOG.info(logMessage);
                    return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
                } else {
                    throw e;
                }
            }
        }
        Optional<UUID> forsendelseId = dataWrapper.getForsendelseId();
        forsendelseId.ifPresent(uuid -> dokumentRepository.oppdaterForsendelseMetadata(uuid, dataWrapper.getArkivId(),
                dataWrapper.getSaksnummer().get(), ForsendelseStatus.PENDING));
        return dataWrapper.nesteSteg(KlargjorForVLTask.TASKNAME);
    }
}
