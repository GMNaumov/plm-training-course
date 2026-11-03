WITH RECURSIVE walk AS (
 SELECT o.id::text path,b.revision_id,o.quantity,o.unit,
        ARRAY['DM-A',b.revision_id] visited,b.revision_id='DM-A' cycle
 FROM lab.occurrence o JOIN lab.baseline b ON b.occurrence_id=o.id AND b.name='BASE-01'
 WHERE o.parent_revision='DM-A'
 UNION ALL
 SELECT w.path||'/'||o.id,b.revision_id,w.quantity*o.quantity,o.unit,
        w.visited||b.revision_id,b.revision_id=ANY(w.visited)
 FROM walk w JOIN lab.occurrence o ON o.parent_revision=w.revision_id
 JOIN lab.baseline b ON b.occurrence_id=o.id AND b.name='BASE-01' WHERE NOT w.cycle
) SELECT path,revision_id,quantity,unit,cycle FROM walk ORDER BY path;
SELECT revision_id,unit,sum(quantity) FROM lab.base_tree WHERE NOT cycle GROUP BY revision_id,unit ORDER BY revision_id;
SELECT parent_revision,id,quantity FROM lab.occurrence WHERE child_item='BOLT' ORDER BY parent_revision;
