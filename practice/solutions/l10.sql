WITH covered AS (SELECT path,sum(quantity) q FROM lab.mapping GROUP BY path)
SELECT coalesce(t.path,c.path) path,t.quantity expected,c.q covered,
 CASE WHEN t.path IS NULL THEN 'EXTRA' WHEN c.path IS NULL THEN 'MISSING'
 WHEN t.quantity<>c.q THEN 'QUANTITY' ELSE 'OK' END diagnosis
FROM lab.base_tree t FULL JOIN covered c ON c.path=t.path ORDER BY path;
SELECT x.path,m.id FROM lab.mapping x JOIN lab.mline m ON m.id=x.mline_id
JOIN lab.base_tree t ON t.path=x.path
WHERE m.component_revision<>t.revision_id OR m.unit<>t.unit OR m.plant<>'P1' OR m.source_baseline<>'BASE-01';
SELECT m.id,m.quantity,coalesce(sum(x.quantity),0) mapped
FROM lab.mline m LEFT JOIN lab.mapping x ON x.mline_id=m.id WHERE m.role='product'
GROUP BY m.id HAVING m.quantity<>coalesce(sum(x.quantity),0);
UPDATE lab.mapping SET quantity=2 WHERE path='o50/e20' AND mline_id='M-BOLT';
SELECT m.id,m.quantity,coalesce(sum(x.quantity),0) mapped
FROM lab.mline m LEFT JOIN lab.mapping x ON x.mline_id=m.id WHERE m.role='product'
GROUP BY m.id HAVING m.quantity<>coalesce(sum(x.quantity),0);
