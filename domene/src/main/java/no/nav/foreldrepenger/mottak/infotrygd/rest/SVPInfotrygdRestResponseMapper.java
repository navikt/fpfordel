package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseBehandlingstema.SVANGERSKAPSPENGER_BEHANDLINGSTEMA;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.FORELDREPENGER_TEMA;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;

final class SVPInfotrygdRestResponseMapper {

    private static final String FA = FORELDREPENGER_TEMA.getKode();
    private static final String SVP = SVANGERSKAPSPENGER_BEHANDLINGSTEMA.getKode();

    private SVPInfotrygdRestResponseMapper() {

    }

    static List<InfotrygdSak> svpInfotrygdSaker(Saker saker, LocalDate dato) {
        return infotrygdSakerFra(
                infotrygdSakerFraLøpendeSaker(saker.getLøpendeSaker(), dato),
                infotrygdSakerFraAvsluttedeSaker(saker.getAvsluttedeSaker(), dato),
                infotrygdSakerFraSaker(saker.getSaker(), dato));
    }

    private static Stream<InfotrygdSak> infotrygdSakerFraSaker(List<Sak> saker, LocalDate dato) {
        return stream(saker)
                .map(SVPInfotrygdRestResponseMapper::tilSVPInfotrygdSak)
                .filter(erNyereEnn(dato));
    }

    private static Stream<InfotrygdSak> infotrygdSakerFraAvsluttedeSaker(AvsluttedeSaker avsluttedeSaker,
            LocalDate fom) {
        return stream(avsluttedeSaker.getSaker())
                .map(SVPInfotrygdRestResponseMapper::tilSVPInfotrygdSak)
                .filter(erNyereEnn(fom));
    }

    private static Stream<InfotrygdSak> infotrygdSakerFraLøpendeSaker(List<LøpendeSak> åpneSaker, LocalDate dato) {
        return stream(åpneSaker)
                .map(SVPInfotrygdRestResponseMapper::tilSVPInfotrygdSak)
                .filter(erNyereEnn(dato));
    }

    private static Predicate<? super InfotrygdSak> erNyereEnn(LocalDate dato) {
        return f -> f.getIverksatt().isPresent() && f.getIverksatt().get().isAfter(dato);
    }

    private static InfotrygdSak tilSVPInfotrygdSak(AvsluttetSak sak) {
        return new InfotrygdSak(null, FA, SVP, sak.getIverksatt(), null);
    }

    private static InfotrygdSak tilSVPInfotrygdSak(Sak sak) {
        return new InfotrygdSak(sak.getSaksnummer(), FA, SVP, sak.getIverksatt(), sak.getVedtatt());
    }

    private static InfotrygdSak tilSVPInfotrygdSak(LøpendeSak sak) {
        return new InfotrygdSak(null, FA, SVP, sak.getIverksatt(), null);
    }

    private static <T> Stream<T> stream(List<T> list) {
        return Optional.ofNullable(list)
                .orElseGet(Collections::emptyList)
                .stream();
    }

    private static <T> List<T> infotrygdSakerFra(Stream<T> s1, Stream<T> s2, Stream<T> s3) {
        return concat(s1, concat(s2, s3))
                .sorted()
                .collect(toList());
    }
}
