CREATE TEMP TABLE measurement(revision_id text,property text,value numeric,unit text);
\copy measurement FROM '/workspace/practice/datasets/measurements.csv' CSV HEADER
SELECT m.revision_id,m.value AS source_value,m.unit AS source_unit,
 CASE WHEN u.code IS NULL THEN 'UNKNOWN_UNIT'
      WHEN u.dimension<>'mass' THEN 'DIMENSION_MISMATCH'
      WHEN m.value IS NULL THEN 'UNKNOWN' ELSE 'OK' END AS diagnosis,
 CASE WHEN u.dimension='mass' THEN m.value*u.factor END AS mass_kg
FROM measurement m LEFT JOIN lab.unit u ON u.code=m.unit ORDER BY m.revision_id;
CREATE TABLE lab.mass_property(
 revision_id text PRIMARY KEY REFERENCES lab.revision,
 value numeric CHECK(value>0), unit text NOT NULL REFERENCES lab.unit,
 status text NOT NULL CHECK(status IN ('known','unknown','not_applicable')),
 CHECK((status='known' AND value IS NOT NULL) OR (status<>'known' AND value IS NULL)));
INSERT INTO lab.mass_property VALUES ('HO-A',2.5,'kg','known'),('RO-A',1.2,'kg','known'),('SEN-A',NULL,'kg','unknown');
-- Размерность для свойства проверяется сервисом либо составным FK на справочник
-- допустимых единиц. Сам FK на unit недостаточен: это явно обсуждается в эталоне.
