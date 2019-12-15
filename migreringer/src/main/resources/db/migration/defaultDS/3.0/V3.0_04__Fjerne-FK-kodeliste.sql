drop index IDX_ARKIV_FILTYPE;
drop index IDX_DOKUMENT_TYPE_ID;

alter table DOKUMENT drop constraint FK_MOTTATT_DOKUMENT_01;
alter table DOKUMENT drop constraint FK_MOTTATT_DOKUMENT_02;

COMMENT ON COLUMN "DOKUMENT"."ARKIV_FILTYPE" IS 'Kodeverdi for innkommende filtype';
COMMENT ON COLUMN "DOKUMENT"."KL_ARKIV_FILTYPE" IS 'Kodeverk for innkommende filtype';
COMMENT ON COLUMN "DOKUMENT"."KL_DOKUMENT_TYPE_ID" IS 'Kodeverk for dokumenttype';