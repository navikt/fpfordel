package no.nav.foreldrepenger.fordel.web.app.util;

public class StringUtils {

    private StringUtils() {}

    public static boolean erIkkeTom(String str) {
        return (str != null) && (str.length() > 0);
    }

    public static boolean erTom(String str) {
        return !StringUtils.erIkkeTom(str);
    }

}
