package no.nav.foreldrepenger.journalføring.oppgave.lager;

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
public class AktørId implements Serializable {
    private static final long serialVersionUID = 1905122041950223207L;

    private static final String VALID_REGEXP = "^\\d{13}$";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);

    @Column(name = "BRUKER_ID", length = 19)
    private String id;

    protected AktørId() {
        // for hibernate
    }

    public AktørId(Long aktørId) {
        this(aktørId.toString());
    }

    public AktørId(String aktørId) {
        this.id = validateBrukerId(aktørId);
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
        return id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + maskerAktørId() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AktørId other && getClass().equals(other.getClass()) && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static boolean erGyldigBrukerId(String aktørId) {
        return aktørId != null && VALID.matcher(aktørId).matches();
    }

    private String maskerAktørId() {
        if (id == null) {
            return "";
        }
        var length = id.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 4) + id.substring(length - 4);
    }
}
