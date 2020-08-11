package no.nav.foreldrepenger.fordel.web.app.selftest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ExtHealthCheck;

class SelftestsJsonSerializerModule extends Module {

    private static final int VERSION_MAJOR = 1;
    private static final int VERSION_MINOR = 0;
    private static final int VERSION_PATCH_LEVEL = 0;
    private static final Version VERSION = new Version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH_LEVEL, null, null, null);

    private static final String FORMAT_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final String FIELD_APPLICATION = "application";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_REVISION = "revision";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_BUILDTIME = "buildTime";
    private static final String FIELD_AGGREGATE_RESULT = "aggregateResult";
    private static final String FIELD_CHECKS = "checks";
    private static final String FIELD_ENDPOINT = "endpoint";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_RESPONSE_TIME = "responseTime";
    private static final String FIELD_RESULT = "result";
    private static final String FIELD_STACKTRACE = "stacktrace";
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";

    @Override
    public String getModuleName() {
        return "selftest";
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void setupModule(SetupContext setupContext) {
        List<JsonSerializer<?>> serializers = Arrays.asList(
                new OverallResultSerializer(),
                new HealthCheckResultSerializer()
        );
        setupContext.addSerializers(new SimpleSerializers(serializers));
    }

    static class OverallResultSerializer extends StdSerializer<SelftestResultat> {

        OverallResultSerializer() {
            super(SelftestResultat.class);
        }

        @Override
        public void serialize(SelftestResultat samletResultat,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {

            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(FIELD_APPLICATION, samletResultat.getApplication());
            jsonGenerator.writeStringField(FIELD_VERSION, samletResultat.getVersion());
            jsonGenerator.writeStringField(FIELD_REVISION, samletResultat.getRevision());
            jsonGenerator.writeStringField(FIELD_BUILDTIME, samletResultat.getBuildTime());

            Date timestamp = toDate(samletResultat.getTimestamp());
            SimpleDateFormat dateFmt = new SimpleDateFormat(FORMAT_TIMESTAMP);
            String date = dateFmt.format(timestamp);
            jsonGenerator.writeStringField(FIELD_TIMESTAMP, date);
            jsonGenerator.writeNumberField(FIELD_AGGREGATE_RESULT, samletResultat.getAggregateResult().getIntValue());
            jsonGenerator.writeArrayFieldStart(FIELD_CHECKS);
            for (HealthCheck.Result result : samletResultat.getAlleResultater()) {
                jsonGenerator.writeObject(result);
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }

        private Date toDate(LocalDateTime localDateTime) {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            return Date.from(localDateTime.atZone(defaultZoneId).toInstant());
        }
    }

    static class HealthCheckResultSerializer extends StdSerializer<HealthCheck.Result> {

        HealthCheckResultSerializer() {
            super(HealthCheck.Result.class);
        }

        @Override
        public void serialize(HealthCheck.Result result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            Map<String, Object> details = result.getDetails();
            String endpoint = null;
            String description = null;
            String responseTime = null;
            if (details != null) {
                endpoint = (String) details.get(ExtHealthCheck.DETAIL_ENDPOINT);
                description = (String) details.get(ExtHealthCheck.DETAIL_DESCRIPTION);
                responseTime = (String) details.get(ExtHealthCheck.DETAIL_RESPONSE_TIME);
            }

            jsonGenerator.writeStartObject();

            if (endpoint != null) {
                jsonGenerator.writeStringField(FIELD_ENDPOINT, endpoint);
            }
            if (description != null) {
                jsonGenerator.writeStringField(FIELD_DESCRIPTION, description);
            }
            if (responseTime != null) {
                jsonGenerator.writeStringField(FIELD_RESPONSE_TIME, responseTime);
            }

            int intResult = result.isHealthy() ? 0 : 1;
            jsonGenerator.writeNumberField(FIELD_RESULT, intResult);

            if (!result.isHealthy()) {
                if (result.getError() != null) {
                    jsonGenerator.writeStringField(FIELD_STACKTRACE, StackTraceFormatter.format(result.getError()));
                } else if (result.getMessage() != null) {
                    jsonGenerator.writeStringField(FIELD_ERROR_MESSAGE, result.getMessage());
                } else {
                    jsonGenerator.writeStringField(FIELD_ERROR_MESSAGE, "(mangler både message og error)");
                }
            }
            jsonGenerator.writeEndObject();
        }
    }
}
