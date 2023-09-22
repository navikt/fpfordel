package no.nav.foreldrepenger.domene;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Id som genereres fra NAV Aktør Register. Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra
 * DNR til FNR i Folkeregisteret. Tilsvarende vil den kunne referere personer som har ident fra et utenlandsk system.
 */
@Embeddable
public class BrukerId {
    private static final String VALID_REGEXP = "^\\d{13}$";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);

    @Column(name = "BRUKER_ID", length = 19)
    private String brukerId;

    protected BrukerId() {
        // for hibernate
    }

    public BrukerId(Long aktørId) {
        this(aktørId.toString());
    }

    public BrukerId(String aktørId) {
        this.brukerId = validateBrukerId(aktørId);
    }

    private String validateBrukerId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        if (!VALID.matcher(aktørId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig aktørId '" + aktørId + "', tillatt pattern: " + VALID_REGEXP);
        }
        return aktørId;
    }

    public String getId() {
        return brukerId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + maskerAktørId() + ">";
    }

    public static boolean erGyldigBrukerId(String aktørId) {
        return aktørId != null && VALID.matcher(aktørId).matches();
    }

    private String maskerAktørId() {
        if (brukerId == null) {
            return "";
        }
        var length = brukerId.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 4) + brukerId.substring(length - 4);
    }
}
