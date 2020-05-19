package no.nav.foreldrepenger.mottak.journal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import no.nav.dok.tjenester.mottainngaaendeforsendelse.ForsendelseInformasjon;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRequest;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.vedtak.felles.integrasjon.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRestKlient;

public class JournalTjenesteImplTest {

    private static final String JOURNALPOST_ID = "1234";
    private static final UUID FORSENDELSE_ID = UUID.randomUUID();

    private JournalTjeneste journalTjeneste; // tjenesten som testes;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MottaInngaaendeForsendelseRestKlient inngaaendeForsendelseKlient = mock(
            MottaInngaaendeForsendelseRestKlient.class);

    @Before
    public void setUp() throws Exception {
        journalTjeneste = new JournalTjeneste(inngaaendeForsendelseKlient);
    }

    @Test
    public void test_journalførDokumentForsendelse_skal_returnere_respons_med_tilstand_endelig_journalført() {
        LocalDateTime ldt = LocalDateTime.now();

        List<Dokument> vedleggListe = new ArrayList<>();
        vedleggListe.add(lagDokument(DokumentTypeId.INNTEKTSMELDING, ArkivFilType.PDFA, false));
        vedleggListe.add(lagDokument(DokumentTypeId.INNTEKTSMELDING, ArkivFilType.PDFA, false));
        vedleggListe.add(lagDokument(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, ArkivFilType.PDFA, false));

        List<Dokument> hovedDokument = new ArrayList<>();
        hovedDokument.add(lagDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, ArkivFilType.XML, true));
        hovedDokument.add(lagDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, ArkivFilType.PDFA, true));

        DokumentforsendelseRequest request = DokumentforsendelseRequest.builder()
                .medBruker("1234")
                .medForsendelseId("1234")
                .medSaksnummer("1234")
                .medAvsender("5678")
                .medForsøkEndeligJF(true)
                .medForsendelseMottatt(ldt)
                .medHoveddokument(hovedDokument)
                .medVedlegg(vedleggListe)
                .medRetrySuffix("ABC")
                .build();

        when(inngaaendeForsendelseKlient.journalførForsendelse(any(MottaInngaaendeForsendelseRequest.class)))
                .thenReturn(lagRespons(MottaInngaaendeForsendelseResponse.JournalTilstand.ENDELIG_JOURNALFOERT));
        DokumentforsendelseResponse response = journalTjeneste.journalførDokumentforsendelse(request);

        ArgumentCaptor<MottaInngaaendeForsendelseRequest> captor = ArgumentCaptor
                .forClass(MottaInngaaendeForsendelseRequest.class);
        verify(inngaaendeForsendelseKlient).journalførForsendelse(captor.capture());

        MottaInngaaendeForsendelseRequest captured = captor.getValue();
        assertThat(captured.getForsokEndeligJF()).isTrue();
        assertThat(captured.getForsendelseInformasjon()).isNotNull();
        assertThat(captured.getForsendelseInformasjon().getKanalReferanseId()).isEqualTo("1234-ABC");

        ForsendelseInformasjon fi = captured.getForsendelseInformasjon();
        assertThat(fi.getArkivSak().getArkivSakId()).contains(request.getSaksnummer());

        assertThat(response.getJournalpostId()).isEqualToIgnoringCase(JOURNALPOST_ID);
        assertThat(response.getJournalTilstand()).isEqualByComparingTo(JournalTilstand.ENDELIG_JOURNALFØRT);
        assertThat(response.getDokumentIdListe()).hasSize(vedleggListe.size());
    }

    private Dokument lagDokument(DokumentTypeId dokTypeId, ArkivFilType arkivFilType, Boolean hoveddok) {
        return DokumentforsendelseTestUtil.lagDokument(FORSENDELSE_ID, dokTypeId, arkivFilType, hoveddok);
    }

    private MottaInngaaendeForsendelseResponse lagRespons(
            MottaInngaaendeForsendelseResponse.JournalTilstand journalTilstand) {
        List<String> dokId = new ArrayList<>();
        dokId.add("2345");
        dokId.add("2564");
        dokId.add("2567");
        MottaInngaaendeForsendelseResponse response = new MottaInngaaendeForsendelseResponse();
        response.setJournalpostId(JOURNALPOST_ID);
        response.setJournalTilstand(journalTilstand);
        response.setDokumentIdListe(dokId);
        return response;
    }
}