package no.nav.foreldrepenger.mottak.journal;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.felles.integrasjon.dokarkiv.DokArkiv;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest;

@ExtendWith(MockitoExtension.class)
class ArkivTjenesteTest {

    private static final String BRUKER_AKTØR_ID = "1234567890123";

    private ArkivTjeneste arkivTjeneste;
    @Mock
    private SafTjeneste safTjeneste;
    @Mock
    private DokArkiv dokArkivTjeneste;
    @Mock
    private PersonInformasjon personTjeneste;

    @BeforeEach
    void setup() {
        arkivTjeneste = new ArkivTjeneste(safTjeneste, dokArkivTjeneste, personTjeneste);
    }


    @Test
    void skal_oppdatere_bruker() {
        when(personTjeneste.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(BRUKER_AKTØR_ID));
        when(dokArkivTjeneste.oppdaterJournalpost(eq(DokumentArkivTestUtil.JOURNALPOST_ID), any(OppdaterJournalpostRequest.class))).thenReturn(true);

        arkivTjeneste.oppdaterJournalpostBruker(DokumentArkivTestUtil.JOURNALPOST_ID, "2343431");

        verify(dokArkivTjeneste).oppdaterJournalpost(eq(DokumentArkivTestUtil.JOURNALPOST_ID), any(OppdaterJournalpostRequest.class));
    }

    @Test
    void titler_og_typer() {
        assertThat(DokumentTypeId.fraTermNavn(SØKNAD_FORELDREPENGER_FØDSEL.getTermNavn())).isEqualTo(SØKNAD_FORELDREPENGER_FØDSEL);
        assertThat(DokumentTypeId.fraTermNavn("Ettersending til NAV 14-05.09 Søknad om foreldrepenger ved fødsel")).isEqualTo(ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL);
        assertThat(DokumentTypeId.fraTermNavn("Inntektsopplysninger for arbeidstaker som skal ha sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger")).isEqualTo(DokumentTypeId.INNTEKTSOPPLYSNINGERNY);
        assertThat(DokumentTypeId.fraTermNavn("klage")).isEqualTo(DokumentTypeId.KLAGE_DOKUMENT);

        assertThat(NAVSkjema.fraTermNavn(NAVSkjema.SKJEMA_SVANGERSKAPSPENGER_SN.getTermNavn())).isEqualTo(NAVSkjema.SKJEMA_SVANGERSKAPSPENGER_SN);
        assertThat(NAVSkjema.fraTermNavn(NAVSkjema.SKJEMA_SVANGERSKAPSPENGER_SN.getTermNavn().toLowerCase())).isEqualTo(NAVSkjema.SKJEMA_SVANGERSKAPSPENGER_SN);
        assertThat(NAVSkjema.fraTermNavn(NAVSkjema.SKJEMA_SVANGERSKAPSPENGER_SN.getTermNavn().toUpperCase())).isEqualTo(NAVSkjema.SKJEMA_SVANGERSKAPSPENGER_SN);
    }

}
