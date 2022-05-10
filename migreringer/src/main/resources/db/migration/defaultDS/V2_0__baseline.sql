--------------------------------------------------------
--  File created - mandag-september-23-2019   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Sequence SEQ_DOKUMENT
--------------------------------------------------------

   CREATE SEQUENCE  "SEQ_DOKUMENT"  MINVALUE 1  INCREMENT BY 50 START WITH 1 NOCACHE NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence SEQ_DOKUMENT_METADATA
--------------------------------------------------------

   CREATE SEQUENCE  "SEQ_DOKUMENT_METADATA"  MINVALUE 1  INCREMENT BY 50 START WITH 1 NOCACHE NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence SEQ_KODELISTE
--------------------------------------------------------

   CREATE SEQUENCE  "SEQ_KODELISTE"  MINVALUE 1  INCREMENT BY 50 START WITH 1010800 NOCACHE NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence SEQ_KONFIG_VERDI
--------------------------------------------------------

   CREATE SEQUENCE  "SEQ_KONFIG_VERDI"  MINVALUE 1000000  INCREMENT BY 50 START WITH 1001550 NOCACHE NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence SEQ_PROSESS_TASK
--------------------------------------------------------

   CREATE SEQUENCE  "SEQ_PROSESS_TASK"  MINVALUE 1000000  INCREMENT BY 50 START WITH 1000000 NOCACHE NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence SEQ_PROSESS_TASK_GRUPPE
--------------------------------------------------------

   CREATE SEQUENCE  "SEQ_PROSESS_TASK_GRUPPE"  MINVALUE 10000000  INCREMENT BY 1000000 START WITH 10000000 NOCACHE NOCYCLE ;
--------------------------------------------------------
--  DDL for Table DOKUMENT
--------------------------------------------------------

  CREATE TABLE "DOKUMENT" 
   (	"ID" NUMBER(19,0), 
	"FORSENDELSE_ID" RAW(16), 
	"DOKUMENT_TYPE_ID" VARCHAR2(100 CHAR), 
	"HOVED_DOKUMENT" CHAR(1 BYTE), 
	"OPPRETTET_AV" VARCHAR2(20 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (3) DEFAULT systimestamp, 
	"ENDRET_AV" VARCHAR2(20 CHAR), 
	"ENDRET_TID" TIMESTAMP (3), 
	"KL_DOKUMENT_TYPE_ID" VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('DOKUMENT_TYPE_ID') VIRTUAL , 
	"ARKIV_FILTYPE" VARCHAR2(100 CHAR), 
	"KL_ARKIV_FILTYPE" VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('ARKIV_FILTYPE') VIRTUAL , 
	"BLOB" BLOB, 
	"BESKRIVELSE" VARCHAR2(200 CHAR)
   ) ;

   COMMENT ON COLUMN "DOKUMENT"."ID" IS 'Primærnøkkel';
   COMMENT ON COLUMN "DOKUMENT"."FORSENDELSE_ID" IS 'Unik ID for forsendelsen';
   COMMENT ON COLUMN "DOKUMENT"."DOKUMENT_TYPE_ID" IS 'Kodeverdi for innkommende dokument type';
   COMMENT ON COLUMN "DOKUMENT"."HOVED_DOKUMENT" IS 'Er dette hoveddokument? (J/N)';
   COMMENT ON TABLE "DOKUMENT"  IS 'Tabell med dokumenter som fordeles';
   COMMENT ON COLUMN "DOKUMENT"."BLOB" IS 'Innkommende dokument blob';
   COMMENT ON COLUMN "DOKUMENT"."BESKRIVELSE" IS 'Brukers beskrivelse av dokumentinnhold';
--------------------------------------------------------
--  DDL for Table DOKUMENT_METADATA
--------------------------------------------------------

  CREATE TABLE "DOKUMENT_METADATA" 
   (	"ID" NUMBER(19,0), 
	"FORSENDELSE_ID" RAW(16), 
	"BRUKER_ID" VARCHAR2(19 CHAR), 
	"SAKSNUMMER" VARCHAR2(32 CHAR), 
	"ARKIV_ID" VARCHAR2(32 CHAR), 
	"OPPRETTET_AV" VARCHAR2(20 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (3) DEFAULT systimestamp, 
	"ENDRET_AV" VARCHAR2(20 CHAR), 
	"ENDRET_TID" TIMESTAMP (3), 
	"FORSENDELSE_STATUS" VARCHAR2(12 CHAR), 
	"FORSENDELSE_MOTTATT" TIMESTAMP (3)
   ) ;

   COMMENT ON TABLE "DOKUMENT_METADATA"  IS 'Metadataene til dokumentene som fordeles';
   COMMENT ON COLUMN "DOKUMENT_METADATA"."ID" IS 'Primærnøkkel';
   COMMENT ON COLUMN "DOKUMENT_METADATA"."FORSENDELSE_ID" IS 'Unik ID for forsendelsen';
   COMMENT ON COLUMN "DOKUMENT_METADATA"."BRUKER_ID" IS 'ID til avsenderen av et dokument';
   COMMENT ON COLUMN "DOKUMENT_METADATA"."SAKSNUMMER" IS 'ID til fagsak et dokument knyttes mot';
   COMMENT ON COLUMN "DOKUMENT_METADATA"."ARKIV_ID" IS 'ID til dokumentet i JOARK';
   COMMENT ON COLUMN "DOKUMENT_METADATA"."FORSENDELSE_STATUS" IS 'Status på dokumentforsendelse';
   COMMENT ON COLUMN "DOKUMENT_METADATA"."FORSENDELSE_MOTTATT" IS 'Tidspunktet forsendelsen ble mottatt hos NAV';
--------------------------------------------------------
--  DDL for Table KODELISTE
--------------------------------------------------------

  CREATE TABLE "KODELISTE" 
   (	"ID" NUMBER(19,0), 
	"KODEVERK" VARCHAR2(100 CHAR), 
	"KODE" VARCHAR2(100 CHAR), 
	"OFFISIELL_KODE" VARCHAR2(1000 CHAR), 
	"NAVN" VARCHAR2(256 CHAR), 
	"BESKRIVELSE" VARCHAR2(4000 CHAR), 
	"SPRAK" VARCHAR2(3 CHAR) DEFAULT 'NB', 
	"GYLDIG_FOM" DATE DEFAULT sysdate, 
	"GYLDIG_TOM" DATE DEFAULT to_date('31.12.9999', 'dd.mm.yyyy'), 
	"OPPRETTET_AV" VARCHAR2(200 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (3) DEFAULT systimestamp, 
	"ENDRET_AV" VARCHAR2(200 CHAR), 
	"ENDRET_TID" TIMESTAMP (3), 
	"EKSTRA_DATA" VARCHAR2(4000 CHAR)
   ) ;

   COMMENT ON COLUMN "KODELISTE"."ID" IS 'Primary Key';
   COMMENT ON COLUMN "KODELISTE"."KODEVERK" IS '(PK) og FK - kodeverk';
   COMMENT ON COLUMN "KODELISTE"."KODE" IS '(PK) Unik kode innenfor kodeverk. Denne koden er alltid brukt internt';
   COMMENT ON COLUMN "KODELISTE"."OFFISIELL_KODE" IS '(Optional) Offisiell kode hos kodeverkeier. Denne kan avvike fra kode der systemet har egne koder. Kan brukes til å veksle inn kode i offisiell kode når det trengs for integrasjon med andre systemer';
   COMMENT ON COLUMN "KODELISTE"."NAVN" IS 'Navn på Kodeverket. Offsielt navn synkes dersom Offsiell kode er satt';
   COMMENT ON COLUMN "KODELISTE"."BESKRIVELSE" IS 'Beskrivelse av koden';
   COMMENT ON COLUMN "KODELISTE"."SPRAK" IS 'Språk Kodeverket er definert for, default NB (norsk bokmål). Bruker ISO 639-1 standard men med store bokstaver siden det representert slik i NAVs offisielle Kodeverk';
   COMMENT ON COLUMN "KODELISTE"."GYLDIG_FOM" IS 'Dato Kodeverket er gyldig fra og med';
   COMMENT ON COLUMN "KODELISTE"."GYLDIG_TOM" IS 'Dato Kodeverket er gyldig til og med';
   COMMENT ON COLUMN "KODELISTE"."EKSTRA_DATA" IS '(Optional) Tilleggsdata brukt av kodeverket.  Format er kodeverk spesifikt - eks. kan være tekst, json, key-value, etc.';
   COMMENT ON TABLE "KODELISTE"  IS 'Inneholder lister av koder for alle Kodeverk som benyttes i applikasjonen.  Både offisielle (synkronisert fra sentralt hold i Nav) såvel som interne Kodeverk.  Offisielle koder skiller seg ut ved at nav_offisiell_kode er populert. Følgelig vil gyldig_tom/fom, navn, språk og beskrivelse lastes ned fra Kodeverkklienten eller annen kilde sentralt';
--------------------------------------------------------
--  DDL for Table KODEVERK
--------------------------------------------------------

  CREATE TABLE "KODEVERK" 
   (	"KODE" VARCHAR2(100 CHAR), 
	"KODEVERK_EIER" VARCHAR2(100 CHAR) DEFAULT 'VL', 
	"KODEVERK_EIER_REF" VARCHAR2(1000 CHAR), 
	"KODEVERK_EIER_VER" VARCHAR2(20 CHAR), 
	"KODEVERK_EIER_NAVN" VARCHAR2(100 CHAR), 
	"KODEVERK_SYNK_NYE" CHAR(1 BYTE) DEFAULT 'J', 
	"KODEVERK_SYNK_EKSISTERENDE" CHAR(1 BYTE) DEFAULT 'J', 
	"NAVN" VARCHAR2(256 CHAR), 
	"BESKRIVELSE" VARCHAR2(4000 CHAR), 
	"OPPRETTET_AV" VARCHAR2(200 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (3) DEFAULT systimestamp, 
	"ENDRET_AV" VARCHAR2(200 CHAR), 
	"ENDRET_TID" TIMESTAMP (3), 
	"SAMMENSATT" VARCHAR2(1 CHAR) DEFAULT 'N'
   ) ;

   COMMENT ON COLUMN "KODEVERK"."KODE" IS 'PK - definerer kodeverk';
   COMMENT ON COLUMN "KODEVERK"."KODEVERK_EIER" IS 'Offisielt kodeverk eier (kode)';
   COMMENT ON COLUMN "KODEVERK"."KODEVERK_EIER_REF" IS 'Offisielt kodeverk referanse (url)';
   COMMENT ON COLUMN "KODEVERK"."KODEVERK_EIER_VER" IS 'Offisielt kodeverk versjon';
   COMMENT ON COLUMN "KODEVERK"."KODEVERK_EIER_NAVN" IS 'Offisielt kodeverk navn';
   COMMENT ON COLUMN "KODEVERK"."KODEVERK_SYNK_NYE" IS 'Om nye koder fra kodeverkeier skal legges til ved oppdatering.';
   COMMENT ON COLUMN "KODEVERK"."KODEVERK_SYNK_EKSISTERENDE" IS 'Om eksisterende koder fra kodeverkeier skal endres ved oppdatering.';
   COMMENT ON COLUMN "KODEVERK"."NAVN" IS 'Navn på kodeverk';
   COMMENT ON COLUMN "KODEVERK"."BESKRIVELSE" IS 'Beskrivelse av kodeverk';
   COMMENT ON COLUMN "KODEVERK"."SAMMENSATT" IS 'Skiller mellom sammensatt kodeverk og enkel kodeliste';
   COMMENT ON TABLE "KODEVERK"  IS 'Registrerte kodeverk. Representerer grupperinger av koder';
--------------------------------------------------------
--  DDL for Table KONFIG_VERDI
--------------------------------------------------------

  CREATE TABLE "KONFIG_VERDI" 
   (	"ID" NUMBER(19,0), 
	"KONFIG_KODE" VARCHAR2(50 CHAR), 
	"KONFIG_GRUPPE" VARCHAR2(100 CHAR), 
	"KONFIG_VERDI" VARCHAR2(255 CHAR), 
	"GYLDIG_FOM" DATE DEFAULT sysdate, 
	"GYLDIG_TOM" DATE DEFAULT to_date('31.12.9999', 'dd.mm.yyyy'), 
	"OPPRETTET_AV" VARCHAR2(20 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (3) DEFAULT systimestamp, 
	"ENDRET_AV" VARCHAR2(20 CHAR), 
	"ENDRET_TID" TIMESTAMP (3), 
	"KL_KONFIG_VERDI_GRUPPE" VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('KONFIG_VERDI_GRUPPE') VIRTUAL 
   ) ;

   COMMENT ON COLUMN "KONFIG_VERDI"."ID" IS 'Primary Key';
   COMMENT ON COLUMN "KONFIG_VERDI"."KONFIG_KODE" IS 'Angir kode som identifiserer en konfigurerbar verdi. ';
   COMMENT ON COLUMN "KONFIG_VERDI"."KONFIG_GRUPPE" IS 'Angir gruppe en konfigurerbar verdi kode tilhører (hvis noen - kan også spesifiseres som INGEN).';
   COMMENT ON COLUMN "KONFIG_VERDI"."KONFIG_VERDI" IS 'Angir verdi';
   COMMENT ON COLUMN "KONFIG_VERDI"."GYLDIG_FOM" IS 'Gydlig fra-og-med dato';
   COMMENT ON COLUMN "KONFIG_VERDI"."GYLDIG_TOM" IS 'Gydlig til-og-med dato';
   COMMENT ON TABLE "KONFIG_VERDI"  IS 'Angir konfigurerbare verdier med kode, eventuelt tilhørende gruppe.';
--------------------------------------------------------
--  DDL for Table KONFIG_VERDI_KODE
--------------------------------------------------------

  CREATE TABLE "KONFIG_VERDI_KODE" 
   (	"KODE" VARCHAR2(50 CHAR), 
	"KONFIG_GRUPPE" VARCHAR2(100 CHAR) DEFAULT 'INGEN', 
	"NAVN" VARCHAR2(50 CHAR), 
	"KONFIG_TYPE" VARCHAR2(100 CHAR), 
	"BESKRIVELSE" VARCHAR2(255 CHAR), 
	"OPPRETTET_AV" VARCHAR2(20 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (3) DEFAULT systimestamp, 
	"ENDRET_AV" VARCHAR2(20 CHAR), 
	"ENDRET_TID" TIMESTAMP (3), 
	"KL_KONFIG_VERDI_GRUPPE" VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('KONFIG_VERDI_GRUPPE') VIRTUAL , 
	"KL_KONFIG_VERDI_TYPE" VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('KONFIG_VERDI_TYPE') VIRTUAL 
   ) ;

   COMMENT ON COLUMN "KONFIG_VERDI_KODE"."KODE" IS 'Primary Key';
   COMMENT ON COLUMN "KONFIG_VERDI_KODE"."KONFIG_GRUPPE" IS 'Angir gruppe en konfigurerbar verdi kode tilhører (hvis noen - kan også spesifiseres som INGEN).';
   COMMENT ON COLUMN "KONFIG_VERDI_KODE"."NAVN" IS 'Angir et visningsnavn';
   COMMENT ON COLUMN "KONFIG_VERDI_KODE"."KONFIG_TYPE" IS 'Type angivelse for koden';
   COMMENT ON COLUMN "KONFIG_VERDI_KODE"."BESKRIVELSE" IS 'Beskrivelse av formålet den konfigurerbare verdien';
   COMMENT ON COLUMN "KONFIG_VERDI_KODE"."KL_KONFIG_VERDI_TYPE" IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
   COMMENT ON COLUMN "KONFIG_VERDI_KODE"."KL_KONFIG_VERDI_GRUPPE" IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
   COMMENT ON TABLE "KONFIG_VERDI_KODE"  IS 'Angir unik kode for en konfigurerbar verdi for validering og utlisting av tilgjengelige koder.';
--------------------------------------------------------
--  DDL for Table PROSESS_TASK
--------------------------------------------------------
/*
  CREATE TABLE "PROSESS_TASK" 
   (	"ID" NUMBER(19,0), 
	"TASK_TYPE" VARCHAR2(50 CHAR), 
	"PRIORITET" NUMBER(3,0) DEFAULT 0, 
	"STATUS" VARCHAR2(20 CHAR) DEFAULT 'KLAR', 
	"TASK_PARAMETERE" VARCHAR2(4000 CHAR), 
	"TASK_PAYLOAD" CLOB, 
	"TASK_GRUPPE" VARCHAR2(250 CHAR), 
	"TASK_SEKVENS" VARCHAR2(100 CHAR) DEFAULT '1', 
	"NESTE_KJOERING_ETTER" TIMESTAMP (0) DEFAULT current_timestamp, 
	"FEILEDE_FORSOEK" NUMBER(5,0) DEFAULT 0, 
	"SISTE_KJOERING_TS" TIMESTAMP (6), 
	"SISTE_KJOERING_FEIL_KODE" VARCHAR2(50 CHAR), 
	"SISTE_KJOERING_FEIL_TEKST" CLOB, 
	"SISTE_KJOERING_SERVER" VARCHAR2(50 CHAR), 
	"VERSJON" NUMBER(19,0) DEFAULT 0, 
	"OPPRETTET_AV" VARCHAR2(30 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (6) DEFAULT systimestamp, 
	"BLOKKERT_AV" NUMBER(19,0), 
	"SISTE_KJOERING_PLUKK_TS" TIMESTAMP (6), 
	"SISTE_KJOERING_SLUTT_TS" TIMESTAMP (6)
   )
  PARTITION BY LIST ("STATUS") 
 (PARTITION "STATUS_FERDIG"  VALUES ('FERDIG') , 
 PARTITION "STATUS_FEILET"  VALUES ('FEILET') , 
 PARTITION "STATUS_KLAR"  VALUES ('KLAR', 'VENTER_SVAR', 'SUSPENDERT', 'VETO') )  ENABLE ROW MOVEMENT ;
 */
	DECLARE

		opprett_process_tabell varchar2(999) := 'CREATE TABLE PROSESS_TASK ' ||
											' ( ID NUMBER(19,0), ' ||
											' TASK_TYPE VARCHAR2(50 CHAR), ' || 
											' PRIORITET NUMBER(3,0) DEFAULT 0, ' || 
											' STATUS VARCHAR2(20 CHAR) DEFAULT ''KLAR'', ' || 
											' TASK_PARAMETERE VARCHAR2(4000 CHAR), ' || 
											' TASK_PAYLOAD CLOB, ' || 
											' TASK_GRUPPE VARCHAR2(250 CHAR), ' || 
											' TASK_SEKVENS VARCHAR2(100 CHAR) DEFAULT ''1'', ' || 
											' NESTE_KJOERING_ETTER TIMESTAMP (0) DEFAULT current_timestamp, ' || 
											' FEILEDE_FORSOEK NUMBER(5,0) DEFAULT 0, ' || 
											' SISTE_KJOERING_TS TIMESTAMP (6), ' || 
											' SISTE_KJOERING_FEIL_KODE VARCHAR2(50 CHAR), ' || 
											' SISTE_KJOERING_FEIL_TEKST CLOB, ' || 
											' SISTE_KJOERING_SERVER VARCHAR2(50 CHAR), ' || 
											' VERSJON NUMBER(19,0) DEFAULT 0, ' || 
											' OPPRETTET_AV VARCHAR2(30 CHAR) DEFAULT ''VL'', ' || 
											' OPPRETTET_TID TIMESTAMP (6) DEFAULT systimestamp, ' || 
											' BLOKKERT_AV NUMBER(19,0), ' || 
											' SISTE_KJOERING_PLUKK_TS TIMESTAMP (6), ' || 
											' SISTE_KJOERING_SLUTT_TS TIMESTAMP (6) ) ';
											
		legg_partisjon varchar2(255) := ' PARTITION by list (status)(' ||
										  ' PARTITION status_ferdig values (''FERDIG''),' ||
										  ' PARTITION status_feilet values (''FEILET''),' ||
										  ' PARTITION status_klar values(''KLAR'', ''VENTER_SVAR'', ''SUSPENDERT'', ''VETO'')) ENABLE ROW MOVEMENT ';
										  
	-- Partisjoner opprettes ikke i XE (11)
	BEGIN

		IF (DBMS_DB_VERSION.VERSION < 12) THEN
			execute immediate opprett_process_tabell;
		ELSE
			execute immediate opprett_process_tabell || legg_partisjon;
		END IF;

	END;
	/
 

   COMMENT ON COLUMN "PROSESS_TASK"."BLOKKERT_AV" IS 'Id til ProsessTask som blokkerer kjøring av denne (når status=VETO)';
   COMMENT ON TABLE "PROSESS_TASK"  IS 'Inneholder tasks som skal kjøres i bakgrunnen';
   COMMENT ON COLUMN "PROSESS_TASK"."TASK_TYPE" IS 'navn på task. Brukes til å matche riktig implementasjon';
   COMMENT ON COLUMN "PROSESS_TASK"."PRIORITET" IS 'prioritet på task.  Høyere tall har høyere prioritet';
   COMMENT ON COLUMN "PROSESS_TASK"."STATUS" IS 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG';
   COMMENT ON COLUMN "PROSESS_TASK"."NESTE_KJOERING_ETTER" IS 'tasken skal ikke kjøeres før tidspunkt er passert';
   COMMENT ON COLUMN "PROSESS_TASK"."FEILEDE_FORSOEK" IS 'antall feilede forsøk';
   COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_TS" IS 'siste gang tasken ble forsøkt kjørt (før kjøring)';
   COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_FEIL_KODE" IS 'siste feilkode tasken fikk';
   COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_FEIL_TEKST" IS 'siste feil tasken fikk';
   COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_SERVER" IS 'navn på node som sist kjørte en task (server@pid)';
   COMMENT ON COLUMN "PROSESS_TASK"."TASK_PARAMETERE" IS 'parametere angitt for en task';
   COMMENT ON COLUMN "PROSESS_TASK"."TASK_PAYLOAD" IS 'inputdata for en task';
   COMMENT ON COLUMN "PROSESS_TASK"."TASK_SEKVENS" IS 'angir rekkefølge på task innenfor en gruppe ';
   COMMENT ON COLUMN "PROSESS_TASK"."TASK_GRUPPE" IS 'angir en unik id som grupperer flere ';
   COMMENT ON COLUMN "PROSESS_TASK"."VERSJON" IS 'angir versjon for optimistisk låsing';
   COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_SLUTT_TS" IS 'tidsstempel siste gang tasken ble kjørt (etter kjøring)';
   COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_PLUKK_TS" IS 'siste gang tasken ble forsøkt plukket (fra db til in-memory, før kjøring)';
--------------------------------------------------------
--  DDL for Table PROSESS_TASK_FEILHAND
--------------------------------------------------------

  CREATE TABLE "PROSESS_TASK_FEILHAND" 
   (	"KODE" VARCHAR2(20 CHAR), 
	"NAVN" VARCHAR2(50 CHAR), 
	"BESKRIVELSE" VARCHAR2(2000 CHAR), 
	"OPPRETTET_AV" VARCHAR2(20 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (3) DEFAULT systimestamp, 
	"ENDRET_AV" VARCHAR2(20 CHAR), 
	"ENDRET_TID" TIMESTAMP (3), 
	"INPUT_VARIABEL1" NUMBER, 
	"INPUT_VARIABEL2" NUMBER
   ) ;

   COMMENT ON COLUMN "PROSESS_TASK_FEILHAND"."KODE" IS 'Kodeverk Primary Key';
   COMMENT ON COLUMN "PROSESS_TASK_FEILHAND"."NAVN" IS 'Lesbart navn på type feilhåndtering brukt i prosesstask';
   COMMENT ON COLUMN "PROSESS_TASK_FEILHAND"."BESKRIVELSE" IS 'Utdypende beskrivelse av koden';
   COMMENT ON TABLE "PROSESS_TASK_FEILHAND"  IS 'Kodetabell for feilhåndterings-metoder. For eksempel antall ganger å prøve på nytt og til hvilke tidspunkt';
   COMMENT ON COLUMN "PROSESS_TASK_FEILHAND"."INPUT_VARIABEL1" IS 'input variabel 1 for feilhåndtering';
   COMMENT ON COLUMN "PROSESS_TASK_FEILHAND"."INPUT_VARIABEL2" IS 'input variabel 2 for feilhåndtering';
--------------------------------------------------------
--  DDL for Table PROSESS_TASK_TYPE
--------------------------------------------------------

  CREATE TABLE "PROSESS_TASK_TYPE" 
   (	"KODE" VARCHAR2(50 CHAR), 
	"NAVN" VARCHAR2(50 CHAR), 
	"FEIL_MAKS_FORSOEK" NUMBER(10,0) DEFAULT 1, 
	"FEIL_SEK_MELLOM_FORSOEK" NUMBER(10,0) DEFAULT 30, 
	"FEILHANDTERING_ALGORITME" VARCHAR2(20 CHAR) DEFAULT 'DEFAULT', 
	"BESKRIVELSE" VARCHAR2(2000 CHAR), 
	"OPPRETTET_AV" VARCHAR2(20 CHAR) DEFAULT 'VL', 
	"OPPRETTET_TID" TIMESTAMP (3) DEFAULT systimestamp, 
	"ENDRET_AV" VARCHAR2(20 CHAR), 
	"ENDRET_TID" TIMESTAMP (3), 
	"CRON_EXPRESSION" VARCHAR2(200 CHAR)
   ) ;

   COMMENT ON COLUMN "PROSESS_TASK_TYPE"."KODE" IS 'Kodeverk Primary Key';
   COMMENT ON COLUMN "PROSESS_TASK_TYPE"."NAVN" IS 'Lesbart navn på prosesstasktype';
   COMMENT ON COLUMN "PROSESS_TASK_TYPE"."FEIL_MAKS_FORSOEK" IS 'MISSING COLUMN COMMENT';
   COMMENT ON COLUMN "PROSESS_TASK_TYPE"."FEIL_SEK_MELLOM_FORSOEK" IS 'MISSING COLUMN COMMENT';
   COMMENT ON COLUMN "PROSESS_TASK_TYPE"."FEILHANDTERING_ALGORITME" IS 'FK: PROSESS_TASK_FEILHAND';
   COMMENT ON COLUMN "PROSESS_TASK_TYPE"."BESKRIVELSE" IS 'Utdypende beskrivelse av koden';
   COMMENT ON TABLE "PROSESS_TASK_TYPE"  IS 'Kodetabell for typer prosesser med beskrivelse og informasjon om hvilken feilhåndteringen som skal benyttes';
   COMMENT ON COLUMN "PROSESS_TASK_TYPE"."CRON_EXPRESSION" IS 'Cron-expression for når oppgaven skal kjøres på nytt';



Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','SOKN','SOK','Søknad','Søknad','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','ESKJ','ES','Elektronisk skjema','Elektronisk skjema','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','BRV','B','Brev','Brev','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','EDIALOG','ELEKTRONISK_DIALOG','Elektronisk dialog','Elektronisk dialog','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','FNOT','FORVALTNINGSNOTAT','Forvaltningsnotat','Forvaltningsnotat','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','IBRV','IB','Informasjonsbrev','Informasjonsbrev','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','KONVEARK','KD','Konvertert fra elektronisk arkiv','Konvertert fra elektronisk arkiv','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','KONVSYS','KS','Konverterte data fra system','Konverterte data fra system','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','PUBEOS','PUBL_BLANKETT_EOS','Publikumsblankett EØS','Publikumsblankett EØS','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','SEDOK','SED','Strukturert elektronisk dokument - EU/EØS','Strukturert elektronisk dokument - EU/EØS','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','TSKJ','TS','Tolkbart skjema','Tolkbart skjema','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','VBRV','VB','Vedtaksbrev','Vedtaksbrev','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','ARENA','AO01','Arena','Arena','NB',to_date('13.02.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','GRISEN','AO11','Grisen','Grisen','NB',to_date('27.01.2011','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','GOSYS','FS22','Gosys','Gosys','NB',to_date('25.04.2009','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','INFOTRYGD','IT01','Infotrygd','Infotrygd','NB',to_date('13.02.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','HJE_HEL_ORT','OEBS','Hjelpemidler, Helsetjenester og Ort. Hjelpemidler','Hjelpemidler, Helsetjenester og Ort. Hjelpemidler','NB',to_date('13.02.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','PESYS','PP01','Pesys','Pesys','NB',to_date('10.12.2011','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','VENTELONN','V2','Ventelønn','Ventelønn','NB',to_date('13.02.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','UNNTAK','UFM','Unntak','Unntak','NB',to_date('01.01.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','FPSAK','FS36','Vedtaksløsning Foreldrepenger','Vedtaksløsning Foreldrepenger','NB',to_date('28.06.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','ALTINN','ALTINN','Altinn','Altinn','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','EIA','EIA','EIA','EIA','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','EKST_OPPS','EKST_OPPS','Eksternt oppslag','Eksternt oppslag','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NAV_NO','NAV_NO','Ditt NAV','Ditt NAV','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','SKAN_NETS','SKAN_NETS','Skanning Nets','Skanning Nets','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','SKAN_PEN','SKAN_PEN','Skanning Pensjon','Skanning Pensjon','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','EESSI','EESSI','EESSI','EESSI','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','PSELV','PSELV','PSELV','PSELV','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','E_POST','E_POST','E-post','E-post','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1400','NETS_PB1400','NETS - postboks 1400','NETS - postboks 1400','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1405','NETS_PB1405','NETS - postboks 1405','NETS - postboks 1405','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1406','NETS_PB1406','NETS - postboks 1406','NETS - postboks 1406','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1407','NETS_PB1407','NETS - postboks 1407','NETS - postboks 1407','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1408','NETS_PB1408',' NETS - postboks 1408',' NETS - postboks 1408','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1411','NETS_PB1411','NETS - postboks 1411','NETS - postboks 1411','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1412','NETS_PB1412','NETS - postboks 1412','NETS - postboks 1412','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1413','NETS_PB1413','NETS - postboks 1413','NETS - postboks 1413','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1423','NETS_PB1423','NETS - postboks 1423','NETS - postboks 1423','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1431','NETS_PB1431','NETS - postboks 1431','NETS - postboks 1431','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'MOTTAK_KANAL','NETS_PB1441','NETS_PB1441','NETS - postboks 1441','NETS - postboks 1441','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','PROD','PRODUKSJON','Produksjonsformat','Produksjonsformat','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','ARKIV','ARKIV','Arkivformat','Arkivformat','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','SKANM','SKANNING_META','Skanning metadata','Skanning metadata','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','BREVB','BREVBESTILLING','Brevbestilling data','Brevbestilling data','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','ORIG','ORIGINAL','Originalformat','Originalformat','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','FULL','FULLVERSJON','Versjon med infotekster','Versjon med infotekster','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','SLADD','SLADDET','Sladdet format','Sladdet format','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','PRDLF','PRODUKSJON_DLF','Produksjonsformat DLF','Produksjonsformat DLF','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'VARIANT_FORMAT','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_TEMA','FA',null,'Foreldrepenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_TEMA','SP',null,'Sykepenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_TEMA','EF',null,'Enslig forsørger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_TEMA','AA','Arbeidsavklaringspenger','Arbeidsavklaringspenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_TEMA','DAGP','Dagpenger','Dagpenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','FØ',null,'Foreldrepenger fødsel',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','AP',null,'Foreldrepenger adopsjon',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','SV',null,'Svangerskapspenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','AE',null,'Adopsjon engangsstønad',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','FE',null,'Fødsel engangsstønad',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','FU',null,'Foreldrepenger fødsel, utland',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','RS',null,'forsikr.risiko sykefravær',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','RT',null,'reisetilskudd',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','SP',null,'sykepenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','SU',null,'sykepenger utenlandsopphold',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','BT',null,'stønad til barnetilsyn',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','FL',null,'tilskudd til flytting',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','OG',null,'overgangsstønad',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','UT',null,'skolepenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','AAP','Arbeidsavklaringspenger','Arbeidsavklaringspenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','DAGO','Ordinære dagpenger','Ordinære dagpenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','PERM','Dagpenger under permitteringer','Dagpenger under permitteringer',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','FISK','Dagp. v/perm fra fiskeindustri','Dagp. v/perm fra fiskeindustri',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'RELATERT_YTELSE_BEH_TEMA','LONN','Lønnsgarantimidler - dagpenger','Lønnsgarantimidler - dagpenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','INNTEKTSMELDING','I000067','Inntektsmelding',null,'NB',to_date('01.12.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','UTBETALINGSMELDING','OB36','Utbetalingsmelding','Utbetalingsmelding','NB',to_date('01.06.2018','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'FAGSYSTEM','MELOSYS','FS38','Melosys','Melosys','NB',to_date('01.05.2018','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','MELDINGSONING','I000068','Melding til NAV om soning',null,'NB',to_date('20.06.2018','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','MELDINGSTRAFFEUNNDRAGELSE','I000069','Melding til NAV om unndragelse av straff',null,'NB',to_date('20.06.2018','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','MELDINGENDRINGINSTITUSJON','I000070','Melding om endring i institusjonsopphold',null,'NB',to_date('20.06.2018','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','MELDINGUTEBLITTINSTITUSJON','I000071','Melding om uteblivelse fra institusjon',null,'NB',to_date('20.06.2018','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_KONTANTSTØTTE','I000072','Søknad om kontantstøtte til småbarnsforeldre',null,'NB',to_date('20.06.2018','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'TEMA','OMS','OMS','Omsorgspenger, Pleiepenger og opplæringspenger','Omsorgspenger, Pleiepenger og opplæringspenger','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','OMS','ab0271','Omsorgspenger, Pleiepenger og opplæringspenger','Omsorgspenger, Pleiepenger og opplæringspenger','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','OMS_OPP','ab0141','Opplæringspenger','Opplæringspenger','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','OMS_PLEIE_BARN','ab0069','Pleiepenger sykt barn','Pleiepenger sykt barn','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','OMS_PLEIE_INSTU','ab0153','Pleiepenger ved institusjonsopphold','Pleiepenger ved institusjonsopphold','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','OMS_OMSORG','ab0149','Omsorgspenger','Omsorgspenger','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','OMS_PLEIE_BARN_NY','ab0320','Pleiepenger sykt barn ny ordning','Pleiepenger sykt barn ny ordning fom 011017','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','SVP','ab0126','Svangerskapspenger','Svangerskapspenger','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_SVANGERSKAPSPENGER','I000001','Søknad om svangerskapspenger',null,'NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','TIF','TIF','TIFF','Filtype TIF','NB',to_date('09.05.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','JSON','JSON','JSON','Filtype JSON','NB',to_date('09.05.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','PNG','PNG','PNG','Filtype PNG','NB',to_date('09.05.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','JPG','JPG','JPG','Filtype JPG','NB',to_date('09.05.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SKJEMA_TILRETTELEGGING_OMPLASSERING','I000109','Skjema for tilrettelegging og omplassering ved graviditet',null,'NB',to_date('22.05.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOKUMENTASJON_ALENEOMSORG','I000110','Dokumentasjon av aleneomsorg',null,'NB',to_date('22.05.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BEGRUNNELSE_SØKNAD_ETTERSKUDD','I000111','Dokumentasjon av begrunnelse for hvorfor man søker tilbake i tid',null,'NB',to_date('22.05.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOKUMENTASJON_INTRODUKSJONSPROGRAM','I000112','Dokumentasjon av deltakelse i introduksjonsprogrammet',null,'NB',to_date('22.05.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','VURDERING_ARBEID_SYKEMELDING','I000107','Vurdering av arbeidsmulighet/sykmelding',null,'NB',to_date('21.08.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','OPPLYSNING_TILRETTELEGGING_SVANGER','I000108','Opplysninger om muligheter og behov for tilrettelegging ved svangerskap',null,'NB',to_date('21.08.2019','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','ENGST_FODS','ab0050','Engangsstønad ved fødsel',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','ENGST_ADOP','ab0027','Engangsstønad ved adopsjon',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','ENGST','ab0327','Engangsstønad',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','FORP_ADOP','ab0072','Foreldrepenger ved adopsjon',null,'NB',to_date('07.12.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','FORP_FODS','ab0047','Foreldrepenger ved fødsel',null,'NB',to_date('07.12.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'BEHANDLING_TEMA','FORP','ab0326','Foreldrepenger',null,'NB',to_date('07.12.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'TEMA','FOR_SVA','FOR','Foreldre- og svangerskapspenger','Foreldre- og svangerskapspenger','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'TEMA','-',null,'Ikke definert','Ikke definert','NB',to_date('01.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_ENGANGSSTØNAD_FØDSEL','I000003','Søknad om engangsstønad ved fødsel','Søknad om engangsstønad ved fødsel','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_ENGANGSSTØNAD_ADOPSJON','I000004','Søknad om engangsstønad ved adopsjon','Søknad om engangsstønad ved adopsjon','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL','I000041','Dokumentasjon av termindato, fødsel eller dato for omsorgsovertakelse','Dokumentasjon av termindato (lev. kun av mor), fødsel eller dato for omsorgsovertakelse','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOKUMENTASJON_AV_OMSORGSOVERTAKELSE','I000042','Dokumentasjon av dato for overtakelse av omsorg','Dokumentasjon av dato for overtakelse av omsorg','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BRUKEROPPLASTET_DOKUMENTASJON','I000047','Brukeropplastet dokumentasjon','Brukeropplastet dokumentasjon','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','KLAGE_DOKUMENT','I000027','Klage/anke','Klage/anke','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','KVITTERING_DOKUMENTINNSENDING','I000046','Kvittering dokumentinnsending','Kvittering dokumentinnsending','NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BEKREFTELSE_VENTET_FØDSELSDATO','I000062','Bekreftelse på ventet fødselsdato','Bekreftelse på ventet fødselsdato','NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','LEGEERKLÆRING','I000023','Legeerklæring',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','GJELDSBREV_GRUPPE_1','I000024','Gjeldsbrev gruppe 1',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BEKREFTELSE_FRA_ARBEIDSGIVER','I000065','Bekreftelse fra arbeidsgiver',null,'NB',to_date('24.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','KOPI_VOGNKORT','I000021','Kopi av vognkort',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','KOPI_SKATTEMELDING','I000066','Kopi av likningsattest eller selvangivelse',null,'NB',to_date('24.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','KOPI_FØRERKORT','I000022','Kopi av førerkort',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BREV_UTLAND','I000028','Brev - utland',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','GJELDSBREV_GRUPPE_2','I000025','Gjeldsbrev gruppe 2',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','INNTEKTSOPPLYSNINGER','I000026','Inntektsopplysninger for arbeidstaker som skal ha sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ANNET','I000060','Annet',null,'NB',to_date('24.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','FØDSELSATTEST','I000063','Fødselsattest',null,'NB',to_date('24.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ELEVDOKUMENTASJON_LÆRESTED','I000064','Elevdokumentasjon fra lærested',null,'NB',to_date('24.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','KOPI_VERGEATTEST','I000020','Kopi av verge- eller hjelpeverge attest',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BEKREFTELSE_FRA_STUDIESTED','I000061','Bekreftelse fra studiested/skole',null,'NB',to_date('24.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_SØKNAD_TILPASSNING_BIL','I500010','Ettersendelse til søknad om spesialutstyr og- tilpassing til bil',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','TREKKOPPLYSNINGER_ETTERSENDT','I500057','Ettersendelse til trekkopplysninger for arbeidstaker som skal ha: sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ANNET_SKJEMA_UTLAND_IKKE_NAV','I000029','Annet skjema (ikke NAV-skjema) - utland',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','OPPPDRAGSKONTRAKT','I000034','Oppdragskontrakt',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','LØNNS_OG_TREKKOPPGAVE','I000035','Lønns- og trekkoppgave',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','OPPHOLDSOPPLYSNINGER','I001000','Oppholdsopplysninger',null,'NB',to_date('26.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','RESULTATREGNSKAP','I000032','Resultatregnskap',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','LØNNSLIPP','I000033','Lønnsslipp',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_MORS_UTDANNING_ARBEID_SYKDOM','I000038','Dokumentasjon av mors utdanning, arbeid eller sykdom',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_MILITÆR_SIVIL_TJENESTE','I000039','Dokumentasjon av militær- eller siviltjeneste',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_FERIE','I000036','Dokumentasjon av ferie',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_INNLEGGELSE','I000037','Dokumentasjon av innleggelse i helseinstitusjon',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','NÆRINGSOPPGAVE','I000030','Næringsoppgave',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','PERSONINNTEKTSKJEMA','I000031','Personinntektsskjema',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','I500027','I500027','Ettersendelse til klage/anke',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BESKRIVELSE_FUNKSJONSNEDSETTELSE','I000045','Beskrivelse av funksjonsnedsettelse',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_FORELDREPENGER_ADOPSJON','I000002','Søknad om foreldrepenger ved adopsjon',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_ARBEIDSFORHOLD','I000043','Dokumentasjon av arbeidsforhold',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_ETTERLØNN','I000044','Dokumentasjon av etterlønn/sluttvederlag',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ANNET_SKJEMA_IKKE_NAV','I000049','Annet skjema (ikke NAV-skjema)',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_FORELDREPENGER_FØDSEL','I000005','Søknad om foreldrepenger ved fødsel',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','FLEKSIBELT_UTTAK_FORELDREPENGER','I000006','Utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BREV','I000048','Brev',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_ASYL_DATO','I000040','Dokumentasjon av dato for asyl',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_REISEUTGIFT_BIL','I000009','Søknad om refusjon av reiseutgifter til bil',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','INNTEKTSOPPLYSNING_SELVSTENDIG','I000007','Inntektsopplysninger om selvstendig næringsdrivende og/eller frilansere som skal ha foreldrepenger eller svangerskapspenger',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_SKAFFE_BIL','I000008','Søknad om stønad til anskaffelse av motorkjøretøy',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_UTGIFT_BARNEPASS','I000056','Dokumentasjon av utgifter til stell og pass av barn',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','TILLEGGSJKJEMA_BIL','I000012','Tilleggskjema for bil',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','TREKKOPPLYSNING_ARBEIDSTAKER','I000057','Trekkopplysninger for arbeidstaker som skal ha: sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BEKREFTELSE_OPPMØTE','I000013','Bekreftelse på oppmøte',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_UTBETALING_FRA_ARBEIDSGIVER','I000054','Dokumentasjon av utbetalinger eller goder fra arbeidsgiver',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SØKNAD_TILPASSNING_BIL','I000010','Søknad om spesialutstyr og -tilpassing til bil',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BEKREFTELSE_OPPHOLDSTILLATELSE','I000055','Bekreftelse på oppholdstillatelse',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','LEGEERKLÆRING_EVNE_KJØRE_BIL','I000011','Legeerklæring om søkerens evne til å føre motorkjøretøy og om behovet for ekstra transport på grunn av funksjonshemmingen',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_INNTEKT','I000016','Dokumentasjon av inntekt',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_UTGIFT_REISE','I000017','Dokumentasjon av reiseutgifter',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_ANDRE_YTELSE','I000058','Dokumentasjon av andre ytelser',null,'NB',to_date('24.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_BEHOV_LEDSAGER','I000014','Dokumentasjon av behov for ledsager',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','TIMELISTER','I000059','Timelister',null,'NB',to_date('24.05.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_BEHOV_TRANSPORTMIDDEL','I000015','Dokumentasjon av behov for dyrere transportmiddel',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SKJEMA_OPPLYSNING_INNTEKT','I000052','Inntektsopplysningsskjema',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD','I500050','Ettersendelse til søknad om endring av uttak av foreldrepenger eller overføring av kvote',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_ANDRE_UTBETALINGER','I000053','Dokumentasjon av andre utbetalinger',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','FORELDREPENGER_ENDRING_SØKNAD','I000050','Søknad om endring av uttak av foreldrepenger eller overføring av kvote',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM','I000051','Bekreftelse på deltakelse i kvalifiseringsprogrammet',null,'NB',to_date('25.04.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON','I500002','Ettersendelse til søknad om foreldrepenger ved adopsjon',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL','I500003','Ettersendelse til søknad om engangsstønad ved fødsel',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG','I500001','Ettersendelse til søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER','I500006','Ettersendelse til utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON','I500004','Ettersendelse til søknad om engangsstønad ved adopsjon',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','SPESIALISTERKLÆRING','I000018','Spesialisterklæring',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL','I500005','Ettersendelse til søknad om foreldrepenger ved fødsel',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','DOK_VEIFORHOLD','I000019','Dokumentasjon av veiforhold',null,'NB',to_date('22.03.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_SØKNAD_SKAFFE_BIL','I500008','Ettersendelse til søknad om stønad til anskaffelse av motorkjøretøy',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_TYPE_ID','ETTERSENDT_SØKNAD_REISEUTGIFT_BIL','I500009','Ettersendelse til søknad om refusjon av reiseutgifter til bil',null,'NB',to_date('16.08.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','PDF','PDF','PDF','Filtype PDF','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','PDFA','PDFA','PDFA','Filtype PDFA','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','XML','XML','XML','Filtype XML','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','AFP','AFP','AFP','Filtype AFP','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','AXML','AXML','AXML','Filtype AXML','NB',to_date('06.07.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','DLF','DLF','DLF','Filtype DLF','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','DOC','DOC','DOC','Filtype DOC','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','DOCX','DOCX','DOCX','Filtype DOCX','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','JPEG','JPEG','JPEG','Filtype JPEG','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','RTF','RTF','RTF','Filtype RTF','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','TIFF','TIFF','TIFF','Filtype TIFF','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','XLS','XLS','XLS','Filtype XLS','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','XLSX','XLSX','XLSX','Filtype XLSX','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'ARKIV_FILTYPE','-',null,'Ikke definert','Ikke definert','NB',to_date('01.07.2006','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'KONFIG_VERDI_TYPE','BOOLEAN',null,'Boolske verdier','Støtter J(a) / N(ei) flagg','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'KONFIG_VERDI_TYPE','PERIOD',null,'Periode verdier','ISO 8601 Periode verdier.  Eks. P10M (10 måneder), P1D (1 dag) ','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'KONFIG_VERDI_TYPE','DURATION',null,'Periode verdier','ISO 8601 Duration (tid) verdier.  Eks. PT1H (1 time), PT1M (1 minutt) ','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'KONFIG_VERDI_TYPE','INTEGER',null,'Heltall','Heltallsverdier (positiv/negativ)','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'KONFIG_VERDI_TYPE','STRING',null,'Streng verdier',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'KONFIG_VERDI_TYPE','URI',null,'Uniform Resource Identifier','URI for å angi id til en ressurs','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'KONFIG_VERDI_GRUPPE','INGEN',null,'-','Ingen gruppe definert (default).  Brukes istdf. NULL siden dette inngår i en Primary Key. Koder som ikke er del av en gruppe må alltid være unike.','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','KLGA','KA','Klage eller anke','Klage eller anke','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KODELISTE.nextval,'DOKUMENT_KATEGORI','ITSKJ','IS','Ikke tolkbart skjema','Ikke tolkbart skjema','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
--  INSERTING into KODEVERK

Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('BEHANDLING_TEMA','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Behandlingstema','9','Behandlingstema','N','N','Behandlingstema','NAV Behandlingstema');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('TEMA','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Tema','2','Tema','N','N','Tema','NAV Tema');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('DOKUMENT_TYPE_ID','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/DokumentTypeId-er','2','DokumentTypeId-er','J','N','DokumentTypeId-er','Typen til et mottatt dokument. Dette er et subset av DokumentTyper; inngående dokumenter, for eksempel søknad, terminbekreftelse o.l');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('ARKIV_FILTYPE','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Arkivfiltyper','4','Arkivfiltyper','N','N','Arkivfiltyper','NAV Arkivfiltyper');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('KONFIG_VERDI_GRUPPE','VL',null,null,null,'N','N','KonfigVerdiGruppe','Angir en gruppe konfigurerbare verdier tilhører. Det åpner for å kunne ha lister og Maps av konfigurerbare verdier');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('KONFIG_VERDI_TYPE','VL',null,null,null,'N','N','KonfigVerdiType','Angir type den konfigurerbare verdien er av slik at dette kan brukes til validering og f-- stilling.');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('DOKUMENT_KATEGORI','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Dokumentkategorier','1','Dokumentkategorier','N','N','Dokumentkategorier','NAV Dokumentkategorier');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('FAGSYSTEM','GSak',null,null,'Fagsystemer','N','N','Fagsystemer','NAV Fagsystemer');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('MOTTAK_KANAL','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Mottakskanaler','1','Mottakskanaler','N','N','Mottakskanaler','NAV Mottakskanaler');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('VARIANT_FORMAT','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Variantformater','1','Variantformater','N','N','Variantformater','NAV Variantformater');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('RELATERT_YTELSE_TEMA','Arena',null,null,null,'N','N','RelatertYtelseTema','Kodeverk for tema på relaterte ytelser.');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE) values ('RELATERT_YTELSE_BEH_TEMA','Arena',null,null,null,'N','N','RelatertYtelseBehandlingTema','Kodeverk for behandlingstema på relaterte ytelser');
--  INSERTING into KONFIG_VERDI

Insert into KONFIG_VERDI (ID,KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KONFIG_VERDI.nextval,'infotrygd.sak.gyldig.periode','INGEN','P10M',to_date('01.01.2016','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KONFIG_VERDI (ID,KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KONFIG_VERDI.nextval,'gsak.ehentsid.fordelingsoppgaver','INGEN','2820',to_date('01.01.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KONFIG_VERDI (ID,KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KONFIG_VERDI.nextval,'infotrygd.inntektsmelding.startdato.akseptert.diff','INGEN','P4D',to_date('01.01.2016','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KONFIG_VERDI (ID,KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KONFIG_VERDI.nextval,'infotrygd.annen.part.gyldig.periode','INGEN','P18M',to_date('01.01.2016','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KONFIG_VERDI (ID,KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KONFIG_VERDI.nextval,'foreldrepenger.startdato','INGEN','2019-01-01',to_date('12.12.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KONFIG_VERDI (ID,KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM) values (SEQ_KONFIG_VERDI.nextval,'funksjonelt.tidsoffset.offset','INGEN','P0D',to_date('12.12.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
--  INSERTING into KONFIG_VERDI_KODE

Insert into KONFIG_VERDI_KODE (KODE,KONFIG_GRUPPE,NAVN,KONFIG_TYPE,BESKRIVELSE) values ('infotrygd.sak.gyldig.periode','INGEN','Tidsperiode i mnd for saker i infotrygd','INTEGER','Tidsperiode for sjekk av saker mot infotrygd. Oppgitt i måneder');
Insert into KONFIG_VERDI_KODE (KODE,KONFIG_GRUPPE,NAVN,KONFIG_TYPE,BESKRIVELSE) values ('gsak.ehentsid.fordelingsoppgaver','INGEN','EnhetsId til bruk for fordelingsoppgaver','STRING','EnhetsId til bruk for fordelingsoppgaver');
Insert into KONFIG_VERDI_KODE (KODE,KONFIG_GRUPPE,NAVN,KONFIG_TYPE,BESKRIVELSE) values ('infotrygd.inntektsmelding.startdato.akseptert.diff','INGEN','Max diff på startdato på dok og infotrygdsak','INTEGER','Akseptert differanse ved sjekk av startdato på inntektsmelding mot infotrygdsaker. Oppgitt i dager');
Insert into KONFIG_VERDI_KODE (KODE,KONFIG_GRUPPE,NAVN,KONFIG_TYPE,BESKRIVELSE) values ('infotrygd.annen.part.gyldig.periode','INGEN','Tidsperiode i mnd for annen part saker i infotrygd','INTEGER','Tidsperiode for sjekk av annen part saker mot infotrygd. Oppgitt i måneder');
Insert into KONFIG_VERDI_KODE (KODE,KONFIG_GRUPPE,NAVN,KONFIG_TYPE,BESKRIVELSE) values ('foreldrepenger.startdato','INGEN','Startdato for journalføring','STRING','F.o.m startdato for når journalføring skal gjøres gjennom VL');
Insert into KONFIG_VERDI_KODE (KODE,KONFIG_GRUPPE,NAVN,KONFIG_TYPE,BESKRIVELSE) values ('funksjonelt.tidsoffset.offset','INGEN','Offset for funksjonell nåtid','PERIOD','Offset antall dager for funksjonelt nå-tidspunkt');

--  INSERTING into PROSESS_TASK_FEILHAND

Insert into PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE,INPUT_VARIABEL1,INPUT_VARIABEL2) values ('DEFAULT','Eksponentiell back-off med tak',null,null,null);
Insert into PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE,INPUT_VARIABEL1,INPUT_VARIABEL2) values ('ÅPNINGSTID','Åpningstidsbasert feilhåndtering','Åpningstidsbasert feilhåndtering. INPUT_VARIABEL1 = åpningstid og INPUT_VARIABEL2 = stengetid','7','18');
Insert into PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE,INPUT_VARIABEL1,INPUT_VARIABEL2) values ('TIL_GSAK_BACKOFF','Til Gsak ved funksjonell feil','Send sak til manuell journalføring hos gsak dersom det oppstår en funksjonell feil. Andre feil håndters som for DEFAULT.',null,null);
Insert into PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE,INPUT_VARIABEL1,INPUT_VARIABEL2) values ('TIL_GSAK_ÅPNINGSTID','Til Gsak ved funksjonell feil','Send sak til manuell journalføring hos gsak dersom det oppstår en funksjonell feil. Andre feil håndters som for ÅPNINGSTID.','7','18');
--  INSERTING into PROSESS_TASK_TYPE

Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.svangerskapspenger','Håndter svangerskapspenger','3','30','DEFAULT','Task som håndterer dokumenter for svangerskapspenger',null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.slettForsendelse','Slett forsendelse etter journalføring','3','30','DEFAULT','Task som sletter forsendelse metadata og dokument etter journalføring',null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.opprettSak','Oppretter ny sak internt Vedtaksløsningen','3','30','DEFAULT',null,null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('integrasjon.gsak.opprettOppgave','Oppretter Oppgave i GSak','2','30','DEFAULT',null,null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.hentFraJoark','Henter metadata og xml fra Joark','3','30','DEFAULT',null,null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.hentOgVurderVLSak','Forsøker å finne matchende sak I repo','3','30','DEFAULT',null,null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.hentOgVurderInfotrygdSak','Forsøker å finne matchende sak i GSAK/Infotrygd','3','30','ÅPNINGSTID',null,null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.tilJournalforing','Setter oppgaven klar til journal','3','30','DEFAULT',null,null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.klargjoering','Klar for klargjoring','3','60','DEFAULT','Task som setter oppgaven klar for klargjøring',null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.behandleDokumentForsendelse','Behandle dokumentforsendelse','3','30','DEFAULT','Task som fordeler dokumentforsendelse',null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('fordeling.midlJournalforing','Midlertidig journalføring før oppgave','3','30','DEFAULT','Task som midlertidig journalfører forsendelse',null);
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,FEILHANDTERING_ALGORITME,BESKRIVELSE,CRON_EXPRESSION) values ('vedlikehold.scheduler','Planlagte oppgaver','3','30','DEFAULT','Task som utfører daglig vedlikehold',null);
--------------------------------------------------------
--  DDL for Index IDX_ARKIV_FILTYPE
--------------------------------------------------------

  CREATE INDEX "IDX_ARKIV_FILTYPE" ON "DOKUMENT" ("ARKIV_FILTYPE") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_DOKUMENT_TYPE_ID
--------------------------------------------------------

  CREATE INDEX "IDX_DOKUMENT_TYPE_ID" ON "DOKUMENT" ("DOKUMENT_TYPE_ID") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KODELISTE_1
--------------------------------------------------------

  CREATE INDEX "IDX_KODELISTE_1" ON "KODELISTE" ("KODE") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KODELISTE_2
--------------------------------------------------------

  CREATE INDEX "IDX_KODELISTE_2" ON "KODELISTE" ("OFFISIELL_KODE") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KODELISTE_3
--------------------------------------------------------

  CREATE INDEX "IDX_KODELISTE_3" ON "KODELISTE" ("GYLDIG_FOM") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KODELISTE_6
--------------------------------------------------------

  CREATE INDEX "IDX_KODELISTE_6" ON "KODELISTE" ("KODEVERK") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KONFIG_VERDI_KODE_6
--------------------------------------------------------

  CREATE INDEX "IDX_KONFIG_VERDI_KODE_6" ON "KONFIG_VERDI_KODE" ("KONFIG_TYPE") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KONFIG_VERDI_KODE_7
--------------------------------------------------------

  CREATE INDEX "IDX_KONFIG_VERDI_KODE_7" ON "KONFIG_VERDI_KODE" ("KONFIG_GRUPPE") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KONFIG_VERDI_1
--------------------------------------------------------

  CREATE INDEX "IDX_KONFIG_VERDI_1" ON "KONFIG_VERDI" ("GYLDIG_FOM", "GYLDIG_TOM") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KONFIG_VERDI_2
--------------------------------------------------------

  CREATE INDEX "IDX_KONFIG_VERDI_2" ON "KONFIG_VERDI" ("KONFIG_GRUPPE") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_KONFIG_VERDI_3
--------------------------------------------------------

  CREATE INDEX "IDX_KONFIG_VERDI_3" ON "KONFIG_VERDI" ("KONFIG_KODE") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_PROSESS_TASK_TYPE_1
--------------------------------------------------------

  CREATE INDEX "IDX_PROSESS_TASK_TYPE_1" ON "PROSESS_TASK_TYPE" ("FEILHANDTERING_ALGORITME") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_PROSESS_TASK_2
--------------------------------------------------------

  CREATE INDEX "IDX_PROSESS_TASK_2" ON "PROSESS_TASK" ("TASK_TYPE") 
  ;
--------------------------------------------------------
--  DDL for Index IDX_PROSESS_TASK_6
--------------------------------------------------------

  CREATE INDEX "IDX_PROSESS_TASK_6" ON "PROSESS_TASK" ("BLOKKERT_AV") 
  ;
--------------------------------------------------------
--  DDL for Index PK_DOKUMENT
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_DOKUMENT" ON "DOKUMENT" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index PK_DOKUMENT_METADATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_DOKUMENT_METADATA" ON "DOKUMENT_METADATA" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index PK_KODELISTE
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_KODELISTE" ON "KODELISTE" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index PK_KODEVERK
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_KODEVERK" ON "KODEVERK" ("KODE") 
  ;
--------------------------------------------------------
--  DDL for Index PK_KONFIG_VERDI
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_KONFIG_VERDI" ON "KONFIG_VERDI" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index PK_KONFIG_VERDI_KODE
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_KONFIG_VERDI_KODE" ON "KONFIG_VERDI_KODE" ("KODE", "KONFIG_GRUPPE") 
  ;
--------------------------------------------------------
--  DDL for Index PK_PROSESS_TASK
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_PROSESS_TASK" ON "PROSESS_TASK" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index PK_PROSESS_TASK_FEILHAND
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_PROSESS_TASK_FEILHAND" ON "PROSESS_TASK_FEILHAND" ("KODE") 
  ;
--------------------------------------------------------
--  DDL for Index PK_PROSESS_TASK_TYPE
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_PROSESS_TASK_TYPE" ON "PROSESS_TASK_TYPE" ("KODE") 
  ;
--------------------------------------------------------
--  DDL for Index UIDX_KONFIG_VERDI_1
--------------------------------------------------------

  CREATE UNIQUE INDEX "UIDX_KONFIG_VERDI_1" ON "KONFIG_VERDI" ("KONFIG_GRUPPE", "GYLDIG_TOM", "KONFIG_KODE") 
  ;
--------------------------------------------------------
--  Constraints for Table DOKUMENT
--------------------------------------------------------

  ALTER TABLE "DOKUMENT" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
  ALTER TABLE "DOKUMENT" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
  ALTER TABLE "DOKUMENT" ADD CONSTRAINT "PK_DOKUMENT" PRIMARY KEY ("ID")
  USING INDEX  ENABLE;
--------------------------------------------------------
--  Constraints for Table DOKUMENT_METADATA
--------------------------------------------------------

  ALTER TABLE "DOKUMENT_METADATA" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
  ALTER TABLE "DOKUMENT_METADATA" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
  ALTER TABLE "DOKUMENT_METADATA" ADD CONSTRAINT "PK_DOKUMENT_METADATA" PRIMARY KEY ("ID")
  USING INDEX  ENABLE;
  ALTER TABLE "DOKUMENT_METADATA" ADD CONSTRAINT "CHK_UNIQUE_FORS_DOKUMENT_MT" UNIQUE ("FORSENDELSE_ID")
  USING INDEX (CREATE UNIQUE INDEX "IDX_DOKUMENT" ON "DOKUMENT_METADATA" ("FORSENDELSE_ID") 
  )  ENABLE;
--------------------------------------------------------
--  Constraints for Table KODELISTE
--------------------------------------------------------

  ALTER TABLE "KODELISTE" ADD CONSTRAINT "CHK_UNIQUE_KODELISTE" UNIQUE ("KODE", "KODEVERK")
  USING INDEX (CREATE UNIQUE INDEX "UIDX_KODELISTE_1" ON "KODELISTE" ("KODE", "KODEVERK") 
  )  ENABLE;
  ALTER TABLE "KODELISTE" ADD CONSTRAINT "PK_KODELISTE" PRIMARY KEY ("ID")
  USING INDEX  ENABLE;
  ALTER TABLE "KODELISTE" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
  ALTER TABLE "KODELISTE" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
  ALTER TABLE "KODELISTE" MODIFY ("GYLDIG_TOM" NOT NULL ENABLE);
  ALTER TABLE "KODELISTE" MODIFY ("GYLDIG_FOM" NOT NULL ENABLE);
  ALTER TABLE "KODELISTE" MODIFY ("KODE" NOT NULL ENABLE);
  ALTER TABLE "KODELISTE" MODIFY ("KODEVERK" NOT NULL ENABLE);
  ALTER TABLE "KODELISTE" MODIFY ("ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table KODEVERK
--------------------------------------------------------

  ALTER TABLE "KODEVERK" MODIFY ("NAVN" NOT NULL ENABLE);
  ALTER TABLE "KODEVERK" MODIFY ("KODEVERK_SYNK_EKSISTERENDE" NOT NULL ENABLE);
  ALTER TABLE "KODEVERK" MODIFY ("KODEVERK_SYNK_NYE" NOT NULL ENABLE);
  ALTER TABLE "KODEVERK" MODIFY ("KODEVERK_EIER" NOT NULL ENABLE);
  ALTER TABLE "KODEVERK" MODIFY ("KODE" NOT NULL ENABLE);
  ALTER TABLE "KODEVERK" ADD CONSTRAINT "PK_KODEVERK" PRIMARY KEY ("KODE")
  USING INDEX  ENABLE;
  ALTER TABLE "KODEVERK" ADD CHECK (kodeverk_synk_eksisterende IN ('J', 'N')) ENABLE;
  ALTER TABLE "KODEVERK" ADD CHECK (kodeverk_synk_nye IN ('J', 'N')) ENABLE;
  ALTER TABLE "KODEVERK" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
  ALTER TABLE "KODEVERK" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table KONFIG_VERDI
--------------------------------------------------------

  ALTER TABLE "KONFIG_VERDI" MODIFY ("KL_KONFIG_VERDI_GRUPPE" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI" ADD CONSTRAINT "PK_KONFIG_VERDI" PRIMARY KEY ("ID")
  USING INDEX  ENABLE;
  ALTER TABLE "KONFIG_VERDI" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI" MODIFY ("GYLDIG_TOM" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI" MODIFY ("GYLDIG_FOM" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI" MODIFY ("KONFIG_GRUPPE" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI" MODIFY ("KONFIG_KODE" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI" MODIFY ("ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table KONFIG_VERDI_KODE
--------------------------------------------------------

  ALTER TABLE "KONFIG_VERDI_KODE" MODIFY ("KL_KONFIG_VERDI_GRUPPE" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI_KODE" MODIFY ("KL_KONFIG_VERDI_TYPE" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI_KODE" ADD CONSTRAINT "PK_KONFIG_VERDI_KODE" PRIMARY KEY ("KODE", "KONFIG_GRUPPE")
  USING INDEX  ENABLE;
  ALTER TABLE "KONFIG_VERDI_KODE" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI_KODE" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI_KODE" MODIFY ("KONFIG_TYPE" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI_KODE" MODIFY ("NAVN" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI_KODE" MODIFY ("KONFIG_GRUPPE" NOT NULL ENABLE);
  ALTER TABLE "KONFIG_VERDI_KODE" MODIFY ("KODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table PROSESS_TASK
--------------------------------------------------------

  ALTER TABLE "PROSESS_TASK" MODIFY ("ID" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK" MODIFY ("TASK_TYPE" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK" MODIFY ("PRIORITET" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK" MODIFY ("STATUS" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK" MODIFY ("TASK_SEKVENS" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK" MODIFY ("VERSJON" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK" ADD CONSTRAINT "CHK_PROSESS_TASK_STATUS" CHECK (status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG')) ENABLE;
  ALTER TABLE "PROSESS_TASK" ADD CONSTRAINT "PK_PROSESS_TASK" PRIMARY KEY ("ID")
  USING INDEX  ENABLE;
  ALTER TABLE "PROSESS_TASK" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table PROSESS_TASK_FEILHAND
--------------------------------------------------------

  ALTER TABLE "PROSESS_TASK_FEILHAND" ADD CONSTRAINT "PK_PROSESS_TASK_FEILHAND" PRIMARY KEY ("KODE")
  USING INDEX  ENABLE;
  ALTER TABLE "PROSESS_TASK_FEILHAND" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK_FEILHAND" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK_FEILHAND" MODIFY ("NAVN" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK_FEILHAND" MODIFY ("KODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table PROSESS_TASK_TYPE
--------------------------------------------------------

  ALTER TABLE "PROSESS_TASK_TYPE" ADD CONSTRAINT "PK_PROSESS_TASK_TYPE" PRIMARY KEY ("KODE")
  USING INDEX  ENABLE;
  ALTER TABLE "PROSESS_TASK_TYPE" MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK_TYPE" MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK_TYPE" MODIFY ("FEIL_SEK_MELLOM_FORSOEK" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK_TYPE" MODIFY ("FEIL_MAKS_FORSOEK" NOT NULL ENABLE);
  ALTER TABLE "PROSESS_TASK_TYPE" MODIFY ("KODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table DOKUMENT
--------------------------------------------------------

  ALTER TABLE "DOKUMENT" ADD CONSTRAINT "FK_MOTTATT_DOKUMENT_01" FOREIGN KEY ("DOKUMENT_TYPE_ID", "KL_DOKUMENT_TYPE_ID")
	  REFERENCES "KODELISTE" ("KODE", "KODEVERK") ENABLE;
  ALTER TABLE "DOKUMENT" ADD CONSTRAINT "FK_MOTTATT_DOKUMENT_02" FOREIGN KEY ("ARKIV_FILTYPE", "KL_ARKIV_FILTYPE")
	  REFERENCES "KODELISTE" ("KODE", "KODEVERK") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table KODELISTE
--------------------------------------------------------

  ALTER TABLE "KODELISTE" ADD CONSTRAINT "FK_KODELISTE_01" FOREIGN KEY ("KODEVERK")
	  REFERENCES "KODEVERK" ("KODE") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table KONFIG_VERDI
--------------------------------------------------------

  ALTER TABLE "KONFIG_VERDI" ADD CONSTRAINT "FK_KONFIG_VERDI_1" FOREIGN KEY ("KONFIG_GRUPPE", "KL_KONFIG_VERDI_GRUPPE")
	  REFERENCES "KODELISTE" ("KODE", "KODEVERK") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table KONFIG_VERDI_KODE
--------------------------------------------------------

  ALTER TABLE "KONFIG_VERDI_KODE" ADD CONSTRAINT "FK_KONFIG_VERDI_KODE_82" FOREIGN KEY ("KONFIG_GRUPPE", "KL_KONFIG_VERDI_GRUPPE")
	  REFERENCES "KODELISTE" ("KODE", "KODEVERK") ENABLE;
  ALTER TABLE "KONFIG_VERDI_KODE" ADD CONSTRAINT "FK_KONFIG_VERDI_KODE_83" FOREIGN KEY ("KONFIG_TYPE", "KL_KONFIG_VERDI_TYPE")
	  REFERENCES "KODELISTE" ("KODE", "KODEVERK") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table PROSESS_TASK
--------------------------------------------------------

  ALTER TABLE "PROSESS_TASK" ADD CONSTRAINT "FK_PROSESS_TASK_1" FOREIGN KEY ("TASK_TYPE")
	  REFERENCES "PROSESS_TASK_TYPE" ("KODE") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table PROSESS_TASK_TYPE
--------------------------------------------------------

  ALTER TABLE "PROSESS_TASK_TYPE" ADD CONSTRAINT "FK_PROSESS_TASK_TYPE_1" FOREIGN KEY ("FEILHANDTERING_ALGORITME")
	  REFERENCES "PROSESS_TASK_FEILHAND" ("KODE") ENABLE;

/*

declare 
   inst_rank number;
   begin
       select max("installed_rank")+1 into inst_rank from "schema_version";
       Insert into "schema_version" ("installed_rank","version","description","type","script","checksum","installed_by","execution_time","success") values (inst_rank,'3','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',null,'FPFORDEL',0,'1');
   end;
/

    -- 2. 
    create table "schema_version_history"  as SELECT * FROM "schema_version" where "version" < to_char('3');
	     
     -- 3.
     delete from  "schema_version" where "version" < to_char('3');

     -- 4.
     commit;


*/
