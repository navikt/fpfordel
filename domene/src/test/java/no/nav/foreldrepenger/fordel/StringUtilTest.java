package no.nav.foreldrepenger.fordel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void is_blank_ok() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank(" "));
        assertTrue(StringUtil.isBlank("    "));
        assertFalse(StringUtil.isBlank("Mike"));
        assertFalse(StringUtil.isBlank("XYZ"));
        assertFalse(StringUtil.isBlank("Pølse"));
    }

    @Test
    void mask() {
        assertEquals("<null>", StringUtil.mask(null));
        assertEquals("35234w*****************", StringUtil.mask("35234wedsfsdsdgsdfs323r"));
        assertEquals("352343*****", StringUtil.mask("    35234311111"));
        assertEquals("35234", StringUtil.mask("35234"));
        assertEquals("352  3*", StringUtil.mask("352  34"));
        assertEquals("352  3********", StringUtil.mask("352  34111111"));
    }
}
