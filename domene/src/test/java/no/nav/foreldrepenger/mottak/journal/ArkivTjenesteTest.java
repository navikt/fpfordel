package no.nav.foreldrepenger.mottak.journal;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ANNET;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.felles.integrasjon.dokarkiv.DokArkiv;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OpprettJournalpostResponse;

@ExtendWith(MockitoExtension.class)
class ArkivTjenesteTest {

    private static final String SAK_ID = "456";
    private static final String AVSENDER_ID = "3000";

    private ArkivTjeneste arkivTjeneste;
    @Mock
    private SafTjeneste safTjeneste;
    @Mock
    private DokArkiv dokArkivTjeneste;
    @Mock
    private DokumentRepository dokumentRepository;
    @Mock
    private PersonInformasjon personTjeneste;

    @BeforeEach
    void setup() {
        when(personTjeneste.hentNavn(any())).thenReturn("For Etternavn");
        when(personTjeneste.hentPersonIdentForAktørId(any())).thenReturn(Optional.of(AVSENDER_ID));
        arkivTjeneste = new ArkivTjeneste(safTjeneste, dokArkivTjeneste, dokumentRepository, personTjeneste);
    }

    @Test
    void skal_sende_hoveddokument_med_xml_og_pdf_versjon_med_ett_vedlegg() {
        UUID forsendelseId = UUID.randomUUID();

        var metadata = DokumentArkivTestUtil.lagMetadata(forsendelseId, SAK_ID);
        var dokumenter = DokumentArkivTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, SØKNAD_FORELDREPENGER_FØDSEL);
        dokumenter.add(DokumentArkivTestUtil.lagDokumentBeskrivelse(forsendelseId, ANNET, ArkivFilType.PDFA, false, "Farskap"));

        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(metadata);
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);
        when(dokArkivTjeneste.opprettJournalpost(any(), anyBoolean())).thenReturn(
            new OpprettJournalpostResponse(DokumentArkivTestUtil.JOURNALPOST_ID, true, List.of()));
        var captor = ArgumentCaptor.forClass(OpprettJournalpostRequest.class);
        var resultat = arkivTjeneste.opprettJournalpost(forsendelseId, AVSENDER_ID, SAK_ID);
        verify(dokArkivTjeneste).opprettJournalpost(captor.capture(), eq(Boolean.TRUE));
        var captured = captor.getValue();
        assertThat(captured.eksternReferanseId()).isEqualTo(forsendelseId.toString());
        assertThat(captured.dokumenter()).hasSize(2);
        assertThat(captured.dokumenter().get(0).dokumentvarianter()).hasSize(2);
        assertThat(captured.journalfoerendeEnhet()).isEqualTo("9999");
        assertThat(captured.tittel()).isEqualTo(SØKNAD_FORELDREPENGER_FØDSEL.getTermNavn());
        assertThat(resultat.journalpostId()).isEqualTo(DokumentArkivTestUtil.JOURNALPOST_ID);
        assertThat(resultat.ferdigstilt()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void skal_returnere_respons_med_journalTilstand_endelig() {
        UUID forsendelseId = UUID.randomUUID();

        var metadata = DokumentArkivTestUtil.lagMetadata(forsendelseId, SAK_ID);
        var dokumenter = DokumentArkivTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, SØKNAD_FORELDREPENGER_FØDSEL);

        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(metadata);
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);
        when(dokArkivTjeneste.opprettJournalpost(any(), anyBoolean())).thenReturn(
            new OpprettJournalpostResponse(DokumentArkivTestUtil.JOURNALPOST_ID, true, Collections.emptyList()));
        var captor = ArgumentCaptor.forClass(OpprettJournalpostRequest.class);

        var resultat = arkivTjeneste.opprettJournalpost(forsendelseId, AVSENDER_ID, SAK_ID);

        verify(dokArkivTjeneste).opprettJournalpost(captor.capture(), eq(Boolean.TRUE));
        var captured = captor.getValue();
        assertThat(captured.eksternReferanseId()).isEqualTo(forsendelseId.toString());
        assertThat(captured.dokumenter()).hasSize(1);
        assertThat(captured.tittel()).isEqualTo(SØKNAD_FORELDREPENGER_FØDSEL.getTermNavn());
        assertThat(resultat.journalpostId()).isEqualTo(DokumentArkivTestUtil.JOURNALPOST_ID);
        assertThat(resultat.ferdigstilt()).isEqualTo(Boolean.TRUE);
    }

}
