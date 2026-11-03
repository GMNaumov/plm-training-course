-- Все изменения действий сервиса выполняются под блокировкой change_order.
BEGIN;
SELECT id FROM lab.change_order WHERE id='CH-1' FOR NO KEY UPDATE;
UPDATE lab.change_action SET status='done' WHERE id='ACT-2';
COMMIT;
