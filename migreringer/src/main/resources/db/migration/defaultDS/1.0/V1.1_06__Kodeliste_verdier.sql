INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'ENGST_FODS', 'ab0050', 'Engangsstønad ved fødsel', NULL, 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'ENGST_ADOP', 'ab0027', 'Engangsstønad ved adopsjon', NULL, 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
                               
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', '-', NULL, 'Ikke definert', 'Ikke definert', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
                               
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'ENGST', 'ab0327', 'Engangsstønad', NULL, 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'FORP_ADOP', 'ab0072', 'Foreldrepenger ved adopsjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'FORP_FODS', 'ab0047', 'Foreldrepenger ved fødsel', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'FORP', 'ab0326', 'Foreldrepenger', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));


INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'TEMA', 'FOR_SVA', 'FOR', 'Foreldre- og svangerskapspenger',
                               'Foreldre- og svangerskapspenger', 'NB', to_date('01.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
                               
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'TEMA', '-', NULL, 'Ikke definert', 'Ikke definert', 'NB',
                               to_date('01.05.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_ENGANGSSTØNAD_FØDSEL', 'I000003',
                               'Søknad om engangsstønad ved fødsel', 'Søknad om engangsstønad ved fødsel', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_ENGANGSSTØNAD_ADOPSJON', 'I000004',
                               'Søknad om engangsstønad ved adopsjon', 'Søknad om engangsstønad ved adopsjon', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
                               
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL', 'I000041',
                               'Dokumentasjon av termin eller fødsel',
                               'Dokumentasjon av termindato (lev. kun av mor), fødsel eller dato for omsorgsovertakelse',
                               'NB', to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
                               
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOKUMENTASJON_AV_OMSORGSOVERTAKELSE', 'I000042',
                               'Dokumentasjon av omsorgsovertakelse', 'Dokumentasjon av dato for overtakelse av omsorg',
                               'NB', to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
        
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', '-', NULL, 'Ikke definert', 'Ikke definert', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
                               
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BRUKEROPPLASTET_DOKUMENTASJON', 'I000047',
                               'Brukeropplastet dokumentasjon', 'Brukeropplastet dokumentasjon', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
        
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'KLAGE_DOKUMENT', 'I000027', 'Klage', 'Klage/anke', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'KVITTERING_DOKUMENTINNSENDING', 'I000046',
                               'Kvittering dokumentinnsending', 'Kvittering dokumentinnsending', 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BEKREFTELSE_VENTET_FØDSELSDATO', 'I000062',
                               'Bekreftelse på ventet fødselsdato', 'Bekreftelse på ventet fødselsdato', 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'LEGEERKLÆRING', 'I000023', 'Legeerklæring', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'GJELDSBREV_GRUPPE_1', 'I000024', 'Gjeldsbrev gruppe 1', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BEKREFTELSE_FRA_ARBEIDSGIVER', 'I000065', 'Bekreftelse fra arbeidsgiver',
                          NULL, 'NB', to_date('24.05.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'KOPI_VOGNKORT', 'I000021', 'Kopi av vognkort', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'KOPI_SKATTEMELDING', 'I000066',
                               'Kopi av likningsattest eller selvangivelse', NULL, 'NB',
                               to_date('24.05.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'KOPI_FØRERKORT', 'I000022', 'Kopi av førerkort', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BREV_UTLAND', 'I000028', 'Brev - utland', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'GJELDSBREV_GRUPPE_2', 'I000025', 'Gjeldsbrev gruppe 2', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'INNTEKTSOPPLYSNINGER', 'I000026',
                               'Inntektsopplysninger for arbeidstaker som skal ha sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger',
                               NULL, 'NB', to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ANNET', 'I000060', 'Annet', NULL, 'NB',
                               to_date('24.05.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'FØDSELSATTEST', 'I000063', 'Fødselsattest', NULL, 'NB',
                               to_date('24.05.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ELEVDOKUMENTASJON_LÆRESTED', 'I000064', 'Elevdokumentasjon fra lærested',
                          NULL, 'NB', to_date('24.05.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'KOPI_VERGEATTEST', 'I000020', 'Kopi av verge- eller hjelpeverge attest',
                          NULL, 'NB', to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BEKREFTELSE_FRA_STUDIESTED', 'I000061',
                               'Bekreftelse fra studiested/skole', NULL, 'NB', to_date('24.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_SØKNAD_TILPASSNING_BIL', 'I500010',
                               'Ettersendelse til søknad om spesialutstyr og- tilpassing til bil', NULL, 'NB',
                               to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'TREKKOPPLYSNINGER_ETTERSENDT', 'I500057',
                               'Ettersendelse til trekkopplysninger for arbeidstaker som skal ha: sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger',
                               NULL, 'NB', to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ANNET_SKJEMA_UTLAND_IKKE_NAV', 'I000029',
                               'Annet skjema (ikke NAV-skjema) - utland', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'OPPPDRAGSKONTRAKT', 'I000034', 'Oppdragskontrakt', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'LØNNS_OG_TREKKOPPGAVE', 'I000035', 'Lønns- og trekkoppgave', NULL, 'NB',
                          to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'OPPHOLDSOPPLYSNINGER', 'I001000', 'Oppholdsopplysninger', NULL, 'NB',
                          to_date('26.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'RESULTATREGNSKAP', 'I000032', 'Resultatregnskap', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'LØNNSLIPP', 'I000033', 'Lønnsslipp', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_MORS_UTDANNING_ARBEID_SYKDOM', 'I000038',
                               'Dokumentasjon av mors utdanning, arbeid eller sykdom', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_MILITÆR_SIVIL_TJENESTE', 'I000039',
                               'Dokumentasjon av militær- eller siviltjeneste', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_FERIE', 'I000036', 'Dokumentasjon av ferie', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_INNLEGGELSE', 'I000037',
                               'Dokumentasjon av innleggelse i helseinstitusjon', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'NÆRINGSOPPGAVE', 'I000030', 'Næringsoppgave', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'PERSONINNTEKTSKJEMA', 'I000031', 'Personinntektsskjema', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'I500027', 'I500027', 'Ettersendelse til klage/anke', NULL, 'NB',
                               to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BESKRIVELSE_FUNKSJONSNEDSETTELSE', 'I000045',
                               'Beskrivelse av funksjonsnedsettelse', NULL, 'NB', to_date('25.04.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG', 'I000001',
                               'Søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_FORELDREPENGER_ADOPSJON', 'I000002',
                               'Søknad om foreldrepenger, mødrekvote eller fedrekvote ved adopsjon', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_ARBEIDSFORHOLD', 'I000043', 'Dokumentasjon av arbeidsforhold', NULL,
                          'NB', to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_ETTERLØNN', 'I000044', 'Dokumentasjon av etterlønn/sluttvederlag',
                          NULL, 'NB', to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ANNET_SKJEMA_IKKE_NAV', 'I000049', 'Annet skjema (ikke NAV-skjema)',
                               NULL, 'NB', to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_FORELDREPENGER_FØDSEL', 'I000005',
                               'Søknad om foreldrepenger, mødrekvote eller fedrekvote ved fødsel', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'FLEKSIBELT_UTTAK_FORELDREPENGER', 'I000006',
                               'Utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BREV', 'I000048', 'Brev', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_ASYL_DATO', 'I000040', 'Dokumentasjon av dato for asyl', NULL, 'NB',
                          to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_REISEUTGIFT_BIL', 'I000009',
                               'Søknad om refusjon av reiseutgifter til bil', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'INNTEKTSOPPLYSNING_SELVSTENDIG', 'I000007',
                               'Inntektsopplysninger om selvstendig næringsdrivende og/eller frilansere som skal ha foreldrepenger eller svangerskapspenger',
                               NULL, 'NB', to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_SKAFFE_BIL', 'I000008',
                               'Søknad om stønad til anskaffelse av motorkjøretøy', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_UTGIFT_BARNEPASS', 'I000056',
                               'Dokumentasjon av utgifter til stell og pass av barn', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'TILLEGGSJKJEMA_BIL', 'I000012', 'Tilleggskjema for bil', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'TREKKOPPLYSNING_ARBEIDSTAKER', 'I000057',
                               'Trekkopplysninger for arbeidstaker som skal ha: sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger',
                               NULL, 'NB', to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BEKREFTELSE_OPPMØTE', 'I000013', 'Bekreftelse på oppmøte', NULL, 'NB',
                          to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_UTBETALING_FRA_ARBEIDSGIVER', 'I000054',
                               'Dokumentasjon av utbetalinger eller goder fra arbeidsgiver', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_TILPASSNING_BIL', 'I000010',
                               'Søknad om spesialutstyr og -tilpassing til bil', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BEKREFTELSE_OPPHOLDSTILLATELSE', 'I000055',
                               'Bekreftelse på oppholdstillatelse', NULL, 'NB', to_date('25.04.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'LEGEERKLÆRING_EVNE_KJØRE_BIL', 'I000011',
                               'Legeerklæring om søkerens evne til å føre motorkjøretøy og om behovet for ekstra transport på grunn av funksjonshemmingen',
                               NULL, 'NB', to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_INNTEKT', 'I000016', 'Dokumentasjon av inntekt', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_UTGIFT_REISE', 'I000017', 'Dokumentasjon av reiseutgifter', NULL,
                          'NB', to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_ANDRE_YTELSE', 'I000058', 'Dokumentasjon av andre ytelser', NULL,
                          'NB', to_date('24.05.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_BEHOV_LEDSAGER', 'I000014', 'Dokumentasjon av behov for ledsager',
                          NULL, 'NB', to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'TIMELISTER', 'I000059', 'Timelister', NULL, 'NB',
                               to_date('24.05.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_BEHOV_TRANSPORTMIDDEL', 'I000015',
                               'Dokumentasjon av behov for dyrere transportmiddel', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SKJEMA_OPPLYSNING_INNTEKT', 'I000052', 'Inntektsopplysningsskjema', NULL,
                          'NB', to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD', 'I500050',
                               'Ettersendelse til søknad om endring av uttak av foreldrepenger eller overføring av kvote',
                               NULL, 'NB', to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_ANDRE_UTBETALINGER', 'I000053',
                               'Dokumentasjon av andre utbetalinger', NULL, 'NB', to_date('25.04.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'FORELDREPENGER_ENDRING_SØKNAD', 'I000050',
                               'Søknad om endring av uttak av foreldrepenger eller overføring av kvote', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM', 'I000051',
                               'Bekreftelse på deltakelse i kvalifiseringsprogrammet', NULL, 'NB',
                               to_date('25.04.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON', 'I500002',
                               'Ettersendelse til søknad om foreldrepenger, mødrekvote eller fedrekvote ved adopsjon',
                               NULL, 'NB', to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL', 'I500003',
                               'Ettersendelse til søknad om engangsstønad ved fødsel', NULL, 'NB',
                               to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG', 'I500001',
                               'Ettersendelse til søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser',
                               NULL, 'NB', to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER', 'I500006',
                               'Ettersendelse til utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)',
                               NULL, 'NB', to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON', 'I500004',
                               'Ettersendelse til søknad om engangsstønad ved adopsjon', NULL, 'NB',
                               to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SPESIALISTERKLÆRING', 'I000018', 'Spesialisterklæring', NULL, 'NB',
                               to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL', 'I500005',
                               'Ettersendelse til søknad om foreldrepenger, mødrekvote eller fedrekvote ved fødsel',
                               NULL, 'NB', to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOK_VEIFORHOLD', 'I000019', 'Dokumentasjon av veiforhold', NULL, 'NB',
                          to_date('22.03.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_SØKNAD_SKAFFE_BIL', 'I500008',
                               'Ettersendelse til søknad om stønad til anskaffelse av motorkjøretøy', NULL, 'NB',
                               to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'ETTERSENDT_SØKNAD_REISEUTGIFT_BIL', 'I500009',
                               'Ettersendelse til søknad om refusjon av reiseutgifter til bil', NULL, 'NB',
                               to_date('16.08.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'PDF', 'PDF', 'PDF', 'Filtype PDF', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'PDFA', 'PDFA', 'PDFA', 'Filtype PDFA', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'XML', 'XML', 'XML', 'Filtype XML', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'AFP', 'AFP', 'AFP', 'Filtype AFP', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'AXML', 'AXML', 'AXML', 'Filtype AXML', 'NB',
                               to_date('06.07.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'DLF', 'DLF', 'DLF', 'Filtype DLF', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'DOC', 'DOC', 'DOC', 'Filtype DOC', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'DOCX', 'DOCX', 'DOCX', 'Filtype DOCX', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'JPEG', 'JPEG', 'JPEG', 'Filtype JPEG', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'RTF', 'RTF', 'RTF', 'Filtype RTF', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'TIFF', 'TIFF', 'TIFF', 'Filtype TIFF', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'XLS', 'XLS', 'XLS', 'Filtype XLS', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'XLSX', 'XLSX', 'XLSX', 'Filtype XLSX', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', '-', NULL, 'Ikke definert', 'Ikke definert', 'NB',
                               to_date('01.07.2006', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'BOOLEAN', NULL, 'Boolske verdier', 'Støtter J(a) / N(ei) flagg', 'NB',
                          to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('30.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'PERIOD', NULL, 'Periode verdier',
                               'ISO 8601 Periode verdier.  Eks. P10M (10 måneder), P1D (1 dag) ', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('30.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'DURATION', NULL, 'Periode verdier',
                               'ISO 8601 Duration (tid) verdier.  Eks. PT1H (1 time), PT1M (1 minutt) ', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('30.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES
  (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'INTEGER', NULL, 'Heltall', 'Heltallsverdier (positiv/negativ)', 'NB',
                          to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                          to_timestamp('30.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'STRING', NULL, 'Streng verdier', NULL, 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('30.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'URI', NULL, 'Uniform Resource Identifier',
                               'URI for å angi id til en ressurs', 'NB', to_date('01.01.2000', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('30.11.2017', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_GRUPPE', 'INGEN', NULL, '-',
                               'Ingen gruppe definert (default).  Brukes istdf. NULL siden dette inngår i en Primary Key. Koder som ikke er del av en gruppe må alltid være unike.',
                               'NB', to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('30.11.2017', 'DD.MM.RRRR'));

Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','KLGA','KA','Klage eller anke','Klage eller anke','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','ITSKJ','IS','Ikke tolkbart skjema','Ikke tolkbart skjema','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','SOKN','SOK','Søknad','Søknad','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','ESKJ','ES','Elektronisk skjema','Elektronisk skjema','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','BRV','B','Brev','Brev','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','EDIALOG','ELEKTRONISK_DIALOG','Elektronisk dialog','Elektronisk dialog','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','FNOT','FORVALTNINGSNOTAT','Forvaltningsnotat','Forvaltningsnotat','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','IBRV','IB','Informasjonsbrev','Informasjonsbrev','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','KONVEARK','KD','Konvertert fra elektronisk arkiv','Konvertert fra elektronisk arkiv','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','KONVSYS','KS','Konverterte data fra system','Konverterte data fra system','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','PUBEOS','PUBL_BLANKETT_EOS','Publikumsblankett EØS','Publikumsblankett EØS','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','SEDOK','SED','Strukturert elektronisk dokument - EU/EØS','Strukturert elektronisk dokument - EU/EØS','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','TSKJ','TS','Tolkbart skjema','Tolkbart skjema','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','VBRV','VB','Vedtaksbrev','Vedtaksbrev','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'DOKUMENT_KATEGORI','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','ARENA','AO01','Arena','Arena','NB',to_date('13.02.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','GRISEN','AO11','Grisen','Grisen','NB',to_date('27.01.2011','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','GOSYS','FS22','Gosys','Gosys','NB',to_date('25.04.2009','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','INFOTRYGD','IT01','Infotrygd','Infotrygd','NB',to_date('13.02.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','HJE_HEL_ORT','OEBS','Hjelpemidler, Helsetjenester og Ort. Hjelpemidler','Hjelpemidler, Helsetjenester og Ort. Hjelpemidler','NB',to_date('13.02.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','PESYS','PP01','Pesys','Pesys','NB',to_date('10.12.2011','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','VENTELONN','V2','Ventelønn','Ventelønn','NB',to_date('13.02.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','UNNTAK','UFM','Unntak','Unntak','NB',to_date('01.01.2010','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','FPSAK','FS36','Vedtaksløsning Foreldrepenger','Vedtaksløsning Foreldrepenger','NB',to_date('28.06.2017','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'FAGSYSTEM','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','ALTINN','ALTINN','Altinn','Altinn','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','EIA','EIA','EIA','EIA','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','EKST_OPPS','EKST_OPPS','Eksternt oppslag','Eksternt oppslag','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NAV_NO','NAV_NO','Ditt NAV','Ditt NAV','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','SKAN_NETS','SKAN_NETS','Skanning Nets','Skanning Nets','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','SKAN_PEN','SKAN_PEN','Skanning Pensjon','Skanning Pensjon','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','EESSI','EESSI','EESSI','EESSI','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','PSELV','PSELV','PSELV','PSELV','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','E_POST','E_POST','E-post','E-post','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1400','NETS_PB1400','NETS - postboks 1400','NETS - postboks 1400','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1405','NETS_PB1405','NETS - postboks 1405','NETS - postboks 1405','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1406','NETS_PB1406','NETS - postboks 1406','NETS - postboks 1406','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1407','NETS_PB1407','NETS - postboks 1407','NETS - postboks 1407','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1408','NETS_PB1408',' NETS - postboks 1408',' NETS - postboks 1408','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1411','NETS_PB1411','NETS - postboks 1411','NETS - postboks 1411','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1412','NETS_PB1412','NETS - postboks 1412','NETS - postboks 1412','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1413','NETS_PB1413','NETS - postboks 1413','NETS - postboks 1413','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1423','NETS_PB1423','NETS - postboks 1423','NETS - postboks 1423','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1431','NETS_PB1431','NETS - postboks 1431','NETS - postboks 1431','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'MOTTAK_KANAL','NETS_PB1441','NETS_PB1441','NETS - postboks 1441','NETS - postboks 1441','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','PROD','PRODUKSJON','Produksjonsformat','Produksjonsformat','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','ARKIV','ARKIV','Arkivformat','Arkivformat','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','SKANM','SKANNING_META','Skanning metadata','Skanning metadata','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','BREVB','BREVBESTILLING','Brevbestilling data','Brevbestilling data','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','ORIG','ORIGINAL','Originalformat','Originalformat','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','FULL','FULLVERSJON','Versjon med infotekster','Versjon med infotekster','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','SLADD','SLADDET','Sladdet format','Sladdet format','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','PRDLF','PRODUKSJON_DLF','Produksjonsformat DLF','Produksjonsformat DLF','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'VARIANT_FORMAT','-',null,'Ikke definert','Ikke definert','NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_TEMA','FA',null,'Foreldrepenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_TEMA','SP',null,'Sykepenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_TEMA','EF',null,'Enslig forsørger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_TEMA','AA','Arbeidsavklaringspenger','Arbeidsavklaringspenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_TEMA','DAGP','Dagpenger','Dagpenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','FØ',null,'Foreldrepenger fødsel',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','AP',null,'Foreldrepenger adopsjon',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','SV',null,'Svangerskapspenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','AE',null,'Adopsjon engangsstønad',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','FE',null,'Fødsel engangsstønad',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','FU',null,'Foreldrepenger fødsel, utland',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','RS',null,'forsikr.risiko sykefravær',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','RT',null,'reisetilskudd',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','SP',null,'sykepenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','SU',null,'sykepenger utenlandsopphold',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','BT',null,'stønad til barnetilsyn',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','FL',null,'tilskudd til flytting',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','OG',null,'overgangsstønad',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','UT',null,'skolepenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','AAP','Arbeidsavklaringspenger','Arbeidsavklaringspenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','DAGO','Ordinære dagpenger','Ordinære dagpenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','PERM','Dagpenger under permitteringer','Dagpenger under permitteringer',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','FISK','Dagp. v/perm fra fiskeindustri','Dagp. v/perm fra fiskeindustri',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,NAVN,BESKRIVELSE,SPRAK,GYLDIG_FOM,GYLDIG_TOM) values (seq_kodeliste.nextval,'RELATERT_YTELSE_BEH_TEMA','LONN','Lønnsgarantimidler - dagpenger','Lønnsgarantimidler - dagpenger',null,'NB',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'INNTEKTSMELDING', 'I000067',
                               'Opplysninger for å behandle krav om blant annet foreldrepenger',
                               NULL, 'NB', to_date('01.12.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('10.11.2017', 'DD.MM.RRRR'));