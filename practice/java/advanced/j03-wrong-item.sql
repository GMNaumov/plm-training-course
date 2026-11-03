-- Существующая схема L05 позволяет это; создание нового снимка должно завершиться отказом.
UPDATE lab.baseline SET revision_id='SEN-A' WHERE name='BASE-01' AND occurrence_id='h10';
