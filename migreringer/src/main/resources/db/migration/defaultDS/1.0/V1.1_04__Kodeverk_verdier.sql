INSERT INTO KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, SAMMENSATT)
VALUES ('BEHANDLING_TEMA', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Behandlingstema', '9', 'Behandlingstema',
                      'N', 'N', 'Behandlingstema', 'NAV Behandlingstema', 'VL',
                      to_timestamp('29.11.2017', 'DD.MM.RRRR'), 'N');

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, SAMMENSATT)
VALUES ('TEMA', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Tema', '2', 'Tema', 'N', 'N', 'Tema', 'NAV Tema',
           'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'),'N');

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, SAMMENSATT)
VALUES ('DOKUMENT_TYPE_ID', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/DokumentTypeId-er', '2',
                            'DokumentTypeId-er', 'J', 'N', 'DokumentTypeId-er',
                            'Typen til et mottatt dokument. Dette er et subset av DokumentTyper; inngÃ¥ende dokumenter, for eksempel sÃ¸knad, terminbekreftelse o.l',
                            'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'),'N');

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, SAMMENSATT)
VALUES ('ARKIV_FILTYPE', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Arkivfiltyper', '3', 'Arkivfiltyper', 'N',
                    'N', 'Arkivfiltyper', 'NAV Arkivfiltyper', 'VL', to_timestamp('29.11.2017', 'DD.MM.RRRR'), 'N');

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, SAMMENSATT)
VALUES ('KONFIG_VERDI_GRUPPE', 'VL', NULL, NULL, NULL, 'N', 'N', 'KonfigVerdiGruppe',
                               'Angir en gruppe konfigurerbare verdier tilhører. Det åpner for å kunne ha lister og Maps av konfigurerbare verdier',
                               'VL', to_timestamp('04.12.2017', 'DD.MM.RRRR'), 'N');

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, SAMMENSATT)
VALUES ('KONFIG_VERDI_TYPE', 'VL', NULL, NULL, NULL, 'N', 'N', 'KonfigVerdiType',
                             'Angir type den konfigurerbare verdien er av slik at dette kan brukes til validering og fremstilling.',
                             'VL', to_timestamp('04.12.2017', 'DD.MM.RRRR'), 'N');

Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,SAMMENSATT) values ('DOKUMENT_KATEGORI','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Dokumentkategorier','1','Dokumentkategorier','N','N','Dokumentkategorier','NAV Dokumentkategorier','N');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,SAMMENSATT) values ('FAGSYSTEM','GSak',null,null,'Fagsystemer','N','N','Fagsystemer','NAV Fagsystemer','N');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,SAMMENSATT) values ('MOTTAK_KANAL','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Mottakskanaler','1','Mottakskanaler','N','N','Mottakskanaler','NAV Mottakskanaler','N');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,SAMMENSATT) values ('VARIANT_FORMAT','Kodeverkforvaltning','http://nav.no/kodeverk/Kodeverk/Variantformater','1','Variantformater','N','N','Variantformater','NAV Variantformater','N');

Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,SAMMENSATT) values ('RELATERT_YTELSE_TEMA','Arena',null,null,null,'N','N','RelatertYtelseTema','Kodeverk for tema på relaterte ytelser.','N');
Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,SAMMENSATT) values ('RELATERT_YTELSE_BEH_TEMA','Arena',null,null,null,'N','N','RelatertYtelseBehandlingTema','Kodeverk for behandlingstema på relaterte ytelser','N');
