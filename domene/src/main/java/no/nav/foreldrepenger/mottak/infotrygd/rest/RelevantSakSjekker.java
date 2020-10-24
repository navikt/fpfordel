package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.rest.fp.FP;

@ApplicationScoped
public class RelevantSakSjekker {

    private InfotrygdTjeneste fp;

    RelevantSakSjekker() {
        //
    }

    @Inject
    public RelevantSakSjekker(@FP InfotrygdTjeneste fp) {
        this.fp = fp;
    }

    public boolean skalMidlertidigJournalføre(String fnr, LocalDate fom, BehandlingTema behandlingTema) {
        return erITSakRelevant(fnr, fom, behandlingTema);
    }

    public boolean skalMidlertidigJournalføreIM(String fnr, LocalDate fom, BehandlingTema behandlingTema) {
        return erITSakRelevantForIM(fnr, fom, behandlingTema);
    }

    private boolean erITSakRelevant(String fnr, LocalDate fom, BehandlingTema tema) {

        if (BehandlingTema.gjelderForeldrepenger(tema)) {
            return erITSakRelevantForFP(fnr, fom);
        }
        return false;
    }

    private boolean erITSakRelevantForIM(String fnr, LocalDate fom, BehandlingTema tema) {
        if (BehandlingTema.gjelderForeldrepenger(tema)) {
            return erFpRelevantForIM(fnr, fom);
        }
        return false;
    }

    private boolean erITSakRelevantForFP(String fnr, LocalDate fom) {
        return restSaker(fp, fnr, fom).stream()
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private boolean erFpRelevantForIM(String fnr, LocalDate fom) {
        return restSaker(fp, fnr, fom).stream()
                .map(InfotrygdSak::getIverksatt)
                .flatMap(Optional::stream)
                .anyMatch(fom::isBefore);
    }

    private static List<InfotrygdSak> restSaker(InfotrygdTjeneste t, String fnr, LocalDate fom) {
        return t.finnSakListe(fnr, fom);
    }

    private static Predicate<? super InfotrygdSak> svpFpRelevantTidFilter(LocalDate fom) {
        // Intensjon med FALSE for å unngå treff pga praksis i enheter med
        // informasjonssaker
        return sak -> sak.getIverksatt().map(fom::isBefore).orElse(false)
                || ((sak.getRegistrert() != null) && fom.isBefore(sak.getRegistrert()));
    }

}