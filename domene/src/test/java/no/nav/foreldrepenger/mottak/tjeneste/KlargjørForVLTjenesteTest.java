package no.nav.foreldrepenger.mottak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.mottak.klient.DokumentmottakRestKlient;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.klient.TilbakekrevingRestKlient;
import no.nav.vedtak.util.FPDateUtil;

public class KlargjørForVLTjenesteTest {

    private KlargjørForVLTjeneste klargjørForVLTjeneste; // objektet vi tester
    private FagsakRestKlient mockFagsakRestKlient;
    private DokumentmottakRestKlient mockDokumentRestTjeneste;

    private static final String ARKIV_ID = "123";
    private static final String SAK_ID = "456";
    private static final String ENHET_ID = "en003";

    @Before
    public void setup() {
        mockFagsakRestKlient = mock(FagsakRestKlient.class);
        mockDokumentRestTjeneste = mock(DokumentmottakRestKlient.class);
        var mockTilbakeRestTjeneste = mock(TilbakekrevingRestKlient.class);
        klargjørForVLTjeneste = new KlargjørForVLTjeneste(mockDokumentRestTjeneste, mockFagsakRestKlient, mockTilbakeRestTjeneste);
    }


    @Test
    public void skal_knytte_og_sende() {

        klargjørForVLTjeneste.klargjørForVL(null, SAK_ID, ARKIV_ID, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FPDateUtil.iDag().atStartOfDay(), BehandlingTema.FORELDREPENGER, null, DokumentKategori.SØKNAD, ENHET_ID);

        ArgumentCaptor<JournalpostKnyttningDto> captorJ = ArgumentCaptor.forClass(JournalpostKnyttningDto.class);
        verify(mockFagsakRestKlient).knyttSakOgJournalpost(captorJ.capture());
        JournalpostKnyttningDto journalPost = captorJ.getValue();
        assertThat(journalPost.getJournalpostId()).isEqualTo(ARKIV_ID);

        ArgumentCaptor<JournalpostMottakDto> captorD = ArgumentCaptor.forClass(JournalpostMottakDto.class);
        verify(mockDokumentRestTjeneste).send(captorD.capture());
        JournalpostMottakDto mottak = captorD.getValue();
        assertThat(mottak.getSaksnummer()).isEqualTo(SAK_ID);
    }

}
