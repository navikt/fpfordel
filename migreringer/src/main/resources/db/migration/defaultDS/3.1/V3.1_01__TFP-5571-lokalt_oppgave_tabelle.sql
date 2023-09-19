create table OPPGAVE
(
    ID NUMBER(19) not null
        constraint PK_OPPGAVE
            primary key,
    OPPGAVE_TYPE VARCHAR2(20 char) not null,
    JOURNALPOST_ID VARCHAR2(32 char) not null,
    BRUKER_ID VARCHAR2(19 char),
    BEHANDLING_TEMA VARCHAR2(20 char),
    PRIORITET VARCHAR2(20 char),
    BESKRIVELSE varchar2(200 char),
    ENHET VARCHAR2(10 char),
    VERSJON INTEGER default 0 not null,
    OPPRETTET_AV VARCHAR2(20 char) default 'VL' not null,
    OPPRETTET_TID TIMESTAMP(3) default systimestamp not null,
    ENDRET_AV VARCHAR2(20 char),
    ENDRET_TID TIMESTAMP(3)
)
/

comment on table OPPGAVE is 'Inneholder oppgaver som skal løses av saksbehandlere i journalføring.'
/
comment on column OPPGAVE.OPPGAVE_TYPE is 'Type av oppgaven f.eks JFR for journalføring. Mappes til Oppgavetype kodeverk.'
/
comment on column OPPGAVE.JOURNALPOST_ID is 'ID til journalposten i JOARK'
/
comment on column OPPGAVE.BRUKER_ID is 'ID til avsenderen av et dokument'
/
comment on column OPPGAVE.BEHANDLING_TEMA is 'Behandlingstema som beskriver ytelsen'
/
comment on column OPPGAVE.PRIORITET is 'Prioritet til oppgaven. HØY, NORM, LAV'
/
comment on column OPPGAVE.BESKRIVELSE is 'Oppgave beskrivelse.'
/
comment on column OPPGAVE.ENHET is 'Tildelt enhet som skal løse oppgaven.'
/
comment on column OPPGAVE.VERSJON is 'Teknisk versjonering av endringer.'
/
