-- J01--J12: дополнительные учебные таблицы. Включается в общую транзакцию reset.
-- Ни одна из них не меняет исходные 9 путей BASE-01 или задания L01--L14.
CREATE TABLE java_measurement(id text PRIMARY KEY, raw_value text, unit text REFERENCES unit);
INSERT INTO java_measurement VALUES
 ('M1','1250','g'),('M2','1.250','kg'),('M3',NULL,'kg'),
 ('M4','NaN','kg'),('M5','-1','kg'),('M6','3','m'),('M7','0','kg');

CREATE TABLE java_snapshot(
 id text PRIMARY KEY, root_revision text NOT NULL REFERENCES revision,
 source_baseline text NOT NULL, rule_version text NOT NULL,
 source_context jsonb NOT NULL, sealed boolean NOT NULL DEFAULT false,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp());
CREATE TABLE java_snapshot_line(
 snapshot_id text REFERENCES java_snapshot, path text NOT NULL,
 parent_revision text NOT NULL REFERENCES revision, occurrence_id text NOT NULL,
 child_item text NOT NULL, revision_id text NOT NULL,
 local_quantity numeric NOT NULL CHECK(local_quantity>0 AND local_quantity<'Infinity'::numeric),
 root_quantity numeric NOT NULL CHECK(root_quantity>0 AND root_quantity<'Infinity'::numeric),
 unit text NOT NULL REFERENCES unit, PRIMARY KEY(snapshot_id,path),
 FOREIGN KEY(child_item,revision_id) REFERENCES revision(item_id,id));
-- Эталонные снимки для независимого J04. Это скопированные значения, а не представление БД.
INSERT INTO java_snapshot(id,root_revision,source_baseline,rule_version,source_context)
 VALUES ('OLD','DM-A','BASE-01','snapshot-v1','{"fixture":"j04-old"}'),
        ('NEW','DM-A','BASE-01','snapshot-v1','{"fixture":"j04-new"}');
INSERT INTO java_snapshot_line
 SELECT 'OLD',t.path,o.parent_revision,o.id,r.item_id,t.revision_id,o.quantity,t.quantity,t.unit
 FROM base_tree t JOIN occurrence o ON o.id=split_part(t.path,'/',-1)
 JOIN revision r ON r.id=t.revision_id;
INSERT INTO java_snapshot_line SELECT 'NEW',path,parent_revision,occurrence_id,child_item,
 revision_id,local_quantity,root_quantity,unit FROM java_snapshot_line WHERE snapshot_id='OLD';
UPDATE java_snapshot_line SET local_quantity=3,root_quantity=3 WHERE snapshot_id='NEW' AND path='o50/e20';
UPDATE java_snapshot_line SET revision_id='SEN-B' WHERE snapshot_id='NEW' AND path='o30';
UPDATE java_snapshot SET sealed=true;
-- После запечатывания запрещены и дописывание строк, и изменение сохранённых данных.
-- Это протокол стенда, не защита от владельца БД/отключения триггеров/TRUNCATE.
CREATE FUNCTION java_immutable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF TG_TABLE_NAME='java_snapshot' AND TG_OP='UPDATE' THEN
   IF NOT OLD.sealed AND NEW.sealed AND (to_jsonb(OLD)-'sealed')=(to_jsonb(NEW)-'sealed') THEN
     RETURN NEW;
   END IF;
 END IF;
 RAISE EXCEPTION 'IMMUTABLE_SNAPSHOT';
END $$;
CREATE FUNCTION java_snapshot_insert() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE locked boolean;
BEGIN
 SELECT sealed INTO locked FROM java_snapshot WHERE id=NEW.snapshot_id FOR UPDATE;
 IF locked THEN RAISE EXCEPTION 'SEALED_SNAPSHOT'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER immutable_header BEFORE UPDATE OR DELETE ON java_snapshot
 FOR EACH ROW EXECUTE FUNCTION java_immutable();
CREATE TRIGGER immutable_lines BEFORE UPDATE OR DELETE ON java_snapshot_line
 FOR EACH ROW EXECUTE FUNCTION java_immutable();
CREATE TRIGGER seal_lines BEFORE INSERT ON java_snapshot_line
 FOR EACH ROW EXECUTE FUNCTION java_snapshot_insert();

CREATE TABLE java_command(id text PRIMARY KEY, change_id text NOT NULL REFERENCES change_order,
 expected_version integer NOT NULL, result text NOT NULL);
CREATE TABLE java_event(event_id text PRIMARY KEY, aggregate_id text NOT NULL,
 version integer NOT NULL CHECK(version>0), payload text NOT NULL,
 result text NOT NULL CHECK(result IN ('PENDING','APPLIED')));
CREATE TABLE java_target(aggregate_id text PRIMARY KEY, version integer NOT NULL DEFAULT 0,
 payload text NOT NULL DEFAULT '');
CREATE TABLE java_outbox(event_id text PRIMARY KEY, aggregate_id text NOT NULL,
 version integer NOT NULL CHECK(version>0), payload text NOT NULL);
CREATE TABLE java_delivery(event_id text PRIMARY KEY REFERENCES java_outbox,
 token uuid, lease_until timestamptz, attempts integer NOT NULL DEFAULT 0,
 delivered boolean NOT NULL DEFAULT false);
INSERT INTO java_outbox SELECT * FROM outbox;
INSERT INTO java_outbox VALUES ('J-EVT-2','MBOM:DM:P2',1,'BASE-01');
INSERT INTO java_delivery(event_id) SELECT event_id FROM java_outbox;

-- J10: обе временные оси, интервалы [from,to), исправления дописываются.
CREATE TABLE java_fact(
 fact_id text NOT NULL, edition integer NOT NULL, child_serial text NOT NULL REFERENCES serial_unit,
 effective_from timestamptz NOT NULL, effective_to timestamptz,
 known_from timestamptz NOT NULL, known_to timestamptz,
 reason text NOT NULL, PRIMARY KEY(fact_id,edition),
 CHECK(effective_to IS NULL OR effective_from<effective_to),
 CHECK(known_to IS NULL OR known_from<known_to));
INSERT INTO java_fact VALUES
 ('F1',1,'S-001','2026-06-01Z',NULL,'2026-06-01Z','2026-08-12Z','Первоначальное знание'),
 ('F1',2,'S-001','2026-06-01Z','2026-08-10Z','2026-08-12Z',NULL,'Уточнение даты демонтажа'),
 ('F2',1,'S-002','2026-08-10Z',NULL,'2026-08-12Z',NULL,'Поздняя регистрация замены');

CREATE TABLE java_file_manifest(path text PRIMARY KEY, role text NOT NULL, tool_version text NOT NULL);
INSERT INTO java_file_manifest VALUES
 ('housing.txt','source','synthetic-v1'),('controller.txt','dependency','synthetic-v1'),
 ('housing-copy.txt','representation','synthetic-v2');
CREATE TABLE java_file_digest(run_id text NOT NULL, path text NOT NULL,
 role text NOT NULL, tool_version text NOT NULL, digest text NOT NULL,
 PRIMARY KEY(run_id,path));
