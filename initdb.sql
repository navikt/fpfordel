alter database set TIME_ZONE='Europe/Oslo';
alter system set recyclebin=OFF DEFERRED;
alter profile default limit password_life_time unlimited;
  
alter system set processes=150 scope=spfile;
alter system set session_cached_cursors=100 scope=spfile;
  
alter system set session_max_open_files=100 scope=spfile;
alter system set sessions=100 scope=spfile;
alter system set license_max_sessions=100 scope=spfile;
alter system set license_sessions_warning=100 scope=spfile;
CREATE USER vl_dba IDENTIFIED BY vl_dba;
GRANT CREATE USER, CONNECT, RESOURCE, DBA, ALTER SESSION TO vl_dba WITH ADMIN OPTION;
