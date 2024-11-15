package no.nav.foreldrepenger.journalføring.utils;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;

import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.ES;
import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.FP;
import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.SVP;

public class YtelseTypeUtils {
    private YtelseTypeUtils() {
        // static
    }

    public static YtelseType mapTilYtelseType(BehandlingTema behandlingstema) {
        if (behandlingstema == null) {
            return null;
        }
        return switch (behandlingstema) {
            case FORELDREPENGER, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FØDSEL -> FP;
            case SVANGERSKAPSPENGER -> SVP;
            case ENGANGSSTØNAD, ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_FØDSEL -> ES;
            default -> null;
        };
    }
}
