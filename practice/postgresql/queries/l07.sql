-- L07: SQL-сверка исходной позиции опционального датчика.
SELECT id,child_item,quantity,option_code FROM lab.occurrence WHERE parent_revision='DM-A' ORDER BY position;
-- Основная заготовка: practice/python/options.py.
