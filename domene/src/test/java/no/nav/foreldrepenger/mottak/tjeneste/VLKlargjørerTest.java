package no.nav.foreldrepenger.mottak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.mottak.klient.DokumentmottakKlient;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.TilbakekrevingKlient;

@ExtendWith(MockitoExtension.class)
class VLKlargjørerTest {

    private static final String ARKIV_ID = "123";
    private static final String SAK_ID = "456";
    private static final String ENHET_ID = "en003";
    private VLKlargjører klargjørForVLTjeneste;
    @Mock
    private Fagsak mockFagsakRestKlient;
    @Mock
    private DokumentmottakKlient mockDokumentRestTjeneste;
    @Mock
    private TilbakekrevingKlient mockTilbakeRestTjeneste;

    @BeforeEach
    void setup() {
        klargjørForVLTjeneste = new VLKlargjører(mockDokumentRestTjeneste, mockFagsakRestKlient, mockTilbakeRestTjeneste);
    }

    @Test
    void skal_knytte_og_sende() {

        klargjørForVLTjeneste.klargjør(null, SAK_ID, ARKIV_ID, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, LocalDate.now().atStartOfDay(),
            BehandlingTema.FORELDREPENGER, null, DokumentKategori.SØKNAD, ENHET_ID, null);

        var captorJ = ArgumentCaptor.forClass(JournalpostKnyttningDto.class);
        verify(mockFagsakRestKlient).knyttSakOgJournalpost(captorJ.capture());
        assertThat(captorJ.getValue().journalpostIdDto().journalpostId()).isEqualTo(ARKIV_ID);

        var captorD = ArgumentCaptor.forClass(JournalpostMottakDto.class);
        verify(mockDokumentRestTjeneste).send(captorD.capture());
        var mottak = captorD.getValue();
        assertThat(mottak.getSaksnummer()).isEqualTo(SAK_ID);
    }
}
