WITH contexts(n,item_id,at_date,series,serial) AS (VALUES
 (1,'HO',DATE '2026-06-30','DM',100),(2,'HO',DATE '2026-07-01','DM',100),
 (3,'HO',DATE '2026-07-01','DM',101),(4,'HO',DATE '2026-06-30','DM',101),
 (5,'SEN',DATE '2026-06-30','DM',100),(6,'SEN',DATE '2026-07-01','DM',100),
 (7,'HO',DATE '2026-07-01','OTHER',100)
), candidates AS (
 SELECT c.n,r.id,dense_rank() OVER(PARTITION BY c.n ORDER BY r.priority DESC,r.rev_order DESC) rk
 FROM contexts c JOIN lab.revision r ON r.item_id=c.item_id
 WHERE r.state='released' AND c.at_date>=r.valid_from AND (r.valid_to IS NULL OR c.at_date<r.valid_to)
 AND c.series=r.series AND c.serial>=r.serial_from AND (r.serial_to IS NULL OR c.serial<=r.serial_to)
)
SELECT c.n,c.item_id,c.at_date,c.serial,
 CASE count(k.id) WHEN 0 THEN 'MISSING' WHEN 1 THEN 'OK' ELSE 'AMBIGUOUS' END diagnosis,
 string_agg(k.id,',' ORDER BY k.id) AS selected
FROM contexts c LEFT JOIN candidates k ON k.n=c.n AND k.rk=1
GROUP BY c.n,c.item_id,c.at_date,c.serial ORDER BY c.n;
INSERT INTO lab.baseline VALUES ('HO-101','o10','HO-B','{"date":"2026-07-01","series":"DM","serial":101,"rule":"released-v1","scope":"single occurrence only"}');
