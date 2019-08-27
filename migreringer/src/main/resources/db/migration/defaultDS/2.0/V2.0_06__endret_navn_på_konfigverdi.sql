-- Legges inn som STRING, da vi ikke har en LocalDate konfigverdi-provider
insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse, opprettet_tid) values ('foreldrepenger.startdato', 'Startdato for journalføring', 'INGEN', 'STRING', 'F.o.m startdato for når journalføring skal gjøres gjennom VL', to_date('12.12.2017', 'dd.mm.yyyy'));
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'foreldrepenger.startdato', 'INGEN', '2019-01-01', to_date('12.12.2017', 'dd.mm.yyyy'));

delete KONFIG_VERDI where konfig_kode = 'inntektsmelding.foreldrepenger.startdato';
delete KONFIG_VERDI_KODE where kode = 'inntektsmelding.foreldrepenger.startdato';