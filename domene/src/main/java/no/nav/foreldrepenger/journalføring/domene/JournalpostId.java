package no.nav.foreldrepenger.journalføring.domene;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Journalpostid refererer til journalpost registret i Joark.
 */
public class JournalpostId implements Serializable {
    private static final String CHARS = "a-z0-9_:-";

    private static final Pattern VALID = Pattern.compile("^(-?[1-9]|[a-z0])[" + CHARS + "]*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVALID = Pattern.compile("[^" + CHARS + "]+", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    @JsonValue
    private String journalpostId;  // NOSONAR

    JournalpostId() {
        // for hibernate
    }

    private JournalpostId(String journalpostId) {
        Objects.requireNonNull(journalpostId, "journalpostId");
        if (!VALID.matcher(journalpostId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException(
                "Ugyldig aktørId, støtter kun A-Z/0-9/:/-/_ tegn. Var: " + journalpostId.replaceAll(INVALID.pattern(), "?") + " (vasket)");
        }
        this.journalpostId = journalpostId;
    }

    public static JournalpostId fra(Long journalpostId) {
        return fra(Long.toString(journalpostId));
    }

    public static JournalpostId fra(String journalpostId) {
        return new JournalpostId(journalpostId);
    }

    public static boolean erGyldig(String input) {
        return input != null && !(input = input.trim()).isEmpty() && VALID.matcher(input).matches();  // NOSONAR
    }

    public String getVerdi() {
        return journalpostId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JournalpostId other && getClass().equals(other.getClass()) && Objects.equals(journalpostId, other.journalpostId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(journalpostId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + journalpostId + ">";
    }
}
