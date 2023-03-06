package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;
import no.nav.foreldrepenger.mottak.klient.FamilieHendelseTypeDto;

import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;

import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
    void tekstFraBeskrivelse() {
        var beskrivelse =
            "--- 17.01.2023 09:44 Duck, Skrue (L568956, 4860) --- printet ut og scannes i bisys --- 17.01.2023 09:37 Duck, Skrue (L568956, 4860) --- Oppgaven er flyttet fra enhet "
                + "4812 til 4860, fra saksbehandler <ingen> til L568956 --- 13.01.2023 08:00 Duck, Donald (B568956, 4812) --- Gjelder farskap --- 12.01.2023 12:30 Dusck, Dolly (R857447, 4806)"
                + " --- Overført rett enhet Oppgaven er flyttet fra enhet 4860 til 4812 Journalføring";

        assertThat(ManuellJournalføringMapper.tekstFraBeskrivelse(beskrivelse)).isEqualTo("Journalføring");
    }

    @Test
    void mapPrioritet() {
        assertThat(ManuellJournalføringMapper.mapPrioritet(Prioritet.NORM)).isEqualTo(ManuellJournalføringRestTjeneste.OppgavePrioritet.NORM);
    }

    @Test
    void journalpostHarMangler() {
        assertThat(ManuellJournalføringMapper.harJournalpostMangler("Journalføring (01.01.2022)")).isTrue();
    }

    @Test
    void journalpostHarIkkeMangler() {
        assertThat(ManuellJournalføringMapper.harJournalpostMangler("Inntektsmelding (01.01.2022)")).isFalse();
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
