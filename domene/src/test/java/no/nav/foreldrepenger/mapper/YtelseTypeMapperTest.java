package no.nav.foreldrepenger.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.fordel.kodeverdi.YtelseType;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;

class YtelseTypeMapperTest {

    @Test
    void map_fra_dto() {
        assertThat(YtelseTypeMapper.mapFraDto(YtelseTypeDto.ENGANGSTØNAD)).isEqualTo(YtelseType.ENGANGSTØNAD);
        assertThat(YtelseTypeMapper.mapFraDto(YtelseTypeDto.FORELDREPENGER)).isEqualTo(YtelseType.FORELDREPENGER);
        assertThat(YtelseTypeMapper.mapFraDto(YtelseTypeDto.SVANGERSKAPSPENGER)).isEqualTo(YtelseType.SVANGERSKAPSPENGER);
    }

    @Test
    void map_til_dto() {
        assertThat(YtelseTypeMapper.mapTilDto(YtelseType.ENGANGSTØNAD)).isEqualTo(YtelseTypeDto.ENGANGSTØNAD);
        assertThat(YtelseTypeMapper.mapTilDto(YtelseType.FORELDREPENGER)).isEqualTo(YtelseTypeDto.FORELDREPENGER);
        assertThat(YtelseTypeMapper.mapTilDto(YtelseType.SVANGERSKAPSPENGER)).isEqualTo(YtelseTypeDto.SVANGERSKAPSPENGER);
    }

}
