INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'SVAP', 'ab0126', 'Svangerskapspenger',
                               'Svangerskapspenger', 'NB', to_date('01.07.2006', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'), 'VL', sysdate);
