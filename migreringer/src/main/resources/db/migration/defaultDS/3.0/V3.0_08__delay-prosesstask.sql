update PROSESS_TASK_TYPE set FEIL_SEK_MELLOM_FORSOEK=10
where KODE in ('fordeling.klargjoering', 'fordeling.behandleDokumentForsendelse') ;

update PROSESS_TASK_TYPE set FEIL_MAKS_FORSOEK=4
where KODE in ('fordeling.klargjoering', 'fordeling.behandleDokumentForsendelse') ;

delete from PROSESS_TASK_TYPE
where kode in ('fordeling.svangerskapspenger', 'fordeling.opprettSak',
               'fordeling.hentOgVurderVLSak','fordeling.hentOgVurderInfotrygdSak');

