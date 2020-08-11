package no.nav.foreldrepenger.fordel.web.app.selftest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import com.codahale.metrics.health.HealthCheck;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ExtHealthCheck;

class SelftestsHtmlFormatter {

    private SelftestsHtmlFormatter() {

    }

    static String format(SelftestResultat samletResultat) {

        StringBuilder sb = new StringBuilder();

        doInTag("html", sb, () -> {

            doInTag("head", sb, () -> {
                appendMeta(sb);
                appendEmbeddedStyles(sb);
            });

            doInTag("h2", sb, () -> {
                sb.append("Applikasjonens status: ");
                sb.append(samletResultat.getAggregateResult());
            });

            doInTag("h3", sb, () -> {
                sb.append("Testet: ");
                sb.append(samletResultat.getTimestamp());
            });

            appendTableForAppInfo(sb, samletResultat);

            appendStartTag(sb, "p");

            appendTableForResults(sb, samletResultat);
        });

        return sb.toString();
    }

    private static void appendTableForAppInfo(StringBuilder sb, SelftestResultat samletResultat) {
        doInTag("table", sb, () -> {
            appendTrNameValue(sb, "Applikasjon", samletResultat.getApplication());
            appendTrNameValue(sb, "Versjon", samletResultat.getVersion());
            appendTrNameValue(sb, "Git revisjon", samletResultat.getRevision());
            appendTrNameValue(sb, "Bygget", samletResultat.getBuildTime());
        });
    }

    private static void appendMeta(StringBuilder sb) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("http-equiv", "Content-Type");
        attributes.put("content", "text/html;charset=UTF-8");
        doInTag("meta", attributes, sb);
    }

    private static void appendEmbeddedStyles(StringBuilder sb) {
        doInTag("style", sb, () -> {
            sb.append("table, th, td { border: 1px solid black; } ");
            sb.append("table { border-collapse: collapse; } ");
            sb.append("th { text-align: left; } ");
            sb.append("th, td { padding: 4px; } ");
            sb.append("td { vertical-align: top; } ");
        });
    }

    private static void appendTableForResults(StringBuilder sb, SelftestResultat samletResultat) {
        doInTag("table", sb, () -> {
            appendTrHeaders(sb, "Status", "Kritisk", "Responstid", "Beskrivelse", "Endepunkt", "Melding", "Stack trace");
            for (HealthCheck.Result result : samletResultat.getKritiskeResultater()) {
                appendTrResult(sb, result, true);
            }
            for (HealthCheck.Result result : samletResultat.getIkkeKritiskeResultater()) {
                appendTrResult(sb, result, false);
            }
        });
    }

    private static void appendTrResult(StringBuilder sb, HealthCheck.Result result, boolean kritisk) {
        String status = result.isHealthy() ? "OK" : "Feilet";
        Map<String, Object> details = result.getDetails();
        String endpoint = "?";
        String description = "?";
        String respTime = "?";
        if (details != null) {
            endpoint = (String) details.get(ExtHealthCheck.DETAIL_ENDPOINT);
            description = (String) details.get(ExtHealthCheck.DETAIL_DESCRIPTION);
            respTime = (String) details.get(ExtHealthCheck.DETAIL_RESPONSE_TIME);
            respTime = emptyIfNull(respTime);
        }
        String msg = "";
        String stacktrace = "";
        if (!result.isHealthy()) {
            if (result.getError() != null) {
                stacktrace = StackTraceFormatter.formatForWeb(result.getError());
            } else if (result.getMessage() != null) {
                msg = result.getMessage();
            } else {
                msg = "(mangler både message og error)";
            }
        }
        String k = kritisk ? "Ja" : "Nei";
        appendTrValues(sb, status, k, respTime, description, endpoint, msg, stacktrace);
    }

    private static void appendTrNameValue(StringBuilder sb, String name, String value) {
        doInTag("tr", sb, () -> {
            doInTag("td", sb, () -> sb.append(name));
            doInTag("td", sb, () -> sb.append(value));
        });
    }

    private static void appendTrHeaders(StringBuilder sb, String... headers) {
        doInTag("tr", sb, () -> {
            for (String hdr : headers) {
                doInTag("th", sb, () -> sb.append(hdr));
            }
        });
    }

    private static void appendTrValues(StringBuilder sb, String... values) {
        doInTag("tr", sb, () -> {
            for (String val : values) {
                doInTag("td", sb, () -> sb.append(val));
            }
        });
    }

    @FunctionalInterface
    private interface Doer {
        void doIt();
    }

    private static void doInTag(String tagName, StringBuilder sb, Doer doer) {
        doInTag(tagName, null, sb, doer);
    }

    private static void doInTag(String tagName, Map<String, String> attributes, StringBuilder sb) {
        doInTag(tagName, attributes, sb, null);
    }

    private static void doInTag(String tagName, Map<String, String> attributes, StringBuilder sb, Doer doer) {
        appendStartTag(sb, tagName, attributes);
        if (doer != null) {
            doer.doIt();
        }
        appendEndTag(sb, tagName);
    }

    private static void appendStartTag(StringBuilder sb, String tagName) {
        appendStartTag(sb, tagName, null);
    }

    private static void appendStartTag(StringBuilder sb, String tagName, Map<String, String> attributes) {
        sb.append('<');
        sb.append(tagName);
        if (attributes != null) {
            attributes.forEach((name, value) -> {
                sb.append(' ');
                sb.append(name);
                sb.append("=\"");
                value = StringEscapeUtils.escapeHtml4(value);
                sb.append(value);
                sb.append('"');
            });
        }
        sb.append('>');
    }

    private static void appendEndTag(StringBuilder sb, String tagName) {
        sb.append("</");
        sb.append(tagName);
        sb.append('>');
    }

    private static String emptyIfNull(String respTime) {
        return respTime == null ? "" : respTime;
    }
}
