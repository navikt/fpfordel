package no.nav.foreldrepenger.mottak.journal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import no.nav.dok.tjenester.mottainngaaendeforsendelse.DokumentInfoHoveddokument;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.DokumentInfoVedlegg;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.DokumentVariant;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.VariantFormat;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.DokumentInformasjonMangler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinnhold;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentkategorier;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.DokumenttypeIder;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journalfoeringsbehov;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.JournalpostMangler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Mottakskanaler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.util.FPDateUtil;

public class JournalTjenesteUtilTest {

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String JOURNALPOST_ID = "1234";
    private static final List<String> DOKUMENT_ID_LISTE = new ArrayList<>();
    public static final String KANAL_REFERANSE = "SKY_CHANNEL";
    public static final String FNR = "99999999999";
    public static final String DOKUMENT_ID = "DOKUMENT_ID";

    private static LocalDate FORSENDELSE_MOTTATT = FPDateUtil.iDag();

    static {
        DOKUMENT_ID_LISTE.add("234");
        DOKUMENT_ID_LISTE.add("256");
    }

    private JournalTjenesteUtil tjenesteUtil;

    @Before
    public void setup() {
        tjenesteUtil = new JournalTjenesteUtil();
    }

    @Test
    public void skalKonvertereTilDokumentforsendelseResponse() {
        MottaInngaaendeForsendelseResponse original = new MottaInngaaendeForsendelseResponse();
        original.setJournalTilstand(MottaInngaaendeForsendelseResponse.JournalTilstand.ENDELIG_JOURNALFOERT);
        original.setJournalpostId(JOURNALPOST_ID);
        original.setDokumentIdListe(DOKUMENT_ID_LISTE);

        DokumentforsendelseResponse resultat = tjenesteUtil.konverterTilDokumentforsendelseResponse(original);
        assertThat(resultat.getJournalTilstand()).isEqualByComparingTo(JournalTilstand.ENDELIG_JOURNALFØRT);
        assertThat(resultat.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(resultat.getDokumentIdListe()).containsAll(DOKUMENT_ID_LISTE);
    }

    @Test
    public void skalKonvertereDokumentTilDokumentInfoHoveddokument() {
        List<Dokument> hoveddokument = new ArrayList<>();
        hoveddokument.add(lagDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, ArkivFilType.XML, true));
        hoveddokument.add(lagDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, ArkivFilType.PDFA, true));

        DokumentInfoHoveddokument resultat = tjenesteUtil.konverterTilDokumentInfoHoveddokument(hoveddokument);

        assertThat(resultat).isNotNull();
        assertThat(resultat.getDokumentTypeId()).isEqualTo(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getOffisiellKode());
        assertThat(resultat.getDokumentVariant()).hasSize(2);

        DokumentVariant xmlVar = resultat.getDokumentVariant().get(0);
        assertThat(xmlVar.getDokument()).isNotNull();
        assertThat(xmlVar.getArkivFilType()).isEqualByComparingTo(DokumentVariant.ArkivFilType.XML);
        assertThat(xmlVar.getVariantFormat()).isEqualByComparingTo(DokumentVariant.VariantFormat.ORIGINAL);

        DokumentVariant pdfVar = resultat.getDokumentVariant().get(1);
        assertThat(pdfVar.getDokument()).isNotNull();
        assertThat(pdfVar.getArkivFilType()).isEqualByComparingTo(DokumentVariant.ArkivFilType.PDFA);
        assertThat(pdfVar.getVariantFormat()).isEqualByComparingTo(DokumentVariant.VariantFormat.ARKIV);
    }

    @Test
    public void skalKonvertereVedleggListeTilDokumentInfoVedleggListe() {
        List<Dokument> vedleggListe = new ArrayList<>();
        vedleggListe.add(lagDokument(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, ArkivFilType.PDFA, false));
        vedleggListe.add(lagDokument(DokumentTypeId.ANNET, ArkivFilType.PDFA, false));

        List<DokumentInfoVedlegg> resultat = tjenesteUtil.konverterTilDokumentInfoVedlegg(vedleggListe, false, false);
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getTittel()).isNullOrEmpty();
        assertThat(resultat.get(0).getDokumentTypeId()).isEqualTo(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL.getOffisiellKode());
        assertThat(resultat.get(0).getDokumentVariant()).hasSize(1);
        assertThat(resultat.get(0).getDokumentVariant().get(0).getArkivFilType()).isEqualByComparingTo(DokumentVariant.ArkivFilType.PDFA);
        assertThat(resultat.get(0).getDokumentVariant().get(0).getVariantFormat()).isEqualByComparingTo(DokumentVariant.VariantFormat.ARKIV);
        assertThat(resultat.get(0).getDokumentVariant().get(0).getDokument()).isNotNull();

        assertThat(resultat.get(1).getTittel()).isNullOrEmpty();
        assertThat(resultat.get(1).getDokumentTypeId()).isEqualTo(DokumentTypeId.ANNET.getOffisiellKode());
        assertThat(resultat.get(1).getDokumentVariant()).hasSize(1);
        assertThat(resultat.get(1).getDokumentVariant().get(0).getArkivFilType()).isEqualByComparingTo(DokumentVariant.ArkivFilType.PDFA);
        assertThat(resultat.get(1).getDokumentVariant().get(0).getVariantFormat()).isEqualByComparingTo(DokumentVariant.VariantFormat.ARKIV);
        assertThat(resultat.get(1).getDokumentVariant().get(0).getDokument()).isNotNull();
    }

    @Test
    public void skalKonvertereVedleggListeTilDokumentInfoVedleggListeEndelig() {
        List<Dokument> vedleggListe = new ArrayList<>();
        vedleggListe.add(lagDokument(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, ArkivFilType.PDFA, false));
        vedleggListe.add(lagDokument(DokumentTypeId.ANNET, ArkivFilType.PDFA, false));
        vedleggListe.add(lagDokumentMedBeskrivelse(DokumentTypeId.ANNET, ArkivFilType.PDFA, false, "Farskap"));

        List<DokumentInfoVedlegg> resultat = tjenesteUtil.konverterTilDokumentInfoVedlegg(vedleggListe, false, true);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getTittel()).isNullOrEmpty();
        assertThat(resultat.get(0).getDokumentTypeId()).isEqualTo(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL.getOffisiellKode());
        assertThat(resultat.get(0).getDokumentVariant()).hasSize(1);

        assertThat(resultat.get(1).getTittel()).isEqualTo(DokumentTypeId.ANNET.getTermNavn());
        assertThat(resultat.get(1).getDokumentTypeId()).isEqualTo(DokumentTypeId.ANNET.getOffisiellKode());
        assertThat(resultat.get(1).getDokumentVariant()).hasSize(1);

        assertThat(resultat.get(2).getTittel()).isEqualTo("Farskap");
        assertThat(resultat.get(2).getDokumentTypeId()).isEqualTo(DokumentTypeId.ANNET.getOffisiellKode());
        assertThat(resultat.get(2).getDokumentVariant()).hasSize(1);
    }


    @Test
    public void skalKonvertereVedleggListeTilDokumentInfoVedleggListeOgSetteTittelPåVedleggNårHoveddokumentIkkeFinnesOgDokumentTypeIdErAnnet() {
        List<Dokument> vedleggListe = new ArrayList<>();
        vedleggListe.add(lagDokument(DokumentTypeId.ANNET, ArkivFilType.PDFA, false));
        vedleggListe.add(lagDokument(DokumentTypeId.ANNET, ArkivFilType.PDFA, false));

        List<DokumentInfoVedlegg> resultat = tjenesteUtil.konverterTilDokumentInfoVedlegg(vedleggListe, false, false);
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getTittel()).isNotEmpty();
        assertThat(resultat.get(1).getTittel()).isEqualToIgnoringCase(DokumentTypeId.ANNET.getKode());
    }

    @Test
    public void skalKonvertereVedleggListeUtenÅSetteTittelPåVedleggNårHoveddokumentIkkeFinnesOgDokumentTypeIkkeErAnnet() {
        List<Dokument> vedleggListe = new ArrayList<>();
        vedleggListe.add(lagDokument(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, ArkivFilType.PDFA, false));

        List<DokumentInfoVedlegg> resultat = tjenesteUtil.konverterTilDokumentInfoVedlegg(vedleggListe, false, false);
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getTittel()).isNullOrEmpty();
    }

    @Test
    public void konverterJournalpostManglerTilJournalPostMangler() {
        DokumentInformasjonMangler dokInfoMangler = new DokumentInformasjonMangler();
        dokInfoMangler.setDokumentId("dokId");
        dokInfoMangler.setDokumentkategori(Journalfoeringsbehov.MANGLER);
        dokInfoMangler.setTittel(Journalfoeringsbehov.MANGLER);

        JournalpostMangler journalpostMangler = new JournalpostMangler();
        journalpostMangler.setAvsenderId(Journalfoeringsbehov.MANGLER_IKKE);
        journalpostMangler.setTema(Journalfoeringsbehov.MANGLER_IKKE);
        journalpostMangler.setArkivSak(Journalfoeringsbehov.MANGLER);
        journalpostMangler.setBruker(Journalfoeringsbehov.MANGLER);
        journalpostMangler.setForsendelseInnsendt(Journalfoeringsbehov.MANGLER);
        journalpostMangler.setInnhold(Journalfoeringsbehov.MANGLER);
        journalpostMangler.setHoveddokument(dokInfoMangler);

        JournalPostMangler resultat = tjenesteUtil.konverterTilJournalmangler(journalpostMangler);
        assertThat(resultat.harMangler()).isTrue();
        assertThat(resultat.getMangler()).isNotEmpty();
        List<JournalPostMangler.JournalMangelType> mangler = resultat.getMangler();
        assertThat(mangler).contains(JournalPostMangler.JournalMangelType.ARKIVSAK);
        assertThat(mangler).contains(JournalPostMangler.JournalMangelType.BRUKER);
        assertThat(mangler).contains(JournalPostMangler.JournalMangelType.INNHOLD);
    }

    @Test
    public void skal_konvertere_metadata() {

        HentJournalpostResponse testResponse = new HentJournalpostResponse();
        testResponse.setInngaaendeJournalpost(lagInngåendeJournalPost());

        List<JournalMetadata<DokumentTypeId>> result = tjenesteUtil.konverterTilMetadata(JOURNALPOST_ID, testResponse);
        assertThat(result.size()).isEqualTo(2);
        JournalMetadata<DokumentTypeId> metadata = result.get(1);
        assertThat(metadata.getDokumentTypeId()).isEqualTo(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);
        assertThat(metadata.getKanalReferanseId()).isEqualTo(KANAL_REFERANSE);
        assertThat(metadata.getForsendelseMottatt()).isEqualTo(FORSENDELSE_MOTTATT);
        assertThat(metadata.getArkivFilType()).isEqualTo(ArkivFilType.PDFA);
        assertThat(metadata.getDokumentId()).isEqualTo(DOKUMENT_ID);
        assertThat(metadata.getBrukerIdentListe()).contains(FNR);
        assertThat(metadata.getVariantFormat()).isEqualTo(VariantFormat.SLADDET);
        assertThat(metadata.getDokumentKategori().orElse(DokumentKategori.UDEFINERT)).isEqualTo(DokumentKategori.ELEKTRONISK_SKJEMA);
    }

    private InngaaendeJournalpost lagInngåendeJournalPost() {
        InngaaendeJournalpost journalpost = new InngaaendeJournalpost();

        journalpost.setMottakskanal(new Mottakskanaler().withValue("NAV_NO"));

        journalpost.setJournaltilstand(Journaltilstand.MIDLERTIDIG);
        journalpost.setForsendelseMottatt(DateUtil.convertToXMLGregorianCalendar(FORSENDELSE_MOTTATT));
        journalpost.setKanalReferanseId(KANAL_REFERANSE);

        Dokumentinformasjon hovedDok = lagDokumentinformasjon();
        journalpost.setHoveddokument(hovedDok);

        Dokumentinformasjon vedlegg = lagDokumentinformasjon();
        journalpost.getVedleggListe().add(vedlegg);
        journalpost.getBrukerListe().add(new Person().withIdent(FNR));

        return journalpost;
    }

    private Dokumentinformasjon lagDokumentinformasjon() {
        Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
        dokumentinformasjon.setDokumentId(DOKUMENT_ID);
        dokumentinformasjon.setDokumenttypeId(new DokumenttypeIder().withValue("I000041"));
        dokumentinformasjon.setDokumentkategori(new Dokumentkategorier().withValue("ES"));

        Dokumentinnhold dokumentinnhold = new Dokumentinnhold();
        dokumentinnhold.setVariantformat(new Variantformater().withValue("SLADDET"));
        dokumentinnhold.setArkivfiltype(new Arkivfiltyper().withValue("PDFA"));

        dokumentinformasjon.getDokumentInnholdListe().add(dokumentinnhold);

        return dokumentinformasjon;

    }

    private Dokument lagDokument(DokumentTypeId dokTypeId, ArkivFilType arkivFilType, Boolean hoveddok) {
        return DokumentforsendelseTestUtil.lagDokument(FORSENDELSE_ID, dokTypeId, arkivFilType, hoveddok);
    }

    private Dokument lagDokumentMedBeskrivelse(DokumentTypeId dokTypeId, ArkivFilType arkivFilType, Boolean hoveddok, String beskrivelse) {
        return DokumentforsendelseTestUtil.lagDokumentBeskrivelse(FORSENDELSE_ID, dokTypeId, arkivFilType, hoveddok, beskrivelse);
    }
}