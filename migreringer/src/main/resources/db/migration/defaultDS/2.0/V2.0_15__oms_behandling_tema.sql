INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'TEMA', 'OMS', 'OMS', 'Omsorgspenger, Pleiepenger og opplæringspenger',
                               'Omsorgspenger, Pleiepenger og opplæringspenger', 'NB', to_date('01.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'OMS', 'ab0271', 'Omsorgspenger, Pleiepenger og opplæringspenger',
                               'Omsorgspenger, Pleiepenger og opplæringspenger', 'NB', to_date('01.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'OMS_OPP', 'ab0141', 'Opplæringspenger',
                               'Opplæringspenger', 'NB', to_date('01.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'OMS_PLEIE_BARN', 'ab0069', 'Pleiepenger sykt barn',
                               'Pleiepenger sykt barn', 'NB', to_date('01.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'OMS_PLEIE_INSTU', 'ab0153', 'Pleiepenger ved institusjonsopphold',
                               'Pleiepenger ved institusjonsopphold', 'NB', to_date('01.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'OMS_OMSORG', 'ab0149', 'Omsorgspenger',
                               'Omsorgspenger', 'NB', to_date('01.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'OMS_PLEIE_BARN_NY', 'ab0320', 'Pleiepenger sykt barn ny ordning',
                               'Pleiepenger sykt barn ny ordning fom 011017', 'NB', to_date('01.05.2017', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL',
                               to_timestamp('29.11.2017', 'DD.MM.RRRR'));

