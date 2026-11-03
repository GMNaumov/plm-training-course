SELECT * FROM lab.outbox ORDER BY event_id;
SELECT * FROM lab.inbox ORDER BY event_id;
SELECT * FROM lab.erp_definition ORDER BY aggregate_id;
-- После EVT-1 и безопасного повтора: 1 inbox, 1 erp_definition, version=1.
