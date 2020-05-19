package no.nav.foreldrepenger.mottak.tjeneste;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ANNET;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;

public class TilJournalføringTjenesteTest {

    private TilJournalføringTjeneste tilJournalføringTjeneste; // objektet vi tester
    private JournalTjeneste mockJournalTjeneste;
    private DokumentRepository mockDokumentRepository;

    private static final String SAK_ID = "456";
    public static final String AVSENDER_ID = "3000";

    @Before
    public void setup() {
        mockJournalTjeneste = mock(JournalTjeneste.class);
        mockDokumentRepository = mock(DokumentRepository.class);
        tilJournalføringTjeneste = new TilJournalføringTjeneste(mockJournalTjeneste, mockDokumentRepository);
    }

    @Test
    public void skal_sende_hoveddokument_med_xml_og_pdf_versjon_med_ett_vedlegg() {
        UUID forsendelseId = UUID.randomUUID();
        JournalTilstand journalTilstand = JournalTilstand.ENDELIG_JOURNALFØRT;
        DokumentforsendelseResponse dokumentforsendelseRespons = DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(journalTilstand, 2);
        when(mockJournalTjeneste.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class)))
                .thenReturn(dokumentforsendelseRespons);

        DokumentMetadata metadata = DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAK_ID);
        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, SØKNAD_FORELDREPENGER_FØDSEL);
        dokumenter.add(DokumentforsendelseTestUtil.lagDokumentBeskrivelse(forsendelseId, ANNET, ArkivFilType.PDFA, false, "Farskap"));

        when(mockDokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(metadata);
        when(mockDokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);
        ArgumentCaptor<DokumentforsendelseRequest> captor = ArgumentCaptor.forClass(DokumentforsendelseRequest.class);

        tilJournalføringTjeneste.journalførDokumentforsendelse(forsendelseId, Optional.of(SAK_ID), Optional.of(AVSENDER_ID), true, Optional.empty());

        verify(mockJournalTjeneste).journalførDokumentforsendelse(captor.capture());
        DokumentforsendelseRequest captured = captor.getValue();
        assertThat(captured.getForsendelseId()).isEqualTo(forsendelseId.toString());
        assertThat(captured.getHoveddokument()).hasSize(2);
        assertThat(captured.getVedlegg()).hasSize(1);
        assertThat(captured.getTittel()).isNullOrEmpty();
    }

    @Test
    public void skal_returnere_respons_med_journalTilstand_endelig() {
        UUID forsendelseId = UUID.randomUUID();
        JournalTilstand journalTilstand = JournalTilstand.ENDELIG_JOURNALFØRT;
        DokumentforsendelseResponse dokumentforsendelseRespons = DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(journalTilstand, 2);
        when(mockJournalTjeneste.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class)))
                .thenReturn(dokumentforsendelseRespons);

        DokumentMetadata metadata = DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAK_ID);
        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, SØKNAD_FORELDREPENGER_FØDSEL);

        when(mockDokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(metadata);
        when(mockDokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);
        DokumentforsendelseResponse response = tilJournalføringTjeneste.journalførDokumentforsendelse(forsendelseId, Optional.of(SAK_ID), Optional.of(AVSENDER_ID), true, Optional.empty());

        assertThat(response).isNotNull();
        assertThat(response.getDokumentIdListe()).hasSize(2);
        assertThat(response.getJournalpostId()).isNotNull();
        assertThat(response.getJournalpostId()).isEqualTo(DokumentforsendelseTestUtil.JOURNALPOST_ID);
        assertThat(response.getJournalTilstand()).isEqualByComparingTo(JournalTilstand.ENDELIG_JOURNALFØRT);
    }

}
