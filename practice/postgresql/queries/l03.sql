-- Готовая искусственная нагрузка, не реальные конструкторские данные.
CREATE TEMP TABLE perf AS SELECT g AS id,'DM-'||(g%1000) AS parent_id FROM generate_series(1,100000) g;
ANALYZE perf;
-- TODO: EXPLAIN (ANALYZE, BUFFERS) выборки parent_id='DM-100'.
-- TODO: создать индекс, повторить план, сравнить оценки и фактические строки.
-- TODO: отдельно выполнить backup/restore из инструкции L03.
