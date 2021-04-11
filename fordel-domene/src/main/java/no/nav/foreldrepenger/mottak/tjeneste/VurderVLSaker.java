package no.nav.foreldrepenger.mottak.tjeneste;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.FagsakTjeneste;
import no.nav.foreldrepenger.mottak.klient.VurderFagsystemResultat;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.konfig.Tid;

/**
 * Tjeneste som henter ut informasjon fra søknadsskjema og vurderer denne i
 * henhold til følgende kriterier.
 *
 * - HVIS aktørID og behandlingstema er likt
 * - Fødselsdato innen intervall -16 - +4 uker fra termin
 * - Fødselsdato matcher innen et visst slingringsmonn
 * - Omsorgsovertagelsesdato matcher innen et slingringsmonn OG fødselsdato for barn matcher eksakt
 *
 * For ustrukturerte forsendelser gjelder andre regler; en sak er "passende" HVIS aktørID er lik, OG saken er åpen.
 *
 * Hvis det ikke finnes noen åpen sak så kan "passende sak" være en avsluttet sak som er nyere enn 3 måneder.
 */

@Dependent
public class VurderVLSaker {


    private final FagsakTjeneste fagsakRestKlient;
    private final VurderInfotrygd vurderInfotrygd;

    @Inject
    public VurderVLSaker(VurderInfotrygd vurderInfotrygd,
            /* @Jersey */FagsakTjeneste fagsakRestKlient) {
        this.vurderInfotrygd = vurderInfotrygd;
        this.fagsakRestKlient = fagsakRestKlient;
    }

    public boolean erVLsak(String saksnummer) {
        return fagsakRestKlient.finnFagsakInfomasjon(new SaksnummerDto(saksnummer)).isPresent();
    }

    public Destinasjon bestemDestinasjon(MottakMeldingDataWrapper dataWrapper) {

        VurderFagsystemResultat res = fagsakRestKlient.vurderFagsystem(dataWrapper);

        res.getSaksnummer().ifPresent(dataWrapper::setSaksnummer);
        if (res.isBehandlesIVedtaksløsningen() && res.getSaksnummer().isPresent()) {
            return new Destinasjon(ForsendelseStatus.FPSAK, res.getSaksnummer().orElseThrow());
        } else if (skalBehandlesEtterTidligereRegler(dataWrapper)) {
            return Destinasjon.GOSYS;
        } else if (res.isBehandlesIVedtaksløsningen()) {
            return Destinasjon.FPSAK_UTEN_SAK;
        } else if (res.isSjekkMotInfotrygd()) {
            return sjekkInfotrygd(dataWrapper);
        } else if (res.isManuellVurdering()) {
            return Destinasjon.GOSYS;
        } else {
            throw new IllegalStateException("Utviklerfeil"); // fix korrekt feilhåndtering
        }
    }

    private Destinasjon sjekkInfotrygd(MottakMeldingDataWrapper w) {
        return vurderInfotrygd.kreverManuellVurdering(w) ?
                Destinasjon.GOSYS : Destinasjon.FPSAK_UTEN_SAK;
    }

    private static boolean skalBehandlesEtterTidligereRegler(MottakMeldingDataWrapper dataWrapper) {
        return tidligsteRelevanteDato(dataWrapper).isBefore(KonfigVerdier.ENDRING_BEREGNING_DATO);
    }

    private static LocalDate tidligsteRelevanteDato(MottakMeldingDataWrapper w) {
        return Stream.of(w.getOmsorgsovertakelsedato(), w.getFørsteUttaksdag(),
                w.getBarnFodselsdato(), w.getBarnTermindato())
                .flatMap(Optional::stream)
                .min(Comparator.naturalOrder()).orElse(Tid.TIDENES_ENDE);
    }

    public String opprettSak(MottakMeldingDataWrapper w) {
        var dokumenttype = w.getDokumentTypeId().orElseThrow();
        if (!DokumentTypeId.erFørsteSøknadType(dokumenttype) && !DokumentTypeId.INNTEKTSMELDING.equals(dokumenttype)) {
            throw new IllegalArgumentException("Kan ikke opprette sak for dokument");
        }
        var saksnummerDto = fagsakRestKlient.opprettSak(new OpprettSakDto(w.getArkivId(),
                w.getBehandlingTema().getOffisiellKode(), w.getAktørId().orElseThrow()));
        w.setSaksnummer(saksnummerDto.getSaksnummer());
        return saksnummerDto.getSaksnummer();
    }

    public boolean kanOppretteSak(MottakMeldingDataWrapper w) {
        // Fyll på med tilfelle som ikke skal opprette sak automatisk
        if (erKlageEllerAnke(w)) {
            return false;
        }
        return true;
    }

    private static boolean erKlageEllerAnke(MottakMeldingDataWrapper data) {
        return (DokumentTypeId.KLAGE_DOKUMENT.equals(data.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))
                || DokumentKategori.KLAGE_ELLER_ANKE
                .equals(data.getDokumentKategori().orElse(DokumentKategori.UDEFINERT)));
    }

}
