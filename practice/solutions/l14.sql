BEGIN;
CREATE TEMP TABLE incoming(LIKE lab.migration_stage INCLUDING ALL) ON COMMIT DROP;
\copy incoming FROM '/artifacts/accepted.csv' CSV HEADER
DO $$ BEGIN
 IF EXISTS(SELECT FROM incoming i JOIN lab.migration_stage s USING(source_id)
 WHERE ROW(i.item_code,i.mass_kg,i.rule_version,i.original) IS DISTINCT FROM ROW(s.item_code,s.mass_kg,s.rule_version,s.original)) THEN
   RAISE EXCEPTION 'Ключ источника повторно использован с другим содержимым';
 END IF;
 IF EXISTS(SELECT item_code FROM incoming GROUP BY item_code HAVING count(DISTINCT mass_kg)>1)
 OR EXISTS(SELECT FROM incoming i JOIN lab.migration_target t USING(item_code) WHERE i.mass_kg<>t.mass_kg) THEN
   RAISE EXCEPTION 'Противоречащие значения массы одного изделия';
 END IF;
END $$;
INSERT INTO lab.migration_stage SELECT * FROM incoming ON CONFLICT(source_id) DO NOTHING;
INSERT INTO lab.migration_target SELECT DISTINCT item_code,mass_kg FROM incoming ON CONFLICT(item_code) DO NOTHING;
INSERT INTO lab.migration_map SELECT source_id,item_code,rule_version FROM incoming ON CONFLICT(source_id) DO NOTHING;
COMMIT;
SELECT (SELECT count(*) FROM lab.migration_stage) accepted_sources,
       (SELECT count(*) FROM lab.migration_target) target_items,
       (SELECT count(*) FROM lab.migration_map) mappings;
