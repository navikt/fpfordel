alter table prosess_task drop constraint chk_prosess_task_status;
alter table prosess_task add constraint CHK_PROSESS_TASK_STATUS
    check (status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG', 'KJOERT')) ENABLE ;

DECLARE
    endre_partisjon varchar2(500);
BEGIN
    IF (DBMS_DB_VERSION.VERSION >= 12) THEN
        BEGIN
            endre_partisjon := ' ALTER TABLE prosess_task MODIFY PARTITION status_klar ADD VALUES (''KJOERT'') ';
            execute immediate endre_partisjon;
        END;
    END IF;
END;