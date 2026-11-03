-- После reset. Кандидат той же идентичности, тот же приоритет и rev_order.
INSERT INTO lab.revision
 SELECT 'SEN-X',item_id,'X',rev_order,state,valid_from,valid_to,series,serial_from,serial_to,priority
 FROM lab.revision WHERE id='SEN-B';
