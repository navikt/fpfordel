package no.nav.foreldrepenger.fordel.kodeverdi;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ANNET;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.BEKREFTELSE_FRA_ARBEIDSGIVER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.BEKREFTELSE_FRA_STUDIESTED;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.BEKREFTELSE_VENTET_FØDSELSDATO;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.BESKRIVELSE_FUNKSJONSNEDSETTELSE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOKUMENTASJON_ALENEOMSORG;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOKUMENTASJON_AV_OMSORGSOVERTAKELSE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOKUMENTASJON_FORSVARSTJENESTE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOKUMENTASJON_NAVTILTAK;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOK_FERIE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOK_INNLEGGELSE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOK_MORS_UTDANNING_ARBEID_SYKDOM;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_KLAGE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.FØDSELSATTEST;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSOPPLYSNINGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSOPPLYSNING_SELVSTENDIG;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.KLAGE_DOKUMENT;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.LEGEERKLÆRING;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.TILBAKEKREV_UTTALELSE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.UDEFINERT;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.FORSIDE_SVP_GAMMEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_FLEKSIBELT_UTTAK;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_FORELDREPENGER_ENDRING;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_INNTEKTSOPPLYSNINGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_INNTEKTSOPPLYSNING_SELVSTENDIG;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_KLAGE;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMAE_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_ANNEN_POST;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_FLEKSIBELT_UTTAK;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_FORELDREPENGER_ENDRING;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_INNTEKTSOPPLYSNINGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_INNTEKTSOPPLYSNING_SELVSTENDIG;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_KLAGE_DOKUMENT;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_SVANGERSKAPSPENGER_SN;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_TILRETTELEGGING_B;
import static no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema.SKJEMA_TILRETTELEGGING_N;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

public class MapNAVSkjemaDokumentTypeId {

    public static final int GEN_RANK = 90;
    public static final int UDEF_RANK = 99;

    private static final Map<NAVSkjema, DokumentTypeId> BREVKODE_DOKUMENT_TYPE = Map.ofEntries(
            Map.entry(SKJEMA_SVANGERSKAPSPENGER, SØKNAD_SVANGERSKAPSPENGER),
            Map.entry(SKJEMA_SVANGERSKAPSPENGER_SN, SØKNAD_SVANGERSKAPSPENGER),
            Map.entry(SKJEMA_TILRETTELEGGING_B, SØKNAD_SVANGERSKAPSPENGER),
            Map.entry(SKJEMA_TILRETTELEGGING_N, SØKNAD_SVANGERSKAPSPENGER),
            Map.entry(FORSIDE_SVP_GAMMEL, SØKNAD_SVANGERSKAPSPENGER),
            Map.entry(SKJEMA_FORELDREPENGER_ADOPSJON, SØKNAD_FORELDREPENGER_ADOPSJON),
            Map.entry(SKJEMA_FORELDREPENGER_FØDSEL, SØKNAD_FORELDREPENGER_FØDSEL),
            Map.entry(SKJEMA_ENGANGSSTØNAD_ADOPSJON, SØKNAD_ENGANGSSTØNAD_ADOPSJON),
            Map.entry(SKJEMA_ENGANGSSTØNAD_FØDSEL, SØKNAD_ENGANGSSTØNAD_FØDSEL),
            Map.entry(SKJEMA_FLEKSIBELT_UTTAK, FLEKSIBELT_UTTAK_FORELDREPENGER),
            Map.entry(SKJEMA_FORELDREPENGER_ENDRING, FORELDREPENGER_ENDRING_SØKNAD),
            Map.entry(SKJEMA_KLAGE_DOKUMENT, KLAGE_DOKUMENT),
            Map.entry(SKJEMA_INNTEKTSOPPLYSNING_SELVSTENDIG, INNTEKTSOPPLYSNING_SELVSTENDIG),
            Map.entry(SKJEMA_INNTEKTSOPPLYSNINGER, INNTEKTSOPPLYSNINGER),
            Map.entry(SKJEMAE_INNTEKTSOPPLYSNING_SELVSTENDIG, INNTEKTSOPPLYSNING_SELVSTENDIG),
            Map.entry(SKJEMAE_INNTEKTSOPPLYSNINGER, INNTEKTSOPPLYSNINGER),
            Map.entry(SKJEMA_INNTEKTSMELDING, INNTEKTSMELDING),
            Map.entry(SKJEMA_ANNEN_POST, ANNET),
            Map.entry(SKJEMAE_SVANGERSKAPSPENGER, ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG),
            Map.entry(SKJEMAE_FORELDREPENGER_ADOPSJON, ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON),
            Map.entry(SKJEMAE_FORELDREPENGER_FØDSEL, ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL),
            Map.entry(SKJEMAE_ENGANGSSTØNAD_ADOPSJON, ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON),
            Map.entry(SKJEMAE_ENGANGSSTØNAD_FØDSEL, ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL),
            Map.entry(SKJEMAE_FLEKSIBELT_UTTAK, ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER),
            Map.entry(SKJEMAE_FORELDREPENGER_ENDRING, ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD),
            Map.entry(SKJEMAE_KLAGE, ETTERSENDT_KLAGE));

    private static final Map<DokumentTypeId, NAVSkjema> DOKUMENT_TYPE_BREVKODE = new ImmutableMap.Builder<DokumentTypeId, NAVSkjema>()
            .put(SØKNAD_SVANGERSKAPSPENGER, SKJEMA_SVANGERSKAPSPENGER)
            .put(SØKNAD_FORELDREPENGER_ADOPSJON, SKJEMA_FORELDREPENGER_ADOPSJON)
            .put(SØKNAD_FORELDREPENGER_FØDSEL, SKJEMA_FORELDREPENGER_FØDSEL)
            .put(SØKNAD_ENGANGSSTØNAD_ADOPSJON, SKJEMA_ENGANGSSTØNAD_ADOPSJON)
            .put(SØKNAD_ENGANGSSTØNAD_FØDSEL, SKJEMA_ENGANGSSTØNAD_FØDSEL)
            .put(FLEKSIBELT_UTTAK_FORELDREPENGER,SKJEMA_FLEKSIBELT_UTTAK)
            .put(FORELDREPENGER_ENDRING_SØKNAD,SKJEMA_FORELDREPENGER_ENDRING)
            .put(KLAGE_DOKUMENT,SKJEMA_KLAGE_DOKUMENT)
            .put(INNTEKTSMELDING,SKJEMA_INNTEKTSMELDING)
            .put(ANNET,SKJEMA_ANNEN_POST)
            .put(UDEFINERT,NAVSkjema.UDEFINERT)
            .build();


    private static final BiMap<DokumentTypeId, Integer> DOKUMENT_RANK = new ImmutableBiMap.Builder<DokumentTypeId, Integer>()
            .put(INNTEKTSMELDING, 1)
            .put(SØKNAD_FORELDREPENGER_FØDSEL, 2)
            .put(SØKNAD_ENGANGSSTØNAD_FØDSEL, 3)
            .put(FLEKSIBELT_UTTAK_FORELDREPENGER, 4)
            .put(SØKNAD_SVANGERSKAPSPENGER, 5)
            .put(FORELDREPENGER_ENDRING_SØKNAD, 6)
            .put(SØKNAD_FORELDREPENGER_ADOPSJON, 7)
            .put(SØKNAD_ENGANGSSTØNAD_ADOPSJON, 8)
            .put(KLAGE_DOKUMENT, 9)
            .put(ETTERSENDT_KLAGE, 10)
            .put(TILBAKEKREV_UTTALELSE, 11)
            .put(LEGEERKLÆRING, 20)
            .put(DOK_INNLEGGELSE, 21)
            .put(DOKUMENTASJON_FORSVARSTJENESTE, 22)
            .put(DOKUMENTASJON_NAVTILTAK, 23)
            .put(DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, 30)
            .put(BEKREFTELSE_VENTET_FØDSELSDATO, 31)
            .put(FØDSELSATTEST, 32)
            .put(DOKUMENTASJON_AV_OMSORGSOVERTAKELSE, 33)
            .put(DOKUMENTASJON_ALENEOMSORG, 34)
            .put(DOK_FERIE, 40)
            .put(DOK_MORS_UTDANNING_ARBEID_SYKDOM, 41)
            .put(BESKRIVELSE_FUNKSJONSNEDSETTELSE, 42)
            .put(BEKREFTELSE_FRA_ARBEIDSGIVER, 43)
            .put(BEKREFTELSE_FRA_STUDIESTED, 44)
            .put(ANNET, 98)
            .put(UDEFINERT, UDEF_RANK).build();

    public static DokumentTypeId mapBrevkode(NAVSkjema brevkode) {
        return Optional.ofNullable(brevkode)
                .map(BREVKODE_DOKUMENT_TYPE::get)
                .orElse(DokumentTypeId.UDEFINERT);
    }

    public static NAVSkjema mapDokumentTypeId(DokumentTypeId id) {
        return Optional.ofNullable(id)
                .map(DOKUMENT_TYPE_BREVKODE::get)
                .orElse(NAVSkjema.UDEFINERT);
    }

    public static int dokumentTypeRank(DokumentTypeId id) {
        return Optional.ofNullable(id)
                .map(DOKUMENT_RANK::get)
                .orElse(GEN_RANK);
    }

    public static DokumentTypeId dokumentTypeFromRank(int rank) {
        return DOKUMENT_RANK.inverse().getOrDefault(rank, UDEFINERT);
    }
}
