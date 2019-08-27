package no.nav.foreldrepenger.fordel.web.app.selftest;

import org.junit.Test;

import no.nav.foreldrepenger.fordel.web.app.selftest.StackTraceFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class StackTraceFormatterTests {

    @Test
    public void test() {
        Exception e;
        try {
            throw new Exception("oi!");
        } catch (Exception e2) {
            e = e2;
        }

        String s = StackTraceFormatter.format(e);
        assertThat(s != null).isTrue();
        assertThat(s.contains("oi!")).isTrue();
    }
}
