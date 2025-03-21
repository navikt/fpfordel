package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EnhetsTjeneste {
    public static final String NK_ENHET_ID = "4292"; // Enhetsnummer NAV Klageinstans Midt-Norge
    public static final String SKJERMET_ENHET_ID = "4883"; // Enhetsnummer NAV Familie og Pensjon Skjermet
    public static final String SF_ENHET_ID = "2103"; // Enhetsnummer NAV K6 enhet
    public static final Set<String> SKJERMINGENHETER = Set.of(SKJERMET_ENHET_ID, SF_ENHET_ID);
    public static final String UTLAND_ENHET_ID = "4806"; // Enhetsnummer NAV Utland enhet
    private static final Set<String> SPESIALENHETER = Set.of(NK_ENHET_ID, SKJERMET_ENHET_ID, SF_ENHET_ID, UTLAND_ENHET_ID);
    private static final String NASJONAL_ENHET_ID = "4867";

    private RutingKlient rutingKlient;

    public EnhetsTjeneste() {
    }

    @Inject
    public EnhetsTjeneste(RutingKlient rutingKlient) {
        this.rutingKlient = rutingKlient;
    }

    public String hentFordelingEnhetId(Optional<String> enhetInput, String aktørId) {
        return enhetInput.filter(SPESIALENHETER::contains) // Scanning med angitt enhet
            .or(() -> Optional.ofNullable(aktørId).map(this::hentEnhetId)) // Basert på bruker
            .orElse(NASJONAL_ENHET_ID);
    }

    public static String enhetEllerNasjonalEnhet(String enhet) {
        return enhet != null && SPESIALENHETER.contains(enhet) ? enhet : NASJONAL_ENHET_ID;
    }

    private String hentEnhetId(String aktørId) {
        var rutingResultater = rutingKlient.finnRutingEgenskaper(Set.of(aktørId));

        if (rutingResultater.contains(RutingResultat.STRENGTFORTROLIG)) {
            return SF_ENHET_ID;
        }

        if (rutingResultater.contains(RutingResultat.SKJERMING)) {
            return SKJERMET_ENHET_ID;
        }

        if (rutingResultater.contains(RutingResultat.UTLAND)) {
            return UTLAND_ENHET_ID;
        }

        return NASJONAL_ENHET_ID;
    }
}
