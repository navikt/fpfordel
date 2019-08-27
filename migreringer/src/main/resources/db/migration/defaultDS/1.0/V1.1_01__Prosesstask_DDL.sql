--------------------------------------------------------
-- DDL for Prosesstask spesifikt for VL-fordeling
-- Viktig å merke seg her at alt av DDL relatert til prosesstask-biten er ikke eid av dette prosjektet, DDL eies
-- av no.nav.vedtak.felles:felles-behandlingsprosess. Endringer i DDL skal gjøres i prosjektet som eier DDLen.
--------------------------------------------------------

CREATE TABLE "PROSESS_TASK"
(
  "ID"                        NUMBER(19, 0),
  "TASK_TYPE"                 VARCHAR2(200 CHAR),
  "PRIORITET"                 NUMBER(3, 0)       DEFAULT 0,
  "STATUS"                    VARCHAR2(20 CHAR)  DEFAULT 'KLAR',
  "TASK_PARAMETERE"           VARCHAR2(4000 CHAR),
  "TASK_PAYLOAD"              CLOB,
  "TASK_GRUPPE"               VARCHAR2(250 CHAR),
  "TASK_SEKVENS"              VARCHAR2(100 CHAR) DEFAULT '1',
  "NESTE_KJOERING_ETTER"      TIMESTAMP(0)       DEFAULT current_timestamp,
  "FEILEDE_FORSOEK"           NUMBER(5, 0)       DEFAULT 0,
  "SISTE_KJOERING_TS"         TIMESTAMP(6),
  "SISTE_KJOERING_FEIL_KODE"  VARCHAR2(50 CHAR),
  "SISTE_KJOERING_FEIL_TEKST" CLOB,
  "SISTE_KJOERING_SERVER"     VARCHAR2(50 CHAR),
  "VERSJON"                   NUMBER(19, 0)      DEFAULT 0
) ENABLE ROW MOVEMENT;

COMMENT ON COLUMN "PROSESS_TASK"."ID" IS 'Primary Key';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_TYPE" IS 'navn på task. Brukes til å matche riktig implementasjon';
COMMENT ON COLUMN "PROSESS_TASK"."PRIORITET" IS 'prioritet på task.  Høyere tall har høyere prioritet';
COMMENT ON COLUMN "PROSESS_TASK"."STATUS" IS 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_PARAMETERE" IS 'parametere angitt for en task';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_PAYLOAD" IS 'inputdata for en task';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_GRUPPE" IS 'angir en unik id som grupperer flere ';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_SEKVENS" IS 'angir rekkefølge på task innenfor en gruppe ';
COMMENT ON COLUMN "PROSESS_TASK"."NESTE_KJOERING_ETTER" IS 'tasken skal ikke kjøeres før tidspunkt er passert';
COMMENT ON COLUMN "PROSESS_TASK"."FEILEDE_FORSOEK" IS 'antall feilede forsøk';
COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_TS" IS 'siste gang tasken ble forsøkt kjørt';
COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_FEIL_KODE" IS 'siste feilkode tasken fikk';
COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_FEIL_TEKST" IS 'siste feil tasken fikk';
COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_SERVER" IS 'navn på node som sist kjørte en task (server@pid)';
COMMENT ON COLUMN "PROSESS_TASK"."VERSJON" IS 'angir versjon for optimistisk låsing';
COMMENT ON TABLE "PROSESS_TASK" IS 'Inneholder tasks som skal kjøres i bakgrunnen';

--------------------------------------------------------
--  DDL for Table PROSESS_TASK_FEILHAND
--------------------------------------------------------

CREATE TABLE "PROSESS_TASK_FEILHAND"
(
  "KODE"            VARCHAR2(20 CHAR),
  "NAVN"            VARCHAR2(50 CHAR),
  "BESKRIVELSE"     VARCHAR2(2000 CHAR),
  "OPPRETTET_AV"    VARCHAR2(20 CHAR) DEFAULT 'VL',
  "OPPRETTET_TID"   TIMESTAMP(3)      DEFAULT systimestamp,
  "ENDRET_AV"       VARCHAR2(20 CHAR),
  "ENDRET_TID"      TIMESTAMP(3),
  "INPUT_VARIABEL1" NUMBER,
  "INPUT_VARIABEL2" NUMBER
);

COMMENT ON COLUMN "PROSESS_TASK_FEILHAND"."KODE" IS 'Kodeverk Primary Key';
COMMENT ON COLUMN "PROSESS_TASK_FEILHAND"."NAVN" IS 'Lesbart navn på type feilhåndtering brukt i prosesstask';
COMMENT ON COLUMN "PROSESS_TASK_FEILHAND"."BESKRIVELSE" IS 'Utdypende beskrivelse av koden';
COMMENT ON TABLE "PROSESS_TASK_FEILHAND" IS 'Kodetabell for feilhåndterings-metoder. For eksempel antall ganger å prøve på nytt og til hvilke tidspunkt';

--------------------------------------------------------
--  DDL for Table PROSESS_TASK_TYPE
--------------------------------------------------------

CREATE TABLE "PROSESS_TASK_TYPE"
(
  "KODE"                     VARCHAR2(50 CHAR),
  "NAVN"                     VARCHAR2(50 CHAR),
  "FEIL_MAKS_FORSOEK"        NUMBER(10, 0)      DEFAULT 1,
  "FEIL_SEK_MELLOM_FORSOEK"  NUMBER(10, 0)      DEFAULT 30,
  "FEILHANDTERING_ALGORITME" VARCHAR2(200 CHAR) DEFAULT 'DEFAULT',
  "BESKRIVELSE"              VARCHAR2(2000 CHAR),
  "OPPRETTET_AV"             VARCHAR2(20 CHAR)  DEFAULT 'VL',
  "OPPRETTET_TID"            TIMESTAMP(3)       DEFAULT systimestamp,
  "ENDRET_AV"                VARCHAR2(20 CHAR),
  "ENDRET_TID"               TIMESTAMP(3)
);

COMMENT ON COLUMN "PROSESS_TASK_TYPE"."KODE" IS 'Kodeverk Primary Key';
COMMENT ON COLUMN "PROSESS_TASK_TYPE"."NAVN" IS 'Lesbart navn på prosesstasktype';
COMMENT ON COLUMN "PROSESS_TASK_TYPE"."FEIL_MAKS_FORSOEK" IS 'MISSING COLUMN COMMENT';
COMMENT ON COLUMN "PROSESS_TASK_TYPE"."FEIL_SEK_MELLOM_FORSOEK" IS 'MISSING COLUMN COMMENT';
COMMENT ON COLUMN "PROSESS_TASK_TYPE"."FEILHANDTERING_ALGORITME" IS 'FK: PROSESS_TASK_FEILHAND';
COMMENT ON COLUMN "PROSESS_TASK_TYPE"."BESKRIVELSE" IS 'Utdypende beskrivelse av koden';
COMMENT ON TABLE "PROSESS_TASK_TYPE" IS 'Kodetabell for typer prosesser med beskrivelse og informasjon om hvilken feilhåndteringen som skal benyttes';

CREATE INDEX "IDX_PROSESS_TASK_2"
  ON "PROSESS_TASK" ("TASK_TYPE");
CREATE INDEX "IDX_PROSESS_TASK_3"
  ON "PROSESS_TASK" ("NESTE_KJOERING_ETTER");
CREATE INDEX "IDX_PROSESS_TASK_5"
  ON "PROSESS_TASK" ("TASK_GRUPPE");
CREATE INDEX "IDX_PROSESS_TASK_1"
  ON "PROSESS_TASK" ("STATUS");
CREATE UNIQUE INDEX "PK_PROSESS_TASK"
  ON "PROSESS_TASK" ("ID");
CREATE UNIQUE INDEX "PK_PROSESS_TASK_FEILHAND"
  ON "PROSESS_TASK_FEILHAND" ("KODE");
CREATE UNIQUE INDEX "PK_PROSESS_TASK_TYPE"
  ON "PROSESS_TASK_TYPE" ("KODE");
CREATE INDEX "IDX_PROSESS_TASK_TYPE_1"
  ON "PROSESS_TASK_TYPE" ("FEILHANDTERING_ALGORITME");

--------------------------------------------------------
--  Constraints for Table PROSESS_TASK
--------------------------------------------------------

ALTER TABLE "PROSESS_TASK"
  ADD CONSTRAINT "PK_PROSESS_TASK" PRIMARY KEY ("ID") ENABLE;
ALTER TABLE "PROSESS_TASK"
  ADD CONSTRAINT "CHK_PROSESS_TASK_STATUS" CHECK (status IN
                                                  ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'FERDIG')) ENABLE;
ALTER TABLE "PROSESS_TASK"
  MODIFY ("VERSJON" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK"
  MODIFY ("TASK_SEKVENS" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK"
  MODIFY ("STATUS" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK"
  MODIFY ("PRIORITET" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK"
  MODIFY ("TASK_TYPE" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK"
  MODIFY ("ID" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table PROSESS_TASK_FEILHAND
--------------------------------------------------------

ALTER TABLE "PROSESS_TASK_FEILHAND"
  ADD CONSTRAINT "PK_PROSESS_TASK_FEILHAND" PRIMARY KEY ("KODE") ENABLE;
ALTER TABLE "PROSESS_TASK_FEILHAND"
  MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK_FEILHAND"
  MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK_FEILHAND"
  MODIFY ("NAVN" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK_FEILHAND"
  MODIFY ("KODE" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table PROSESS_TASK_TYPE
--------------------------------------------------------

ALTER TABLE "PROSESS_TASK_TYPE"
  ADD CONSTRAINT "PK_PROSESS_TASK_TYPE" PRIMARY KEY ("KODE") ENABLE;
ALTER TABLE "PROSESS_TASK_TYPE"
  MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK_TYPE"
  MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK_TYPE"
  MODIFY ("FEIL_SEK_MELLOM_FORSOEK" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK_TYPE"
  MODIFY ("FEIL_MAKS_FORSOEK" NOT NULL ENABLE);
ALTER TABLE "PROSESS_TASK_TYPE"
  MODIFY ("KODE" NOT NULL ENABLE);

--------------------------------------------------------
--  Ref Constraints for Table PROSESS_TASK
--------------------------------------------------------

ALTER TABLE "PROSESS_TASK"
  ADD CONSTRAINT "FK_PROSESS_TASK_1" FOREIGN KEY ("TASK_TYPE")
REFERENCES "PROSESS_TASK_TYPE" ("KODE") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table PROSESS_TASK_TYPE
--------------------------------------------------------

ALTER TABLE "PROSESS_TASK_TYPE"
  ADD CONSTRAINT "FK_PROSESS_TASK_TYPE_1" FOREIGN KEY ("FEILHANDTERING_ALGORITME")
REFERENCES "PROSESS_TASK_FEILHAND" ("KODE") ENABLE;


CREATE SEQUENCE SEQ_PROSESS_TASK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_PROSESS_TASK_GRUPPE MINVALUE 10000000 START WITH 10000000 INCREMENT BY 1000000 NOCACHE NOCYCLE;