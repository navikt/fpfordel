package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem.INFOTRYGD;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.gsak.GsakSakTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.rest.fp.FP;
import no.nav.foreldrepenger.mottak.infotrygd.rest.svp.SVP;

@ApplicationScoped
public class RelevantSakSjekker {
    private static final Period GSAK_EKSTRA_MND = Period.ofMonths(2);

    private InfotrygdTjeneste svp;
    private InfotrygdTjeneste fp;

    private GsakSakTjeneste gsak;

    RelevantSakSjekker() {
        //
    }

    @Inject
    public RelevantSakSjekker(
            @SVP InfotrygdTjeneste svp,
            @FP InfotrygdTjeneste fp,
            GsakSakTjeneste gsak) {
        this.svp = svp;
        this.fp = fp;
        this.gsak = gsak;
    }

    public boolean skalMidlertidigJournalføre(String fnr, LocalDate fom, Tema tema, BehandlingTema behandlingTema) {
        return harGsakSaker(fnr, fom, tema) && erITSakRelevant(fnr, fom, behandlingTema);
    }

    public boolean skalMidlertidigJournalføreIM(String fnr, LocalDate fom, Tema tema, BehandlingTema behandlingTema) {
        return harGsakSaker(fnr, fom.minus(GSAK_EKSTRA_MND), tema) && erITSakRelevantForIM(fnr, fom, behandlingTema);
    }

    private boolean harGsakSaker(String fnr, LocalDate fom, Tema tema) {
        return gsak.finnSaker(fnr)
                .stream()
                .filter(sak -> sak.getFagsystem().equals(INFOTRYGD))
                .filter(sak -> sak.getTema().equals(tema))
                .anyMatch(sak -> sak.getSistEndret()
                        .map(fom::isBefore)
                        .orElse(true));
    }

    private boolean erITSakRelevant(String fnr, LocalDate fom, BehandlingTema tema) {

        if (BehandlingTema.gjelderForeldrepenger(tema)) {
            return erITSakRelevantForFP(fnr, fom);
        }
        if (BehandlingTema.gjelderSvangerskapspenger(tema)) {
            return erITSakRelevantForSVP(fnr, fom);
        }
        return false;
    }

    private boolean erITSakRelevantForIM(String fnr, LocalDate fom, BehandlingTema tema) {
        if (BehandlingTema.gjelderForeldrepenger(tema)) {
            return erFpRelevantForIM(fnr, fom);
        }
        if (BehandlingTema.gjelderSvangerskapspenger(tema)) {
            return erITSakRelevantForSVP(fnr, fom);
        }
        return false;
    }

    private boolean erITSakRelevantForFP(String fnr, LocalDate fom) {
        return restSaker(fp, fnr, fom).stream()
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private boolean erITSakRelevantForSVP(String fnr, LocalDate fom) {
        return restSaker(svp, fnr, fom).stream()
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private boolean erFpRelevantForIM(String fnr, LocalDate fom) {
        return restSaker(fp, fnr, fom).stream()
                .map(InfotrygdSak::getIverksatt)
                .flatMap(Optional::stream)
                .anyMatch(fom::isBefore);
    }

    private List<InfotrygdSak> restSaker(InfotrygdTjeneste t, String fnr, LocalDate fom) {
        return t.finnSakListe(fnr, fom);
    }

    private static Predicate<? super InfotrygdSak> svpFpRelevantTidFilter(LocalDate fom) {
        // Intensjon med FALSE for å unngå treff pga praksis i enheter med
        // informasjonssaker
        return sak -> sak.getIverksatt().map(fom::isBefore).orElse(false)
                || (sak.getRegistrert() != null && fom.isBefore(sak.getRegistrert()));
    }

}