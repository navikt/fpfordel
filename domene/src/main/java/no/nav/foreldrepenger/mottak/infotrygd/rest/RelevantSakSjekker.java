package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.fordel.kodeverk.Fagsystem.INFOTRYGD;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdPersonIkkeFunnetException;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class RelevantSakSjekker {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelevantSakSjekker.class);
    private static final Period GSAK_EKSTRA_MND = Period.ofMonths(2);
    private static String TOGGLE_KALLE_REST = "fpfordel.rest.svp.kalltjeneste";
    private static String TOGGLE_REST_STYRER = "fpfordel.rest.svp.presedens";

    private InfotrygdTjeneste infotrygd;
    private InfotrygdTjeneste svp;
    private Unleash unleash;
    private GsakSakTjeneste gsak;

    RelevantSakSjekker() {
        //
    }

    @Inject
    public RelevantSakSjekker(
            @SVP InfotrygdTjeneste svp,
            InfotrygdTjeneste infotrygd,
            GsakSakTjeneste gsak,
            Unleash unleash) {
        this.svp = svp;
        this.infotrygd = infotrygd;
        this.unleash = unleash;
        this.gsak = gsak;
    }

    public boolean skalMidlertidigJournalføre(LocalDate fom, String fnr, Tema tema, BehandlingTema behandlingTema) {
        return harGsakSaker(fom, fnr, tema) && erITSakRelevant(fom, fnr, behandlingTema);
    }

    public boolean skalMidlertidigJournalføreIM(LocalDate fom, String fnr, Tema tema, BehandlingTema behandlingTema) {
        return harGsakSaker(fom.minus(GSAK_EKSTRA_MND), fnr, tema) && erITSakRelevantForIM(fnr, behandlingTema, fom);
    }

    private boolean harGsakSaker(LocalDate fom, String fnr, Tema tema) {
        return gsak.finnSaker(fnr)
                .stream()
                .filter(sak -> sak.getFagsystem().equals(INFOTRYGD))
                .filter(sak -> sak.getTema().equals(tema))
                .anyMatch(sak -> sak.getSistEndret()
                        .map(fom::isBefore)
                        .orElse(true));
    }

    private boolean erITSakRelevant(LocalDate fom, String fnr, BehandlingTema tema) {

        if (tema.gjelderForeldrepenger()) {
            return erITSakRelevantForFP(fom, fnr);
        }
        if (tema.gjelderSvangerskapspenger()) {
            return erITSakRelevantForSVP(fom, fnr);
        }
        return false;
    }

    private boolean erITSakRelevantForIM(String fnr, BehandlingTema tema, LocalDate fom) {
        if (erMann(fnr)) {
            return true;
        }
        if (tema.gjelderForeldrepenger()) {
            return erFpRelevantForIM(fom, fnr);
        }
        if (tema.gjelderSvangerskapspenger()) {
            return erSvpRelevantIM(fom, fnr);
        }
        return false;
    }

    private boolean erITSakRelevantForFP(LocalDate fom, String fnr) {
        return finnSakListe(fom, fnr, InfotrygdSak::gjelderForeldrepenger)
                .stream()
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private boolean erITSakRelevantForSVP(LocalDate fom, String fnr) {
        List<InfotrygdSak> restSaker = getInfotrygdSakRest(fom, fnr);
        var wsSaker = finnSakListe(fom, fnr, InfotrygdSak::gjelderSvangerskapspenger);
        return sammenlignOgSjekk(restSaker, wsSaker)
                .stream()
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private List<InfotrygdSak> getInfotrygdSakRest(LocalDate fom, String fnr) {
        if (unleash.isEnabled(TOGGLE_KALLE_REST)) {
            return svp.finnSakListe(fnr, fom);
        }
        return Collections.emptyList();
    }

    private boolean erFpRelevantForIM(LocalDate fom, String fnr) {
        return finnSakListe(fom, fnr, InfotrygdSak::gjelderForeldrepenger)
                .stream()
                .map(InfotrygdSak::getIverksatt)
                .flatMap(Optional::stream)
                .anyMatch(fom::isBefore);
    }

    private boolean erSvpRelevantIM(LocalDate fom, String fnr) {
        List<InfotrygdSak> restSaker = getInfotrygdSakRest(fom, fnr);
        var wsSaker = finnSakListe(fom, fnr, InfotrygdSak::gjelderSvangerskapspenger);

        return sammenlignOgSjekk(restSaker, wsSaker)
                .stream()
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private List<InfotrygdSak> finnSakListe(LocalDate fom, String fnr, Predicate<? super InfotrygdSak> gjelder) {
        try {
            return infotrygd.finnSakListe(fnr, fom)
                    .stream()
                    .filter(gjelder)
                    .collect(toList());
        } catch (InfotrygdPersonIkkeFunnetException e) {
            Feilene.FACTORY.feilFraInfotrygdSakFordeling(e).log(LOGGER);
        }
        return emptyList();
    }

    private static Predicate<? super InfotrygdSak> svpFpRelevantTidFilter(LocalDate fom) {
        // Intensjon med FALSE for å unngå treff pga praksis i enheter med
        // informasjonssaker
        return sak -> fom.isBefore(sak.getRegistrert())
                || sak.getIverksatt().map(fom::isBefore).orElse(false);
    }

    private static boolean erMann(String fnr) {
        return Character.getNumericValue(fnr.charAt(8)) % 2 != 0;
    }

    private List<InfotrygdSak> sammenlignOgSjekk(List<InfotrygdSak> restSaker, List<InfotrygdSak> wsSaker) {
        if (!restSaker.containsAll(wsSaker)) {
            LOGGER.warn("Forskjellig respons fra WS og REST. Fikk {} fra REST og {} fra WS", restSaker, wsSaker);
        } else {
            LOGGER.info("Identisk respons fra WS og REST, {} sak(er)", restSaker.size());
        }
        if (unleash.isEnabled(TOGGLE_REST_STYRER)) {
            return restSaker;
        }
        return wsSaker;
    }

    private interface Feilene extends DeklarerteFeil {
        Feilene FACTORY = FeilFactory.create(Feilene.class);

        @TekniskFeil(feilkode = "FP-074122", feilmelding = "PersonIkkeFunnet fra infotrygdSak", logLevel = LogLevel.WARN)
        Feil feilFraInfotrygdSakFordeling(Exception cause);
    }

}