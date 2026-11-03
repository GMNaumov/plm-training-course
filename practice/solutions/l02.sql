GRANT USAGE ON SCHEMA lab TO lab_reader;
GRANT SELECT ON lab.item,lab.revision TO lab_reader;
ALTER TABLE lab.revision ENABLE ROW LEVEL SECURITY;
CREATE POLICY released_only ON lab.revision FOR SELECT TO lab_reader USING(state='released');
SET ROLE lab_reader;
SELECT id,state FROM lab.revision ORDER BY id;
RESET ROLE;
