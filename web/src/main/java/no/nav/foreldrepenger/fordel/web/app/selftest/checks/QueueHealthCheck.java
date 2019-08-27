package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;

import no.nav.vedtak.felles.integrasjon.jms.QueueSelftest;
import no.nav.vedtak.felles.integrasjon.jms.pausing.MQExceptionUtil;

public abstract class QueueHealthCheck extends ExtHealthCheck {

    protected QueueSelftest client;

    protected QueueHealthCheck() {
        // for CDI proxy
    }

    protected QueueHealthCheck(QueueSelftest client) {
        this.client = client;
    }

    @Override
    protected String getDescription() {
        return "Test av meldingskø for " + getDescriptionSuffix();
    }

    protected abstract String getDescriptionSuffix();

    @Override
    protected String getEndpoint() {
        String endpoint;
        try {
            endpoint = client.getConnectionEndpoint();
        } catch (Exception e) { // NOSONAR
            endpoint = "Uventet feil: " + e.getMessage();
        }
        return endpoint;
    }

    @Override
    protected InternalResult performCheck() {
        InternalResult intTestRes = new InternalResult();

        try {
            client.testConnection();
            intTestRes.setOk(true);
        } catch (JMSRuntimeException e) { // NOSONAR
            intTestRes.setMessage(MQExceptionUtil.extract(e));
        } catch (JMSException e) { // NOSONAR
            intTestRes.setMessage(MQExceptionUtil.extract(e));
        } catch (Exception e) {
            intTestRes.setException(e);
        }

        intTestRes.noteResponseTime();
        return intTestRes;
    }
}
