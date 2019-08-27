--------------------------------------------------------
--  DDL for Table KODEVERK
--------------------------------------------------------

CREATE TABLE "KODEVERK"
(
  "KODE"                       VARCHAR2(100 CHAR),
  "KODEVERK_EIER"              VARCHAR2(100 CHAR) DEFAULT 'VL',
  "KODEVERK_EIER_REF"          VARCHAR2(1000 CHAR),
  "KODEVERK_EIER_VER"          VARCHAR2(20 CHAR),
  "KODEVERK_EIER_NAVN"         VARCHAR2(100 CHAR),
  "KODEVERK_SYNK_NYE"          CHAR(1 BYTE)       DEFAULT 'J',
  "KODEVERK_SYNK_EKSISTERENDE" CHAR(1 BYTE)       DEFAULT 'J',
  "NAVN"                       VARCHAR2(256 CHAR),
  "BESKRIVELSE"                VARCHAR2(4000 CHAR),
  "OPPRETTET_AV"               VARCHAR2(200 CHAR) DEFAULT 'VL',
  "OPPRETTET_TID"              TIMESTAMP(3)       DEFAULT systimestamp,
  "ENDRET_AV"                  VARCHAR2(200 CHAR),
  "ENDRET_TID"                 TIMESTAMP(3),
  "SAMMENSATT"                 VARCHAR2(1 CHAR)   DEFAULT 'N'
);

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
COMMENT ON TABLE "KODEVERK" IS 'Registrerte kodeverk. Representerer grupperinger av koder';

--------------------------------------------------------
--  DDL for Index PK_KODEVERK
--------------------------------------------------------

CREATE UNIQUE INDEX "PK_KODEVERK"
  ON "KODEVERK" ("KODE");

--------------------------------------------------------
--  Constraints for Table KODEVERK
--------------------------------------------------------

ALTER TABLE "KODEVERK"
  MODIFY ("NAVN" NOT NULL ENABLE);
ALTER TABLE "KODEVERK"
  MODIFY ("KODEVERK_SYNK_EKSISTERENDE" NOT NULL ENABLE);
ALTER TABLE "KODEVERK"
  MODIFY ("KODEVERK_SYNK_NYE" NOT NULL ENABLE);
ALTER TABLE "KODEVERK"
  MODIFY ("KODEVERK_EIER" NOT NULL ENABLE);
ALTER TABLE "KODEVERK"
  MODIFY ("KODE" NOT NULL ENABLE);
ALTER TABLE "KODEVERK"
  ADD CONSTRAINT "PK_KODEVERK" PRIMARY KEY ("KODE") ENABLE;
ALTER TABLE "KODEVERK"
  ADD CHECK (kodeverk_synk_eksisterende IN ('J', 'N')) ENABLE;
ALTER TABLE "KODEVERK"
  ADD CHECK (kodeverk_synk_nye IN ('J', 'N')) ENABLE;
ALTER TABLE "KODEVERK"
  MODIFY ("OPPRETTET_TID" NOT NULL ENABLE);
ALTER TABLE "KODEVERK"
  MODIFY ("OPPRETTET_AV" NOT NULL ENABLE);
