package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.symmetricDifference;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.fordel.kodeverk.Fagsystem.INFOTRYGD;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
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
import no.nav.foreldrepenger.mottak.infotrygd.rest.fp.FP;
import no.nav.foreldrepenger.mottak.infotrygd.rest.svp.SVP;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class RelevantSakSjekker {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantSakSjekker.class);
    private static final Period GSAK_EKSTRA_MND = Period.ofMonths(2);
    private static String TOGGLE_REST_STYRER = "fpfordel.rest.svp.presedens";

    private InfotrygdTjeneste infotrygd;
    private InfotrygdTjeneste svp;
    private InfotrygdTjeneste fp;

    private Unleash unleash;
    private GsakSakTjeneste gsak;

    RelevantSakSjekker() {
        //
    }

    @Inject
    public RelevantSakSjekker(
            @SVP InfotrygdTjeneste svp,
            @FP InfotrygdTjeneste fp,
            InfotrygdTjeneste infotrygd,
            GsakSakTjeneste gsak,
            Unleash unleash) {
        this.svp = svp;
        this.fp = fp;
        this.infotrygd = infotrygd;
        this.unleash = unleash;
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

        if (tema.gjelderForeldrepenger()) {
            return erITSakRelevantForFP(fnr, fom);
        }
        if (tema.gjelderSvangerskapspenger()) {
            return erITSakRelevantForSVP(fnr, fom);
        }
        return false;
    }

    private boolean erITSakRelevantForIM(String fnr, LocalDate fom, BehandlingTema tema) {
        if (erMann(fnr)) {
            return true;
        }
        if (tema.gjelderForeldrepenger()) {
            return erFpRelevantForIM(fnr, fom);
        }
        if (tema.gjelderSvangerskapspenger()) {
            return erITSakRelevantForSVP(fnr, fom);
        }
        return false;
    }

    private boolean erITSakRelevantForFP(String fnr, LocalDate fom) {
        return sammenlign(fp.finnSakListe(fnr, fom), finnSakListe(fnr, fom, InfotrygdSak::gjelderForeldrepenger))
                .stream()
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private boolean erITSakRelevantForSVP(String fnr, LocalDate fom) {
        return sammenlign(svp.finnSakListe(fnr, fom), finnSakListe(fnr, fom, InfotrygdSak::gjelderSvangerskapspenger))
                .stream()
                .anyMatch(svpFpRelevantTidFilter(fom));
    }

    private boolean erFpRelevantForIM(String fnr, LocalDate fom) {
        return sammenlign(fp.finnSakListe(fnr, fom), finnSakListe(fnr, fom, InfotrygdSak::gjelderForeldrepenger))
                .stream()
                .map(InfotrygdSak::getIverksatt)
                .flatMap(Optional::stream)
                .anyMatch(fom::isBefore);
    }

    private List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom, Predicate<? super InfotrygdSak> saktype) {
        try {
            return infotrygd.finnSakListe(fnr, fom)
                    .stream()
                    .filter(saktype)
                    .collect(toList());
        } catch (InfotrygdPersonIkkeFunnetException e) {
            Feilene.FACTORY.feilFraInfotrygdSakFordeling(e).log(LOG);
        }
        return emptyList();
    }

    private static Predicate<? super InfotrygdSak> svpFpRelevantTidFilter(LocalDate fom) {
        // Intensjon med FALSE for å unngå treff pga praksis i enheter med
        // informasjonssaker
        return sak -> sak.getIverksatt().map(fom::isBefore).orElse(false)
                || (sak.getRegistrert() != null && fom.isBefore(sak.getRegistrert()));
    }

    private static boolean erMann(String fnr) {
        return Character.getNumericValue(fnr.charAt(8)) % 2 != 0;
    }

    private List<InfotrygdSak> sammenlign(List<InfotrygdSak> restSaker, List<InfotrygdSak> wsSaker) {
        if (!restSaker.containsAll(wsSaker)) {
            warn(restSaker, wsSaker);
        } else {
            LOG.info("{} sak(er) med identisk respons fra WS og REST", restSaker.size());
        }
        return unleash.isEnabled(TOGGLE_REST_STYRER) ? restSaker : wsSaker;
    }

    private static void warn(List<InfotrygdSak> restSaker, List<InfotrygdSak> wsSaker) {
        HashSet<InfotrygdSak> rest = new HashSet<>(restSaker);
        HashSet<InfotrygdSak> ws = new HashSet<>(wsSaker);
        LOG.warn("Forskjellig respons fra WS og REST. Fikk {} fra REST og {} fra WS", restSaker, wsSaker);
        LOG.warn("Elementer som ikke er tilstede i begge responser er {}", symmetricDifference(rest, ws));
        LOG.warn("Elementer fra REST men ikke fra WS {}", difference(rest, ws));
        LOG.warn("Elementer fra WS men ikke fra REST {}", difference(ws, rest));
    }

    private interface Feilene extends DeklarerteFeil {
        Feilene FACTORY = FeilFactory.create(Feilene.class);

        @TekniskFeil(feilkode = "FP-074122", feilmelding = "PersonIkkeFunnet fra infotrygdSak", logLevel = LogLevel.WARN)
        Feil feilFraInfotrygdSakFordeling(Exception cause);
    }

}