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
    void skal_legge_aktør_id_og_ikke_fnr_på_request() {
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
