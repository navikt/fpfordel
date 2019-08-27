Declare

  antall number;
  opprett_tmp_tabell varchar2(75) := 'Create table PROSESS_TASK_TMP as select * from PROSESS_TASK';

  dropp_process_tabell varchar2(75) := 'Drop table PROSESS_TASK cascade constraints';

  copydata varchar2(75) := 'Insert into PROSESS_TASK select * from PROSESS_TASK_TMP';

  dropp_tmp_tabell varchar2(75) := 'Drop table PROSESS_TASK_TMP cascade constraints';

  opprett_process_tabell varchar2(2000) := 'CREATE TABLE PROSESS_TASK ' ||
                                       ' ( ID NUMBER(19,0) NOT NULL ENABLE, ' ||
                                       ' TASK_TYPE VARCHAR2(200 CHAR) NOT NULL ENABLE, ' ||
                                       ' PRIORITET NUMBER(3,0) DEFAULT 0 NOT NULL ENABLE, ' ||
                                       ' STATUS VARCHAR2(20 CHAR) DEFAULT ''KLAR'' NOT NULL ENABLE, ' ||
                                       ' TASK_PARAMETERE VARCHAR2(4000 CHAR), ' ||
                                       ' TASK_PAYLOAD CLOB, ' ||
                                       ' TASK_GRUPPE VARCHAR2(250 CHAR), ' ||
                                       ' TASK_SEKVENS VARCHAR2(100 CHAR) DEFAULT ''1'' NOT NULL ENABLE, ' ||
                                       ' NESTE_KJOERING_ETTER TIMESTAMP (0) DEFAULT current_timestamp, ' ||
                                       ' FEILEDE_FORSOEK NUMBER(5,0) DEFAULT 0, ' ||
                                       ' SISTE_KJOERING_TS TIMESTAMP (6), ' ||
                                       ' SISTE_KJOERING_FEIL_KODE VARCHAR2(50 CHAR), ' ||
                                       ' SISTE_KJOERING_FEIL_TEKST CLOB, ' ||
                                       ' SISTE_KJOERING_SERVER VARCHAR2(50 CHAR), ' ||
                                       ' VERSJON NUMBER(19,0) DEFAULT 0 NOT NULL ENABLE, ' ||
                                       ' CONSTRAINT CHK_PROSESS_TASK_STATUS CHECK (status IN (''KLAR'', ''FEILET'', ''VENTER_SVAR'', ''SUSPENDERT'', ''FERDIG'')) ENABLE, ' ||
                                       ' CONSTRAINT PK_PROSESS_TASK PRIMARY KEY (ID), ' ||
                                       ' CONSTRAINT FK_PROSESS_TASK_1 FOREIGN KEY (TASK_TYPE)REFERENCES PROSESS_TASK_TYPE (KODE) ENABLE) enable row movement';

  legg_partisjon varchar2(255) := ' PARTITION by list (status)(' ||
                                      ' PARTITION status_ferdig values (''FERDIG''),' ||
                                      ' PARTITION status_feilet values (''FEILET''),' ||
                                      ' PARTITION status_klar values(''KLAR'', ''VENTER_SVAR'', ''SUSPENDERT''))';

BEGIN

  select count(*) into antall from USER_TABLES where TABLE_NAME = 'PROSESS_TASK';
  IF (antall = 1) THEN
    execute immediate opprett_tmp_tabell;
    execute immediate dropp_process_tabell;
  END IF;

  IF (DBMS_DB_VERSION.VERSION < 12) THEN
    execute immediate opprett_process_tabell;
  ELSE
    execute immediate opprett_process_tabell || legg_partisjon;
  END IF;

  IF (antall = 1) THEN
    execute immediate copydata;
    execute immediate dropp_tmp_tabell;
  END IF;
END;
