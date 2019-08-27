INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SKJEMA_TILRETTELEGGING_OMPLASSERING', 'I000107', 'Skjema for tilrettelegging og omplassering ved graviditet',
        'NB', to_date('22.05.2019', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL', sysdate);

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOKUMENTASJON_ALENEOMSORG', 'I000108', 'Dokumentasjon av aleneomsorg',
        'NB', to_date('22.05.2019', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL', sysdate);

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'BEGRUNNELSE_SØKNAD_ETTERSKUDD', 'I000109', 'Dokumentasjon av begrunnelse for hvorfor man søker tilbake i tid',
        'NB', to_date('22.05.2019', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL', sysdate);

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'DOKUMENTASJON_INTRODUKSJONSPROGRAM', 'I000110', 'Dokumentasjon av deltakelse i introduksjonsprogrammet',
        'NB', to_date('22.05.2019', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'), 'VL', sysdate);
