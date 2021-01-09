package no.nav.foreldrepenger.mottak.task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.klient.FagsakTjeneste;
import no.nav.foreldrepenger.mottak.klient.VurderFagsystemResultat;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProssessTask som oppretter ny sak internt i Vedtaksløsningen (med mindre en
 * sak allerede er opprettet)
 * </p>
 */
@Dependent
@ProsessTask(OpprettSakTask.TASKNAME)
public class OpprettSakTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.opprettSak";
    private static final Logger LOG = LoggerFactory.getLogger(OpprettSakTask.class);

    private final FagsakTjeneste fagsakRestKlient;

    @Inject
    public OpprettSakTask(ProsessTaskRepository prosessTaskRepository,
            FagsakTjeneste fagsakRestKlient) {
        super(prosessTaskRepository);
        this.fagsakRestKlient = fagsakRestKlient;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getDokumentTypeId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getDokumentKategori().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.DOKUMENTKATEGORI_ID_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (TilJournalføringTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())) {
            if (dataWrapper.getSaksnummer().isEmpty()) {
                throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME,
                        MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()).toException();
            }
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        if (erKlageEllerAnke(dataWrapper)) {
            return dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
        }

        // Før vi oppretter sak, må vi sjekke at det ikke er opprettet en sak for det
        // samme tilfellet (noe som kan skje hvis vi får inn flere søknader på samme sak
        // innen "kort tid").
        VurderFagsystemResultat vurderFagsystemRespons = fagsakRestKlient.vurderFagsystem(dataWrapper);

        if (vurderFagsystemRespons.isManuellVurdering()) { // Dette skal ikke skje på dette stadiet
            LOG.info("vurderFagsystem returnerte uventet fagsystem. Setter saken til manuell journalføring.");
            return dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
        }

        vurderFagsystemRespons.getSaksnummer().ifPresent(dataWrapper::setSaksnummer);
        if (vurderFagsystemRespons.getSaksnummer().isEmpty()) {
            SaksnummerDto saksnummerDto = fagsakRestKlient.opprettSak(new OpprettSakDto(dataWrapper.getArkivId(),
                    dataWrapper.getBehandlingTema().getOffisiellKode(), dataWrapper.getAktørId().get()));
            dataWrapper.setSaksnummer(saksnummerDto.getSaksnummer());
        }

        return dataWrapper.nesteSteg(TilJournalføringTask.TASKNAME);
    }

    private static boolean erKlageEllerAnke(MottakMeldingDataWrapper data) {
        return (DokumentTypeId.KLAGE_DOKUMENT.equals(data.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))
                || DokumentKategori.KLAGE_ELLER_ANKE
                        .equals(data.getDokumentKategori().orElse(DokumentKategori.UDEFINERT)));
    }
}
