create sequence SEQ_PROSESS_TASK
    minvalue 1000000
    increment by 50
    nocache
/

create sequence SEQ_PROSESS_TASK_GRUPPE
    minvalue 10000000
    increment by 1000000
    nocache
/

create sequence SEQ_DOKUMENT
    increment by 50
    nocache
/

create sequence SEQ_DOKUMENT_METADATA
    increment by 50
    nocache
/

create sequence SEQ_JOURNALPOST
    increment by 50
    nocache
/

create table PROSESS_TASK
(
    ID NUMBER(19) not null
        constraint PK_PROSESS_TASK
        primary key,
    TASK_TYPE VARCHAR2(50 char) not null,
    PRIORITET NUMBER(3) default 0 not null,
    STATUS VARCHAR2(20 char) default 'KLAR' not null
        constraint CHK_PROSESS_TASK_STATUS
        check (status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG', 'KJOERT')),
    TASK_PARAMETERE VARCHAR2(4000 char),
    TASK_PAYLOAD CLOB,
    TASK_GRUPPE VARCHAR2(250 char),
    TASK_SEKVENS VARCHAR2(100 char) default '1' not null,
    NESTE_KJOERING_ETTER TIMESTAMP(0) default current_timestamp,
    FEILEDE_FORSOEK NUMBER(5) default 0,
    SISTE_KJOERING_TS TIMESTAMP(6),
    SISTE_KJOERING_FEIL_KODE VARCHAR2(50 char),
    SISTE_KJOERING_FEIL_TEKST CLOB,
    SISTE_KJOERING_SERVER VARCHAR2(50 char),
    VERSJON NUMBER(19) default 0 not null,
    OPPRETTET_AV VARCHAR2(30 char) default 'VL' not null,
    OPPRETTET_TID TIMESTAMP(6) default systimestamp not null,
    BLOKKERT_AV NUMBER(19),
    SISTE_KJOERING_PLUKK_TS TIMESTAMP(6),
    SISTE_KJOERING_SLUTT_TS TIMESTAMP(6)
)
    /

comment on table PROSESS_TASK is 'Inneholder tasks som skal kjøres i bakgrunnen'
/

comment on column PROSESS_TASK.TASK_TYPE is 'navn på task. Brukes til å matche riktig implementasjon'
/

comment on column PROSESS_TASK.PRIORITET is 'prioritet på task.  Høyere tall har høyere prioritet'
/

comment on column PROSESS_TASK.STATUS is 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG'
/

comment on column PROSESS_TASK.TASK_PARAMETERE is 'parametere angitt for en task'
/

comment on column PROSESS_TASK.TASK_PAYLOAD is 'inputdata for en task'
/

comment on column PROSESS_TASK.TASK_GRUPPE is 'angir en unik id som grupperer flere '
/

comment on column PROSESS_TASK.TASK_SEKVENS is 'angir rekkefølge på task innenfor en gruppe '
/

comment on column PROSESS_TASK.NESTE_KJOERING_ETTER is 'tasken skal ikke kjøeres før tidspunkt er passert'
/

comment on column PROSESS_TASK.FEILEDE_FORSOEK is 'antall feilede forsøk'
/

comment on column PROSESS_TASK.SISTE_KJOERING_TS is 'siste gang tasken ble forsøkt kjørt (før kjøring)'
/

comment on column PROSESS_TASK.SISTE_KJOERING_FEIL_KODE is 'siste feilkode tasken fikk'
/

comment on column PROSESS_TASK.SISTE_KJOERING_FEIL_TEKST is 'siste feil tasken fikk'
/

comment on column PROSESS_TASK.SISTE_KJOERING_SERVER is 'navn på node som sist kjørte en task (server@pid)'
/

comment on column PROSESS_TASK.VERSJON is 'angir versjon for optimistisk låsing'
/

comment on column PROSESS_TASK.BLOKKERT_AV is 'Id til ProsessTask som blokkerer kjøring av denne (når status=VETO)'
/

comment on column PROSESS_TASK.SISTE_KJOERING_PLUKK_TS is 'siste gang tasken ble forsøkt plukket (fra db til in-memory, før kjøring)'
/

comment on column PROSESS_TASK.SISTE_KJOERING_SLUTT_TS is 'tidsstempel siste gang tasken ble kjørt (etter kjøring)'
/

create index IDX_PROSESS_TASK_2
    on PROSESS_TASK (TASK_TYPE)
    /

create index IDX_PROSESS_TASK_6
    on PROSESS_TASK (BLOKKERT_AV)
    /

create table DOKUMENT
(
    ID NUMBER(19) not null
        constraint PK_DOKUMENT
        primary key,
    FORSENDELSE_ID RAW(16),
    DOKUMENT_TYPE_ID VARCHAR2(100 char),
    HOVED_DOKUMENT CHAR,
    OPPRETTET_AV VARCHAR2(20 char) default 'VL' not null,
    OPPRETTET_TID TIMESTAMP(3) default systimestamp not null,
    ENDRET_AV VARCHAR2(20 char),
    ENDRET_TID TIMESTAMP(3),
    ARKIV_FILTYPE VARCHAR2(100 char),
    BLOB BLOB,
    BESKRIVELSE VARCHAR2(200 char)
)
    /

comment on table DOKUMENT is 'Tabell med dokumenter som fordeles'
/

comment on column DOKUMENT.ID is 'Primærnøkkel'
/

comment on column DOKUMENT.FORSENDELSE_ID is 'Unik ID for forsendelsen'
/

comment on column DOKUMENT.DOKUMENT_TYPE_ID is 'Kodeverdi for innkommende dokument type'
/

comment on column DOKUMENT.HOVED_DOKUMENT is 'Er dette hoveddokument? (J/N)'
/

comment on column DOKUMENT.ARKIV_FILTYPE is 'Kodeverdi for innkommende filtype'
/

comment on column DOKUMENT.BLOB is 'Innkommende dokument blob'
/

comment on column DOKUMENT.BESKRIVELSE is 'Brukers beskrivelse av dokumentinnhold'
/

create table DOKUMENT_METADATA
(
    ID NUMBER(19) not null
        constraint PK_DOKUMENT_METADATA
        primary key,
    FORSENDELSE_ID RAW(16),
    BRUKER_ID VARCHAR2(19 char),
    SAKSNUMMER VARCHAR2(32 char),
    ARKIV_ID VARCHAR2(32 char),
    OPPRETTET_AV VARCHAR2(20 char) default 'VL' not null,
    OPPRETTET_TID TIMESTAMP(3) default systimestamp not null,
    ENDRET_AV VARCHAR2(20 char),
    ENDRET_TID TIMESTAMP(3),
    FORSENDELSE_STATUS VARCHAR2(12 char),
    FORSENDELSE_MOTTATT TIMESTAMP(3)
)
    /

comment on table DOKUMENT_METADATA is 'Metadataene til dokumentene som fordeles'
/

comment on column DOKUMENT_METADATA.ID is 'Primærnøkkel'
/

comment on column DOKUMENT_METADATA.FORSENDELSE_ID is 'Unik ID for forsendelsen'
/

comment on column DOKUMENT_METADATA.BRUKER_ID is 'ID til avsenderen av et dokument'
/

comment on column DOKUMENT_METADATA.SAKSNUMMER is 'ID til fagsak et dokument knyttes mot'
/

comment on column DOKUMENT_METADATA.ARKIV_ID is 'ID til dokumentet i JOARK'
/

comment on column DOKUMENT_METADATA.FORSENDELSE_STATUS is 'Status på dokumentforsendelse'
/

comment on column DOKUMENT_METADATA.FORSENDELSE_MOTTATT is 'Tidspunktet forsendelsen ble mottatt hos NAV'
/

create unique index IDX_DOKUMENT
    on DOKUMENT_METADATA (FORSENDELSE_ID)
    /

alter table DOKUMENT_METADATA
    add constraint CHK_UNIQUE_FORS_DOKUMENT_MT
        unique (FORSENDELSE_ID)
    /

create table JOURNALPOST
(
    ID NUMBER(19) not null
        constraint PK_JOURNALPOST
        primary key,
    JOURNALPOST_ID VARCHAR2(32 char) not null,
    TILSTAND VARCHAR2(12 char),
    KANAL VARCHAR2(16 char),
    REFERANSE VARCHAR2(64 char),
    OPPRETTET_AV VARCHAR2(20 char) default 'VL' not null,
    OPPRETTET_TID TIMESTAMP(3) default systimestamp not null
)
    /

comment on table JOURNALPOST is 'Metadataene til dokumentene som fordeles'
/

comment on column JOURNALPOST.ID is 'Primærnøkkel'
/

comment on column JOURNALPOST.JOURNALPOST_ID is 'ID til journalposten i JOARK'
/

comment on column JOURNALPOST.TILSTAND is 'Journalpostens tilstand'
/

comment on column JOURNALPOST.KANAL is 'Innsendingskanal'
/

comment on column JOURNALPOST.REFERANSE is 'Unik referanse fra kanal'
/

create index IDX_JOURNALPOST_ID
    on JOURNALPOST (JOURNALPOST_ID)
    /
