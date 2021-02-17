package no.nav.foreldrepenger.fordel.web.app.rest;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.UDEFINERT;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype.INNGÅENDE;
import static no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus.MOTTATT;
import static no.nav.foreldrepenger.fordel.kodeverdi.Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class VurderJournalpostYtelseRestTjenesteTest {

    private static final String ARKIV_ID = "12345";
    private static final String AKTØR_ID = "9999999999999";

    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private PersonInformasjon aktørConsumer;
    private VurderJournalpostYtelseRestTjeneste restTjeneste;

    @BeforeEach
    void setUp() throws Exception {
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID));
        restTjeneste = new VurderJournalpostYtelseRestTjeneste(arkivTjeneste, aktørConsumer);
    }

    @Test
    void skalVurdereInntektsmeldingForeldrepender() {
        var journalpost = byggJournalpost(INNTEKTSMELDING, "testdata/inntektsmelding-foreldrepenger.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);
        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);
        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(FORELDREPENGER.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isTrue();
        assertThat(respons.getErFørstegangssøknad()).isFalse();
    }

    @Test
    void skalVurdereInntektsmeldingSvangerskapspenger() {
        var journalpost = byggJournalpost(INNTEKTSMELDING, "testdata/inntektsmelding-svangerskapspenger.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);
        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);
        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(SVANGERSKAPSPENGER.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isTrue();
        assertThat(respons.getErFørstegangssøknad()).isFalse();
    }

    @Test
    void skalVurdereSøknadForeldrepenger() {
        var journalpost = byggJournalpost(SØKNAD_FORELDREPENGER_FØDSEL, "testdata/selvb-soeknad-forp.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);
        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);
        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(FORELDREPENGER.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isFalse();
        assertThat(respons.getErFørstegangssøknad()).isTrue();
    }

    @Test
    void skalVurdereSøknadEngangsstønad() {
        var journalpost = byggJournalpost(SØKNAD_ENGANGSSTØNAD_FØDSEL, null);
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);
        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);
        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(ENGANGSSTØNAD.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isFalse();
        assertThat(respons.getErFørstegangssøknad()).isTrue();
    }

    @Test
    void skalGiUdefinertForVedlegg() {
        var journalpost = byggJournalpost(DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, null);
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(journalpost);
        var respons = restTjeneste.utledBehandlingstemaFra(ARKIV_ID);
        assertThat(respons.getBehandlingstemaOffisiellKode()).isEqualTo(UDEFINERT.getOffisiellKode());
        assertThat(respons.getErInntektsmelding()).isFalse();
        assertThat(respons.getErFørstegangssøknad()).isFalse();
    }

    private ArkivJournalpost byggJournalpost(DokumentTypeId dokumentTypeId, String filnavn) {
        var jp = ArkivJournalpost.getBuilder()
                .medJournalpostId(ARKIV_ID)
                .medHovedtype(dokumentTypeId)
                .medJournalposttype(INNGÅENDE)
                .medTilstand(MOTTATT)
                .medTema(FORELDRE_OG_SVANGERSKAPSPENGER);
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
