package no.nav.foreldrepenger.mottak.tjeneste;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;

public class ArkivUtil {

    public static DokumentKategori utledKategoriFraDokumentType(DokumentTypeId doktype) {
        if (DokumentTypeId.erSøknadType(doktype))
            return DokumentKategori.SØKNAD;
        if (DokumentTypeId.erKlageType(doktype))
            return DokumentKategori.KLAGE_ELLER_ANKE;
        return DokumentKategori.IKKE_TOLKBART_SKJEMA;
    }

    public static BehandlingTema behandlingTemaFraDokumentType(BehandlingTema behandlingTema,
                                                               DokumentTypeId dokumentTypeId) {
        if (behandlingTema == null) {
            behandlingTema = BehandlingTema.UDEFINERT;
        }
        if (BehandlingTema.ikkeSpesifikkHendelse(behandlingTema)) {
            if (DokumentTypeId.erForeldrepengerRelatert(dokumentTypeId)) {
                behandlingTema = BehandlingTema.FORELDREPENGER;
                if (DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.equals(dokumentTypeId)) {
                    behandlingTema = BehandlingTema.FORELDREPENGER_FØDSEL;
                } else if (DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON.equals(dokumentTypeId)) {
                    behandlingTema = BehandlingTema.FORELDREPENGER_ADOPSJON;
                }
            } else if (DokumentTypeId.erEngangsstønadRelatert(dokumentTypeId)) {
                behandlingTema = BehandlingTema.ENGANGSSTØNAD;
                if (DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.equals(dokumentTypeId)) {
                    behandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
                } else if (DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON.equals(dokumentTypeId)) {
                    behandlingTema = BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
                }
            } else if (DokumentTypeId.erSvangerskapspengerRelatert(dokumentTypeId)) {
                behandlingTema = BehandlingTema.SVANGERSKAPSPENGER;
            }
        }
        return behandlingTema;
    }

}