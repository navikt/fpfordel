package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste.NK_ENHET_ID;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.SAKSNUMMER_KEY;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.SlettForsendelseTask;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

/**
 * <p>
 * ProsessTask som oppretter en oppgave i GSAK for manuell behandling av
 * tilfeller som ikke kan håndteres automatisk av vedtaksløsningen.
 * <p>
 * </p>
 */
@Dependent
@ProsessTask(value = "integrasjon.gsak.opprettOppgave", prioritet = 2, maxFailedRuns = 2)
public class OpprettGSakOppgaveTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OpprettGSakOppgaveTask.class);

    private final Journalføringsoppgave oppgaverTjeneste;
    private final ProsessTaskTjeneste taskTjeneste;
    private final EnhetsTjeneste enhetsTjeneste;

    @Inject
    public OpprettGSakOppgaveTask(ProsessTaskTjeneste taskTjeneste, Journalføringsoppgave oppgaverTjeneste, EnhetsTjeneste enhetsTjeneste) {
        this.taskTjeneste = taskTjeneste;
        this.oppgaverTjeneste = oppgaverTjeneste;
        this.enhetsTjeneste = enhetsTjeneste;
    }

    private static String lagBeskrivelse(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, ProsessTaskData data) {
        if (DokumentTypeId.UDEFINERT.equals(dokumentTypeId)) {
            return BehandlingTema.UDEFINERT.equals(behandlingTema) ? "Journalføring" : "Journalføring " + behandlingTema.getTermNavn();
        }
        String beskrivelse = dokumentTypeId.getTermNavn();
        if (DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.equals(dokumentTypeId) && (
            data.getPropertyValue(MottakMeldingDataWrapper.FØRSTE_UTTAKSDAG_KEY) != null)) {
            String uttakStart = data.getPropertyValue(MottakMeldingDataWrapper.FØRSTE_UTTAKSDAG_KEY);
            beskrivelse = beskrivelse + " (" + uttakStart + ")";
        }
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            if (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKTSMELDING_YTELSE) != null) {
                beskrivelse = beskrivelse + " (" + data.getPropertyValue(MottakMeldingDataWrapper.INNTEKTSMELDING_YTELSE) + ")";
            }
            if (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY) != null) {
                beskrivelse = beskrivelse + " (" + (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY)) + ")";
            }
        }
        return beskrivelse;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var behandlingTema = BehandlingTema.fraKodeDefaultUdefinert(prosessTaskData.getPropertyValue(BEHANDLINGSTEMA_KEY));
        var dokumentTypeId = Optional.ofNullable(prosessTaskData.getPropertyValue(DOKUMENTTYPE_ID_KEY))
            .map(DokumentTypeId::fraKodeDefaultUdefinert)
            .orElse(DokumentTypeId.UDEFINERT);
        behandlingTema = ArkivUtil.behandlingTemaFraDokumentType(behandlingTema, dokumentTypeId);

        var journalpostId = JournalpostId.fra(prosessTaskData.getPropertyValue(ARKIV_ID_KEY));
        if (oppgaverTjeneste.finnesÅpeneJournalføringsoppgaverFor(journalpostId)) {
            var ikkeLokalOppgave = Optional.ofNullable(prosessTaskData.getPropertyValue(JOURNAL_ENHET))
                .filter(OpprettGSakOppgaveTask::erGosysOppgave).isPresent();
            LOG.info("FPFORDEL JFR-OPPGAVE: finnes allerede åpen oppgave for journalpostId: {}", journalpostId.getVerdi());
            // Behold oppgave hvis skal behandles i Gosys - ellers lag lokal oppgave
            if (ikkeLokalOppgave) {
                return;
            } else {
                oppgaverTjeneste.ferdigstillAlleÅpneJournalføringsoppgaverFor(journalpostId);
            }
        }

        String oppgaveId = opprettOppgave(prosessTaskData, behandlingTema, dokumentTypeId);

        LOG.info("FPFORDEL JFR-OPPGAVE: opprettet oppgave med id {} for journalpostId: {}", oppgaveId, journalpostId.getVerdi());

        String forsendelseIdString = prosessTaskData.getPropertyValue(FORSENDELSE_ID_KEY);
        if ((forsendelseIdString != null) && !forsendelseIdString.isEmpty()) {
            opprettSletteTask(prosessTaskData);
        }
    }

    private void opprettSletteTask(ProsessTaskData prosessTaskData) {
        var nesteStegProsessTaskData = ProsessTaskData.forProsessTask(SlettForsendelseTask.class);
        // Gi selvbetjening tid til å polle ferdig + Kafka-hendelse tid til å nå fram
        // (og bli ignorert)
        nesteStegProsessTaskData.setNesteKjøringEtter(LocalDateTime.now().plusHours(2));
        long nesteSekvens = prosessTaskData.getSekvens() == null ? 1L : Long.parseLong(prosessTaskData.getSekvens()) + 1;
        nesteStegProsessTaskData.setSekvens(Long.toString(nesteSekvens));
        nesteStegProsessTaskData.setProperties(prosessTaskData.getProperties());
        nesteStegProsessTaskData.setPayload(prosessTaskData.getPayloadAsString());
        nesteStegProsessTaskData.setGruppe(prosessTaskData.getGruppe());
        nesteStegProsessTaskData.setCallIdFraEksisterende();
        taskTjeneste.lagre(nesteStegProsessTaskData);
    }

    private String opprettOppgave(ProsessTaskData prosessTaskData, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId) {
        var enhetInput = prosessTaskData.getPropertyValue(JOURNAL_ENHET);
        // Oppgave har ikke mapping for alle undertyper fødsel/adopsjon
        var brukBT = BehandlingTema.forYtelseUtenFamilieHendelse(behandlingTema);

        var journalpostId = prosessTaskData.getPropertyValue(ARKIV_ID_KEY);

        // Overstyr saker fra NFP+NK, deretter egen logikk hvis fødselsnummer ikke er
        // oppgitt
        var enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, behandlingTema,
            Optional.ofNullable(enhetInput), prosessTaskData.getAktørId());

        var beskrivelse = lagBeskrivelse(behandlingTema, dokumentTypeId, prosessTaskData);
        var saksref = prosessTaskData.getPropertyValue(SAKSNUMMER_KEY);

        var journalpost = JournalpostId.fra(journalpostId);

        var nyOppgave = NyOppgave.builder()
            .medJournalpostId(journalpost)
            .medEnhetId(enhetId)
            .medAktørId(prosessTaskData.getAktørId())
            .medSaksref(saksref)
            .medBehandlingTema(brukBT)
            .medBeskrivelse(beskrivelse)
            .build();

        if (erGosysOppgave(enhetId)) {
            LOG.info("Oppretter en gosys oppgave for {} med {}", journalpost, dokumentTypeId);
            return oppgaverTjeneste.opprettGosysJournalføringsoppgaveFor(nyOppgave);
        } else {
            LOG.info("Oppretter en lokal oppgave for {} med {}", journalpost, dokumentTypeId);
            return oppgaverTjeneste.opprettJournalføringsoppgaveFor(nyOppgave);
        }
    }

    private static boolean erGosysOppgave(String enhet) {
        return NK_ENHET_ID.equals(enhet);
    }
}
