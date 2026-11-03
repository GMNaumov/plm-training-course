CREATE TEMP TABLE measurement(revision_id text,property text,value numeric,unit text);
\copy measurement FROM '/workspace/practice/datasets/measurements.csv' CSV HEADER
-- TODO: LEFT JOIN unit, рассчитать массу в kg только для известных значений массы.
-- TODO: явно различить OK, UNKNOWN и DIMENSION_MISMATCH, не заменяя NULL нулём.
-- TODO: создать таблицу свойств с FK на revision и unit и ограничением положительности.
