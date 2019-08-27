package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.xml.ws.WebServiceException;

public abstract class WebServiceHealthCheck extends ExtHealthCheck {

    protected WebServiceHealthCheck() {
        super();
    }
    
    protected abstract void performWebServiceSelftest();

    @Override
    protected InternalResult performCheck() {

        InternalResult intTestRes = new InternalResult();

        try {
            performWebServiceSelftest();

        } catch (WebServiceException e) {
            Throwable cause;
            if (e.getCause() != null) {
                cause = e.getCause();
            } else {
                cause = e;
            }
            intTestRes.setMessage(cause.getMessage());
            return intTestRes;
        } catch (Exception e) {
            // Uventet/annen exception: ikke set message - vi ønsker stack trace i JSON-svar til Auras klient
            intTestRes.setException(e);
            return intTestRes;
        }

        intTestRes.noteResponseTime();
        intTestRes.setOk(true);
        return intTestRes;
    }
}
