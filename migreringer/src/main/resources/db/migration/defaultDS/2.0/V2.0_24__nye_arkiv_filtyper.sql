-- Nye dokumenttyper
merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'ARKIV_FILTYPE' and k.kode = 'TIF') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'TIF', 'TIF', 'TIFF', 'Filtype TIF', 'NB', to_date('09.05.2019', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'ARKIV_FILTYPE' and k.kode = 'JSON') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'JSON', 'JSON', 'JSON', 'Filtype JSON', 'NB', to_date('09.05.2019', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'ARKIV_FILTYPE' and k.kode = 'PNG') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'PNG', 'PNG', 'PNG', 'Filtype PNG', 'NB', to_date('09.05.2019', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'ARKIV_FILTYPE' and k.kode = 'JPG') when not matched then
  INSERT (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
  VALUES (seq_kodeliste.nextval, 'ARKIV_FILTYPE', 'JPG', 'JPG', 'JPG', 'Filtype JPG', 'NB', to_date('09.05.2019', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

-- Kodeverkversjon
update kodeverk set KODEVERK_EIER_VER='4' where kode='ARKIV_FILTYPE';