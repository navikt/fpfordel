-- ##################################################
-- ### Opplegg for enhetstester (lokal + jenkins) ###
-- ##################################################
DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = 'FPFORDEL_UNIT';
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER FPFORDEL_UNIT IDENTIFIED BY fpfordel_unit');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO fpfordel_unit;

-- ###############################
-- ### Opplegg for lokal jetty ###
-- ###############################
DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = 'FPFORDEL';
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER FPFORDEL IDENTIFIED BY fpfordel');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO FPFORDEL;