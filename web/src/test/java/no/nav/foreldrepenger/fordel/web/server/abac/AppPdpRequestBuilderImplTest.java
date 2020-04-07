package no.nav.foreldrepenger.fordel.web.server.abac;

import static no.nav.foreldrepenger.fordel.web.server.abac.AbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.foreldrepenger.fordel.web.server.abac.AbacAttributter.RESOURCE_FELLES_PERSON_FNR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.foreldrepenger.pip.PipRepository;
import no.nav.foreldrepenger.sikkerhet.abac.AppAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;

public class AppPdpRequestBuilderImplTest {
    private static final String DUMMY_ID_TOKEN = "dummyheader.dymmypayload.dummysignaturee";
    private static final String AKTØR = "AktørID_1";
    private static final UUID DOKUMENTFORSENDELSE = UUID.randomUUID();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PipRepository pipRepository = Mockito.mock(PipRepository.class);
    // private AktørConsumerMedCache aktørConsumer =
    // Mockito.mock(AktørConsumerMedCache.class);

    private AppPdpRequestBuilderImpl requestBuilder = new AppPdpRequestBuilderImpl(pipRepository);

    @Test
    public void skal_legge_aktør_id_og_ikke_fnr_på_request() throws Exception {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.AKTØR_ID, AKTØR));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(AKTØR);
        assertThat(request.getAntall(RESOURCE_FELLES_PERSON_FNR)).isEqualTo(0);
    }

    @Test
    public void skal_hente_aktør_id_gitt_forsendelse_id_som_input() {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(
                AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.FORSENDELSE_UUID, DOKUMENTFORSENDELSE));

        when(pipRepository.hentAktørIdForForsendelser(Collections.singleton(DOKUMENTFORSENDELSE)))
                .thenReturn(Collections.singleton(AKTØR));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).containsOnly(AKTØR);
    }

    @Test
    public void skal_hente_to_aktør_id_gitt_forsendelse_id_som_input() {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.AKTØR_ID, AKTØR + "_A"));
        attributter.leggTil(
                AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.FORSENDELSE_UUID, DOKUMENTFORSENDELSE));

        when(pipRepository.hentAktørIdForForsendelser(Collections.singleton(DOKUMENTFORSENDELSE)))
                .thenReturn(Collections.singleton(AKTØR));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getListOfString(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).contains(AKTØR);
        assertThat(request.getListOfString(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).hasSize(2);
    }

    private AbacAttributtSamling byggAbacAttributtSamling() {
        AbacAttributtSamling attributtSamling = AbacAttributtSamling.medJwtToken(DUMMY_ID_TOKEN);
        attributtSamling.setActionType(BeskyttetRessursActionAttributt.READ);
        attributtSamling.setResource(BeskyttetRessursResourceAttributt.FAGSAK.getEksternKode());
        return attributtSamling;
    }
}
