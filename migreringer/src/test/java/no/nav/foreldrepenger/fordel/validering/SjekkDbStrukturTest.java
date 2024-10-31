package no.nav.foreldrepenger.fordel.validering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.mottak.extensions.JpaExtension;

/**
 * Tester at alle migreringer følger standarder for navn og god praksis.
 */
@ExtendWith(JpaExtension.class)
class SjekkDbStrukturTest extends EntityManagerAwareTest {

    @Test
    void sjekk_at_alle_tabeller_er_dokumentert() {
        var sql = """
            SELECT table_name
              FROM ALL_TAB_COMMENTS
             WHERE (comments IS NULL OR comments in ('', 'MISSING COLUMN COMMENT'))
               AND owner = sys_context('userenv', 'current_schema')
               AND upper(table_name) NOT LIKE '%SCHEMA_%'""";
        var query = getEntityManager().createNativeQuery(sql, String.class);
        var avvik = query.getResultStream().toList();
        assertThat(avvik).isEmpty();
    }

    @Test
    void sjekk_at_alle_relevant_kolonner_er_dokumentert() {
        var sql = """
            SELECT t.owner||'.'||t.table_name||'.'||t.column_name
              FROM ALL_COL_COMMENTS t
             WHERE (t.comments IS NULL OR t.comments = '')
               AND t.owner = sys_context('userenv', 'current_schema')
               AND (upper(t.table_name) NOT LIKE '%SCHEMA_%')
               AND NOT EXISTS (SELECT 1 FROM ALL_CONSTRAINTS a, ALL_CONS_COLUMNS b
                                WHERE a.table_name = b.table_name
                                  AND b.table_name = t.table_name
                                  AND a.constraint_name = b.constraint_name
                                  AND b.column_name = t.column_name
                                  AND constraint_type IN ('P','R')
                                  AND a.owner = t.owner
                                  AND b.owner = a.owner)
               AND upper(t.column_name) NOT IN ('OPPRETTET_TID','ENDRET_TID','OPPRETTET_AV','ENDRET_AV','VERSJON','BESKRIVELSE','NAVN','FOM','TOM','AKTIV')
             ORDER BY t.table_name, t.column_name""";

        var query = getEntityManager().createNativeQuery(sql, String.class);
        var avvik = query.getResultStream().map(row ->"\n" + row).toList();

        var hjelpetekst = """
            Du har nylig lagt til en ny tabell eller kolonne som ikke er dokumentert ihht. gjeldende regler for dokumentasjon.

            Vennligst gå over SQL-skriptene og dokumenter tabellene på korrekt måte.
            """;

        assertThat(avvik).withFailMessage("Mangler dokumentasjon for %s kolonner. %s\n\n%s", avvik.size(), avvik, hjelpetekst).isEmpty();
    }

    @Test
    void sjekk_at_alle_FK_kolonner_har_fornuftig_indekser() {
        var sql = """
            SELECT
                UC.TABLE_NAME,
                UC.CONSTRAINT_NAME,
                listagg(DCC.COLUMN_NAME, ',') WITHIN GROUP (ORDER BY DCC.POSITION) AS COLUMNS
            FROM ALL_CONSTRAINTS UC
                INNER JOIN ALL_CONS_COLUMNS DCC ON DCC.CONSTRAINT_NAME = UC.CONSTRAINT_NAME AND DCC.OWNER = UC.OWNER
            WHERE UC.CONSTRAINT_TYPE = 'R'
                AND upper(UC.OWNER) = upper(:owner)
                AND EXISTS (
                    SELECT UCC.POSITION, UCC.COLUMN_NAME
                    FROM ALL_CONS_COLUMNS UCC
                    WHERE UCC.CONSTRAINT_NAME = UC.CONSTRAINT_NAME
                        AND UC.OWNER = UCC.OWNER
                    MINUS
                    SELECT UIC.COLUMN_POSITION AS POSITION, UIC.COLUMN_NAME
                    FROM ALL_IND_COLUMNS UIC
                    WHERE UIC.TABLE_NAME = UC.TABLE_NAME
                        AND UIC.TABLE_OWNER = UC.OWNER
                )
            GROUP BY UC.TABLE_NAME, UC.CONSTRAINT_NAME
            ORDER BY UC.TABLE_NAME
            """;


        var query = getEntityManager().createNativeQuery(sql, Object[].class);
        query.setParameter("owner", JpaExtension.DEFAULT_TEST_DB_SCHEMA_NAME);
        List<Object[]> resultList = query.getResultList();

        var tekst = resultList.stream().map(row -> Arrays.stream(row).map(String.class::cast).collect(Collectors.joining(", "))).collect(Collectors.joining("\n"));
        var manglerIndeks = "Kolonner som inngår i Foreign Keys skal ha indekser (ikke KL_ kolonner).\nMangler indekser for %s foreign keys\n%s";
        assertThat(resultList).withFailMessage(manglerIndeks, resultList.size(), tekst).isEmpty();
    }

    @Test
    void skal_ha_primary_key_i_hver_tabell_som_begynner_med_PK() {
        var sql = """
            SELECT table_name
              FROM all_tables at
             WHERE table_name
               NOT IN (SELECT ac.table_name
                         FROM all_constraints ac
                        WHERE ac.constraint_type ='P'
                          AND at.owner = ac.owner
                          AND ac.constraint_name LIKE 'PK_%')
              AND upper(at.owner) = upper(:owner)
              AND upper(at.table_name) NOT LIKE '%SCHEMA_%'""";

        var query = getEntityManager().createNativeQuery(sql, String.class);
        query.setParameter("owner", JpaExtension.DEFAULT_TEST_DB_SCHEMA_NAME);

        var avvik = query.getResultList();
        var tekst = avvik.stream().collect(Collectors.joining("\n"));
        var sz = avvik.size();
        var feilTekst = "Feil eller mangelende definisjon av primary key (skal hete 'PK_<tabell navn>'). Antall feil = %s \n\nTabell:\n%s";
        assertThat(avvik).withFailMessage(feilTekst, sz, tekst).isEmpty();
    }

    @Test
    void skal_ha_alle_foreign_keys_begynne_med_FK() {
        var sql = """
            SELECT ac.table_name, ac.constraint_name
              FROM all_constraints ac
             WHERE ac.constraint_type = 'R'
               AND upper(ac.owner) = upper(:owner)
               AND constraint_name NOT LIKE 'FK_%'""";

        var query = getEntityManager().createNativeQuery(sql, Object[].class);
        query.setParameter("owner", JpaExtension.DEFAULT_TEST_DB_SCHEMA_NAME);
        List<Object[]> rowList = query.getResultList();
        var tekst = rowList.stream().map(row -> Arrays.stream(row).map(String.class::cast).collect(Collectors.joining(", "))).collect(Collectors.joining("\n"));
        var feilTekst = "Feil eller mangelende definisjon av foreign key (skal hete 'FK_<tabell navn>_<løpenummer>'). Antall feil = %s\n\nTabell, Foreign Key\n%s";
        assertThat(rowList).withFailMessage(feilTekst, rowList.size(), tekst).isEmpty();
    }

    @Test
    void skal_ha_korrekt_index_navn() throws Exception {
        var sql = """
                SELECT table_name, index_name, column_name
                  FROM all_ind_columns
                 WHERE table_owner = upper(:owner)
                   AND index_name NOT LIKE 'PK_%'
                   AND index_name NOT LIKE 'IDX_%'
                   AND index_name NOT LIKE 'UIDX_%'
                   AND upper(table_name) NOT LIKE '%SCHEMA_%'""";

        var query = getEntityManager().createNativeQuery(sql, Object[].class);
        query.setParameter("owner", JpaExtension.DEFAULT_TEST_DB_SCHEMA_NAME);
        List<Object[]> rowList = query.getResultList();
        var tekst = rowList.stream().map(row -> Arrays.stream(row).map(String.class::cast).collect(Collectors.joining(", "))).collect(Collectors.joining("\n"));

        var feilTekst = "Feil navngiving av index.  Primary Keys skal ha prefiks PK_, andre unike indekser prefiks UIDX_, vanlige indekser prefiks IDX_. Antall feil = %s\n\nTabell, Index, Kolonne\n%s";
        assertThat(rowList).withFailMessage(feilTekst, rowList.size(), tekst).isEmpty();
    }

    @Test
    void skal_ha_samme_data_type_for_begge_sider_av_en_FK() {
        var sql = """
            SELECT TO_CHAR(T.TABLE_NAME) AS TABLE_NAME,
              TO_CHAR(TCC.COLUMN_NAME) AS KOL_A,
              TO_CHAR(ATT.DATA_TYPE) AS KOL_A_DATA_TYPE,
              TO_CHAR(ATT.CHAR_LENGTH) AS KOL_A_CHAR_LENGTH,
              TO_CHAR(ATT.CHAR_USED) AS KOL_A_CHAR_USED,
              TO_CHAR(RCC.COLUMN_NAME) AS KOL_B,
              TO_CHAR(ATR.DATA_TYPE) AS KOL_B_DATA_TYPE,
              TO_CHAR(ATR.CHAR_LENGTH) AS KOL_B_CHAR_LENGTH,
              TO_CHAR(ATR.CHAR_USED) AS KOL_B_CHAR_USED
            FROM ALL_CONSTRAINTS T
            INNER JOIN ALL_CONSTRAINTS R ON R.OWNER = T.OWNER AND R.CONSTRAINT_NAME = T.R_CONSTRAINT_NAME
            INNER JOIN ALL_CONS_COLUMNS TCC ON TCC.TABLE_NAME = T.TABLE_NAME AND TCC.OWNER = T.OWNER AND TCC.CONSTRAINT_NAME = T.CONSTRAINT_NAME
            INNER JOIN ALL_CONS_COLUMNS RCC ON RCC.TABLE_NAME = R.TABLE_NAME AND RCC.OWNER = R.OWNER AND RCC.CONSTRAINT_NAME = R.CONSTRAINT_NAME
            INNER JOIN ALL_TAB_COLS ATT ON ATT.COLUMN_NAME = TCC.COLUMN_NAME AND ATT.OWNER = TCC.OWNER AND ATT.TABLE_NAME = TCC.TABLE_NAME
            INNER JOIN ALL_TAB_COLS ATR ON ATR.COLUMN_NAME = RCC.COLUMN_NAME AND ATR.OWNER = RCC.OWNER AND ATR.TABLE_NAME = RCC.TABLE_NAME
            WHERE T.OWNER = upper(:owner)
              AND T.CONSTRAINT_TYPE = 'R'
              AND TCC.POSITION = RCC.POSITION
              AND TCC.POSITION IS NOT NULL
              AND RCC.POSITION IS NOT NULL
              AND (
                  (ATT.DATA_TYPE != ATR.DATA_TYPE)
                  OR (ATT.CHAR_LENGTH != ATR.CHAR_LENGTH OR ATT.CHAR_USED != ATR.CHAR_USED)
                  OR (ATT.DATA_TYPE NOT LIKE '%CHAR%' AND ATT.DATA_LENGTH != ATR.DATA_LENGTH)
              )
            ORDER BY T.TABLE_NAME, TCC.COLUMN_NAME""";


        var query = getEntityManager().createNativeQuery(sql, Object[].class);
        query.setParameter("owner", JpaExtension.DEFAULT_TEST_DB_SCHEMA_NAME);
        List<Object[]> rowList = query.getResultList();

        var tekst = rowList.stream()
            .map(row -> Arrays.stream(row).map(column -> column instanceof Character c ? c.toString() : (String) column).collect(Collectors.joining(", ")))
            .collect(Collectors.joining("\n"));

        var feilTekst = "Forskjellig datatype for kolonne på hver side av en FK. Kan være deklarert feil (husk VARCHAR2(100 CHAR) og ikke VARCHAR2(100)). Antall feil = %s%s%s";
        var cols = ".\n\nTABELL, KOL_A, KOL_A_DATA_TYPE, KOL_A_CHAR_LENGTH, KOL_A_CHAR_USED, KOL_B, KOL_B_DATA_TYPE, KOL_B_CHAR_LENGTH, KOL_B_CHAR_USED\n";
        assertThat(rowList).withFailMessage(feilTekst, rowList.size(), cols, tekst).isEmpty();
    }

    @Test
    void skal_deklarere_VARCHAR2_kolonner_som_CHAR_ikke_BYTE_semantikk() {
        var sql = """
            SELECT TO_CHAR(TABLE_NAME) AS TABLE_NAME,
                   TO_CHAR(COLUMN_NAME) AS COLUMN_NAME,
                   TO_CHAR(DATA_TYPE) AS DATA_TYPE,
                   TO_CHAR(CHAR_USED) AS CHAR_USED,
                   TO_CHAR(CHAR_LENGTH) AS CHAR_LENGTH
            FROM ALL_TAB_COLS
            WHERE DATA_TYPE = 'VARCHAR2'
              AND CHAR_USED != 'C'
              AND upper(TABLE_NAME) NOT LIKE '%SCHEMA%'
              AND CHAR_LENGTH > 1
              AND OWNER = upper(:owner)
            ORDER BY TABLE_NAME, COLUMN_NAME""";


        var query = getEntityManager().createNativeQuery(sql, Object[].class);
        query.setParameter("owner", JpaExtension.DEFAULT_TEST_DB_SCHEMA_NAME);
        List<Object[]> rowList = query.getResultList();

        var tekst = rowList.stream()
            .map(row -> Arrays.stream(row).map(column -> column instanceof Character c ? c.toString() : (String) column).collect(Collectors.joining(", ")))
            .collect(Collectors.joining("\n"));

        var feilTekst = "Feil deklarasjon av VARCHAR2 kolonne (husk VARCHAR2(100 CHAR) og ikke VARCHAR2(100)). Antall feil = %s%s%s";
        var cols = ".\n\nTABELL, KOLONNE, DATA_TYPE, CHAR_USED, CHAR_LENGTH\n";
        assertThat(rowList).withFailMessage(feilTekst, rowList.size(), cols, tekst).isEmpty();
    }

    @Test
    void skal_ikke_bruke_FLOAT_eller_DOUBLE() {
        String sql = """
            SELECT TO_CHAR(TABLE_NAME) AS table_name,
                   TO_CHAR(COLUMN_NAME) AS column_name,
                   TO_CHAR(DATA_TYPE) AS data_type
            FROM ALL_TAB_COLS
            WHERE OWNER = upper(:owner)
              AND DATA_TYPE IN ('FLOAT', 'DOUBLE')
            ORDER BY TABLE_NAME, COLUMN_NAME
            """;

        var query = getEntityManager().createNativeQuery(sql, Object[].class);
        query.setParameter("owner", JpaExtension.DEFAULT_TEST_DB_SCHEMA_NAME);
        List<Object[]> rowList = query.getResultList();

        var tekst = rowList.stream()
            .map(row -> Arrays.stream(row).map(String.class::cast).collect(Collectors.joining(", ")))
            .collect(Collectors.joining("\n"));

        var feilTekst = "Feil bruk av datatype, skal ikke ha FLOAT eller DOUBLE (bruk NUMBER for alle desimaltall, spesielt der penger representeres). Antall feil = %s\n\nTabell, Kolonne, Datatype\n%s";
        assertThat(rowList).withFailMessage(feilTekst, rowList.size(), tekst).isEmpty();
    }

    @Test
    void sjekk_at_status_verdiene_i_prosess_task_tabellen_er_også_i_pollingSQL() {
        String sql = """
            SELECT SEARCH_CONDITION
            FROM ALL_CONSTRAINTS
            WHERE table_name = 'PROSESS_TASK'
              AND constraint_name = 'CHK_PROSESS_TASK_STATUS'
              AND owner = sys_context('userenv', 'current_schema')""";

        var query = getEntityManager().createNativeQuery(sql, String.class);
        var statusVerdier = query.getResultList();

        var feilTekst = "Ved innføring av ny stauser må sqlen i TaskManager_pollTask.sql må oppdateres.";
        assertThat(statusVerdier).withFailMessage(feilTekst)
                .containsExactly("status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG', 'KJOERT')");
    }
}
