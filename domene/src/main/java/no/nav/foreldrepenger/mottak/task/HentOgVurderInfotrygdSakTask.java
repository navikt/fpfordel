package no.nav.foreldrepenger.mottak.task;

import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.AKTØR_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ANNEN_PART_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAmount;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.infotrygd.rest.RelevantSakSjekker;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProssessTask som sjekker om det eksisterer en sak i InfoTrygd eller om
 * søknaden er en klage eller anke.
 * </p>
 */
@Dependent
@ProsessTask(HentOgVurderInfotrygdSakTask.TASKNAME)
public class HentOgVurderInfotrygdSakTask extends WrappedProsessTaskHandler {

    /*
     * Konfigurasjon for hvor langt tilbake i tid man spør Infotrygd om det finnes saker
     */
    private static final TemporalAmount INFOTRYGD_SAK_GYLDIG_PERIODE = Period.ofMonths(10);
    private static final TemporalAmount INFOTRYGD_ANNENPART_GYLDIG_PERIODE = Period.ofMonths(18);

    private static final Logger LOGGER = LoggerFactory.getLogger(HentOgVurderInfotrygdSakTask.class);

    public static final String TASKNAME = "fordeling.hentOgVurderInfotrygdSak";

    private final AktørConsumerMedCache aktør;
    private final RelevantSakSjekker relevansSjekker;

    @Inject
    public HentOgVurderInfotrygdSakTask(ProsessTaskRepository prosessTaskRepository,
                                        RelevantSakSjekker relevansSjekker,
                                        AktørConsumerMedCache aktør) {
        super(prosessTaskRepository);
        this.relevansSjekker = relevansSjekker;
        this.aktør = aktør;
        LOGGER.info("Konstruert {}", this);
    }

    @Override
    public void precondition(MottakMeldingDataWrapper w) {
        try {
            w.getTema();
        } catch (IllegalStateException e) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    TEMA_KEY, w.getId()).toException();
        }
        if (gjelderIM(w)
                && !gjelderSvangerskapspenger(w)
                && w.getInntektsmeldingStartDato().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    INNTEKSTMELDING_STARTDATO_KEY, w.getId()).toException();
        }
        if (w.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    AKTØR_ID_KEY, w.getId()).toException();
        }
        if (gjelderForeldrepenger(w) && !gjelderIM(w)) {
            if (w.getAnnenPartId().isEmpty()) {
                throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                        ANNEN_PART_ID_KEY, w.getId()).toException();
            }
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {

        if (gjelderIM(w)) {
            return nesteStegForIM(w);
        }
        if (gjelderEngangsstønad(w)) {
            return nesteStegOpprettet(w);
        }
        if (gjelderForeldrepenger(w)) {
            return nesteStegForForeldrepenger(w);
        }
        if (gjelderSvangerskapspenger(w)) {
            return nesteStegForSvangerskapspenger(w);
        }
        throw MottakMeldingFeil.FACTORY.ukjentBehandlingstema(w.getBehandlingTema().getKode()).toException();
    }

    private MottakMeldingDataWrapper nesteStegForSvangerskapspenger(MottakMeldingDataWrapper w) {
        if (skalMidlertidigJournalføre(w, fnr(w), LocalDate.now().minus(INFOTRYGD_SAK_GYLDIG_PERIODE))) {
            LOGGER.info("FPFORDEL VINFOTRYGD svp journalpost {}", w.getArkivId());
            return midlertidigJournalført(w);
        }
        return nesteStegOpprettet(w);
    }

    private MottakMeldingDataWrapper nesteStegForForeldrepenger(MottakMeldingDataWrapper w) {
        // Her sjekker vi annen part - unngå at løpende fedrekvoter gir manuell
        // journalføring
        String annenPart = fnrAnnenPart(w);
        if (erMann(annenPart)) {
            return nesteStegOpprettet(w);
        }
        if (skalMidlertidigJournalføre(w, annenPart, LocalDate.now().minus(INFOTRYGD_ANNENPART_GYLDIG_PERIODE))) {
            LOGGER.info("FPFORDEL VINFOTRYGD fp journalpost {}", w.getArkivId());
            return midlertidigJournalført(w);
        }
        return nesteStegOpprettet(w);
    }

    private static boolean gjelderSvangerskapspenger(MottakMeldingDataWrapper w) {
        return BehandlingTema.gjelderSvangerskapspenger(w.getBehandlingTema());
    }

    private static boolean gjelderForeldrepenger(MottakMeldingDataWrapper w) {
        return BehandlingTema.gjelderForeldrepenger(w.getBehandlingTema());
    }

    private static boolean gjelderEngangsstønad(MottakMeldingDataWrapper w) {
        return BehandlingTema.gjelderEngangsstønad(w.getBehandlingTema());
    }

    private String fnr(MottakMeldingDataWrapper w) {
        return aktør.hentPersonIdentForAktørId(w.getAktørId().get())
                .orElseThrow(() -> MottakMeldingFeil.FACTORY
                        .fantIkkePersonidentForAktørId(TASKNAME, w.getId()).toException());
    }

    private String fnrAnnenPart(MottakMeldingDataWrapper w) {
        return aktør.hentPersonIdentForAktørId(w.getAnnenPartId().get())
                .orElseThrow(() -> MottakMeldingFeil.FACTORY
                        .fantIkkePersonidentForAktørId(TASKNAME, w.getId()).toException());
    }

    private static MottakMeldingDataWrapper midlertidigJournalført(MottakMeldingDataWrapper w) {
        return w.nesteSteg(MidlJournalføringTask.TASKNAME);
    }

    private static MottakMeldingDataWrapper nesteStegOpprettet(MottakMeldingDataWrapper w) {
        return w.nesteSteg(OpprettSakTask.TASKNAME);
    }

    private static boolean erMann(String fnr) {
        return Character.digit(fnr.charAt(8), 10) % 2 != 0;
    }

    private static boolean gjelderIM(MottakMeldingDataWrapper wrapper) {
        return DokumentTypeId.INNTEKTSMELDING.equals(wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT));
    }

    private MottakMeldingDataWrapper nesteStegForIM(MottakMeldingDataWrapper w) {
        String fnr = fnr(w);
        if (skalMidlertidigJournalføreIM(w, fnr, LocalDate.now())) {
            LOGGER.info("FPFORDEL VINFOTRYGD IM journalpost {}", w.getArkivId());
            return midlertidigJournalført(w);
        }
        return nesteStegOpprettet(w);
    }

    private boolean skalMidlertidigJournalføre(MottakMeldingDataWrapper w, String fnr, LocalDate fom) {
        return relevansSjekker.skalMidlertidigJournalføre(w.getAktørId().orElse(null), fnr, fom, w.getTema(), w.getBehandlingTema());
    }

    private boolean skalMidlertidigJournalføreIM(MottakMeldingDataWrapper w, String fnr, LocalDate fom) {
        return relevansSjekker.skalMidlertidigJournalføreIM(w.getAktørId().orElse(null), fnr, fom.minus(INFOTRYGD_SAK_GYLDIG_PERIODE), w.getTema(),
                w.getBehandlingTema());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[infotrygdSakGyldigPeriode=" + INFOTRYGD_SAK_GYLDIG_PERIODE
                + ", infotrygdAnnenPartGyldigPeriode=" + INFOTRYGD_ANNENPART_GYLDIG_PERIODE + ", aktør=" + aktør
                + ", relevansSjekker=" + relevansSjekker + "]";
    }
}
