package no.nav.foreldrepenger.mottak.task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.tjeneste.VurderVLSaker;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProssessTask som henter ut informasjon fra søknadsskjema og vurderer denne i
 * henhold til følgende kritterier.
 * </p>
 * <p>
 * En sak er en "passende sak" HVIS aktørID og behandlingstema er likt OG minst
 * en av følgende tilfeller er sanne
 * <ul>
 * <li>Fødselsdato innen intervall -16 - +4 uker fra termin</li>
 * <li>Fødselsdato matcher innen et visst slingringsmonn</li>
 * <li>Imsorgsovertagelsesdato matcher innen et slingringsmonn OG fødselsdato
 * for barn matcher eksakt</li>
 * </ul>
 * </p>
 * <p>
 * For ustrukturerte forsendelser gjelder andre regler; en sak er "passende"
 * HVIS aktørID er lik, OG saken er åpen.
 * </p>
 * <p>
 * Hvis det ikke finnes noen åpen sak så kan "passende sak" være en avsluttet
 * sak som er nyere enn 3 måneder.
 * </p>
 * <p>
 * Hvis det er flere enn en sak som tilfredstiller kriteriene over så
 * foretrekkes den saken som har nyeste behandling.
 * </p>
 */

@Dependent
@ProsessTask(HentOgVurderVLSakTask.TASKNAME)
public class HentOgVurderVLSakTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.hentOgVurderVLSak";

    private final VurderVLSaker vurderVLSaker;

    @Inject
    public HentOgVurderVLSakTask(ProsessTaskRepository prosessTaskRepository,
                                 VurderVLSaker vurderVLSaker) {
        super(prosessTaskRepository);
        this.vurderVLSaker = vurderVLSaker;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId());
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId());
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {

        var destinasjon = vurderVLSaker.bestemDestinasjon(dataWrapper);
        if (ForsendelseStatus.GOSYS.equals(destinasjon.system())) {
            return dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
        } else if (destinasjon.saksnummer() != null) {
            return dataWrapper.nesteSteg(TilJournalføringTask.TASKNAME);
        } else {
            return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
        }

    }
}
