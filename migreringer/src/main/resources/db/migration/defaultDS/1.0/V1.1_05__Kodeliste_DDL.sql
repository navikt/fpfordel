--------------------------------------------------------
--  DDL for Table KODELISTE
--------------------------------------------------------

CREATE TABLE "KODELISTE"
(
  "ID"             NUMBER(19, 0),
  "KODEVERK"       VARCHAR2(100 CHAR),
  "KODE"           VARCHAR2(100 CHAR),
  "OFFISIELL_KODE" VARCHAR2(1000 CHAR),
  "NAVN"           VARCHAR2(256 CHAR),
  "BESKRIVELSE"    VARCHAR2(4000 CHAR),
  "SPRAK"          VARCHAR2(3 CHAR)   DEFAULT 'NB',
  "GYLDIG_FOM"     DATE               DEFAULT sysdate,
  "GYLDIG_TOM"     DATE               DEFAULT to_date('31.12.9999', 'dd.mm.yyyy'),
  "OPPRETTET_AV"   VARCHAR2(200 CHAR) DEFAULT 'VL',
  "OPPRETTET_TID"  TIMESTAMP(3)       DEFAULT systimestamp,
  "ENDRET_AV"      VARCHAR2(200 CHAR),
  "ENDRET_TID"     TIMESTAMP(3),
  "EKSTRA_DATA"    VARCHAR2(4000 CHAR)
);

CREATE SEQUENCE SEQ_KODELISTE MINVALUE 1 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

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
COMMENT ON TABLE "KODELISTE" IS 'Inneholder lister av koder for alle Kodeverk som benyttes i applikasjonen.  Både offisielle (synkronisert fra sentralt hold i Nav) såvel som interne Kodeverk.  Offisielle koder skiller seg ut ved at nav_offisiell_kode er populert. Følgelig vil gyldig_tom/fom, navn, språk og beskrivelse lastes ned fra Kodeverkklienten eller annen kilde sentralt';

--------------------------------------------------------
--  DDL for Index PK_KODELISTE
--------------------------------------------------------

CREATE UNIQUE INDEX "PK_KODELISTE"
  ON "KODELISTE" ("ID");
--------------------------------------------------------
--  DDL for Index IDX_KODELISTE_1
--------------------------------------------------------

CREATE INDEX "IDX_KODELISTE_1"
  ON "KODELISTE" ("KODE");
--------------------------------------------------------
--  DDL for Index IDX_KODELISTE_2
--------------------------------------------------------

CREATE INDEX "IDX_KODELISTE_2"
  ON "KODELISTE" ("OFFISIELL_KODE");
--------------------------------------------------------
--  DDL for Index IDX_KODELISTE_3
--------------------------------------------------------

CREATE INDEX "IDX_KODELISTE_3"
  ON "KODELISTE" ("GYLDIG_FOM");
--------------------------------------------------------
--  DDL for Index UIDX_KODELISTE_1
--------------------------------------------------------

CREATE UNIQUE INDEX "UIDX_KODELISTE_1"
  ON "KODELISTE" ("KODE", "KODEVERK");
--------------------------------------------------------
--  Constraints for Table KODELISTE
--------------------------------------------------------

ALTER TABLE "KODELISTE"
  ADD CONSTRAINT "CHK_UNIQUE_KODELISTE" UNIQUE ("KODE", "KODEVERK") ENABLE;
ALTER TABLE "KODELISTE"
  ADD CONSTRAINT "PK_KODELISTE" PRIMARY KEY ("ID") ENABLE;
ALTER TABLE "KODELISTE"
  MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
ALTER TABLE "KODELISTE"
  MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
ALTER TABLE "KODELISTE"
  MODIFY ("GYLDIG_TOM" NOT NULL ENABLE);
ALTER TABLE "KODELISTE"
  MODIFY ("GYLDIG_FOM" NOT NULL ENABLE);
ALTER TABLE "KODELISTE"
  MODIFY ("KODE" NOT NULL ENABLE);
ALTER TABLE "KODELISTE"
  MODIFY ("KODEVERK" NOT NULL ENABLE);
ALTER TABLE "KODELISTE"
  MODIFY ("ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table KODELISTE
--------------------------------------------------------

ALTER TABLE "KODELISTE"
  ADD CONSTRAINT "FK_KODELISTE_01" FOREIGN KEY ("KODEVERK")
REFERENCES "KODEVERK" ("KODE") ENABLE;
