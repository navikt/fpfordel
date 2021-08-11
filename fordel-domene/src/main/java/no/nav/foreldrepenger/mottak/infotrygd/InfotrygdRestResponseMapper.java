package no.nav.foreldrepenger.mottak.infotrygd;

import static java.util.stream.Stream.concat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker.AvsluttedeSaker;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker.AvsluttedeSaker.AvsluttetSak;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker.IkkeStartetSak;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker.LøpendeSak;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker.Sak;

final class InfotrygdRestResponseMapper {

    InfotrygdRestResponseMapper() {
    }

    public List<InfotrygdSak> map(Saker saker) {
        return infotrygdSakerFra(
                infotrygdSakerFraIkkeStartedeSaker(saker.ikkeStartet()),
                infotrygdSakerFraLøpendeSaker(saker.løpendeSaker()),
                infotrygdSakerFraAvsluttedeSaker(saker.avsluttedeSaker()),
                infotrygdSakerFraSaker(saker.saker()));
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
        return stream(avsluttedeSaker.saker())
                .map(this::tilInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraLøpendeSaker(List<LøpendeSak> løpendeSaker) {
        return stream(løpendeSaker)
                .map(this::tilInfotrygdSak);
    }

    private InfotrygdSak tilInfotrygdSak(AvsluttetSak sak) {
        return new InfotrygdSak(sak.iverksatt(), null);
    }

    private InfotrygdSak tilInfotrygdSak(IkkeStartetSak sak) {
        return new InfotrygdSak(sak.iverksatt(), sak.registrert());
    }

    private InfotrygdSak tilInfotrygdSak(Sak sak) {
        return new InfotrygdSak(sak.iverksatt(), sak.vedtatt());
    }

    private InfotrygdSak tilInfotrygdSak(LøpendeSak sak) {
        return new InfotrygdSak(sak.iverksatt(), null);
    }

    private static <T> Stream<T> stream(List<T> list) {
        return Optional.ofNullable(list)
                .orElseGet(Collections::emptyList)
                .stream();
    }

    private static <T> List<T> infotrygdSakerFra(Stream<T> s1, Stream<T> s2, Stream<T> s3,
            Stream<T> s4) {
        return concat(s1, concat(s2, concat(s3, s4)))
                .toList();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
