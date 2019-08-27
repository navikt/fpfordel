INSERT INTO PROSESS_TASK_TYPE (KODE, NAVN, FEIL_MAKS_FORSOEK, FEIL_SEK_MELLOM_FORSOEK, FEILHANDTERING_ALGORITME, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID)
VALUES ('fordeling.midlJournalforing', 'Midlertidig journalføring før oppgave', '3', '30', 'DEFAULT', 'Task som midlertidig journalfører forsendelse', 'VL', to_timestamp('13.01.2019', 'DD.MM.RRRR'));

update PROSESS_TASK_TYPE set FEIL_MAKS_FORSOEK=2 where kode='integrasjon.gsak.opprettOppgave';
