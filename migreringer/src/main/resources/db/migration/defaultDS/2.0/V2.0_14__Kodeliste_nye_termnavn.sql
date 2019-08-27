-- Nye dokumenttyper
merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.kode = 'MELDINGSONING') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'MELDINGSONING', 'I000068', 'Melding til NAV om soning', NULL, 'NB', to_date('20.06.2018', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.kode = 'MELDINGSTRAFFEUNNDRAGELSE') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'MELDINGSTRAFFEUNNDRAGELSE', 'I000069', 'Melding til NAV om unndragelse av straff', NULL, 'NB', to_date('20.06.2018', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.kode = 'MELDINGENDRINGINSTITUSJON') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'MELDINGENDRINGINSTITUSJON', 'I000070', 'Melding om endring i institusjonsopphold', NULL, 'NB', to_date('20.06.2018', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.kode = 'MELDINGUTEBLITTINSTITUSJON') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'MELDINGUTEBLITTINSTITUSJON', 'I000071', 'Melding om uteblivelse fra institusjon', NULL, 'NB', to_date('20.06.2018', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE_ID' and k.kode = 'SØKNAD_KONTANTSTØTTE') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'SØKNAD_KONTANTSTØTTE', 'I000072', 'Søknad om kontantstøtte til småbarnsforeldre', NULL, 'NB', to_date('20.06.2018', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

-- Oppdaterte termnavn januar 2018
UPDATE KODELISTE SET navn='Søknad om foreldrepenger ved adopsjon' WHERE KODEVERK='DOKUMENT_TYPE_ID' AND KODE='SØKNAD_FORELDREPENGER_ADOPSJON';
UPDATE KODELISTE SET navn='Søknad om foreldrepenger ved fødsel' WHERE KODEVERK='DOKUMENT_TYPE_ID' AND KODE='SØKNAD_FORELDREPENGER_FØDSEL';
UPDATE KODELISTE SET navn='Klage/anke' WHERE KODEVERK='DOKUMENT_TYPE_ID' AND KODE='KLAGE_DOKUMENT';
UPDATE KODELISTE SET navn='Dokumentasjon av termindato, fødsel eller dato for omsorgsovertakelse' WHERE KODEVERK='DOKUMENT_TYPE_ID' AND KODE='DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL';
UPDATE KODELISTE SET navn='Dokumentasjon av dato for overtakelse av omsorg' WHERE KODEVERK='DOKUMENT_TYPE_ID' AND KODE='DOKUMENTASJON_AV_OMSORGSOVERTAKELSE';
UPDATE KODELISTE SET navn='Inntektsmelding' WHERE KODEVERK='DOKUMENT_TYPE_ID' AND KODE='INNTEKTSMELDING';
UPDATE KODELISTE SET navn='Ettersendelse til søknad om foreldrepenger ved adopsjon' WHERE KODEVERK='DOKUMENT_TYPE_ID' AND KODE='ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON';
UPDATE KODELISTE SET navn='Ettersendelse til søknad om foreldrepenger ved fødsel' WHERE KODEVERK='DOKUMENT_TYPE_ID' AND KODE='ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL';

