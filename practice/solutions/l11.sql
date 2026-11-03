SELECT m.id,m.quantity,coalesce(sum(a.quantity),0) allocated
FROM lab.mline m LEFT JOIN lab.allocation a ON a.mline_id=m.id GROUP BY m.id
HAVING m.quantity<>coalesce(sum(a.quantity),0) ORDER BY m.id;
SELECT o.id,o.family,r.family resource_family,r.qualified
FROM lab.operation o JOIN lab.resource r ON r.id=o.resource_id
WHERE o.family<>r.family OR NOT r.qualified;
WITH RECURSIVE p AS (
 SELECT before_id,after_id,ARRAY[before_id,after_id] visited,false cycle FROM lab.precedence
 UNION ALL
 SELECT p.before_id,e.after_id,p.visited||e.after_id,e.after_id=ANY(p.visited)
 FROM p JOIN lab.precedence e ON e.before_id=p.after_id WHERE NOT p.cycle
) SELECT * FROM p WHERE cycle;
INSERT INTO lab.allocation VALUES ('M-FW','OP60',1);
SELECT o.id FROM lab.operation o WHERE NOT EXISTS(SELECT FROM lab.precedence p WHERE p.after_id=o.id) ORDER BY o.id;
SELECT o.id AS finish FROM lab.operation o WHERE NOT EXISTS(SELECT FROM lab.precedence p WHERE p.before_id=o.id) ORDER BY o.id;
