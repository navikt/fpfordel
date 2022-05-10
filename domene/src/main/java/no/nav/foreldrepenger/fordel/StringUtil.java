package no.nav.foreldrepenger.fordel;

import static java.util.function.Predicate.not;

import java.util.Optional;

public final class StringUtil {
    private static final int KEEP_FIRST = 6;

    private StringUtil() {
    }

    public static String mask(String value) {
        return Optional.ofNullable(value)
                .map(String::stripLeading)
                .filter(not(String::isBlank))
                .map(StringUtil::keepFirstMaskRest)
                .orElse("<null>");
    }

    private static String keepFirstMaskRest(String value) {
        return Optional.ofNullable(value)
                .filter(v -> v.length() > KEEP_FIRST)
                .map(v -> v.substring(0, KEEP_FIRST) + "*".repeat(v.length() - KEEP_FIRST))
                .orElse(value);
    }

}
