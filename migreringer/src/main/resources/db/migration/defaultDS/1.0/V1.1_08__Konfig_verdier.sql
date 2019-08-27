insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('infotrygd.sak.gyldig.periode', 'Tidsperiode i mnd for saker i infotrygd', 'INGEN', 'INTEGER', 'Tidsperiode for sjekk av saker mot infotrygd. Oppgitt i måneder');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'infotrygd.sak.gyldig.periode', 'INGEN', 'P10M', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('gsak.ehentsid.fordelingsoppgaver', 'EnhetsId til bruk for fordelingsoppgaver', 'INGEN', 'STRING', 'EnhetsId til bruk for fordelingsoppgaver');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'gsak.ehentsid.fordelingsoppgaver', 'INGEN', '2820', to_date('01.01.2017', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('infotrygd.inntektsmelding.startdato.akseptert.diff', 'Max diff på startdato på dok og infotrygdsak', 'INGEN', 'INTEGER', 'Akseptert differanse ved sjekk av startdato på inntektsmelding mot infotrygdsaker. Oppgitt i dager');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'infotrygd.inntektsmelding.startdato.akseptert.diff', 'INGEN', 'P4D', to_date('01.01.2016', 'dd.mm.yyyy'));

-- Legges inn som STRING, da vi ikke har en LocalDate konfigverdi-provider
insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse, opprettet_tid) values ('inntektsmelding.foreldrepenger.startdato', 'Startdato inntektsmelding for journalføring', 'INGEN', 'STRING', 'Startdato for inntektsmelding f.o.m hvilket journalføring gjøres gjennom VL', to_date('12.12.2017', 'dd.mm.yyyy'));
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'inntektsmelding.foreldrepenger.startdato', 'INGEN', '2019-01-01', to_date('12.12.2017', 'dd.mm.yyyy'));

