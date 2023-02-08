package no.nav.foreldrepenger.mapper;

import no.nav.foreldrepenger.fordel.kodeverdi.YtelseType;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;

public class YtelseTypeMapper {

    private YtelseTypeMapper() {
    }

    public static YtelseTypeDto mapTilDto(YtelseType ytelseType) {
        if (null == ytelseType) {
            return null;
        }
        return switch (ytelseType) {
            case FORELDREPENGER -> YtelseTypeDto.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseTypeDto.SVANGERSKAPSPENGER;
            case ENGANGSTØNAD -> YtelseTypeDto.ENGANGSTØNAD;
        };
    }

    public static YtelseType mapFraDto(YtelseTypeDto ytelseTypeDto) {
        if (null == ytelseTypeDto) {
            return null;
        }
        return switch (ytelseTypeDto) {
            case FORELDREPENGER -> YtelseType.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseType.SVANGERSKAPSPENGER;
            case ENGANGSTØNAD -> YtelseType.ENGANGSTØNAD;
        };
    }
}
