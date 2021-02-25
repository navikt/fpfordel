package no.nav.foreldrepenger.mottak.tjeneste;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.UDEFINERT;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;

public final class ArkivUtil {

    private static final int UDEF_RANK = 99;

    private static final Map<DokumentTypeId, BehandlingTema> DOKUMENT_BEHANDLING_TEMA = Map.ofEntries(
            Map.entry(SØKNAD_SVANGERSKAPSPENGER, SVANGERSKAPSPENGER),
            Map.entry(SØKNAD_FORELDREPENGER_ADOPSJON, FORELDREPENGER_ADOPSJON),
            Map.entry(SØKNAD_FORELDREPENGER_FØDSEL, FORELDREPENGER_FØDSEL),
            Map.entry(SØKNAD_ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_ADOPSJON),
            Map.entry(SØKNAD_ENGANGSSTØNAD_FØDSEL, ENGANGSSTØNAD_FØDSEL),
            Map.entry(FLEKSIBELT_UTTAK_FORELDREPENGER, FORELDREPENGER),
            Map.entry(FORELDREPENGER_ENDRING_SØKNAD, FORELDREPENGER),
            Map.entry(ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG, SVANGERSKAPSPENGER),
            Map.entry(ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON, FORELDREPENGER_ADOPSJON),
            Map.entry(ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL, FORELDREPENGER_FØDSEL),
            Map.entry(ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_ADOPSJON),
            Map.entry(ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL, ENGANGSSTØNAD_FØDSEL),
            Map.entry(ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER, FORELDREPENGER),
            Map.entry(ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD, FORELDREPENGER));

    private static BiMap<Integer, BehandlingTema> RANK_TEMA = new ImmutableBiMap.Builder<Integer, BehandlingTema>()
            .put(1, FORELDREPENGER_FØDSEL)
            .put(2, ENGANGSSTØNAD_FØDSEL)
            .put(3, FORELDREPENGER_ADOPSJON)
            .put(4, ENGANGSSTØNAD_ADOPSJON)
            .put(5, FORELDREPENGER)
            .put(6, ENGANGSSTØNAD)
            .put(7, SVANGERSKAPSPENGER)
            .put(UDEF_RANK, UDEFINERT)
            .build();

    private ArkivUtil() {
    }

    public static DokumentKategori utledKategoriFraDokumentType(DokumentTypeId doktype) {
        if (DokumentTypeId.erSøknadType(doktype)) {
            return DokumentKategori.SØKNAD;
        }
        if (DokumentTypeId.erKlageType(doktype)) {
            return DokumentKategori.KLAGE_ELLER_ANKE;
        }
        return DokumentKategori.IKKE_TOLKBART_SKJEMA;
    }

    public static BehandlingTema behandlingTemaFraDokumentType(BehandlingTema behandlingTema,
            DokumentTypeId dokumentTypeId) {
        int btRank = behandlingstemaRank(behandlingTema);
        int dtRank = behandlingstemaRank(mapDokumenttype(dokumentTypeId));

        return behandlingstemaFromRank(Math.min(btRank, dtRank));
    }

    public static BehandlingTema behandlingTemaFraDokumentTypeSet(BehandlingTema behandlingTema,
            Collection<DokumentTypeId> typer) {
        int btRank = behandlingstemaRank(behandlingTema);
        int dtRank = typer.stream()
                .map(ArkivUtil::mapDokumenttype)
                .map(ArkivUtil::behandlingstemaRank)
                .min(Comparator.naturalOrder())
                .orElseGet(() -> behandlingstemaRank(null));

        return behandlingstemaFromRank(Math.min(btRank, dtRank));
    }

    private static BehandlingTema mapDokumenttype(DokumentTypeId type) {
        return Optional.ofNullable(type)
                .map(DOKUMENT_BEHANDLING_TEMA::get)
                .filter(Objects::nonNull)
                .orElse(UDEFINERT);
    }

    private static int behandlingstemaRank(BehandlingTema bt) {
        return Optional.ofNullable(bt)
                .map(RANK_TEMA.inverse()::get)
                .filter(Objects::nonNull)
                .orElse(UDEF_RANK);
    }

    private static BehandlingTema behandlingstemaFromRank(int rank) {
        return RANK_TEMA.getOrDefault(rank, UDEFINERT);
    }

}