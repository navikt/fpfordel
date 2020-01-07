package no.nav.foreldrepenger.fordel.web.app.selftest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ExtHealthCheck;

@RequestScoped
public class SelftestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelftestService.class);
    private static final String RESPONSE_CACHE_KEY = "Cache-Control";
    private static final String RESPONSE_CACHE_VAL = "must-revalidate,no-cache,no-store";

    private Selftests selftests;
    private ObjectMapper mapper;
    private SelftestsHtmlFormatter htmlFormatter;

    public SelftestService() {
        // CDI
    }

    @Inject
    public SelftestService(Selftests selftests) {
        this.selftests = selftests;
        this.mapper = new ObjectMapper().registerModule(new SelftestsJsonSerializerModule());
        this.htmlFormatter = new SelftestsHtmlFormatter();
    }

    public Response doSelftest(String contentType, Boolean writeJsonAsHtml) {
        SelftestResultat samletResultat = selftests.run();

        loggSelftestResultat(samletResultat);

        Response.ResponseBuilder builder = Response.ok()
                .encoding(StandardCharsets.UTF_8.name())
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL);

        try {
            ObjectWriter objWriter;
            String output;
            if (MediaType.APPLICATION_JSON.equals(contentType)) {
                builder.type(APPLICATION_JSON);
                objWriter = mapper.writer();
                output = objWriter.writeValueAsString(samletResultat);
            } else {
                builder.type(TEXT_HTML);
                if (writeJsonAsHtml) {
                    objWriter = mapper.writerWithDefaultPrettyPrinter();
                    output = "<pre>" + objWriter.writeValueAsString(samletResultat) + "</pre>";
                } else {
                    output = htmlFormatter.format(samletResultat);
                }
            }
            byte[] utfEncoded = output.getBytes(StandardCharsets.UTF_8);
            builder.entity(utfEncoded);
        } catch (IOException e) {
            SelftestFeil.FACTORY.uventetSelftestFeil(e).log(LOGGER);
        }

        return builder.build();
    }

    public boolean kritiskTjenesteFeilet() {
        SelftestResultat samletResultat = selftests.run(true);
        return SelftestResultat.AggregateResult.ERROR.equals(samletResultat.getAggregateResult());
    }

    private void loggSelftestResultat(SelftestResultat samletResultat) {
        kritiskeFeilTilLogg(samletResultat.getKritiskeResultater());
        ikkeKritiskFeilTilLogg(samletResultat.getIkkeKritiskeResultater());
    }

    private void kritiskeFeilTilLogg(List<HealthCheck.Result> resultat) {
        resultat.forEach(entry -> {
            if (!entry.isHealthy()) {
                SelftestFeil.FACTORY.kritiskSelftestFeilet(
                        getDetailValue(entry, ExtHealthCheck.DETAIL_DESCRIPTION),
                        getDetailValue(entry, ExtHealthCheck.DETAIL_ENDPOINT),
                        getDetailValue(entry, ExtHealthCheck.DETAIL_RESPONSE_TIME),
                        entry.getMessage()).toException().log(LOGGER);
            }
        });
    }

    private void ikkeKritiskFeilTilLogg(List<HealthCheck.Result> resultat) {
        resultat.forEach(entry -> {
            if (!entry.isHealthy()) {
                SelftestFeil.FACTORY.ikkeKritiskSelftestFeilet(
                        getDetailValue(entry, ExtHealthCheck.DETAIL_DESCRIPTION),
                        getDetailValue(entry, ExtHealthCheck.DETAIL_ENDPOINT),
                        getDetailValue(entry, ExtHealthCheck.DETAIL_RESPONSE_TIME),
                        entry.getMessage()).toException().log(LOGGER);
            }
        });
    }

    private String getDetailValue(HealthCheck.Result resultat, String key) {
        Map<String, Object> details = resultat.getDetails();
        if (details != null) {
            return (String) details.get(key);
        } else {
            return null;
        }
    }
}
