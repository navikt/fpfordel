package no.nav.foreldrepenger.fordel;

import static java.util.function.Predicate.not;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Strings;

public final class StringUtil {
    private static final int DEFAULT_LENGTH = 50;

    private StringUtil() {
    }

    public static String taint(String value) {
        if (!value.matches("[a-zA-Z0-9]++"))
            throw new IllegalArgumentException(value);
        return value;
    }

    public static String endelse(List<?> liste) {
        return liste.size() == 1 ? "" : "er";
    }

    public static String limit(String tekst) {
        return limit(tekst, DEFAULT_LENGTH);
    }

    public static String limit(String tekst, int max) {
        return Optional.ofNullable(tekst)
                .filter(t -> t.length() >= max)
                .map(s -> s.substring(0, max - 1) + "...")
                .orElse(tekst);
    }

    public static String limit(byte[] bytes, int max) {
        return limit(Arrays.toString(bytes), max);
    }

    public static String partialMask(String value) {
        return partialMask(value, 11);
    }

    public static String partialMask(String value, int length) {
        return (value != null) && (value.length() == length) ? Strings.padEnd(value.substring(0, length / 2 + length % 2), length, '*') : value;
    }

    public static String mask(String value) {
        return Optional.ofNullable(value)
                .map(String::stripLeading)
                .filter(not(String::isBlank))
                .map(v -> "*".repeat(v.length()))
                .orElse("<null>");
    }

    public static String encode(String string) {
        try {
            return Base64.getEncoder().encodeToString(string.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
