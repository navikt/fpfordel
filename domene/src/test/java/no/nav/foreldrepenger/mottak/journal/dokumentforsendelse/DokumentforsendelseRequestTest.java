package no.nav.foreldrepenger.mottak.journal.dokumentforsendelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;

public class DokumentforsendelseRequestTest {
    private final static UUID FORSENDELSE_ID = UUID.randomUUID();

    @Test
    public void test_builder_lag_tittel_på_forsendelseInfo_når_hoveddok_ikke_finnes() {
        List<Dokument> dokumentListe = new ArrayList<>();
        dokumentListe.add(lagDokument(DokumentTypeId.INNTEKTSMELDING, ArkivFilType.PDFA, false));
        dokumentListe.add(lagDokument(DokumentTypeId.INNTEKTSMELDING, ArkivFilType.PDFA, false));
        dokumentListe.add(lagDokument(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, ArkivFilType.PDFA, false));

        DokumentforsendelseRequest request = DokumentforsendelseRequest.builder()
                .medBruker("bruker")
                .medForsendelseId("1234")
                .medForsendelseMottatt(LocalDateTime.now())
                .medForsøkEndeligJF(false)
                .medSaksnummer("1234")
                .medVedlegg(dokumentListe)
                .build();

        assertThat(request.getTittel()).isNotEmpty();
        assertThat(request.getTittel()).isEqualToIgnoringCase("Ettersendelse: Inntektsmelding (2x), Dokumentasjon_av_termin_eller_fødsel (1x)");
    }

    @Test
    public void test_builder_ikke_lag_tittel_på_forsendelseInfo_når_hoveddok_finnes() {
        List<Dokument> hoveddokument = new ArrayList<>();
        hoveddokument.add(lagDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, ArkivFilType.XML, true));
        hoveddokument.add(lagDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, ArkivFilType.PDFA, true));

        List<Dokument> vedleggListe = new ArrayList<>();
        vedleggListe.add(lagDokument(DokumentTypeId.INNTEKTSMELDING, ArkivFilType.PDFA, false));
        vedleggListe.add(lagDokument(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, ArkivFilType.PDFA, false));

        DokumentforsendelseRequest request = DokumentforsendelseRequest.builder()
                .medBruker("bruker")
                .medForsendelseId("1234")
                .medForsendelseMottatt(LocalDateTime.now())
                .medForsøkEndeligJF(true)
                .medSaksnummer("1234")
                .medHoveddokument(hoveddokument)
                .medVedlegg(vedleggListe)
                .build();

        assertThat(request.getTittel()).isNullOrEmpty();
    }

    private Dokument lagDokument(DokumentTypeId dokTypeId, ArkivFilType arkivFilType, Boolean hoveddok) {
        return DokumentforsendelseTestUtil.lagDokument(FORSENDELSE_ID, dokTypeId, arkivFilType, hoveddok);
    }

}