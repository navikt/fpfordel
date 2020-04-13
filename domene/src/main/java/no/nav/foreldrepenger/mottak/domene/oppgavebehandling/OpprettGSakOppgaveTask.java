package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask.TASKNAME;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ANNEN_PART_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_MOTTATT_TIDSPUNKT_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.SAKSNUMMER_KEY;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.kodeverdi.Temagruppe;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest.OppgaveRestKlient;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest.OpprettOppgave;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest.Prioritet;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.SlettForsendelseTask;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumer;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BrukerType;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.FagomradeKode;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.PrioritetKode;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProsessTask som oppretter en oppgave i GSAK for manuell behandling av
 * tilfeller som ikke kan håndteres automatisk av vedtaksløsningen.
 * <p>
 * </p>
 */
@Dependent
@ProsessTask(TASKNAME)
public class OpprettGSakOppgaveTask implements ProsessTaskHandler {

    public static final String TASKNAME = "integrasjon.gsak.opprettOppgave";

    private static final FagomradeKode FAGOMRADE_KODE = FagomradeKode.FOR;
    private static final PrioritetKode PRIORITET_KODE = PrioritetKode.NORM_FOR;
    private static final boolean IKKE_LEST = false;
    private static final Logger log = LoggerFactory.getLogger(OpprettGSakOppgaveTask.class);

    /**
     * Journalføring foreldrepenger - JFR_FOR er ikke dokumentert i
     * tjenestedokumentasjon, men er koden som blir brukt i kodeverk i GSAK.
     */
    static final String JFR_FOR = "JFR_FOR";

    private final BehandleoppgaveConsumer service;

    private final EnhetsTjeneste enhetsidTjeneste;
    private final AktørConsumerMedCache aktørConsumer;
    private final ProsessTaskRepository prosessTaskRepository;
    private final OppgaveRestKlient restKlient;

    @Inject
    public OpprettGSakOppgaveTask(ProsessTaskRepository prosessTaskRepository,
                                  BehandleoppgaveConsumer service,
                                  EnhetsTjeneste enhetsidTjeneste,
                                  AktørConsumerMedCache aktørConsumer,
                                  OppgaveRestKlient restKlient) {
        this.service = service;
        this.enhetsidTjeneste = enhetsidTjeneste;
        this.aktørConsumer = aktørConsumer;
        this.prosessTaskRepository = prosessTaskRepository;
        this.restKlient = restKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        BehandlingTema behandlingTema = finnBehandlingTema(
                Optional.ofNullable(prosessTaskData.getPropertyValue(BEHANDLINGSTEMA_KEY)));
        DokumentTypeId dokumentTypeId = Optional.ofNullable(prosessTaskData.getPropertyValue(DOKUMENTTYPE_ID_KEY))
                .map(DokumentTypeId::fraKodeDefaultUdefinert).orElse(DokumentTypeId.UDEFINERT);
        behandlingTema = HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(behandlingTema, dokumentTypeId);

        String oppgaveId = opprettOppgave(prosessTaskData, behandlingTema, dokumentTypeId);

        log.info("Oppgave opprettet i Gosys med nummer: {}", oppgaveId);

        String forsendelseIdString = prosessTaskData.getPropertyValue(FORSENDELSE_ID_KEY);
        Optional<UUID> forsendelseId = forsendelseIdString == null ? Optional.empty()
                : Optional.of(UUID.fromString(forsendelseIdString));
        if (forsendelseId.isPresent()) {
            opprettSletteTask(prosessTaskData);
        }
    }

    private void opprettSletteTask(ProsessTaskData prosessTaskData) {
        ProsessTaskData nesteStegProsessTaskData = new ProsessTaskData(SlettForsendelseTask.TASKNAME);
        nesteStegProsessTaskData.setNesteKjøringEtter(LocalDateTime.now().plusMinutes(30)); // Gi selvbetjening tid til å
                                                                                        // polle ferdig
        long nesteSekvens = prosessTaskData.getSekvens() == null ? 1L
                : Long.parseLong(prosessTaskData.getSekvens()) + 1;
        nesteStegProsessTaskData.setSekvens(Long.toString(nesteSekvens));
        nesteStegProsessTaskData.setProperties(prosessTaskData.getProperties());
        nesteStegProsessTaskData.setPayload(prosessTaskData.getPayload());
        nesteStegProsessTaskData.setGruppe(prosessTaskData.getGruppe());
        prosessTaskRepository.lagre(nesteStegProsessTaskData);
    }

    private BehandlingTema finnBehandlingTema(Optional<String> kode) {
        BehandlingTema behandlingTema;
        try {
            behandlingTema = kode.map(BehandlingTema::fraKodeDefaultUdefinert)
                    .orElse(BehandlingTema.UDEFINERT);
        } catch (NoResultException e) { // NOSONAR
            // Vi skal tåle ukjent behandlingstema
            behandlingTema = BehandlingTema.UDEFINERT;
        }
        return behandlingTema;
    }

    /**
     * Det er to typer oppgaver som kan sendes til GSAK. Journalføringsoppgave eller
     * fordelingsopgpave. Fordelingsoppgave er ikke lenger i bruk. EnhetsId til
     * andre oppgaver skal hentes fra ekstern tjeneste.
     */
    private String opprettOppgave(ProsessTaskData prosessTaskData, BehandlingTema behandlingTema,
            DokumentTypeId dokumentTypeId) {
        final Optional<String> fødselsnr = hentPersonidentifikatorFraTaskData(prosessTaskData.getAktørId());
        final Optional<String> annenpartFnr = hentAnnenPartFraTaskData(prosessTaskData);
        final String enhetInput = prosessTaskData.getPropertyValue(JOURNAL_ENHET);

        String arkivId = prosessTaskData.getPropertyValue(ARKIV_ID_KEY);

        // Overstyr saker fra NFP+NK, deretter egen logikk hvis fødselsnummer ikke er
        // oppgitt
        final String enhetId = enhetsidTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, behandlingTema,
                Optional.ofNullable(enhetInput), fødselsnr, annenpartFnr);
        final String beskrivelse = lagBeskrivelse(behandlingTema, dokumentTypeId, prosessTaskData);

        try {
            var request = OpprettOppgave.getBuilder()
                    .medAktoerId(prosessTaskData.getAktørId())
                    .medSaksreferanse(prosessTaskData.getPropertyValue(SAKSNUMMER_KEY))
                    .medTildeltEnhetsnr(enhetId)
                    .medOpprettetAvEnhetsnr(enhetId)
                    .medJournalpostId(arkivId)
                    .medAktivDato(LocalDate.now())
                    .medFristFerdigstillelse(helgeJustertFrist(LocalDate.now().plusDays(1L)))
                    .medBeskrivelse(beskrivelse)
                    .medTemagruppe(Temagruppe.FAMILIEYTELSER.getKode())
                    .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
                    .medBehandlingstema(BehandlingTema.UDEFINERT.equals(behandlingTema) ? null : behandlingTema.getOffisiellKode())
                    .medOppgavetype("JFR")
                    .medPrioritet(Prioritet.NORM);
            var oppgave = restKlient.opprettetOppgave(request);
            if (oppgave == null || oppgave.getId() == null)
                throw new IllegalStateException("Gosys rest: kunne ikke opprette oppgave");
            return oppgave.getId().toString();
        } catch (Exception e) {
            log.info("FPFORDEL GOSYS rest - feil ved oppretting av oppgave",e );
        }

        OpprettOppgaveRequest request = createRequest(prosessTaskData, enhetId, beskrivelse, behandlingTema,
                dokumentTypeId, arkivId, fødselsnr);

        return service.opprettOppgave(request).getOppgaveId();

    }

    private String lagBeskrivelse(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, ProsessTaskData data) {
        if (DokumentTypeId.UDEFINERT.equals(dokumentTypeId)) {
            return BehandlingTema.UDEFINERT.equals(behandlingTema) ? "Journalføring" : behandlingTema.getTermNavn();
        }
        String beskrivelse = dokumentTypeId.getTermNavn();
        if (DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.equals(dokumentTypeId)
                && data.getPropertyValue(MottakMeldingDataWrapper.FØRSTE_UTTAKSDAG_KEY) != null) {
            String uttakStart = data.getPropertyValue(MottakMeldingDataWrapper.FØRSTE_UTTAKSDAG_KEY);
            beskrivelse = beskrivelse + " (" + uttakStart + ")";
        }
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            if (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKTSMELDING_YTELSE) != null) {
                beskrivelse = beskrivelse + " ("
                        + data.getPropertyValue(MottakMeldingDataWrapper.INNTEKTSMELDING_YTELSE) + ")";
            }
            if (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY) != null) {
                beskrivelse = beskrivelse + " ("
                        + (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY)) + ")";
            }
        }
        return beskrivelse;
    }

    private OpprettOppgaveRequest createRequest(ProsessTaskInfo prosessTaskData, String enhetsId, String beskrivelse,
            BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, String arkivId, Optional<String> fødselsnr) {
        OpprettOppgaveRequest.Builder builder = OpprettOppgaveRequest.builder();

        // Kodeverk fra FGSAK / Gosys. Søk etter ENGANGSST_FOR på confluence og bruk
        // verdier fra regneark (sic)....
        setFagområdeOgPrioritet(prosessTaskData, builder, behandlingTema, dokumentTypeId);
        if (fødselsnr.isPresent()) {
            builder = builder.medFnr(fødselsnr.get());
        }
        if (prosessTaskData.getPropertyValue(SAKSNUMMER_KEY) != null) {
            builder.medSaksnummer(prosessTaskData.getPropertyValue(SAKSNUMMER_KEY));
        }

        return builder
                .medOpprettetAvEnhetId(Integer.parseInt(enhetsId))
                .medAnsvarligEnhetId(enhetsId)
                .medDokumentId(arkivId)
                .medBrukerTypeKode(BrukerType.PERSON)
                .medMottattDato(
                        hentDatoFraTaskData(prosessTaskData.getPropertyValue(FORSENDELSE_MOTTATT_TIDSPUNKT_KEY)))
                .medAktivFra(LocalDate.now())
                .medAktivTil(helgeJustertFrist(LocalDate.now().plusDays(1L)))
                .medBeskrivelse(beskrivelse)
                .medLest(IKKE_LEST)
                .build();
    }

    private void setFagområdeOgPrioritet(ProsessTaskInfo info, OpprettOppgaveRequest.Builder builder,
            BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId) {
        builder.medFagomradeKode(FAGOMRADE_KODE.toString())
                .medPrioritetKode(PRIORITET_KODE.toString())
                .medOppgavetypeKode(JFR_FOR);
        if (BehandlingTema.gjelderForeldrepenger(behandlingTema) || DokumentTypeId.erForeldrepengerRelatert(dokumentTypeId)) {
            builder.medUnderkategoriKode("FORELDREPE_FOR");
        } else if (BehandlingTema.gjelderEngangsstønad(behandlingTema) || DokumentTypeId.erEngangsstønadRelatert(dokumentTypeId)) {
            builder.medUnderkategoriKode("ENGANGSST_FOR");
        } else if (BehandlingTema.gjelderSvangerskapspenger(behandlingTema)
                || DokumentTypeId.erSvangerskapspengerRelatert(dokumentTypeId)) {
            builder.medUnderkategoriKode("SVANGERSKAPSPE_FOR");
        }
    }

    // Sett frist til mandag hvis fristen er i helgen.
    private LocalDate helgeJustertFrist(LocalDate dato) {
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            return dato.plusDays(1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue());
        }
        return dato;
    }

    private LocalDate hentDatoFraTaskData(String propertyValue) {
        return propertyValue == null ? null : LocalDateTime.parse(propertyValue).toLocalDate();
    }

    private Optional<String> hentPersonidentifikatorFraTaskData(String aktørId) {
        if (aktørId == null) {
            return Optional.empty();
        }
        return aktørConsumer.hentPersonIdentForAktørId(aktørId);
    }

    private Optional<String> hentAnnenPartFraTaskData(ProsessTaskData prosessTaskData) {
        var annenpart = prosessTaskData.getPropertyValue(ANNEN_PART_ID_KEY);
        if (annenpart == null) {
            return Optional.empty();
        }
        try {
            return aktørConsumer.hentPersonIdentForAktørId(annenpart);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
