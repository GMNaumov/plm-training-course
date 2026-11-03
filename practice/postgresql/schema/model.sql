CREATE TABLE unit(code text PRIMARY KEY, dimension text NOT NULL, factor numeric NOT NULL CHECK(factor>0));
CREATE TABLE item(id text PRIMARY KEY, name text NOT NULL, kind text NOT NULL CHECK(kind IN ('assembly','part','software','material')), unit text NOT NULL REFERENCES unit);
CREATE TABLE revision(
 id text PRIMARY KEY, item_id text NOT NULL REFERENCES item, label text NOT NULL,
 rev_order integer NOT NULL CHECK(rev_order>0), state text NOT NULL CHECK(state IN ('draft','released','blocked')),
 valid_from date NOT NULL, valid_to date, series text NOT NULL,
 serial_from integer NOT NULL, serial_to integer, priority integer NOT NULL DEFAULT 1,
 UNIQUE(item_id,label), UNIQUE(item_id,id), CHECK(valid_to IS NULL OR valid_from<valid_to),
 CHECK(serial_from>0 AND (serial_to IS NULL OR serial_to>=serial_from))
);
CREATE TABLE occurrence(
 id text PRIMARY KEY, parent_revision text NOT NULL REFERENCES revision,
 position text NOT NULL, child_item text NOT NULL REFERENCES item, fixed_revision text,
 quantity numeric NOT NULL CHECK(quantity>0), unit text NOT NULL REFERENCES unit,
 option_code text CHECK(option_code IS NULL OR option_code='REDUNDANT'),
 UNIQUE(parent_revision,position),
 FOREIGN KEY(child_item,fixed_revision) REFERENCES revision(item_id,id)
);
CREATE TABLE baseline(name text NOT NULL, occurrence_id text NOT NULL REFERENCES occurrence,
 revision_id text NOT NULL REFERENCES revision, context jsonb NOT NULL, PRIMARY KEY(name,occurrence_id));
CREATE TABLE document(id text PRIMARY KEY, revision text NOT NULL, status text NOT NULL, source_revision text NOT NULL REFERENCES lab.revision, uri text NOT NULL);
CREATE TABLE requirement(id text PRIMARY KEY, text text NOT NULL, target_revision text NOT NULL REFERENCES revision);
CREATE TABLE evidence(id text PRIMARY KEY, requirement_id text NOT NULL REFERENCES requirement,
 tested_revision text NOT NULL REFERENCES revision, document_id text NOT NULL REFERENCES document,
 result text NOT NULL CHECK(result IN ('pass','fail','pending')));
CREATE TABLE change_order(id text PRIMARY KEY, state text NOT NULL, from_revision text NOT NULL REFERENCES revision,
 to_revision text NOT NULL REFERENCES revision, lock_version integer NOT NULL DEFAULT 0);
CREATE TABLE change_action(id text PRIMARY KEY, change_id text NOT NULL REFERENCES change_order,
 scope text NOT NULL, status text NOT NULL CHECK(status IN ('pending','done','excluded')), reason text,
 CHECK(status<>'excluded' OR (reason IS NOT NULL AND length(trim(reason))>0)));
CREATE TABLE mline(id text PRIMARY KEY, plant text NOT NULL, source_baseline text NOT NULL,
 component_revision text NOT NULL REFERENCES revision, quantity numeric NOT NULL CHECK(quantity>0),
 unit text NOT NULL REFERENCES unit, role text NOT NULL CHECK(role IN ('product','consumable')), reason text,
 CHECK(role<>'consumable' OR (reason IS NOT NULL AND length(trim(reason))>0)));
-- Полный путь важен: один и тот же узел может использоваться в разных местах.
CREATE TABLE mapping(path text NOT NULL, mline_id text NOT NULL REFERENCES mline,
 quantity numeric NOT NULL CHECK(quantity>0), PRIMARY KEY(path,mline_id));
CREATE TABLE resource(id text PRIMARY KEY, family text NOT NULL, qualified boolean NOT NULL);
CREATE TABLE operation(id text PRIMARY KEY, route text NOT NULL, family text NOT NULL,
 resource_id text NOT NULL REFERENCES resource, instruction text NOT NULL);
CREATE TABLE precedence(before_id text REFERENCES operation, after_id text REFERENCES operation,
 PRIMARY KEY(before_id,after_id), CHECK(before_id<>after_id));
CREATE TABLE allocation(mline_id text REFERENCES mline, operation_id text REFERENCES operation,
 quantity numeric NOT NULL CHECK(quantity>0), PRIMARY KEY(mline_id,operation_id));
CREATE TABLE lot(id text PRIMARY KEY, material text NOT NULL, certificate text NOT NULL);
CREATE TABLE serial_unit(id text PRIMARY KEY, revision_id text NOT NULL REFERENCES revision, lot_id text REFERENCES lot);
CREATE TABLE installation(id text PRIMARY KEY, parent_serial text NOT NULL REFERENCES serial_unit,
 position text NOT NULL, child_serial text NOT NULL REFERENCES serial_unit,
 effective_from timestamptz NOT NULL, effective_to timestamptz,
 recorded_at timestamptz NOT NULL, reason text NOT NULL,
 CHECK(effective_to IS NULL OR effective_to>effective_from), CHECK(parent_serial<>child_serial));
CREATE TABLE software_load(parent_serial text REFERENCES serial_unit, software_revision text REFERENCES revision,
 effective_from timestamptz NOT NULL, effective_to timestamptz, digest text NOT NULL,
 PRIMARY KEY(parent_serial,effective_from), CHECK(effective_to IS NULL OR effective_to>effective_from));
CREATE TABLE compatibility(hardware_revision text REFERENCES revision, software_revision text REFERENCES revision,
 PRIMARY KEY(hardware_revision,software_revision));
CREATE TABLE outbox(event_id text PRIMARY KEY, aggregate_id text NOT NULL, version integer NOT NULL, payload text NOT NULL);
CREATE TABLE inbox(event_id text PRIMARY KEY, aggregate_id text NOT NULL, version integer NOT NULL, payload text NOT NULL, result text NOT NULL);
CREATE TABLE erp_definition(aggregate_id text PRIMARY KEY, version integer NOT NULL, payload text NOT NULL);
CREATE TABLE migration_stage(source_id text PRIMARY KEY, item_code text NOT NULL,
 mass_kg numeric NOT NULL CHECK(mass_kg>0 AND mass_kg NOT IN ('NaN'::numeric,'Infinity'::numeric,'-Infinity'::numeric)),
 rule_version text NOT NULL, original text NOT NULL);
CREATE TABLE migration_target(item_code text PRIMARY KEY,
 mass_kg numeric NOT NULL CHECK(mass_kg>0 AND mass_kg NOT IN ('NaN'::numeric,'Infinity'::numeric,'-Infinity'::numeric)));
CREATE TABLE migration_map(source_id text PRIMARY KEY REFERENCES migration_stage, item_code text NOT NULL REFERENCES migration_target, rule_version text NOT NULL);

-- Готовое исходное представление для последующих тем. В L05 его надо воспроизвести
-- самостоятельно, не читая определение до сверки с эталоном.
CREATE VIEW base_tree AS
 WITH RECURSIVE walk AS (
   SELECT o.id::text AS path, b.revision_id, o.quantity AS quantity, o.unit,
          ARRAY['DM-A',b.revision_id] AS visited, b.revision_id='DM-A' AS cycle
   FROM occurrence o JOIN baseline b ON b.occurrence_id=o.id AND b.name='BASE-01'
   WHERE o.parent_revision='DM-A'
   UNION ALL
   SELECT w.path||'/'||o.id, b.revision_id, w.quantity*o.quantity, o.unit,
          w.visited||b.revision_id, b.revision_id=ANY(w.visited)
   FROM walk w JOIN occurrence o ON o.parent_revision=w.revision_id
   JOIN baseline b ON b.occurrence_id=o.id AND b.name='BASE-01'
   WHERE NOT w.cycle
 ) SELECT path,revision_id,quantity,unit,cycle FROM walk;
