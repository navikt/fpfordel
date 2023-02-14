package no.nav.foreldrepenger.fordel.web.server.abac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.pip.PipRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;

@ExtendWith(MockitoExtension.class)
class AppPdpRequestBuilderImplTest {
    private static final String DUMMY_ID_TOKEN = "eyJraWQiOiI3Mzk2ZGIyZC1hN2MyLTQ1OGEtYjkzNC02ODNiNDgzYzUyNDIiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdF9oYXNoIjoiRzJ1Zl83OW1TTUhHSWFfNjFxTnJfUSIsInN1YiI6IjA5MDg4NDIwNjcyIiwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6XC9cL3Rva2VuZGluZ3MuZGV2LWdjcC5uYWlzLmlvIiwibm9uY2UiOiJWR1dyS1Zsa3RXZ3hCdTlMZnNnMHliMmdMUVhoOHRaZHRaVTJBdWdPZVl3IiwiY2xpZW50X2lkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBzb2tuYWQtbW90dGFrIiwiYXVkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBpbmZvIiwiYWNyIjoiTGV2ZWw0IiwibmJmIjoxNjE2Njg1NDA0LCJpZHAiOiJodHRwczpcL1wvbmF2dGVzdGIyYy5iMmNsb2dpbi5jb21cL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMlwvdjIuMFwvIiwiYXV0aF90aW1lIjoxNjE2Njg1NDAyLCJleHAiOjE2MTY2ODU3MDQsImlhdCI6MTYxNjY4NTQwNCwianRpIjoiNGMwNzBmMGUtNzI0Ny00ZTdjLWE1OWEtYzk2Yjk0NWMxZWZhIn0.OvzjuabvPHG9nlRVc_KlCUTHOdfeT9GtBkASUGIoMayWGeIBDkr4-jc9gu6uT_WQqi9IJnvPkWgP3veqYHcOHpapD1yVNaQpxlrJQ04yP6N3gvkn-DcrBRDb3II_6qSaPQ_us2PJBDPq2VD5TGrNOL6EFwr8FK3zglYr-PgjW016ULTcmx_7gdHmbiC5PEn1_OtGNxzoUhSGKoD3YtUWP0qdsXzoKyeFL5FG9uZMSrDHHiJBZQFXGL9OzBU49Zb2K-iEPqa9m91O2JZGkhebfLjCAIPLPN4J68GFyfTvtNkZO71znorjo-e1nWxz53Wkj---RDY3JlIqNqzqHTfJgQ";
    private static final String AKTØR = "AktørID_1";
    private static final UUID DOKUMENTFORSENDELSE = UUID.randomUUID();

    @Mock
    private PipRepository pipRepository;
    private AppPdpRequestBuilderImpl requestBuilder;

    @BeforeEach
    void beforeEach() {
        requestBuilder = new AppPdpRequestBuilderImpl(pipRepository);
    }

    @Test
    void skal_legge_aktør_id_og_ikke_fnr_på_request() throws Exception {
        var attributter = AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.AKTØR_ID, AKTØR);

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getAktørIdSet()).containsOnly(AKTØR);
        assertThat(request.getFødselsnumre()).isEmpty();
    }

    @Test
    void skal_hente_aktør_id_gitt_forsendelse_id_som_input() {
        var attributter = AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.FORSENDELSE_UUID, DOKUMENTFORSENDELSE);

        when(pipRepository.hentAktørIdForForsendelser(Collections.singleton(DOKUMENTFORSENDELSE))).thenReturn(Collections.singleton(AKTØR));

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getAktørIdSet()).containsOnly(AKTØR);
    }

    @Test
    void skal_hente_to_aktør_id_gitt_forsendelse_id_som_input() {
        var attributter = AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.AKTØR_ID, AKTØR + "_A");
        attributter.leggTil(AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.FORSENDELSE_UUID, DOKUMENTFORSENDELSE));

        when(pipRepository.hentAktørIdForForsendelser(Collections.singleton(DOKUMENTFORSENDELSE))).thenReturn(Collections.singleton(AKTØR));

        var request = requestBuilder.lagAppRessursData(attributter);
        assertThat(request.getAktørIdSet()).contains(AKTØR);
        assertThat(request.getAktørIdSet()).hasSize(2);
    }

}
