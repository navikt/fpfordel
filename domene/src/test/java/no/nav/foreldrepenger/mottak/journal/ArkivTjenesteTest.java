package no.nav.foreldrepenger.mottak.journal;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ANNET;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.DokArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostResponse;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

public class ArkivTjenesteTest {

    private static final String SAK_ID = "456";
    private static final String AVSENDER_ID = "3000";

    private ArkivTjeneste arkivTjeneste; // objektet vi tester
    // Mocks
    private SafTjeneste safTjeneste = mock(SafTjeneste.class);
    private DokArkivTjeneste dokArkivTjeneste = mock(DokArkivTjeneste.class);
    private DokumentRepository dokumentRepository = mock(DokumentRepository.class);
    private PersonConsumer personConsumer = mock(PersonConsumer.class);
    private AktørConsumerMedCache aktørConsumer = mock(AktørConsumerMedCache.class);

    @BeforeEach
    public void setup() throws Exception {
        when(personConsumer.hentPersonResponse(any()))
                .thenReturn(new HentPersonResponse().withPerson(new Person().withPersonnavn(new Personnavn().withSammensattNavn("For Etternavn"))));
        when(aktørConsumer.hentPersonIdentForAktørId(any())).thenReturn(Optional.of(AVSENDER_ID));
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(DokumentArkivTestUtil.BRUKER_ID));
        var brukerTjeneste = new PersonTjeneste(personConsumer, null, aktørConsumer);
        arkivTjeneste = new ArkivTjeneste(safTjeneste, dokArkivTjeneste, dokumentRepository, brukerTjeneste, aktørConsumer);
    }

    @Test
    public void skal_sende_hoveddokument_med_xml_og_pdf_versjon_med_ett_vedlegg() {
        UUID forsendelseId = UUID.randomUUID();

        DokumentMetadata metadata = DokumentArkivTestUtil.lagMetadata(forsendelseId, SAK_ID);
        List<Dokument> dokumenter = DokumentArkivTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, SØKNAD_FORELDREPENGER_FØDSEL);
        dokumenter.add(DokumentArkivTestUtil.lagDokumentBeskrivelse(forsendelseId, ANNET, ArkivFilType.PDFA, false, "Farskap"));

        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(metadata);
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);
        when(dokArkivTjeneste.opprettJournalpost(any(), anyBoolean()))
                .thenReturn(new OpprettJournalpostResponse(DokumentArkivTestUtil.JOURNALPOST_ID, true, Collections.emptyList()));
        ArgumentCaptor<OpprettJournalpostRequest> captor = ArgumentCaptor.forClass(OpprettJournalpostRequest.class);

        var resultat = arkivTjeneste.opprettJournalpost(forsendelseId, AVSENDER_ID, SAK_ID);

        verify(dokArkivTjeneste).opprettJournalpost(captor.capture(), eq(Boolean.TRUE));
        OpprettJournalpostRequest captured = captor.getValue();
        assertThat(captured.getEksternReferanseId()).isEqualTo(forsendelseId.toString());
        assertThat(captured.getDokumenter()).hasSize(2);
        assertThat(captured.getDokumenter().get(0).getDokumentvarianter()).hasSize(2);
        assertThat(captured.getJournalfoerendeEnhet()).isEqualTo("9999");
        assertThat(captured.getTittel()).isEqualTo(SØKNAD_FORELDREPENGER_FØDSEL.getTermNavn());
        assertThat(resultat.getJournalpostId()).isEqualTo(DokumentArkivTestUtil.JOURNALPOST_ID);
        assertThat(resultat.isFerdigstilt()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void skal_returnere_respons_med_journalTilstand_endelig() {
        UUID forsendelseId = UUID.randomUUID();

        DokumentMetadata metadata = DokumentArkivTestUtil.lagMetadata(forsendelseId, SAK_ID);
        List<Dokument> dokumenter = DokumentArkivTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, SØKNAD_FORELDREPENGER_FØDSEL);

        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(metadata);
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);
        when(dokArkivTjeneste.opprettJournalpost(any(), anyBoolean()))
                .thenReturn(new OpprettJournalpostResponse(DokumentArkivTestUtil.JOURNALPOST_ID, true, Collections.emptyList()));
        ArgumentCaptor<OpprettJournalpostRequest> captor = ArgumentCaptor.forClass(OpprettJournalpostRequest.class);

        var resultat = arkivTjeneste.opprettJournalpost(forsendelseId, AVSENDER_ID, SAK_ID);

        verify(dokArkivTjeneste).opprettJournalpost(captor.capture(), eq(Boolean.TRUE));
        OpprettJournalpostRequest captured = captor.getValue();
        assertThat(captured.getEksternReferanseId()).isEqualTo(forsendelseId.toString());
        assertThat(captured.getDokumenter()).hasSize(1);
        assertThat(captured.getTittel()).isEqualTo(SØKNAD_FORELDREPENGER_FØDSEL.getTermNavn());
        assertThat(resultat.getJournalpostId()).isEqualTo(DokumentArkivTestUtil.JOURNALPOST_ID);
        assertThat(resultat.isFerdigstilt()).isEqualTo(Boolean.TRUE);
    }

}
