package no.nav.foreldrepenger.mottak.journal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinnhold;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentkategorier;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.DokumenttypeIder;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journalfoeringsbehov;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.JournalpostMangler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

class JournalTjenesteUtil {

    private static Map<Journaltilstand, JournalMetadata.Journaltilstand> journaltilstandPrjournaltilstandJaxb;

    static {
        journaltilstandPrjournaltilstandJaxb = new EnumMap<>(Journaltilstand.class);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.MIDLERTIDIG,
                JournalMetadata.Journaltilstand.MIDLERTIDIG);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.UTGAAR, JournalMetadata.Journaltilstand.UTGAAR);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.ENDELIG, JournalMetadata.Journaltilstand.ENDELIG);
    }

    JournalTjenesteUtil() {
    }

    DokumentforsendelseResponse konverterTilDokumentforsendelseResponse(
            MottaInngaaendeForsendelseResponse inngåendeForsendelseResponse) {
        return DokumentforsendelseResponse.builder()
                .medJournalpostId(inngåendeForsendelseResponse.getJournalpostId())
                .medJournalTilstand(
                        JournalTilstand.fromValue(inngåendeForsendelseResponse.getJournalTilstand().value()))
                .medDokumentIdListe(inngåendeForsendelseResponse.getDokumentIdListe())
                .build();
    }

    DokumentInfoHoveddokument konverterTilDokumentInfoHoveddokument(List<Dokument> hoveddokument) {
        var info = new DokumentInfoHoveddokument();
        if (!hoveddokument.isEmpty()) {
            List<DokumentVariant> dokVariant = new ArrayList<>();
            for (Dokument dok : hoveddokument) {
                DokumentVariant variant = new DokumentVariant();
                variant.setDokument(dok.getBase64EncodetDokument());
                variant.setArkivFilType(DokumentVariant.ArkivFilType.fromValue(dok.getArkivFilType().getKode()));
                if (ArkivFilType.PDFA.equals(dok.getArkivFilType())) {
                    variant.setVariantFormat(DokumentVariant.VariantFormat.ARKIV);
                } else {
                    variant.setVariantFormat(DokumentVariant.VariantFormat.ORIGINAL);
                }
                dokVariant.add(variant);
            }
            info.setDokumentVariant(dokVariant);
            info.setDokumentTypeId(hoveddokument.get(0).getDokumentTypeId().getOffisiellKode());
        }
        return info;
    }

    List<DokumentInfoVedlegg> konverterTilDokumentInfoVedlegg(List<Dokument> vedlegg, boolean harHoveddokument,
            boolean endelig) {
        List<DokumentInfoVedlegg> infoList = new ArrayList<>();
        long antallAnnet = vedlegg.stream().map(Dokument::getDokumentTypeId).filter(DokumentTypeId.ANNET::equals)
                .count();
        boolean krevTittel = !harHoveddokument && vedlegg.size() == antallAnnet;

        for (Dokument dokument : vedlegg) {
            infoList.add(getDokumentInfoVedlegg(dokument, endelig, krevTittel));
        }

        return infoList;
    }

    String tittelFraDokument(Dokument dokument, boolean endelig, boolean kreverTittel) {
        DokumentTypeId dokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokument.getDokumentTypeId())
                ? DokumentTypeId.ANNET
                : dokument.getDokumentTypeId();
        String dokumentTypeTittel = dokumentTypeId.getTermNavn();
        if (DokumentTypeId.ANNET.equals(dokumentTypeId)) {
            // Brukers beskrivelse hvis ulikt "Annet"
            if (dokument.getBeskrivelse() != null && !dokumentTypeTittel.equals(dokument.getBeskrivelse())) {
                return dokument.getBeskrivelse();
            }
            // Ikke sett tittel hvis midlertidig. SBH gjør det fra Gosys
            return endelig || kreverTittel ? dokumentTypeTittel : null;
        }
        return kreverTittel ? dokumentTypeTittel : null;
    }

    JournalPostMangler konverterTilJournalmangler(JournalpostMangler journalpostMangler) {
        JournalPostMangler mangler = new JournalPostMangler();
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.ARKIVSAK,
                journalpostMangler.getArkivSak() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.AVSENDERID,
                journalpostMangler.getAvsenderId() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.AVSENDERNAVN,
                journalpostMangler.getAvsenderNavn() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.BRUKER,
                journalpostMangler.getBruker() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.INNHOLD,
                journalpostMangler.getInnhold() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.TEMA,
                journalpostMangler.getTema() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.HOVEDOK_KATEGORI,
                journalpostMangler.getHoveddokument().getDokumentkategori() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.HOVEDOK_TITTEL,
                journalpostMangler.getHoveddokument().getTittel() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.VEDLEGG_KATEGORI,
                journalpostMangler.getVedleggListe().stream()
                        .anyMatch(vedlegg -> vedlegg.getDokumentkategori() == Journalfoeringsbehov.MANGLER));
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.VEDLEGG_TITTEL, journalpostMangler
                .getVedleggListe().stream().anyMatch(vedlegg -> vedlegg.getTittel() == Journalfoeringsbehov.MANGLER));
        return mangler;
    }

    List<JournalMetadata> konverterTilMetadata(String journalpostId, HentJournalpostResponse response) {
        InngaaendeJournalpost journalpost = response.getInngaaendeJournalpost();

        List<JournalMetadata> metadataList = new ArrayList<>();

        populerMetadataListe(journalpostId, journalpost, journalpost.getHoveddokument(), true, metadataList);
        if (journalpost.getVedleggListe() != null) {
            for (Dokumentinformasjon dokumentInfo : journalpost.getVedleggListe()) {
                populerMetadataListe(journalpostId, journalpost, dokumentInfo, false, metadataList);
            }
        }
        return metadataList;
    }

    private DokumentInfoVedlegg getDokumentInfoVedlegg(Dokument dokument, boolean endelig, boolean krevTittel) {
        DokumentTypeId dokumentTypeId = dokument.getDokumentTypeId();
        DokumentInfoVedlegg info = new DokumentInfoVedlegg();
        List<DokumentVariant> dokumentVariantList = new ArrayList<>();
        DokumentVariant dokVariant = new DokumentVariant();

        dokVariant.setDokument(dokument.getBase64EncodetDokument());
        dokVariant.setArkivFilType(DokumentVariant.ArkivFilType.fromValue(dokument.getArkivFilType().getKode()));
        dokVariant.setVariantFormat(DokumentVariant.VariantFormat.ARKIV);
        dokumentVariantList.add(dokVariant);

        info.setTittel(tittelFraDokument(dokument, endelig, krevTittel));
        info.setDokumentTypeId(dokumentTypeId.getOffisiellKode());
        info.setDokumentVariant(dokumentVariantList);

        return info;
    }

    private void populerMetadataListe(String journalpostId, InngaaendeJournalpost journalpost,
            Dokumentinformasjon dokumentinfo, boolean erHoveddokument,
            List<JournalMetadata> metadataList) {

        Journaltilstand journaltilstandJaxb = journalpost.getJournaltilstand();
        JournalMetadata.Journaltilstand journaltilstand = journaltilstandJaxb != null
                ? journaltilstandPrjournaltilstandJaxb.get(journaltilstandJaxb)
                : null;

        LocalDate forsendelseMottatt = DateUtil.convertToLocalDate(journalpost.getForsendelseMottatt());
        LocalDateTime forsendelseMottattTidspunkt = DateUtil
                .convertToLocalDateTime(journalpost.getForsendelseMottatt());
        Optional<String> kanalReferanseId = Optional.ofNullable(journalpost.getKanalReferanseId());
        Optional<String> mottaksKanal = Optional.ofNullable(journalpost.getMottakskanal() != null ? journalpost.getMottakskanal().getValue() : null);
        Optional<String> journalEnhet = Optional.ofNullable(journalpost.getJournalfEnhet());

        List<no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Aktoer> brukerListe = journalpost
                .getBrukerListe();

        final String dokumentId = dokumentinfo.getDokumentId();
        final DokumentTypeId dokumentTypeId = getDokumentTypeId(dokumentinfo);
        final Optional<DokumentKategori> dokumentKategori = getDokumentKategori(dokumentinfo);

        List<String> brukerIdentList = brukerListe.stream().filter((a) -> {
            // instanceof OK - eksternt grensesnitt
            return a instanceof no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Person; // NOSONAR
        }).map(a -> ((no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Person) a).getIdent())
                .collect(Collectors.toList());

        for (Dokumentinnhold dokumentinnhold : dokumentinfo.getDokumentInnholdListe()) {
            VariantFormat variantFormat = getVariantFormat(dokumentinnhold);
            ArkivFilType arkivFilType = getArkivFilType(dokumentinnhold);

            JournalMetadata.Builder builder = JournalMetadata.builder();
            builder.medJournalpostId(journalpostId);
            builder.medDokumentId(dokumentId);
            builder.medVariantFormat(variantFormat);
            builder.medDokumentType(dokumentTypeId);
            dokumentKategori.ifPresent(builder::medDokumentKategori);
            builder.medArkivFilType(arkivFilType);
            builder.medJournaltilstand(journaltilstand);
            builder.medErHoveddokument(erHoveddokument);
            builder.medForsendelseMottatt(forsendelseMottatt);
            builder.medForsendelseMottattTidspunkt(forsendelseMottattTidspunkt);
            builder.medBrukerIdentListe(brukerIdentList);
            kanalReferanseId.ifPresent(builder::medKanalReferanseId);
            mottaksKanal.ifPresent(builder::medMottaksKanal);
            journalEnhet.ifPresent(builder::medJournalførendeEnhet);
            JournalMetadata metadata = builder.build();

            metadataList.add(metadata);
        }
    }

    private DokumentTypeId getDokumentTypeId(Dokumentinformasjon dokumentinfo) {
        DokumentTypeId dokumentTypeId = null;
        DokumenttypeIder dokumenttypeJaxb = dokumentinfo.getDokumenttypeId();
        if (dokumenttypeJaxb != null && dokumenttypeJaxb.getValue() != null) {
            final String offisiellKode = dokumenttypeJaxb.getValue();
            dokumentTypeId = DokumentTypeId.fraOffisiellKode(offisiellKode);
        }
        return dokumentTypeId;
    }

    private Optional<DokumentKategori> getDokumentKategori(Dokumentinformasjon dokumentinfo) {
        DokumentKategori dokumentKategori = DokumentKategori.UDEFINERT;
        Dokumentkategorier dokumentkategoriJaxb = dokumentinfo.getDokumentkategori();
        if (dokumentkategoriJaxb != null && dokumentkategoriJaxb.getValue() != null) {
            String offisiellKode = dokumentkategoriJaxb.getValue();
            dokumentKategori = DokumentKategori.fraOffisiellKode(offisiellKode);
        }
        return DokumentKategori.UDEFINERT.equals(dokumentKategori) ? Optional.empty() : Optional.of(dokumentKategori);
    }

    private VariantFormat getVariantFormat(Dokumentinnhold dokumentinnhold) {
        VariantFormat variantFormat = null;
        Variantformater variantformatJaxb = dokumentinnhold.getVariantformat();
        if (variantformatJaxb != null && variantformatJaxb.getValue() != null) {
            String offisiellKode = variantformatJaxb.getValue();
            variantFormat = VariantFormat.fraKodeDefaultUdefinert(offisiellKode);
        }
        return variantFormat;
    }

    private ArkivFilType getArkivFilType(Dokumentinnhold dokumentinnhold) {
        ArkivFilType arkivFilType = null;
        Arkivfiltyper arkivfiltypeJaxb = dokumentinnhold.getArkivfiltype();
        if (arkivfiltypeJaxb != null && arkivfiltypeJaxb.getValue() != null) {
            String offisiellKode = arkivfiltypeJaxb.getValue();
            arkivFilType = ArkivFilType.fraKodeDefaultUdefinert(offisiellKode);
        }
        return arkivFilType;
    }

}
