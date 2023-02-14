package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class SaksbehandlerIdentDtoTest {

    @Test
    void serdes() {
        var ident = "s345233";
        var dto = new SaksbehandlerIdentDto(ident);

        //serialisering
        var json = DefaultJsonMapper.toJson(dto);
        assertThat(json).contains(ident);

        //deserialisering
        var result = DefaultJsonMapper.fromJson(json, SaksbehandlerIdentDto.class);
        assertEquals(dto, result);
        assertEquals(dto.ident(), result.ident());
    }
}
