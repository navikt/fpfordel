package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;
import no.nav.foreldrepenger.mottak.klient.FamilieHendelseTypeDto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class ManuellJournalføringMapperTest {

    @Test
    void mapHendelseTypeJF() {
        assertThat(ManuellJournalføringMapper.mapHendelseTypeJF(FamilieHendelseTypeDto.FØDSEL)).isEqualTo(FamilihendelseTypeJFDto.FØDSEL);
    }

    @Test
    void mapYtelseTypeFraDto() {
        assertThat(ManuellJournalføringMapper.mapYtelseTypeFraDto(YtelseTypeDto.ENGANGSTØNAD)).isEqualTo(FagsakYtelseTypeDto.ENGANGSTØNAD);
        assertThat(ManuellJournalføringMapper.mapYtelseTypeFraDto(YtelseTypeDto.FORELDREPENGER)).isEqualTo(FagsakYtelseTypeDto.FORELDREPENGER);
        assertThat(ManuellJournalføringMapper.mapYtelseTypeFraDto(YtelseTypeDto.SVANGERSKAPSPENGER)).isEqualTo(FagsakYtelseTypeDto.SVANGERSKAPSPENGER);
    }

    @Test
    void mapYtelseTypeTilDto() {
        assertThat(ManuellJournalføringMapper.mapYtelseTypeTilDto(FagsakYtelseTypeDto.ENGANGSTØNAD)).isEqualTo(YtelseTypeDto.ENGANGSTØNAD);
        assertThat(ManuellJournalføringMapper.mapYtelseTypeTilDto(FagsakYtelseTypeDto.FORELDREPENGER)).isEqualTo(YtelseTypeDto.FORELDREPENGER);
        assertThat(ManuellJournalføringMapper.mapYtelseTypeTilDto(FagsakYtelseTypeDto.SVANGERSKAPSPENGER)).isEqualTo(YtelseTypeDto.SVANGERSKAPSPENGER);
    }

    @Test
    void mapPrioritet() {
        assertThat(ManuellJournalføringMapper.mapPrioritet(Prioritet.NORM)).isEqualTo(ManuellJournalføringRestTjeneste.OppgavePrioritet.NORM);
    }

    @Test
    void mapTilYtelseType() {
        assertThat(ManuellJournalføringMapper.mapTilYtelseType("ab0326")).isEqualTo(YtelseTypeDto.FORELDREPENGER);
    }
@Test
    void serialiseringFamilieHendelseTypeDtoTest() {
        var familiehendelseType = "FODSL";
        var dto = FamilihendelseTypeJFDto.FØDSEL;

        //serialisering
        var json = DefaultJsonMapper.toJson(dto);
        assertThat(json).contains(familiehendelseType);

        //deserialisering
        var result = DefaultJsonMapper.fromJson(json, FamilihendelseTypeJFDto.class);
        assertEquals(dto, result);
    }
}
