package no.nav.foreldrepenger.fordel.web.app.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;

@RunWith(MockitoJUnitRunner.class)
public class VurderJournalpostYtelseRestTjenesteTest {

    private static final String ARKIV_ID = "12345";
    private static final String AKTØR_ID = "9999999999999";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ArkivTjeneste arkivTjeneste;
    private AktørConsumerMedCache aktørConsumer;
    private VurderJournalpostYtelseRestTjeneste restTjeneste;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        aktørConsumer = mock(AktørConsumerMedCache.class);
        arkivTjeneste = mock(ArkivTjeneste.class);
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID));
        restTjeneste = new VurderJournalpostYtelseRestTjeneste(arkivTjeneste, aktørConsumer);

    }

    @Test
    public void skalVurdereInntektsmeldingForeldrepender() {
        var journalpost = byggJournalpost(DokumentTypeId.INNTEKTSMELDING, "testdata/inntektsmelding-foreldrepenger.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);

        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);

        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(BehandlingTema.FORELDREPENGER.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isTrue();
        assertThat(respons.getErFørstegangssøknad()).isFalse();
    }

    @Test
    public void skalVurdereInntektsmeldingSvangerskapspenger() {
        var journalpost = byggJournalpost(DokumentTypeId.INNTEKTSMELDING, "testdata/inntektsmelding-svangerskapspenger.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);

        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);

        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(BehandlingTema.SVANGERSKAPSPENGER.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isTrue();
        assertThat(respons.getErFørstegangssøknad()).isFalse();
    }

    @Test
    public void skalVurdereSøknadForeldrepenger() {
        var journalpost = byggJournalpost(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, "testdata/selvb-soeknad-forp.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);

        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);

        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(BehandlingTema.FORELDREPENGER.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isFalse();
        assertThat(respons.getErFørstegangssøknad()).isTrue();
    }

    @Test
    public void skalVurdereSøknadEngangsstønad() {
        var journalpost = byggJournalpost(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, null);
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);

        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);

        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(BehandlingTema.ENGANGSSTØNAD.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isFalse();
        assertThat(respons.getErFørstegangssøknad()).isTrue();
    }

    @Test
    public void skalGiUdefinertForVedlegg() {
        var journalpost = byggJournalpost(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, null);
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);

        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);

        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(BehandlingTema.UDEFINERT.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isFalse();
        assertThat(respons.getErFørstegangssøknad()).isFalse();
    }

    private ArkivJournalpost byggJournalpost(DokumentTypeId dokumentTypeId, String filnavn) {
        var jp = ArkivJournalpost.getBuilder()
                .medJournalpostId(ARKIV_ID)
                .medHovedtype(dokumentTypeId)
                .medJournalposttype(Journalposttype.INNGÅENDE)
                .medTilstand(Journalstatus.MOTTATT)
                .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        if (filnavn != null) {
            try {
                Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filnavn)).toURI());
                jp.medStrukturertPayload(Files.readString(path));
            } catch (Exception e) {
                //
            }
        }
        return jp.build();
    }
}
