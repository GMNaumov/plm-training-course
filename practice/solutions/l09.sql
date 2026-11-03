SELECT a.id,a.scope,a.status,a.reason FROM lab.change_action a WHERE change_id='CH-1' ORDER BY id;
BEGIN;
SELECT id FROM lab.change_order WHERE id='CH-1' FOR UPDATE;
UPDATE lab.change_order SET state='implemented',lock_version=lock_version+1
 WHERE id='CH-1' AND state='approved' AND lock_version=0
 AND NOT EXISTS(SELECT FROM lab.change_action WHERE change_id='CH-1' AND status='pending');
-- До устранения pending ожидается UPDATE 0.
UPDATE lab.change_action SET status='done',reason='Учебное подтверждение переноса определения P1: DEC-P1-01' WHERE id='ACT-2';
UPDATE lab.change_order SET state='implemented',lock_version=lock_version+1
 WHERE id='CH-1' AND state='approved' AND lock_version=0
 AND NOT EXISTS(SELECT FROM lab.change_action WHERE change_id='CH-1' AND status='pending');
COMMIT;
UPDATE lab.change_order SET state='implemented',lock_version=lock_version+1 WHERE id='CH-1' AND lock_version=0;
SELECT id,state,lock_version FROM lab.change_order;
