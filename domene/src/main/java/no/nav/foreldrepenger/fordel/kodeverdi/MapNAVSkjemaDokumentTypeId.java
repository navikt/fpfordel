package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.Map;

public class MapNAVSkjemaDokumentTypeId {

    private static final Map<NAVSkjema, DokumentTypeId> BREVKODE_DOKUMENT_TYPE = Map.ofEntries(
            Map.entry(NAVSkjema.SKJEMA_SVANGERSKAPSPENGER, DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER),
            Map.entry(NAVSkjema.SKJEMA_FORELDREPENGER_ADOPSJON, DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON),
            Map.entry(NAVSkjema.SKJEMA_FORELDREPENGER_FØDSEL, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL),
            Map.entry(NAVSkjema.SKJEMA_ENGANGSSTØNAD_ADOPSJON, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON),
            Map.entry(NAVSkjema.SKJEMA_ENGANGSSTØNAD_FØDSEL, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL),
            Map.entry(NAVSkjema.SKJEMA_FLEKSIBELT_UTTAK, DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER),
            Map.entry(NAVSkjema.SKJEMA_FORELDREPENGER_ENDRING, DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD),
            Map.entry(NAVSkjema.SKJEMA_KLAGE_DOKUMENT, DokumentTypeId.KLAGE_DOKUMENT),
            Map.entry(NAVSkjema.SKJEMA_INNTEKTSOPPLYSNING_SELVSTENDIG, DokumentTypeId.INNTEKTSOPPLYSNING_SELVSTENDIG),
            Map.entry(NAVSkjema.SKJEMA_INNTEKTSOPPLYSNINGER, DokumentTypeId.INNTEKTSOPPLYSNINGER),
            Map.entry(NAVSkjema.SKJEMA_INNTEKTSMELDING, DokumentTypeId.INNTEKTSMELDING),
            Map.entry(NAVSkjema.SKJEMA_ANNEN_POST, DokumentTypeId.ANNET),
            Map.entry(NAVSkjema.SKJEMAE_SVANGERSKAPSPENGER, DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG),
            Map.entry(NAVSkjema.SKJEMAE_FORELDREPENGER_ADOPSJON, DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON),
            Map.entry(NAVSkjema.SKJEMAE_FORELDREPENGER_FØDSEL, DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL),
            Map.entry(NAVSkjema.SKJEMAE_ENGANGSSTØNAD_ADOPSJON, DokumentTypeId.ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON),
            Map.entry(NAVSkjema.SKJEMAE_ENGANGSSTØNAD_FØDSEL, DokumentTypeId.ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL),
            Map.entry(NAVSkjema.SKJEMAE_FLEKSIBELT_UTTAK, DokumentTypeId.ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER),
            Map.entry(NAVSkjema.SKJEMAE_FORELDREPENGER_ENDRING, DokumentTypeId.ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD),
            Map.entry(NAVSkjema.SKJEMAE_KLAGE, DokumentTypeId.ETTERSENDT_KLAGE)
    );

    private static final Map<DokumentTypeId, Integer> DOKUMENT_TYPE_RANK = Map.ofEntries(
            Map.entry(DokumentTypeId.INNTEKTSMELDING, 1),
            Map.entry(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, 2),
            Map.entry(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, 3),
            Map.entry(DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER, 4),
            Map.entry(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER, 5),
            Map.entry(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, 6),
            Map.entry(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON, 7),
            Map.entry(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON, 8),
            Map.entry(DokumentTypeId.KLAGE_DOKUMENT, 9),
            Map.entry(DokumentTypeId.ETTERSENDT_KLAGE, 10),
            Map.entry(DokumentTypeId.ANNET, 98),
            Map.entry(DokumentTypeId.UDEFINERT, 99)
    );

    private static final Map<Integer, DokumentTypeId> RANK_DOKUMENT_TYPE = Map.ofEntries(
            Map.entry(1, DokumentTypeId.INNTEKTSMELDING),
            Map.entry(2, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL),
            Map.entry(3, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL),
            Map.entry(4, DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER),
            Map.entry(5, DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER),
            Map.entry(6, DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD),
            Map.entry(7, DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON),
            Map.entry(8, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON),
            Map.entry(9, DokumentTypeId.KLAGE_DOKUMENT),
            Map.entry(10, DokumentTypeId.ETTERSENDT_KLAGE),
            Map.entry(98, DokumentTypeId.ANNET),
            Map.entry(99, DokumentTypeId.UDEFINERT)
    );

    public static DokumentTypeId mapBrevkode(NAVSkjema brevkode) {
        if (brevkode == null)
            return DokumentTypeId.UDEFINERT;
        return BREVKODE_DOKUMENT_TYPE.getOrDefault(brevkode, DokumentTypeId.UDEFINERT);
    }

    public static int dokumentTypeRank(DokumentTypeId dokumentTypeId) {
        if (dokumentTypeId == null)
            return 99;
        return DOKUMENT_TYPE_RANK.getOrDefault(dokumentTypeId, 90);
    }

    public static DokumentTypeId dokumentTypeFromRank(int rank) {
        return RANK_DOKUMENT_TYPE.getOrDefault(rank, DokumentTypeId.UDEFINERT);
    }
}
