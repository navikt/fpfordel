package no.nav.foreldrepenger.mottak.tjeneste;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori.KLAGE_ELLER_ANKE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.KLAGE_DOKUMENT;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.UDEFINERT;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.erFørsteSøknadType;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.FagsakTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.konfig.Tid;

/**
 * Tjeneste som henter ut informasjon fra søknadsskjema og vurderer denne i
 * henhold til følgende kriterier.
 *
 * - HVIS aktørID og behandlingstema er likt - Fødselsdato innen intervall -16 -
 * +4 uker fra termin - Fødselsdato matcher innen et visst slingringsmonn -
 * Omsorgsovertagelsesdato matcher innen et slingringsmonn OG fødselsdato for
 * barn matcher eksakt
 *
 * For ustrukturerte forsendelser gjelder andre regler; en sak er "passende"
 * HVIS aktørID er lik, OG saken er åpen.
 *
 * Hvis det ikke finnes noen åpen sak så kan "passende sak" være en avsluttet
 * sak som er nyere enn 3 måneder.
 */

@Dependent
public class DestinasjonsRuter {

    private final FagsakTjeneste fagsakRestKlient;
    private final VurderInfotrygd vurderInfotrygd;

    @Inject
    public DestinasjonsRuter(VurderInfotrygd vurderInfotrygd,
            FagsakTjeneste fagsakRestKlient) {
        this.vurderInfotrygd = vurderInfotrygd;
        this.fagsakRestKlient = fagsakRestKlient;
    }

    public Destinasjon bestemDestinasjon(MottakMeldingDataWrapper w) {

        var res = fagsakRestKlient.vurderFagsystem(w);

        res.getSaksnummer().ifPresent(w::setSaksnummer);
        if (res.isBehandlesIVedtaksløsningen() && res.getSaksnummer().isPresent()) {
            return new Destinasjon(ForsendelseStatus.FPSAK, res.getSaksnummer().orElseThrow());
        }
        if (skalBehandlesEtterTidligereRegler(w)) {
            return Destinasjon.GOSYS;
        }
        if (res.isBehandlesIVedtaksløsningen()) {
            return Destinasjon.FPSAK_UTEN_SAK;
        }
        if (res.isSjekkMotInfotrygd()) {
            return sjekkInfotrygd(w);
        }
        if (res.isManuellVurdering()) {
            return Destinasjon.GOSYS;
        }
        throw new IllegalStateException("Utviklerfeil"); // fix korrekt feilhåndtering

    }

    private Destinasjon sjekkInfotrygd(MottakMeldingDataWrapper w) {
        return vurderInfotrygd.kreverManuellVurdering(w) ? Destinasjon.GOSYS : Destinasjon.FPSAK_UTEN_SAK;
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
        if (!erFørsteSøknadType(dokumenttype) && !INNTEKTSMELDING.equals(dokumenttype)) {
            throw new IllegalArgumentException("Kan ikke opprette sak for dokument");
        }
        var saksnummerDto = fagsakRestKlient.opprettSak(new OpprettSakDto(w.getArkivId(),
                w.getBehandlingTema().getOffisiellKode(), w.getAktørId().orElseThrow()));
        w.setSaksnummer(saksnummerDto.getSaksnummer());
        return saksnummerDto.getSaksnummer();
    }

    public boolean kanOppretteSak(MottakMeldingDataWrapper w) {
        return !erKlageEllerAnke(w);
    }

    private static boolean erKlageEllerAnke(MottakMeldingDataWrapper data) {
        return (KLAGE_DOKUMENT.equals(data.getDokumentTypeId().orElse(UDEFINERT))
                || KLAGE_ELLER_ANKE.equals(data.getDokumentKategori().orElse(DokumentKategori.UDEFINERT)));
    }
}
