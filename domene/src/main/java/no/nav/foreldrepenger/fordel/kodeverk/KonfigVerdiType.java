package no.nav.foreldrepenger.fordel.kodeverk;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Period;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Type håndtering av konfigurerbare verdier.
 */
@Entity(name = "KonfigVerdiType")
@DiscriminatorValue(KonfigVerdiType.DISCRIMINATOR)
public class KonfigVerdiType extends Kodeliste {

    public static final String DISCRIMINATOR = "KONFIG_VERDI_TYPE";

    private static final Map<String, Predicate<String>> VALIDER_FUNKSJONER = new ConcurrentHashMap<>();

    public static final KonfigVerdiType BOOLEAN_TYPE = internOpprett("BOOLEAN", KonfigVerdiType::validerBoolean); //$NON-NLS-1$
    public static final KonfigVerdiType INTEGER_TYPE = internOpprett("INTEGER", KonfigVerdiType::validerInteger); //$NON-NLS-1$
    public static final KonfigVerdiType STRING_TYPE = internOpprett("STRING", KonfigVerdiType::validerString); //$NON-NLS-1$
    public static final KonfigVerdiType URI_TYPE = internOpprett("URI", KonfigVerdiType::validerUri); //$NON-NLS-1$
    public static final KonfigVerdiType PERIOD_TYPE = internOpprett("PERIOD", KonfigVerdiType::validerPeriod); //$NON-NLS-1$
    public static final KonfigVerdiType DURATION_TYPE = internOpprett("DURATION", KonfigVerdiType::validerDuration); //$NON-NLS-1$

    private KonfigVerdiType() {
    }

    private KonfigVerdiType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    private static KonfigVerdiType internOpprett(String kode, Predicate<String> testFunksjon) {
        VALIDER_FUNKSJONER.putIfAbsent(kode, testFunksjon);
        return new KonfigVerdiType(kode);

    }

    public boolean erGyldigFormat(String verdi) {
        try {
            return valider(verdi);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean valider(String verdi) {
        return Optional.ofNullable(verdi)
                .map(v -> VALIDER_FUNKSJONER.get(getKode()).test(v))
                .orElse(true);
    }

    static boolean validerString(String str) {
        return true;
    }

    static boolean validerBoolean(String str) {
        return "J".equals(str) || "N".equals(str);
        // booleans!
    }

    static boolean validerInteger(String str) {
        Integer.parseInt(str);
        return true;
    }

    static boolean validerUri(String str) {
        try {
            new URI(str); // NOSONAR
            return true;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Ugyldig URI for verdi:" + str, e); //$NON-NLS-1$
        }
    }

    static boolean validerPeriod(String str) {
        Period.parse(str); // NOSONAR validerer som side-effect
        return true;
    }

    static boolean validerDuration(String str) {
        Duration.parse(str); // NOSONAR validerer som side-effect
        return true;
    }

}
