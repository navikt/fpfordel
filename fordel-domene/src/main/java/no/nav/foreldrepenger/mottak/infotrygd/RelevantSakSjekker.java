package no.nav.foreldrepenger.mottak.infotrygd;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class RelevantSakSjekker {

    private final InfotrygdTjeneste fp;

    @Inject
    public RelevantSakSjekker(InfotrygdTjeneste fp) {
        this.fp = fp;
    }

    public boolean skalMidlertidigJournalføre(String fnr, LocalDate fom) {
        return erITSakRelevantForFP(fnr, fom);
    }

    public boolean skalMidlertidigJournalføreIM(String fnr, LocalDate fom) {
        return erFpRelevantForIM(fnr, fom);
    }

    private boolean erITSakRelevantForFP(String fnr, LocalDate fom) {
        return restSaker(fp, fnr, fom)
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private boolean erFpRelevantForIM(String fnr, LocalDate fom) {
        return restSaker(fp, fnr, fom)
                .map(InfotrygdSak::getIverksatt)
                .flatMap(Optional::stream)
                .anyMatch(fom::isBefore);
    }

    private static Stream<InfotrygdSak> restSaker(InfotrygdTjeneste t, String fnr, LocalDate fom) {
        return t.finnSakListe(fnr, fom).stream();
    }

    private static Predicate<? super InfotrygdSak> svpFpRelevantTidFilter(LocalDate fom) {
        // Intensjon med FALSE for å unngå treff pga praksis i enheter med
        // informasjonssaker
        return sak -> sak.getIverksatt().map(fom::isBefore).orElse(false)
                || ((sak.registrert() != null) && fom.isBefore(sak.registrert()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [fp=" + fp + "]";
    }

}