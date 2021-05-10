package no.nav.foreldrepenger.mottak.felles;

import no.nav.vedtak.log.mdc.MDCOperations;

public class LogSettings {

    public static String ensureCallId() {
        var callId = MDCOperations.getCallId();
        if (callId == null || callId.isBlank()) {
            callId = MDCOperations.generateCallId();
            MDCOperations.putCallId(callId);
        }
        return callId;
    }

}
