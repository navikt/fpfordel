package no.nav.foreldrepenger.mottak.tjeneste;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAmount;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.infotrygd.RelevantSakSjekker;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;

@Dependent
public class VurderInfotrygd {

    /*
     * Konfigurasjon for hvor langt tilbake i tid man spør Infotrygd om det finnes
     * saker
     */
    private static final TemporalAmount INFOTRYGD_SAK_GYLDIG_PERIODE = Period.ofMonths(10);
    private static final TemporalAmount INFOTRYGD_ANNENPART_GYLDIG_PERIODE = Period.ofMonths(18);

    private static final Logger LOGGER = LoggerFactory.getLogger(VurderInfotrygd.class);

    private final PersonInformasjon aktør;
    private final RelevantSakSjekker relevansSjekker;

    @Inject
    public VurderInfotrygd(RelevantSakSjekker relevansSjekker,
                           PersonInformasjon aktør) {
        this.relevansSjekker = relevansSjekker;
        this.aktør = aktør;
    }

    public boolean kreverManuellVurdering(MottakMeldingDataWrapper w) {
        if (gjelderIM(w)) {
            return manuellVurderingForIM(w);
        }
        if (gjelderEngangsstønad(w) || gjelderSvangerskapspenger(w)) {
            return false;
        }
        if (gjelderForeldrepenger(w)) {
            return manuellVurderingForForeldrepenger(w);
        }
        throw new TekniskException("FP-785833",
        String.format("Ukjent behandlingstema {%s}", w.getBehandlingTema().getKode()));
    }

    private boolean manuellVurderingForForeldrepenger(MottakMeldingDataWrapper w) {
        // Her sjekker vi annen part - unngå at løpende fedrekvoter gir manuell journalføring
        if (w.getAnnenPartId().isEmpty()) {
            return false;
        }
        String annenPart = fnrAnnenPart(w);
        if (erMann(annenPart)) {
            return false;
        }
        if (relevansSjekker.skalMidlertidigJournalføre(annenPart, LocalDate.now().minus(INFOTRYGD_ANNENPART_GYLDIG_PERIODE))) {
            LOGGER.info("FPFORDEL VINFOTRYGD fp journalpost {}", w.getArkivId());
            return true;
        }
        return false;
    }

    private static boolean gjelderSvangerskapspenger(MottakMeldingDataWrapper w) {
        return BehandlingTema.gjelderSvangerskapspenger(w.getBehandlingTema());
    }

    private static boolean gjelderForeldrepenger(MottakMeldingDataWrapper w) {
        return BehandlingTema.gjelderForeldrepenger(w.getBehandlingTema());
    }

    private static boolean gjelderEngangsstønad(MottakMeldingDataWrapper w) {
        return BehandlingTema.gjelderEngangsstønad(w.getBehandlingTema());
    }

    private String fnr(MottakMeldingDataWrapper w) {
        return w.getAktørId().flatMap(aktør::hentPersonIdentForAktørId)
                .orElseThrow(() -> new TekniskException("FP-254631",
                String.format("Fant ikke personident for aktørId i task %s.  TaskId: %s", "VurderInfotrygd", w.getId())));
    }

    private String fnrAnnenPart(MottakMeldingDataWrapper w) {
        return w.getAnnenPartId().flatMap(aktør::hentPersonIdentForAktørId)
                .orElseThrow(() -> new TekniskException("FP-254631",
                String.format("Fant ikke personident for aktørId i task %s.  TaskId: %s", "VurderInfotrygd", w.getId())));
    }

    private static boolean erMann(String fnr) {
        return (Character.digit(fnr.charAt(8), 10) % 2) != 0;
    }

    private static boolean gjelderIM(MottakMeldingDataWrapper wrapper) {
        return DokumentTypeId.INNTEKTSMELDING.equals(wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT));
    }

    private boolean manuellVurderingForIM(MottakMeldingDataWrapper w) {
        String fnr = fnr(w);
        if (!erMann(fnr) || !gjelderForeldrepenger(w)) {
            return false;
        }
        if (relevansSjekker.skalMidlertidigJournalføreIM(fnr, LocalDate.now().minus(INFOTRYGD_SAK_GYLDIG_PERIODE))) {
            LOGGER.info("FPFORDEL VINFOTRYGD IM journalpost {}", w.getArkivId());
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[infotrygdSakGyldigPeriode=" + INFOTRYGD_SAK_GYLDIG_PERIODE
                + ", infotrygdAnnenPartGyldigPeriode=" + INFOTRYGD_ANNENPART_GYLDIG_PERIODE + ", aktør=" + aktør
                + ", relevansSjekker=" + relevansSjekker + "]";
    }

}
