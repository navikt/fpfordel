package no.nav.foreldrepenger.mottak.journal.saf;

public class SafException extends RuntimeException {
    public SafException(String message) {
        super(message);
    }

    public SafException(String message, Exception cause) {
        super(message, cause);
    }
}
