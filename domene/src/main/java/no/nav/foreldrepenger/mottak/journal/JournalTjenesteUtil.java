package no.nav.foreldrepenger.mottak.journal;

import java.util.ArrayList;
import java.util.List;

import no.nav.dok.tjenester.mottainngaaendeforsendelse.DokumentInfoHoveddokument;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.DokumentInfoVedlegg;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.DokumentVariant;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;

class JournalTjenesteUtil {

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

}
