package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.klient.VurderFagsystemResultat;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.Tid;

/**
 * <p>
 * ProssessTask som henter ut informasjon fra søknadsskjema og vurderer denne i henhold til følgende kritterier.
 * </p>
 * <p>
 * En sak er en "passende sak" HVIS aktørID og behandlingstema er likt OG minst en av følgende tilfeller er sanne
 * <ul>
 * <li>Fødselsdato innen intervall -16 - +4 uker fra termin</li>
 * <li>Fødselsdato matcher innen et visst slingringsmonn</li>
 * <li>Imsorgsovertagelsesdato matcher innen et slingringsmonn OG fødselsdato for barn matcher eksakt</li>
 * </ul>
 * </p>
 * <p>
 * For ustrukturerte forsendelser gjelder andre regler; en sak er "passende" HVIS aktørID er lik, OG saken er åpen.
 * </p>
 * <p>
 * Hvis det ikke finnes noen åpen sak så kan "passende sak" være en avsluttet sak som er nyere enn 3 måneder.
 * </p>
 * <p>
 * Hvis det er flere enn en sak som tilfredstiller kriteriene over så foretrekkes den saken som har nyeste behandling.
 * </p>
 */

@Dependent
@ProsessTask(HentOgVurderVLSakTask.TASKNAME)
public class HentOgVurderVLSakTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.hentOgVurderVLSak";
    private final LocalDate konfigVerdiStartdatoForeldrepenger;

    private FagsakRestKlient fagsakRestKlient;

    @Inject
    public HentOgVurderVLSakTask(ProsessTaskRepository prosessTaskRepository, KodeverkRepository kodeverkRepository, FagsakRestKlient fagsakRestKlient,
                                 @KonfigVerdi(value = "foreldrepenger.startdato") String konfigVerdiStartdatoForeldrepenger) {
        super(prosessTaskRepository, kodeverkRepository);
        this.fagsakRestKlient = fagsakRestKlient;
        this.konfigVerdiStartdatoForeldrepenger = LocalDate.parse(konfigVerdiStartdatoForeldrepenger, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        /*
         * TODO: Prouksjonserfaring tilsier at man kun oppretter sak automatisk for IM eller søknader - elektronisk eller på papir
         * Dette fordi man ellers kun får en vurder dokument og ikke kan gjøre noe med mindre det kommer en søknad senere
         *
         * Har lagt inn sjekk på dette i vurderfagsystem ,(hente journalpost) - men: fpfordel vet dokumenttypeid og dokumentkategori -
         * så disse testene kunne like godt vært gjort nedenfor - evt kunne man sendt denne informasjonen til fpsak
         *
         * TODO: PFP-1737 Etter noen måneder er ikke logikken om å sjekke infotrygd for ES lenger relevant - bør gå rett til opprett sak.
         * Dette fordi man kun ser om sakene i Infotrygd er av type ES - vurdering av ES/FP gjøres i aksjonspunkt
         */

        VurderFagsystemResultat behandlendeFagsystemDto = fagsakRestKlient.vurderFagsystem(dataWrapper);

        behandlendeFagsystemDto.getSaksnummer().ifPresent(dataWrapper::setSaksnummer);
        MottakMeldingDataWrapper nesteSteg;
        if (behandlendeFagsystemDto.isPrøvIgjen()) {
            Optional<LocalDateTime> ventIntervallOptional = behandlendeFagsystemDto.getPrøvIgjenTidspunkt();
            if (ventIntervallOptional.isPresent()) {
                nesteSteg = dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME, ventIntervallOptional.get());
            } else {
                throw new IllegalStateException("Utviklerfeil"); //fix korrekt feilhåndtering
            }
        } else if (behandlendeFagsystemDto.isBehandlesIVedtaksløsningen() && behandlendeFagsystemDto.getSaksnummer().isPresent()) {
            nesteSteg = dataWrapper.nesteSteg(TilJournalføringTask.TASKNAME);
        } else if (skalBehandlesEtterTidligereRegler(dataWrapper)) {
            nesteSteg = dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
        } else if (behandlendeFagsystemDto.isBehandlesIVedtaksløsningen()) {
            nesteSteg = dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
        } else if (behandlendeFagsystemDto.isSjekkMotInfotrygd()) {
            nesteSteg = dataWrapper.nesteSteg(HentOgVurderInfotrygdSakTask.TASKNAME);
        } else if (behandlendeFagsystemDto.isManuellVurdering()) {
            nesteSteg = dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
        } else {
            throw new IllegalStateException("Utviklerfeil"); //fix korrekt feilhåndtering
        }
        return nesteSteg;
    }

    private boolean skalBehandlesEtterTidligereRegler(MottakMeldingDataWrapper dataWrapper) {
        if (konfigVerdiStartdatoForeldrepenger.isAfter(dataWrapper.getOmsorgsovertakelsedato().orElse(Tid.TIDENES_ENDE))) {
            return true;
        }
        Optional<Boolean> annenPartHarRett = dataWrapper.getAnnenPartHarRett();
        if (annenPartHarRett.orElse(Boolean.FALSE)) {
            LocalDate barnFødselsdato = dataWrapper.getBarnFodselsdato().orElse(dataWrapper.getBarnTermindato().orElse(Tid.TIDENES_ENDE));
            return konfigVerdiStartdatoForeldrepenger.isAfter(barnFødselsdato);
        }
        return false;
    }

}
