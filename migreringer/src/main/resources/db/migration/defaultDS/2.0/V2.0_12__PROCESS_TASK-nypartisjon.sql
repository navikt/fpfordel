alter table prosess_task drop constraint chk_prosess_task_status;
alter table prosess_task add constraint "CHK_PROSESS_TASK_STATUS" check (status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG')) enable;
alter table prosess_task add opprettet_av varchar2(30 char) default 'VL' not null;
alter table prosess_task add OPPRETTET_TID  TIMESTAMP(6) DEFAULT systimestamp NOT NULL;
alter table prosess_task add BLOKKERT_AV NUMBER(19, 0) NULL;

CREATE INDEX "IDX_PROSESS_TASK_6" ON "PROSESS_TASK" ("BLOKKERT_AV") ;
COMMENT ON COLUMN "PROSESS_TASK"."BLOKKERT_AV" IS 'Id til ProsessTask som blokkerer kjøring av denne (når status=VETO)';


-- Ny partisjon: opprettes kun i 12c, XE støtter ikke
BEGIN

  IF (DBMS_DB_VERSION.VERSION > 11) THEN
    execute immediate 'ALTER TABLE PROSESS_TASK MODIFY PARTITION STATUS_KLAR ADD VALUES (''VETO'')';
  END IF;

END;

