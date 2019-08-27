package no.nav.foreldrepenger.mottak.journal.dokumentforsendelse;

import java.util.HashMap;
import java.util.Map;

public enum JournalTilstand {
    ENDELIG_JOURNALFØRT("ENDELIG_JOURNALFOERT"),
    MIDLERTIDIG_JOURNALFØRT("MIDLERTIDIG_JOURNALFOERT");

    private static final Map<String, JournalTilstand> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (JournalTilstand c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JournalTilstand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static JournalTilstand fromValue(String value) {
        JournalTilstand constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
}
