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
import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverk.VariantFormat;
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
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Mottakskanaler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class JournalTjenesteUtil {

    private KodeverkRepository kodeverkRepository;

    private static Map<Journaltilstand, JournalMetadata.Journaltilstand> journaltilstandPrjournaltilstandJaxb;

    static {
        journaltilstandPrjournaltilstandJaxb = new EnumMap<>(Journaltilstand.class);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.MIDLERTIDIG, JournalMetadata.Journaltilstand.MIDLERTIDIG);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.UTGAAR, JournalMetadata.Journaltilstand.UTGAAR);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.ENDELIG, JournalMetadata.Journaltilstand.ENDELIG);
    }

    public JournalTjenesteUtil(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    public DokumentforsendelseResponse konverterTilDokumentforsendelseResponse(MottaInngaaendeForsendelseResponse inngåendeForsendelseResponse) {
        return DokumentforsendelseResponse.builder()
                .medJournalpostId(inngåendeForsendelseResponse.getJournalpostId())
                .medJournalTilstand(JournalTilstand.fromValue(inngåendeForsendelseResponse.getJournalTilstand().value()))
                .medDokumentIdListe(inngåendeForsendelseResponse.getDokumentIdListe())
                .build();
    }

    public DokumentInfoHoveddokument konverterTilDokumentInfoHoveddokument(List<Dokument> hoveddokument) {
        DokumentInfoHoveddokument info = new DokumentInfoHoveddokument();
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
            info.setDokumentTypeId(kodeverkRepository.finn(DokumentTypeId.class, hoveddokument.get(0).getDokumentTypeId()).getOffisiellKode());
        }
        return info;
    }

    public List<DokumentInfoVedlegg> konverterTilDokumentInfoVedlegg(List<Dokument> vedlegg, Boolean harHoveddokument, Boolean endelig) {
        List<DokumentInfoVedlegg> infoList = new ArrayList<>();
        Boolean settTittelAnnet = endelig;

        long antallAnnet = vedlegg.stream().map(Dokument::getDokumentTypeId).filter(DokumentTypeId.ANNET::equals).count();

        if (!harHoveddokument && vedlegg.size() == antallAnnet) {
            settTittelAnnet = antallAnnet > 0;
        }

        for (Dokument dokument : vedlegg) {
            infoList.add(getDokumentInfoVedlegg(dokument, settTittelAnnet));
        }

        return infoList;
    }

    public JournalPostMangler konverterTilJournalmangler(JournalpostMangler journalpostMangler) {
        JournalPostMangler mangler = new JournalPostMangler();
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.ARKIVSAK, journalpostMangler.getArkivSak() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.AVSENDERID, journalpostMangler.getAvsenderId() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.AVSENDERNAVN, journalpostMangler.getAvsenderNavn() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.BRUKER, journalpostMangler.getBruker() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.INNHOLD, journalpostMangler.getInnhold() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.TEMA, journalpostMangler.getTema() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.HOVEDOK_KATEGORI, journalpostMangler.getHoveddokument().getDokumentkategori() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.HOVEDOK_TITTEL, journalpostMangler.getHoveddokument().getTittel() == Journalfoeringsbehov.MANGLER);
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.VEDLEGG_KATEGORI, journalpostMangler.getVedleggListe().stream().anyMatch(vedlegg -> vedlegg.getDokumentkategori() == Journalfoeringsbehov.MANGLER));
        mangler.leggTilJournalMangel(JournalPostMangler.JournalMangelType.VEDLEGG_TITTEL, journalpostMangler.getVedleggListe().stream().anyMatch(vedlegg -> vedlegg.getTittel() == Journalfoeringsbehov.MANGLER));
        return mangler;
    }

    public List<JournalMetadata<DokumentTypeId>> konverterTilMetadata(String journalpostId, HentJournalpostResponse response) {
        InngaaendeJournalpost journalpost = response.getInngaaendeJournalpost();

        List<JournalMetadata<DokumentTypeId>> metadataList = new ArrayList<>();

        populerMetadataListe(journalpostId, journalpost, journalpost.getHoveddokument(), true, metadataList);
        if (journalpost.getVedleggListe() != null) {
            for (Dokumentinformasjon dokumentInfo : journalpost.getVedleggListe()) {
                populerMetadataListe(journalpostId, journalpost, dokumentInfo, false, metadataList);
            }
        }
        return metadataList;
    }

    private DokumentInfoVedlegg getDokumentInfoVedlegg(Dokument dokument, Boolean settTittelAnnet) {
        DokumentInfoVedlegg info = new DokumentInfoVedlegg();
        List<DokumentVariant> dokumentVariantList = new ArrayList<>();
        DokumentVariant dokVariant = new DokumentVariant();

        dokVariant.setDokument(dokument.getBase64EncodetDokument());
        dokVariant.setArkivFilType(DokumentVariant.ArkivFilType.fromValue(dokument.getArkivFilType().getKode()));
        dokVariant.setVariantFormat(DokumentVariant.VariantFormat.ARKIV);
        dokumentVariantList.add(dokVariant);

        if (settTittelAnnet && DokumentTypeId.ANNET.equals(dokument.getDokumentTypeId())) {
            info.setTittel(kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.ANNET).getNavn());
        }
        info.setDokumentTypeId(kodeverkRepository.finn(DokumentTypeId.class, dokument.getDokumentTypeId()).getOffisiellKode());
        info.setDokumentVariant(dokumentVariantList);

        return info;
    }

    private void populerMetadataListe(String journalpostId, InngaaendeJournalpost journalpost,
                                      Dokumentinformasjon dokumentinfo, boolean erHoveddokument,
                                      List<JournalMetadata<DokumentTypeId>> metadataList) {
        MottakKanal mottakKanal = getMottakKanal(journalpost);

        Journaltilstand journaltilstandJaxb = journalpost.getJournaltilstand();
        JournalMetadata.Journaltilstand journaltilstand = journaltilstandJaxb != null ? journaltilstandPrjournaltilstandJaxb.get(journaltilstandJaxb) : null;

        LocalDate forsendelseMottatt = DateUtil.convertToLocalDate(journalpost.getForsendelseMottatt());
        LocalDateTime forsendelseMottattTidspunkt = DateUtil.convertToLocalDateTime(journalpost.getForsendelseMottatt());
        Optional<String> kanalReferanseId = Optional.ofNullable(journalpost.getKanalReferanseId());
        Optional<String> journalEnhet = Optional.ofNullable(journalpost.getJournalfEnhet());

        List<no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Aktoer> brukerListe = journalpost.getBrukerListe();

        final String dokumentId = dokumentinfo.getDokumentId();
        final DokumentTypeId dokumentTypeId = getDokumentTypeId(dokumentinfo);
        final DokumentKategori dokumentKategori = getDokumentKategori(dokumentinfo);

        List<String> brukerIdentList = brukerListe.stream().filter((a) -> {
            // instanceof OK - eksternt grensesnitt
            return a instanceof no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Person;  // NOSONAR
        }).map(a -> ((no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Person) a).getIdent()).collect(Collectors.toList());

        for (Dokumentinnhold dokumentinnhold : dokumentinfo.getDokumentInnholdListe()) {
            VariantFormat variantFormat = getVariantFormat(dokumentinnhold);
            ArkivFilType arkivFilType = getArkivFilType(dokumentinnhold);

            JournalMetadata.Builder<DokumentTypeId> builder = JournalMetadata.builder();
            builder.medJournalpostId(journalpostId);
            builder.medDokumentId(dokumentId);
            builder.medVariantFormat(variantFormat);
            builder.medMottakKanal(mottakKanal);
            builder.medDokumentType(dokumentTypeId);
            builder.medDokumentKategori(dokumentKategori);
            builder.medArkivFilType(arkivFilType);
            builder.medJournaltilstand(journaltilstand);
            builder.medErHoveddokument(erHoveddokument);
            builder.medForsendelseMottatt(forsendelseMottatt);
            builder.medForsendelseMottattTidspunkt(forsendelseMottattTidspunkt);
            builder.medBrukerIdentListe(brukerIdentList);
            kanalReferanseId.ifPresent(builder::medKanalReferanseId);
            journalEnhet.ifPresent(builder::medJournalførendeEnhet);
            JournalMetadata<DokumentTypeId> metadata = builder.build();

            metadataList.add(metadata);
        }
    }

    private MottakKanal getMottakKanal(no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.InngaaendeJournalpost journalpost) {
        MottakKanal mottakKanal = null;
        Mottakskanaler mottakskanalJaxb = journalpost.getMottakskanal();
        if (mottakskanalJaxb != null && mottakskanalJaxb.getValue() != null) {
            String offisiellKode = mottakskanalJaxb.getValue();
            mottakKanal = kodeverkRepository.finnForKodeverkEiersKode(MottakKanal.class, offisiellKode);
        }
        return mottakKanal;
    }

    private DokumentTypeId getDokumentTypeId(Dokumentinformasjon dokumentinfo) {
        DokumentTypeId dokumentTypeId = null;
        DokumenttypeIder dokumenttypeJaxb = dokumentinfo.getDokumenttypeId();
        if (dokumenttypeJaxb != null && dokumenttypeJaxb.getValue() != null) {
            final String offisiellKode = dokumenttypeJaxb.getValue();
            dokumentTypeId = kodeverkRepository.finnForKodeverkEiersKode(DokumentTypeId.class, offisiellKode, DokumentTypeId.UDEFINERT);
        }
        return dokumentTypeId;
    }

    private DokumentKategori getDokumentKategori(Dokumentinformasjon dokumentinfo) {
        DokumentKategori dokumentKategori = null;
        Dokumentkategorier dokumentkategoriJaxb = dokumentinfo.getDokumentkategori();
        if (dokumentkategoriJaxb != null && dokumentkategoriJaxb.getValue() != null) {
            String offisiellKode = dokumentkategoriJaxb.getValue();
            dokumentKategori = kodeverkRepository.finnForKodeverkEiersKode(DokumentKategori.class, offisiellKode);
        }
        return dokumentKategori;
    }

    private VariantFormat getVariantFormat(Dokumentinnhold dokumentinnhold) {
        VariantFormat variantFormat = null;
        Variantformater variantformatJaxb = dokumentinnhold.getVariantformat();
        if (variantformatJaxb != null && variantformatJaxb.getValue() != null) {
            String offisiellKode = variantformatJaxb.getValue();
            variantFormat = kodeverkRepository.finnForKodeverkEiersKode(VariantFormat.class, offisiellKode);
        }
        return variantFormat;
    }

    private ArkivFilType getArkivFilType(Dokumentinnhold dokumentinnhold) {
        ArkivFilType arkivFilType = null;
        Arkivfiltyper arkivfiltypeJaxb = dokumentinnhold.getArkivfiltype();
        if (arkivfiltypeJaxb != null && arkivfiltypeJaxb.getValue() != null) {
            String offisiellKode = arkivfiltypeJaxb.getValue();
            arkivFilType = kodeverkRepository.finnForKodeverkEiersKode(ArkivFilType.class, offisiellKode);
        }
        return arkivFilType;
    }

}
