package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdPersonIkkeFunnetException;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.FPDateUtil;

/**
 * <p>ProssessTask som sjekker om det eksisterer en sak i InfoTrygd eller om søknaden er en klage eller anke.</p>
 */
@Dependent
@ProsessTask(HentOgVurderInfotrygdSakTask.TASKNAME)
public class HentOgVurderInfotrygdSakTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.hentOgVurderInfotrygdSak";

    private final TemporalAmount infotrygdSakGyldigPeriode;
    private final TemporalAmount infotrygdAnnenPartGyldigPeriode;

    private final Period gsakEkstraMåneder = Period.parse("P2M");

    private GsakSakTjeneste gsakSakTjeneste;
    private InfotrygdTjeneste infotrygdTjeneste;
    private AktørConsumerMedCache aktørConsumer;

    private static final Logger LOGGER = LoggerFactory.getLogger(HentOgVurderInfotrygdSakTask.class);

    @Inject
    public HentOgVurderInfotrygdSakTask(ProsessTaskRepository prosessTaskRepository, KodeverkRepository kodeverkRepository,
                                        GsakSakTjeneste gsakSakTjeneste, InfotrygdTjeneste infotrygdTjeneste, AktørConsumerMedCache aktørConsumer,
                                        @KonfigVerdi("infotrygd.sak.gyldig.periode") Instance<Period> sakPeriode,
                                        @KonfigVerdi("infotrygd.annen.part.gyldig.periode") Instance<Period> annenPartPeriode) {
        super(prosessTaskRepository, kodeverkRepository);

        this.gsakSakTjeneste = gsakSakTjeneste;
        this.infotrygdTjeneste = infotrygdTjeneste;
        this.aktørConsumer = aktørConsumer;

        this.infotrygdSakGyldigPeriode = sakPeriode.get();
        this.infotrygdAnnenPartGyldigPeriode = annenPartPeriode.get();
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        try {
            dataWrapper.getTema();
        } catch (IllegalStateException e) { // NOSONAR
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.TEMA_KEY, dataWrapper.getId()).toException();
        }
        if (DokumentTypeId.INNTEKTSMELDING.equals(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)) && !BehandlingTema.SVANGERSKAPSPENGER.equals(dataWrapper.getBehandlingTema()) && !dataWrapper.getInntektsmeldingStartDato().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY, dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
        if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema()) && !DokumentTypeId.INNTEKTSMELDING.equals(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))) {
            if (!dataWrapper.getAnnenPartId().isPresent()) {
                throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.ANNEN_PART_ID_KEY, dataWrapper.getId()).toException();
            }
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        Tema tema = dataWrapper.getTema();
        String fnr;
        TemporalAmount periode;

        if (DokumentTypeId.INNTEKTSMELDING.equals(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))) {
            String fnrBruker = aktørConsumer.hentPersonIdentForAktørId(dataWrapper.getAktørId().get())
                    .orElseThrow(() -> MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException());

            LocalDate fom = FPDateUtil.iDag().minus(infotrygdSakGyldigPeriode);
            List<GsakSak> sakerBruker = finnSaker(tema, fnrBruker, fom.minus(gsakEkstraMåneder));
            if (!sakerBruker.isEmpty() && erInfotrygdSakRelevantForInntektsmelding(fnrBruker, dataWrapper, fom)) {
                return dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
            } else {
                return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
            }
        }

        if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema())) {
            fnr = aktørConsumer.hentPersonIdentForAktørId(dataWrapper.getAnnenPartId().get())
                    .orElseThrow(() -> MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException());
            periode = infotrygdAnnenPartGyldigPeriode;
            // Her sjekker vi annen part - unngå at løpende fedrekvoter gir manuell journalføring
            if (Character.digit(fnr.charAt(8), 10) % 2 != 0) {
                return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
            }
        } else if (BehandlingTema.gjelderEngangsstønad(dataWrapper.getBehandlingTema())) {
            return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
        } else if (BehandlingTema.SVANGERSKAPSPENGER.equals(dataWrapper.getBehandlingTema())) {
            fnr = aktørConsumer.hentPersonIdentForAktørId(dataWrapper.getAktørId().get())
                    .orElseThrow(() -> MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException());
            periode = infotrygdSakGyldigPeriode;
        } else {
            throw MottakMeldingFeil.FACTORY.ukjentBehandlingstema(dataWrapper.getBehandlingTema()).toException();
        }

        LocalDate fom = FPDateUtil.iDag().minus(periode);
        List<GsakSak> saker = finnSaker(tema, fnr, fom);
        if (!saker.isEmpty() && erInfotrygdSakRelevant(fnr, dataWrapper, fom)) {
            return dataWrapper.nesteSteg(MidlJournalføringTask.TASKNAME);
        }
        return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
    }

    private List<GsakSak> finnSaker(Tema tema, String fnr, LocalDate gsakFom) {
        List<GsakSak> saker = gsakSakTjeneste.finnSaker(fnr);
        saker = saker.stream()
                .filter(infotrygdSak -> infotrygdSak.getFagsystem().equals(Fagsystem.INFOTRYGD))
                .filter(infotrygdSak -> infotrygdSak.getTema().equals(tema))
                .filter(infotrygdSak -> !infotrygdSak.getSistEndret().isPresent() || infotrygdSak.getSistEndret().get().isAfter(gsakFom))
                .collect(Collectors.toList());
        return saker;
    }

    private boolean erInfotrygdSakRelevant(String fnr, MottakMeldingDataWrapper dataWrapper, LocalDate fom) {

        if (BehandlingTema.gjelderEngangsstønad(dataWrapper.getBehandlingTema())) {
            return false; // Ref logikk over. Skal ikke havne her
        } else if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema())) {
            return erInfotrygdSakRelevantForForeldrepenger(fnr, fom);
        } else if (BehandlingTema.SVANGERSKAPSPENGER.equals(dataWrapper.getBehandlingTema())) {
            return erInfotrygdSakRelevantForSvangerskapspenger(fnr, fom);
        } else {
            return false;
        }
    }

    /**
     * Sjekker om vi har en foreldrepenger i infotrygd som er nyere enn 18 mnd(konfigurerbar verdi, men er pt satt til 18 mnd).
     *
     * @param fnr søkers fødselsnummer
     */
    private boolean erInfotrygdSakRelevantForForeldrepenger(String fnr, LocalDate fom) {
        List<InfotrygdSak> infotrygdSaker = hentInfotrygdSaker(fnr, fom);
        return infotrygdSaker.stream().filter(InfotrygdSak::gjelderForeldrepenger)
                .anyMatch(sak -> sak.getRegistrert().isAfter(fom) || sak.getIverksatt().orElse(Tid.TIDENES_BEGYNNELSE).isAfter(fom));
    }

    private boolean erInfotrygdSakRelevantForSvangerskapspenger(String fnr, LocalDate fom) {
        List<InfotrygdSak> infotrygdSaker = hentInfotrygdSaker(fnr, fom);
        return infotrygdSaker.stream().filter(InfotrygdSak::gjelderSvangerskapspenger)
                .anyMatch(sak -> sak.getRegistrert().isAfter(fom) || sak.getIverksatt().orElse(Tid.TIDENES_BEGYNNELSE).isAfter(fom));
    }

    private boolean erInfotrygdSakRelevantForInntektsmelding(String fnr, MottakMeldingDataWrapper dataWrapper, LocalDate fom) {
        if ((Character.getNumericValue(fnr.charAt(8)) % 2 != 0)) {
            return true;
        }
        List<InfotrygdSak> infotrygdSaker = hentInfotrygdSaker(fnr, fom);
        if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema())) {
            // Ved behov vurder en ekstra anymatch på sak getRegistrert() er etter fom
            return infotrygdSaker.stream()
                    .filter(InfotrygdSak::gjelderForeldrepenger)
                    .filter(sak -> sak.getIverksatt().isPresent())
                    .anyMatch(infotrygdSak -> infotrygdSak.getIverksatt().orElse(Tid.TIDENES_BEGYNNELSE).isAfter(fom));
        } else if (BehandlingTema.SVANGERSKAPSPENGER.equals(dataWrapper.getBehandlingTema())) {
            return infotrygdSaker.stream()
                    .filter(InfotrygdSak::gjelderSvangerskapspenger)
                    .anyMatch(infotrygdSak -> infotrygdSak.getRegistrert().isAfter(fom) || infotrygdSak.getIverksatt().orElse(Tid.TIDENES_ENDE).isAfter(fom));
        }
        return false;
    }

    private List<InfotrygdSak> hentInfotrygdSaker(String fnr, LocalDate fom) {
        try {
            return infotrygdTjeneste.finnSakListe(fnr, fom);
        } catch (InfotrygdPersonIkkeFunnetException e) {
            Feilene.FACTORY.feilFraInfotrygdSakFordeling(e).log(LOGGER);
        }
        return new ArrayList<>();
    }

    interface Feilene extends DeklarerteFeil {
        Feilene FACTORY = FeilFactory.create(Feilene.class);

        @TekniskFeil(feilkode = "FP-074122", feilmelding = "PersonIkkeFunnet fra infotrygdSak", logLevel = LogLevel.WARN)
        Feil feilFraInfotrygdSakFordeling(Exception cause);
    }
}
