package no.nav.foreldrepenger.fordel.web.app.startupinfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SystemPropertiesHelper {

    private static final List<String> SECRET_NAME_PARTS = Arrays.asList("passord", "password", "passwd");
    private static final String FILTERED_VALUE = "*****";
    private static final Pattern SECRET_VERDI_REGEX = Pattern.compile("([^= ]*?(?:passord|password|passwd)[^=]*\\s*=\\s*)[^ ]*", Pattern.CASE_INSENSITIVE);
    private static final String FILTER_PATTERN="$1*****";

    private SystemPropertiesHelper() {
        // skjult
    }

    static SystemPropertiesHelper getInstance() {
        return new SystemPropertiesHelper();
    }

    SortedMap<String, String> filteredSortedProperties() {

        Properties sysProps = System.getProperties();
        SortedMap<String, String> map = new TreeMap<>();
        for (String name : sysProps.stringPropertyNames()) {
            map.put(name, sysProps.getProperty(name));
        }
        filter(map);
        return map;
    }

    SortedMap<String, String> filteredSortedEnvVars() {

        SortedMap<String, String> map = new TreeMap<>();
        map.putAll(System.getenv());
        filter(map);
        return map;
    }

    static void filter(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (containsSecretNamePart(entry.getKey())) {
                entry.setValue(FILTERED_VALUE);
            }
            if (containsSecretNamePart(entry.getValue())) {
                Matcher matcher = SECRET_VERDI_REGEX.matcher(entry.getValue());
                String filtered = matcher.replaceFirst(FILTER_PATTERN);
                entry.setValue(filtered);
            }
        }
    }

    private static boolean containsSecretNamePart(String name) {
        boolean containsSecretNamePart = SECRET_NAME_PARTS.stream()
                .anyMatch(secrNamePart -> name.toLowerCase().contains(secrNamePart.toLowerCase()));
        return containsSecretNamePart;
    }
}
