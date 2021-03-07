package no.nav.foreldrepenger.mottak.task;

import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.AKTØR_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ANNEN_PART_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.tjeneste.VurderInfotrygd;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(HentOgVurderInfotrygdSakTask.class);

    public static final String TASKNAME = "fordeling.hentOgVurderInfotrygdSak";

    private final VurderInfotrygd vurderInfotrygd;

    @Inject
    public HentOgVurderInfotrygdSakTask(ProsessTaskRepository prosessTaskRepository,
            VurderInfotrygd vurderInfotrygd) {
        super(prosessTaskRepository);
        this.vurderInfotrygd = vurderInfotrygd;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper w) {
        try {
            w.getTema();
        } catch (IllegalStateException e) {
            throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                    TEMA_KEY, w.getId());
        }
        if (gjelderIM(w)
                && !gjelderSvangerskapspenger(w)
                && w.getInntektsmeldingStartDato().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                    INNTEKSTMELDING_STARTDATO_KEY, w.getId());
        }
        if (w.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                    AKTØR_ID_KEY, w.getId());
        }
        if (gjelderForeldrepenger(w) && !gjelderIM(w)) {
            if (w.getAnnenPartId().isEmpty()) {
                throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                        ANNEN_PART_ID_KEY, w.getId());
            }
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {
        return vurderInfotrygd.kreverManuellVurdering(w) ? midlertidigJournalført(w) : nesteStegOpprettet(w);
    }

    private static MottakMeldingDataWrapper midlertidigJournalført(MottakMeldingDataWrapper w) {
        return w.nesteSteg(MidlJournalføringTask.TASKNAME);
    }

    private static MottakMeldingDataWrapper nesteStegOpprettet(MottakMeldingDataWrapper w) {
        return w.nesteSteg(OpprettSakTask.TASKNAME);
    }

    private static boolean gjelderIM(MottakMeldingDataWrapper wrapper) {
        return DokumentTypeId.INNTEKTSMELDING.equals(wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT));
    }

    private static boolean gjelderSvangerskapspenger(MottakMeldingDataWrapper w) {
        return BehandlingTema.gjelderSvangerskapspenger(w.getBehandlingTema());
    }

    private static boolean gjelderForeldrepenger(MottakMeldingDataWrapper w) {
        return BehandlingTema.gjelderForeldrepenger(w.getBehandlingTema());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[vurderInfotrygd=" + vurderInfotrygd + "]";
    }
}
