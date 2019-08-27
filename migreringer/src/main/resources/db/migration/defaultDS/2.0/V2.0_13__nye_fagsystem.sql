-- TFP-184
merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'FAGSYSTEM' and k.kode = 'UTBETALINGSMELDING') when not matched then
  insert (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
  values (SEQ_KODELISTE.nextval, 'FAGSYSTEM', 'UTBETALINGSMELDING', 'OB36', 'Utbetalingsmelding', 'Utbetalingsmelding', to_date('2018-06-01', 'YYYY-MM-DD'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'FAGSYSTEM' and k.kode = 'MELOSYS') when not matched then
  insert (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
  values (SEQ_KODELISTE.nextval, 'FAGSYSTEM', 'MELOSYS', 'FS38', 'Melosys', 'Melosys', to_date('2018-05-01', 'YYYY-MM-DD'));