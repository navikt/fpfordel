package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.FORELDREPENGER_TEMA;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseBehandlingstema;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;

public final class InfotrygdRestResponseMapper {

    private static final String FA = FORELDREPENGER_TEMA.getKode();

    private final String behandlingstema;

    public InfotrygdRestResponseMapper(RelatertYtelseBehandlingstema behandlingstema) {
        this.behandlingstema = behandlingstema.getKode();
    }

    public String getBehandlingstema() {
        return behandlingstema;
    }

    public List<InfotrygdSak> map(Saker saker) {
        return infotrygdSakerFra(
                infotrygdSakerFraLøpendeSaker(saker.getLøpendeSaker()),
                infotrygdSakerFraAvsluttedeSaker(saker.getAvsluttedeSaker()),
                infotrygdSakerFraSaker(saker.getSaker()));
    }

    private Stream<InfotrygdSak> infotrygdSakerFraSaker(List<Sak> saker) {
        return stream(saker)
                .map(this::tilSVPInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraAvsluttedeSaker(AvsluttedeSaker avsluttedeSaker) {
        return stream(avsluttedeSaker.getSaker())
                .map(this::tilSVPInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraLøpendeSaker(List<LøpendeSak> løpendeSaker) {
        return stream(løpendeSaker)
                .map(this::tilSVPInfotrygdSak);
    }

    private InfotrygdSak tilSVPInfotrygdSak(AvsluttetSak sak) {
        return new InfotrygdSak(null, FA, behandlingstema, sak.getIverksatt(), null);
    }

    private InfotrygdSak tilSVPInfotrygdSak(Sak sak) {
        return new InfotrygdSak(sak.getSaksnummer(), FA, behandlingstema, sak.getIverksatt(), null);
    }

    private InfotrygdSak tilSVPInfotrygdSak(LøpendeSak sak) {
        return new InfotrygdSak(null, FA, behandlingstema, sak.getIverksatt(), null);
    }

    private static <T> Stream<T> stream(List<T> list) {
        return Optional.ofNullable(list)
                .orElseGet(Collections::emptyList)
                .stream();
    }

    private static <T> List<T> infotrygdSakerFra(Stream<T> s1, Stream<T> s2, Stream<T> s3) {
        return concat(s1, concat(s2, s3))
                .collect(toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[behandlingstema=" + behandlingstema + "]";
    }
}
