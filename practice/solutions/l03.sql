CREATE TEMP TABLE perf AS SELECT g AS id,'DM-'||(g%1000) AS parent_id FROM generate_series(1,100000) g;
ANALYZE perf;
EXPLAIN (ANALYZE,BUFFERS) SELECT * FROM perf WHERE parent_id='DM-100';
CREATE INDEX ON perf(parent_id);
ANALYZE perf;
EXPLAIN (ANALYZE,BUFFERS) SELECT * FROM perf WHERE parent_id='DM-100';
SELECT count(*) FROM perf WHERE parent_id='DM-100';
SELECT count(*) AS baseline_paths FROM lab.base_tree;
