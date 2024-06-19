package no.nav.foreldrepenger.fordel.validering;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering;
import no.nav.foreldrepenger.mottak.extensions.EntityManagerAwareTest;

/**
 * Tester at alle migreringer følger standarder for navn og god praksis.
 */
class SjekkDbStrukturTest extends EntityManagerAwareTest {

    private static final String HJELP =
        "\n\nDu har nylig lagt til en ny tabell eller kolonne som ikke er dokumentert ihht. gjeldende regler for dokumentasjon."
            + "\nVennligst gå over sql scriptene og dokumenter tabellene på korrekt måte.";

    private static DataSource ds;
    private static String schema;

    @BeforeAll
    static void setup() {
        Databaseskjemainitialisering.migrerUnittestSkjemaer();
        ds = Databaseskjemainitialisering.initUnitTestDataSource();
        schema = Databaseskjemainitialisering.USER;
    }

    @Test
    void sjekk_at_alle_tabeller_er_dokumentert() throws Exception {
        String sql = "SELECT table_name FROM all_tab_comments WHERE (comments IS NULL OR comments in ('', 'MISSING COLUMN COMMENT')) AND owner=sys_context('userenv', 'current_schema') AND table_name NOT LIKE 'schema_%' AND table_name not like '%_MOCK' AND table_name not like 'HTE_%'";
        List<String> avvik = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                avvik.add(rs.getString(1));
            }

        }

        assertThat(avvik).isEmpty();
    }

    @Test
    void sjekk_at_alle_relevant_kolonner_er_dokumentert() throws Exception {
        List<String> avvik = new ArrayList<>();

        String sql = "SELECT t.table_name||'.'||t.column_name "
            + "  FROM all_col_comments t "
            + " WHERE (t.comments IS NULL OR t.comments = '') "
            + "   AND t.owner = sys_context('userenv','current_schema') "
            + "   AND ( upper(t.table_name) NOT LIKE 'SCHEMA_%' AND upper(t.table_name) NOT LIKE '%_MOCK' AND upper(t.table_name) NOT LIKE 'HTE_%') "
            + "   AND NOT EXISTS (SELECT 1 FROM all_constraints a, all_cons_columns b "
            + "                    WHERE a.table_name = b.table_name "
            + "                      AND b.table_name = t.table_name "
            + "                      AND a.constraint_name = b.constraint_name "
            + "                      AND b.column_name = t.column_name "
            + "                      AND constraint_type IN ('P','R') "
            + "                      AND a.owner = t.owner "
            + "                      AND b.owner = a.owner) "
            + "   AND upper(t.column_name) NOT IN ('OPPRETTET_TID','ENDRET_TID','OPPRETTET_AV','ENDRET_AV','VERSJON','BESKRIVELSE','NAVN','FOM', 'TOM','LAND', 'LANDKODE', 'KL_LANDKODE', 'KL_LANDKODER', 'AKTIV') "
            + " ORDER BY t.table_name, t.column_name ";

        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                avvik.add("\n" + rs.getString(1));
            }

        }

        assertThat(avvik).withFailMessage("Mangler dokumentasjon for %s kolonner. %s\n %s", avvik.size(), avvik, HJELP).isEmpty();
    }

    @Test
    void sjekk_at_alle_FK_kolonner_har_fornuftig_indekser() throws Exception {
        String sql = "SELECT "
            + "  uc.table_name, uc.constraint_name, LISTAGG(dcc.column_name, ',') WITHIN GROUP (ORDER BY dcc.position) as columns" +
            " FROM all_Constraints Uc" +
            "   INNER JOIN all_cons_columns dcc ON dcc.constraint_name  =uc.constraint_name AND dcc.owner=uc.owner" +
            " WHERE Uc.Constraint_Type='R'" +
            "   AND Uc.Owner            = upper(?)" +
            //            "   AND Dcc.Column_Name NOT LIKE 'KL_%'" +
            "   AND EXISTS" +
            "       (SELECT ucc.position, ucc.column_name" +
            "         FROM all_cons_columns ucc" +
            "         WHERE Ucc.Constraint_Name=Uc.Constraint_Name" +
            "           AND Uc.Owner             =Ucc.Owner" +
            //            "           AND ucc.column_name NOT LIKE 'KL_%'" +
            "       MINUS" +
            "        SELECT uic.column_position AS position, uic.column_name" +
            "        FROM all_ind_columns uic" +
            "        WHERE uic.table_name=uc.table_name" +
            "          AND uic.table_owner =uc.owner" +
            "       )" +
            " GROUP BY Uc.Table_Name, Uc.Constraint_Name" +
            " ORDER BY uc.table_name";

        List<String> avvik = new ArrayList<>();
        StringBuilder tekst = new StringBuilder();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String t = rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3);
                    avvik.add(t);
                    tekst.append(t).append("\n");
                }
            }

        }
        int sz = avvik.size();
        String manglerIndeks = "Kolonner som inngår i Foreign Keys skal ha indeker (ikke KL_ kolonner).\nMangler indekser for ";

        assertThat(avvik).withFailMessage(manglerIndeks + sz + " foreign keys\n" + tekst).isEmpty();

    }

    @Test
    void skal_ha_KL_prefiks_for_kodeverk_kolonne_i_source_tabell() throws Exception {
        String sql = """
            Select cola.table_name, cola.column_name From All_Constraints Uc
            Inner Join All_Cons_Columns Cola On Cola.Constraint_Name=Uc.Constraint_Name And Cola.Owner=Uc.Owner
            Inner Join All_Cons_Columns Colb On Colb.Constraint_Name=Uc.r_Constraint_Name And Colb.Owner=Uc.Owner

            Where Uc.Constraint_Type='R' And Uc.Owner= upper(?)
            And Colb.Column_Name='KODEVERK' And Colb.Table_Name='KODELISTE'
            And Colb.Position=Cola.Position
            And Cola.Table_Name Not Like 'KODELI%'
            and cola.column_name not like 'KL_%' """;

        List<String> avvik = new ArrayList<>();
        StringBuilder tekst = new StringBuilder();
        try (Connection conn = ds.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String t = rs.getString(1) + ", " + rs.getString(2);
                    avvik.add(t);
                    tekst.append(t).append("\n");
                }
            }

        }

        int sz = avvik.size();
        String feilTekst = "Feil navn på kolonner som refererer KODELISTE, skal ha 'KL_' prefiks. Antall feil=";

        assertThat(avvik).withFailMessage(feilTekst + sz + ".\n\nTabell, kolonne\n" + tekst).isEmpty();

    }

    @Test
    void skal_ha_primary_key_i_hver_tabell_som_begynner_med_PK() throws Exception {
        String sql = "SELECT table_name FROM all_tables at "
            + " WHERE table_name "
            + " NOT IN ( SELECT ac.table_name FROM all_constraints ac "
            + "         WHERE ac.constraint_type ='P' and at.owner=ac.owner and ac.constraint_name like 'PK_%') "
            + " AND at.owner=upper(?) and at.table_name not like 'schema_%' and at.table_name not like 'HTE_%'";

        List<String> avvik = new ArrayList<>();
        StringBuilder tekst = new StringBuilder();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String t = rs.getString(1);
                    avvik.add(t);
                    tekst.append(t).append("\n");
                }
            }

        }

        int sz = avvik.size();
        String feilTekst = "Feil eller mangelende definisjon av primary key (skal hete 'PK_<tabell navn>'). Antall feil=";

        assertThat(avvik).withFailMessage(feilTekst + +sz + "\n\nTabell\n" + tekst).isEmpty();

    }

    @Test
    void skal_ha_alle_foreign_keys_begynne_med_FK() throws Exception {
        String sql = "SELECT ac.table_name, ac.constraint_name FROM all_constraints ac"
            + " WHERE ac.constraint_type ='R' and ac.owner=upper(?) and constraint_name NOT LIKE 'FK_%'";

        List<String> avvik = new ArrayList<>();
        StringBuilder tekst = new StringBuilder();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String t = rs.getString(1) + ", " + rs.getString(2);
                    avvik.add(t);
                    tekst.append(t).append("\n");
                }
            }

        }

        int sz = avvik.size();
        String feilTekst = "Feil eller mangelende definisjon av foreign key (skal hete 'FK_<tabell navn>_<løpenummer>'). Antall feil=";

        assertThat(avvik).withFailMessage(feilTekst + sz + "\n\nTabell, Foreign Key\n" + tekst).isEmpty();

    }

    @Test
    void skal_ha_korrekt_index_navn() throws Exception {
        String sql = "select table_name, index_name, column_name"
            + " from all_ind_columns"
            + " where table_owner=upper(?)"
            + " and index_name not like 'PK_%' and index_name not like 'IDX_%' and index_name not like 'UIDX_%'"
            + " and table_name not like 'schema_%' and table_name not like 'HTE_%'";

        List<String> avvik = new ArrayList<>();
        StringBuilder tekst = new StringBuilder();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String t = rs.getString(1) + ", " + rs.getString(2);
                    avvik.add(t);
                    tekst.append(t).append("\n");
                }
            }

        }

        int sz = avvik.size();
        String feilTekst = "Feil navngiving av index.  Primary Keys skal ha prefiks PK_, andre unike indekser prefiks UIDX_, vanlige indekser prefiks IDX_. Antall feil=";

        assertThat(avvik).withFailMessage(feilTekst + +sz + "\n\nTabell, Index, Kolonne\n" + tekst).isEmpty();

    }

    @Test
    void skal_ha_samme_data_type_for_begge_sider_av_en_FK() throws Exception {
        String sql = "SELECT T.TABLE_NAME\n" +
            ", TCC.COLUMN_NAME AS KOL_A\n" +
            ", ATT.DATA_TYPE AS KOL_A_DATA_TYPE\n" +
            ", ATT.CHAR_LENGTH AS KOL_A_CHAR_LENGTH\n" +
            ", ATT.CHAR_USED AS KOL_A_CHAR_USED\n" +
            ", RCC.COLUMN_NAME AS KOL_B \n" +
            ", ATR.DATA_TYPE AS KOL_B_DATA_TYPE\n" +
            ", ATR.CHAR_LENGTH AS KOL_B_CHAR_LENGTH\n" +
            ", atr.CHAR_USED as KOL_B_CHAR_USED\n" +
            "FROM ALL_CONSTRAINTS T \n" +
            "INNER JOIN ALL_CONSTRAINTS R ON R.OWNER=T.OWNER AND R.CONSTRAINT_NAME = T.R_CONSTRAINT_NAME\n" +
            "INNER JOIN ALL_CONS_COLUMNS TCC ON TCC.TABLE_NAME=T.TABLE_NAME AND TCC.OWNER=T.OWNER AND TCC.CONSTRAINT_NAME=T.CONSTRAINT_NAME \n" +
            "INNER JOIN ALL_CONS_COLUMNS RCC ON RCC.TABLE_NAME = R.TABLE_NAME AND RCC.OWNER=R.OWNER AND RCC.CONSTRAINT_NAME=R.CONSTRAINT_NAME\n" +
            "INNER JOIN ALL_TAB_COLS ATT ON ATT.COLUMN_NAME=TCC.COLUMN_NAME AND ATT.OWNER=TCC.OWNER AND Att.TABLE_NAME=TCC.TABLE_NAME\n" +
            "inner join all_tab_cols atr on atr.column_name=rcc.column_name and atr.owner=rcc.owner and atr.table_name=rcc.table_name\n" +
            "WHERE T.OWNER=upper(?) AND T.CONSTRAINT_TYPE='R'\n" +
            "AND TCC.POSITION = RCC.POSITION\n" +
            "AND TCC.POSITION IS NOT NULL AND RCC.POSITION IS NOT NULL\n" +
            "AND ((ATT.DATA_TYPE!=ATR.DATA_TYPE) OR (ATT.CHAR_LENGTH!=ATR.CHAR_LENGTH OR ATT.CHAR_USED!=ATR.CHAR_USED) OR (ATT.DATA_TYPE NOT LIKE '%CHAR%' AND ATT.DATA_LENGTH!=ATR.DATA_LENGTH))\n"
            +
            "ORDER BY T.TABLE_NAME, TCC.COLUMN_NAME";

        List<String> avvik = new ArrayList<>();
        StringBuilder tekst = new StringBuilder();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String t = rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3) + ", " + rs.getString(4) + ", " + rs.getString(5)
                        + ", " + rs.getString(6) + ", " + rs.getString(7) + ", " + rs.getString(8) + ", " + rs.getString(9);
                    avvik.add(t);
                    tekst.append(t).append("\n");
                }
            }

        }

        int sz = avvik.size();
        String feilTekst = "Forskjellig datatype for kolonne på hver side av en FK. Kan være deklarert feil (husk VARCHAR2(100 CHAR) og ikke VARCHAR2(100)). Antall feil=";
        String cols = ".\n\nTABELL, KOL_A, KOL_A_DATA_TYPE, KOL_A_CHAR_LENGTH, KOL_A_CHAR_USED, KOL_B, KOL_B_DATA_TYPE, KOL_B_CHAR_LENGTH, KOL_B_CHAR_USED\n";

        assertThat(avvik).withFailMessage(feilTekst + +sz + cols + tekst).isEmpty();

    }

    @Test
    void skal_deklarere_VARCHAR2_kolonner_som_CHAR_ikke_BYTE_semantikk() throws Exception {
        String sql = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHAR_USED, CHAR_LENGTH\n"
            + "FROM ALL_TAB_COLS\n"
            + "WHERE DATA_TYPE = 'VARCHAR2'\n"
            + "AND CHAR_USED !='C' AND TABLE_NAME NOT LIKE '%schema%' AND CHAR_LENGTH>1 AND OWNER=upper(?)\n"
            + "ORDER BY 1, 2";

        List<String> avvik = new ArrayList<>();
        StringBuilder tekst = new StringBuilder();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String t = rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3) + ", " + rs.getString(4) + ", " + rs.getString(5);
                    avvik.add(t);
                    tekst.append(t).append("\n");
                }
            }

        }

        int sz = avvik.size();
        String feilTekst = "Feil deklarasjon av VARCHAR2 kolonne (husk VARCHAR2(100 CHAR) og ikke VARCHAR2(100)). Antall feil=";
        String cols = ".\n\nTABELL, KOLONNE, DATA_TYPE, CHAR_USED, CHAR_LENGTH\n";

        assertThat(avvik).withFailMessage(feilTekst + +sz + cols + tekst).isEmpty();

    }

    @Test
    void skal_ikke_bruke_FLOAT_eller_DOUBLE() throws Exception {
        String sql = "select table_name, column_name, data_type from all_tab_cols where owner=upper(?) and data_type in ('FLOAT', 'DOUBLE') order by 1, 2";

        List<String> avvik = new ArrayList<>();
        StringBuilder tekst = new StringBuilder();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String t = rs.getString(1) + ", " + rs.getString(2);
                    avvik.add(t);
                    tekst.append(t).append("\n");
                }
            }

        }

        int sz = avvik.size();
        String feilTekst = "Feil bruk av datatype, skal ikke ha FLOAT eller DOUBLE (bruk NUMBER for alle desimaltall, spesielt der penger representeres). Antall feil=";

        assertThat(avvik).withFailMessage(feilTekst + +sz + "\n\nTabell, Kolonne, Datatype\n" + tekst).isEmpty();

    }

    @Test
    void sjekk_at_status_verdiene_i_prosess_task_tabellen_er_også_i_pollingSQL() throws Exception {
        String sql = "SELECT SEARCH_CONDITION\n" +
            "FROM all_constraints\n" +
            "WHERE table_name = 'PROSESS_TASK'\n" +
            "AND constraint_name = 'CHK_PROSESS_TASK_STATUS'\n" +
            "AND owner = sys_context('userenv','current_schema')";

        List<String> statusVerdier = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statusVerdier.add(rs.getString(1));
            }

        }
        String feilTekst = "Ved innføring av ny stause må sqlen i TaskManager_pollTask.sql må oppdateres ";
        assertThat(statusVerdier).withFailMessage(feilTekst)
            .containsExactly("status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG', 'KJOERT')");
    }
}
