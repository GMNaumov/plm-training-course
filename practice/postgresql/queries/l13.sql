-- Контроль состояния после ручных запусков Java из L13.
SELECT * FROM lab.outbox ORDER BY event_id;
SELECT * FROM lab.inbox ORDER BY event_id;
SELECT * FROM lab.erp_definition ORDER BY aggregate_id;
