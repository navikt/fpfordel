package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import com.codahale.metrics.health.HealthCheck;

/**
 * HealthCheck utvidet til å produsere resultater med ekstra info som kreves av Auras selftest.
 */
public abstract class ExtHealthCheck extends HealthCheck implements SelftestHealthCheck {

    public static final String DETAIL_DESCRIPTION = "description";
    public static final String DETAIL_ENDPOINT = "endpoint";
    public static final String DETAIL_RESPONSE_TIME = "responseTime";

    protected abstract String getDescription();

    protected abstract String getEndpoint();

    protected abstract InternalResult performCheck();

    @Override
    protected Result check() {
        InternalResult intTestRes = performCheck();
        HealthCheck.ResultBuilder builder = getResultBuilder(intTestRes);
        if (intTestRes.isOk()) {
            builder.healthy();
        } else {
            if (intTestRes.getException() != null) {
                builder.unhealthy(intTestRes.getException());
            } else {
                if (intTestRes.getMessage() != null) {
                    builder.withMessage(intTestRes.getMessage());
                }
                builder.unhealthy();
            }
        }

        return builder.build();
    }

    protected ResultBuilder getResultBuilder(InternalResult internalResult) {
        ResultBuilder builder = HealthCheck.Result.builder();
        builder.withDetail(DETAIL_DESCRIPTION, getDescription());
        builder.withDetail(DETAIL_ENDPOINT, getEndpoint());
        Long respTime = internalResult.getResponseTimeMs();
        if (respTime != null) {
            builder.withDetail(DETAIL_RESPONSE_TIME, String.format("%dms", respTime));
        }
        return builder;
    }

    public boolean erKritiskTjeneste() {
        return true;
    }

    protected static class InternalResult {
        private boolean ok = false;
        private Exception exception = null;
        private String message = null;
        private long startTimeMs = System.currentTimeMillis();
        private Long responseTimeMs = null;

        public boolean isOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(CharSequence cs) {
            setMessage(cs == null ? null : cs.toString());
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void noteResponseTime() {
            long stopTimeMs = System.currentTimeMillis();
            responseTimeMs = stopTimeMs - startTimeMs;
        }

        public Long getResponseTimeMs() {
            return responseTimeMs;
        }
    }
}
