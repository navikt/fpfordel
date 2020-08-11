package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.AvsluttedeSaker;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.AvsluttetSak;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.IkkeStartetSak;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.LøpendeSak;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Sak;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;

public final class InfotrygdRestResponseMapper {


    public InfotrygdRestResponseMapper() {
    }

    public List<InfotrygdSak> map(Saker saker) {
        return infotrygdSakerFra(
                infotrygdSakerFraIkkeStartedeSaker(saker.getIkkeStartedeSaker()),
                infotrygdSakerFraLøpendeSaker(saker.getLøpendeSaker()),
                infotrygdSakerFraAvsluttedeSaker(saker.getAvsluttedeSaker()),
                infotrygdSakerFraSaker(saker.getSaker()));
    }

    private Stream<InfotrygdSak> infotrygdSakerFraIkkeStartedeSaker(List<IkkeStartetSak> ikkeStartedeSaker) {
        return stream(ikkeStartedeSaker)
                .map(this::tilInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraSaker(List<Sak> saker) {
        return stream(saker)
                .map(this::tilInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraAvsluttedeSaker(AvsluttedeSaker avsluttedeSaker) {
        return stream(avsluttedeSaker.getSaker())
                .map(this::tilInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraLøpendeSaker(List<LøpendeSak> løpendeSaker) {
        return stream(løpendeSaker)
                .map(this::tilInfotrygdSak);
    }

    private InfotrygdSak tilInfotrygdSak(AvsluttetSak sak) {
        return new InfotrygdSak(sak.getIverksatt(), null);
    }

    private InfotrygdSak tilInfotrygdSak(IkkeStartetSak sak) {
        return new InfotrygdSak(sak.getIverksatt(), sak.getRegistrert());
    }

    private InfotrygdSak tilInfotrygdSak(Sak sak) {
        return new InfotrygdSak(sak.getIverksatt(), sak.getVedtatt());
    }

    private InfotrygdSak tilInfotrygdSak(LøpendeSak sak) {
        return new InfotrygdSak(sak.getIverksatt(), null);
    }

    private static <T> Stream<T> stream(List<T> list) {
        return Optional.ofNullable(list)
                .orElseGet(Collections::emptyList)
                .stream();
    }

    private static <T extends InfotrygdSak> List<T> infotrygdSakerFra(Stream<T> s1, Stream<T> s2, Stream<T> s3,
            Stream<T> s4) {
        return concat(s1, concat(s2, concat(s3, s4)))
                .collect(toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
