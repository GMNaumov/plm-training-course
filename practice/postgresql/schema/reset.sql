\set ON_ERROR_STOP on
-- Полный возврат только учебной схемы lab. Данные упражнений будут потеряны.
BEGIN;
DROP SCHEMA IF EXISTS lab CASCADE;
CREATE SCHEMA lab;
SET search_path = lab, public;
ALTER DATABASE plm SET search_path = lab, public;
DO $$ BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='lab_reader') THEN
    CREATE ROLE lab_reader NOLOGIN;
  END IF;
END $$;
\ir model.sql
\ir ../../datasets/seed.sql
COMMIT;
