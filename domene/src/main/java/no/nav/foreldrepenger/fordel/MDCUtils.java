package no.nav.foreldrepenger.fordel;

import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.getCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.putCallId;

public class MDCUtils {

    private MDCUtils() {
    }

    public static void ensureCallId() {
        var callId = getCallId();
        if (callId == null || callId.isBlank()) {
            putCallId(generateCallId());
        }
    }
}
