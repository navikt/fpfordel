package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseBehandlingstema.SVANGERSKAPSPENGER_BEHANDLINGSTEMA;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.FORELDREPENGER_TEMA;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;

final class SVPInfotrygdRestResponseMapper {

    private static final String FA = FORELDREPENGER_TEMA.getKode();
    private static final String SVP = SVANGERSKAPSPENGER_BEHANDLINGSTEMA.getKode();

    private SVPInfotrygdRestResponseMapper() {

    }

    static List<InfotrygdSak> svpInfotrygdSaker(Saker saker) {
        return infotrygdSakerFra(
                infotrygdSakerFraLøpendeSaker(saker.getLøpendeSaker()),
                infotrygdSakerFraAvsluttedeSaker(saker.getAvsluttedeSaker()),
                infotrygdSakerFraSaker(saker.getSaker()));
    }

    private static Stream<InfotrygdSak> infotrygdSakerFraSaker(List<Sak> saker) {
        return stream(saker)
                .map(SVPInfotrygdRestResponseMapper::tilSVPInfotrygdSak);
    }

    private static Stream<InfotrygdSak> infotrygdSakerFraAvsluttedeSaker(AvsluttedeSaker avsluttedeSaker) {
        return stream(avsluttedeSaker.getSaker())
                .map(SVPInfotrygdRestResponseMapper::tilSVPInfotrygdSak);
    }

    private static Stream<InfotrygdSak> infotrygdSakerFraLøpendeSaker(List<LøpendeSak> åpneSaker) {
        return stream(åpneSaker)
                .map(SVPInfotrygdRestResponseMapper::tilSVPInfotrygdSak);
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
