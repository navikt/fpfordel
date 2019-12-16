ALTER TABLE DOKUMENT SET UNUSED(KL_DOKUMENT_TYPE_ID);
ALTER TABLE DOKUMENT SET UNUSED(KL_ARKIV_FILTYPE);

drop table kodeliste cascade constraints;
drop table kodeverk cascade constraints;
