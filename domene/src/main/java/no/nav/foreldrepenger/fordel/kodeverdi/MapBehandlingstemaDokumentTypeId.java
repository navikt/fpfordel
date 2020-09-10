package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.Map;

public class MapBehandlingstemaDokumentTypeId {

    public static final int UDEF_RANK = 99;

    private static final Map<DokumentTypeId, BehandlingTema> DOKUMENT_BEHANDLING_TEMA = Map.ofEntries(
            Map.entry(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER, BehandlingTema.SVANGERSKAPSPENGER),
            Map.entry(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON, BehandlingTema.FORELDREPENGER_ADOPSJON),
            Map.entry(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, BehandlingTema.FORELDREPENGER_FØDSEL),
            Map.entry(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON, BehandlingTema.ENGANGSSTØNAD_ADOPSJON),
            Map.entry(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, BehandlingTema.ENGANGSSTØNAD_FØDSEL),
            Map.entry(DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER, BehandlingTema.FORELDREPENGER),
            Map.entry(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, BehandlingTema.FORELDREPENGER),
            Map.entry(DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG, BehandlingTema.SVANGERSKAPSPENGER),
            Map.entry(DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON, BehandlingTema.FORELDREPENGER_ADOPSJON),
            Map.entry(DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL, BehandlingTema.FORELDREPENGER_FØDSEL),
            Map.entry(DokumentTypeId.ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON, BehandlingTema.ENGANGSSTØNAD_ADOPSJON),
            Map.entry(DokumentTypeId.ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL, BehandlingTema.ENGANGSSTØNAD_FØDSEL),
            Map.entry(DokumentTypeId.ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER, BehandlingTema.FORELDREPENGER),
            Map.entry(DokumentTypeId.ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD, BehandlingTema.FORELDREPENGER));

    private static final Map<BehandlingTema, Integer> BTEMA_RANK = Map.ofEntries(
            Map.entry(BehandlingTema.FORELDREPENGER_FØDSEL, 1),
            Map.entry(BehandlingTema.ENGANGSSTØNAD_FØDSEL, 2),
            Map.entry(BehandlingTema.FORELDREPENGER_ADOPSJON, 3),
            Map.entry(BehandlingTema.ENGANGSSTØNAD_ADOPSJON, 4),
            Map.entry(BehandlingTema.FORELDREPENGER, 5),
            Map.entry(BehandlingTema.ENGANGSSTØNAD, 6),
            Map.entry(BehandlingTema.SVANGERSKAPSPENGER, 7),
            Map.entry(BehandlingTema.UDEFINERT, UDEF_RANK));

    private static final Map<Integer, BehandlingTema> RANK_BTEMA = Map.ofEntries(
            Map.entry(1, BehandlingTema.FORELDREPENGER_FØDSEL),
            Map.entry(2, BehandlingTema.ENGANGSSTØNAD_FØDSEL),
            Map.entry(3, BehandlingTema.FORELDREPENGER_ADOPSJON),
            Map.entry(4, BehandlingTema.ENGANGSSTØNAD_ADOPSJON),
            Map.entry(5, BehandlingTema.FORELDREPENGER),
            Map.entry(6, BehandlingTema.ENGANGSSTØNAD),
            Map.entry(7, BehandlingTema.SVANGERSKAPSPENGER),
            Map.entry(UDEF_RANK, BehandlingTema.UDEFINERT));

    public static BehandlingTema mapDokumenttype(DokumentTypeId type) {
        if (type == null) {
            return BehandlingTema.UDEFINERT;
        }
        return DOKUMENT_BEHANDLING_TEMA.getOrDefault(type, BehandlingTema.UDEFINERT);
    }

    public static int behandlingstemaRank(BehandlingTema bt) {
        if (bt == null) {
            return BTEMA_RANK.get(BehandlingTema.UDEFINERT);
        }
        return BTEMA_RANK.getOrDefault(bt, UDEF_RANK);
    }

    public static BehandlingTema behandlingstemaFromRank(int rank) {
        return RANK_BTEMA.getOrDefault(rank, BehandlingTema.UDEFINERT);
    }
}
