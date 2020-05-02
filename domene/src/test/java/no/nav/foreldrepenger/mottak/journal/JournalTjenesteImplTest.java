package no.nav.foreldrepenger.mottak.journal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringFerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostOppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.FerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.JournalpostIkkeInngaeende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.ObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.ArkivSak;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal.BehandleInngaaendeJournalConsumer;
import no.nav.vedtak.felles.integrasjon.inngaaendejournal.InngaaendeJournalConsumer;
import no.nav.vedtak.felles.integrasjon.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRestKlient;

public class JournalTjenesteImplTest {

    private static final String JOURNALPOST_ID = "1234";
    private static final String DOKUMENT_ID = "1234";
    private static final UUID FORSENDELSE_ID = UUID.randomUUID();

    private JournalTjeneste journalTjeneste; // tjenesten som testes;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private InngaaendeJournalConsumer inngaaendeMock = mock(InngaaendeJournalConsumer.class);
    private BehandleInngaaendeJournalConsumer behandleInngaaendeMock = mock(BehandleInngaaendeJournalConsumer.class);
    private MottaInngaaendeForsendelseRestKlient inngaaendeForsendelseKlient = mock(
            MottaInngaaendeForsendelseRestKlient.class);

    @Before
    public void setUp() throws Exception {
        journalTjeneste = new JournalTjeneste(
                inngaaendeMock,
                behandleInngaaendeMock,
                inngaaendeForsendelseKlient);
    }

    @Test
    public void test_utledJournalføringsbehov_skal_kaste_journalUtilgjengeligSikkerhetsbegrensning() throws Exception {
        when(inngaaendeMock.utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class)))
                .thenThrow(new UtledJournalfoeringsbehovSikkerhetsbegrensning());

        expectedException.expect(ManglerTilgangException.class);
        expectedException.expectMessage("FP-751834");

        journalTjeneste.utledJournalføringsbehov(JOURNALPOST_ID);
        verify(inngaaendeMock).utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class));
    }

    @Test
    public void test_utledJournalføringsbehov_skal_kaste_journalfoeringsbehovUgyldigInput() throws Exception {
        when(inngaaendeMock.utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class)))
                .thenThrow(new UtledJournalfoeringsbehovUgyldigInput());

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-976237");

        journalTjeneste.utledJournalføringsbehov(JOURNALPOST_ID);
        verify(inngaaendeMock).utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class));
    }

    @Test
    public void test_utledJournalføringsbehov_skal_kaste_journalfoeringsbehovJournalpostKanIkkeBehandles()
            throws Exception {
        when(inngaaendeMock.utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class)))
                .thenThrow(new UtledJournalfoeringsbehovJournalpostKanIkkeBehandles());

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("204098");

        journalTjeneste.utledJournalføringsbehov(JOURNALPOST_ID);
        verify(inngaaendeMock).utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class));
    }

    @Test
    public void test_utledJournalføringsbehov_skal_kaste_journalføringsbehovJournalpostIkkeFunnet() throws Exception {
        when(inngaaendeMock.utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class)))
                .thenThrow(new UtledJournalfoeringsbehovJournalpostIkkeFunnet());

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-856651");

        journalTjeneste.utledJournalføringsbehov(JOURNALPOST_ID);
        verify(inngaaendeMock).utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class));
    }

    @Test
    public void test_utledJournalføringsbehov_skal_kaste_journalføringsbehovJournalpostIkkeInngående()
            throws Exception {
        when(inngaaendeMock.utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class)))
                .thenThrow(new UtledJournalfoeringsbehovJournalpostIkkeInngaaende());

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-729774");

        journalTjeneste.utledJournalføringsbehov(JOURNALPOST_ID);
        verify(inngaaendeMock).utledJournalfoeringsbehov(any(UtledJournalfoeringsbehovRequest.class));
    }

    @Test
    public void test_ferdigstillJournalføring_skal_kalle_ferdigstillJournalfoering() throws Exception {
        journalTjeneste.ferdigstillJournalføring(JOURNALPOST_ID, "enhetId");
        verify(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));
    }

    @Test
    public void test_ferdigstillJournalføring_skal_kaste_journalfoeringFerdigstillingIkkeMulig() throws Exception {
        doThrow(new FerdigstillJournalfoeringFerdigstillingIkkeMulig("ikke mulig", new FerdigstillingIkkeMulig()))
                .when(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-453958");

        journalTjeneste.ferdigstillJournalføring(JOURNALPOST_ID, "enhetId");
        verify(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));
    }

    @Test
    public void test_ferdigstillJournalføring_skal_kaste_journalfoeringJournalpostIkkeInngaaende() throws Exception {
        doThrow(new FerdigstillJournalfoeringJournalpostIkkeInngaaende("ikke mulig", new JournalpostIkkeInngaeende()))
                .when(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-346800");

        journalTjeneste.ferdigstillJournalføring(JOURNALPOST_ID, "enhetId");
        verify(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));
    }

    @Test
    public void test_ferdigstillJournalføring_skal_kaste_journalfoeringUgyldigInput() throws Exception {
        doThrow(new FerdigstillJournalfoeringUgyldigInput("ugyldig", new UgyldigInput()))
                .when(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-345598");

        journalTjeneste.ferdigstillJournalføring(JOURNALPOST_ID, "enhetId");
        verify(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));
    }

    @Test
    public void test_ferdigstillJournalføring_skal_kaste_journalfoeringSikkerhetsbegrensning() throws Exception {
        doThrow(new FerdigstillJournalfoeringSikkerhetsbegrensning("sikkerhet",
                new no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.Sikkerhetsbegrensning()))
                        .when(behandleInngaaendeMock)
                        .ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));

        expectedException.expect(ManglerTilgangException.class);
        expectedException.expectMessage("FP-009810");

        journalTjeneste.ferdigstillJournalføring(JOURNALPOST_ID, "enhetId");
        verify(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));
    }

    @Test
    public void test_ferdigstillJournalføring_skal_kaste_journalfoeringObjektIkkeFunnet() throws Exception {
        doThrow(new FerdigstillJournalfoeringObjektIkkeFunnet("ikke mulig", new ObjektIkkeFunnet()))
                .when(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-737540");

        journalTjeneste.ferdigstillJournalføring(JOURNALPOST_ID, "enhetId");
        verify(behandleInngaaendeMock).ferdigstillJournalfoering(any(FerdigstillJournalfoeringRequest.class));
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

    @Test
    public void oppdater_journalpost_skal_oppdatere_journalpost() throws OppdaterJournalpostSikkerhetsbegrensning,
            OppdaterJournalpostOppdateringIkkeMulig, OppdaterJournalpostUgyldigInput,
            OppdaterJournalpostJournalpostIkkeInngaaende, OppdaterJournalpostObjektIkkeFunnet {
        String arkivSakId = "arkivsakId";
        JournalPost journalPost = new JournalPost("id");
        journalPost.setArkivSakId(arkivSakId);
        String ar = "ARKIVREF";
        String fnr = "99999999999";
        journalPost.setFnr(fnr);
        journalPost.setArkivSakSystem(ar);

        journalTjeneste.oppdaterJournalpost(journalPost);

        ArgumentCaptor<OppdaterJournalpostRequest> captor = ArgumentCaptor.forClass(OppdaterJournalpostRequest.class);
        verify(behandleInngaaendeMock).oppdaterJournalpost(captor.capture());
        InngaaendeJournalpost inngaaendeJournalpost = captor.getValue().getInngaaendeJournalpost();
        ArkivSak arkivSak = inngaaendeJournalpost.getArkivSak();

        assertThat(((Person) inngaaendeJournalpost.getBruker()).getIdent()).isEqualTo(fnr);
        assertThat(arkivSak.getArkivSakId()).isEqualTo(arkivSakId);
        assertThat(arkivSak.getArkivSakSystem()).isEqualTo(ar);
    }

    @Test
    public void oppdater_journalpost_avsender_skal_oppdatere_journalpost()
            throws OppdaterJournalpostSikkerhetsbegrensning, OppdaterJournalpostOppdateringIkkeMulig,
            OppdaterJournalpostUgyldigInput, OppdaterJournalpostJournalpostIkkeInngaaende,
            OppdaterJournalpostObjektIkkeFunnet {
        JournalPost journalPost = new JournalPost("id");
        String fnr = "99999999999";
        journalPost.setAvsenderFnr(fnr);

        journalTjeneste.oppdaterJournalpost(journalPost);

        ArgumentCaptor<OppdaterJournalpostRequest> captor = ArgumentCaptor.forClass(OppdaterJournalpostRequest.class);
        verify(behandleInngaaendeMock).oppdaterJournalpost(captor.capture());
        InngaaendeJournalpost inngaaendeJournalpost = captor.getValue().getInngaaendeJournalpost();

        assertThat((inngaaendeJournalpost.getAvsender().getAvsenderId())).isEqualTo(fnr);
    }

    @Test
    public void skal_feile_når_journalpost_er_null() {
        expectedException.expect(IllegalArgumentException.class);
        journalTjeneste.oppdaterJournalpost(null);
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