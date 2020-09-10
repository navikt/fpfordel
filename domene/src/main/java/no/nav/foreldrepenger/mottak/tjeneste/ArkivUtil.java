package no.nav.foreldrepenger.mottak.tjeneste;

import java.util.Collection;
import java.util.Comparator;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.MapBehandlingstemaDokumentTypeId;

public final class ArkivUtil {

    private ArkivUtil() {
        // NOSONAR
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
        int btRank = MapBehandlingstemaDokumentTypeId.behandlingstemaRank(behandlingTema);
        int dtRank = MapBehandlingstemaDokumentTypeId.behandlingstemaRank(MapBehandlingstemaDokumentTypeId.mapDokumenttype(dokumentTypeId));

        return MapBehandlingstemaDokumentTypeId.behandlingstemaFromRank(Math.min(btRank, dtRank));
    }

    public static BehandlingTema behandlingTemaFraDokumentTypeSet(BehandlingTema behandlingTema,
            Collection<DokumentTypeId> typer) {
        int btRank = MapBehandlingstemaDokumentTypeId.behandlingstemaRank(behandlingTema);
        int dtRank = typer.stream()
                .map(MapBehandlingstemaDokumentTypeId::mapDokumenttype)
                .map(MapBehandlingstemaDokumentTypeId::behandlingstemaRank)
                .min(Comparator.naturalOrder())
                .orElse(MapBehandlingstemaDokumentTypeId.behandlingstemaRank(null));

        return MapBehandlingstemaDokumentTypeId.behandlingstemaFromRank(Math.min(btRank, dtRank));
    }

}