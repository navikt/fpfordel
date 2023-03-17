package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.vedtak.exception.TekniskException;

@ExtendWith(MockitoExtension.class)
class FerdigstillJournalføringRestTjenesteTest {

    private static final String JOURNALPOST_ID = "123";
    private static final String ENHETID = "4567";
    private static final String SAKSNUMMER = "789";
    private static final Long OPPGAVE_ID = 123456L;
    private static final String AKTØR_ID = "9000000000009";

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    private FerdigstillJournalføringRestTjeneste behandleJournalpost;
    @Mock
    private Fagsak fagsak;
    @Mock
    FerdigstillJournalføringTjeneste journalføringTjeneste;


    private static FerdigstillJournalføringRestTjeneste.FerdigstillRequest req(String enhetid, String journalpostId, String sakId) {
        return new FerdigstillJournalføringRestTjeneste.FerdigstillRequest(journalpostId, enhetid, sakId, OPPGAVE_ID,null);
    }

    @BeforeEach
    public void setUp() {

        lenient().when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any()))
            .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, ENGANGSSTØNAD.getOffisiellKode())));

        behandleJournalpost = new FerdigstillJournalføringRestTjeneste(journalføringTjeneste);
    }

    @Test
    void skalValiderePåkrevdInput_enhetId() {
        var req = req(null, JOURNALPOST_ID, SAKSNUMMER);
        Exception ex = assertThrows(TekniskException.class, () -> behandleJournalpost.oppdaterOgFerdigstillJournalfoering(req));

        assertThat(ex.getMessage()).contains("Ugyldig input: EnhetId");
    }

    @Test
    void skalValiderePåkrevdInput_journalpostId() {
        var req = req(ENHETID, null, SAKSNUMMER);
        Exception ex = assertThrows(TekniskException.class, () -> behandleJournalpost.oppdaterOgFerdigstillJournalfoering(req));
        assertThat(ex.getMessage()).contains("Ugyldig input: JournalpostId");
    }

    @Test
    void skalValiderePåkrevdInput_opprettSakDto() {
        var req = req(ENHETID, JOURNALPOST_ID, null);
        Exception ex = assertThrows(TekniskException.class, () -> behandleJournalpost.oppdaterOgFerdigstillJournalfoering(req));
        assertThat(ex.getMessage()).contains("OpprettSakDto kan ikke være null ved opprettelse av en sak.");
    }

    @Test
    void oppdatereJounralpost_skalKasteExceptionNårIngenDokumentÅOppdatere() {
        var request = new OppdaterJournalpostMedTittelRequest(JOURNALPOST_ID, Collections.emptyList());
        Exception ex = assertThrows(TekniskException.class, () -> behandleJournalpost.oppdaterJournalpost(request));

        assertThat(ex.getMessage()).contains("FpFordel: Ingen dokumenter å oppdatere for journalpostId");
    }
}
