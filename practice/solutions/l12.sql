-- Фактическое время, с использованием всех имеющихся сегодня сведений.
SELECT position,child_serial FROM lab.installation
WHERE parent_serial='DM-100' AND effective_from<='2026-08-11T00:00:00Z'
AND (effective_to IS NULL OR '2026-08-11T00:00:00Z'<effective_to) ORDER BY position;
-- Полная реконструкция "что система знала" невозможна: история внесения
-- effective_to у I-1 не хранится. recorded_at у I-2 недостаточно. Не выдаём ложный ответ.
SELECT 'UNKNOWN: no history of relationship corrections' AS knowledge_at_aug11;
SELECT DISTINCT i.parent_serial FROM lab.installation i JOIN lab.serial_unit s ON s.id=i.child_serial
WHERE s.lot_id='LOT-1' ORDER BY i.parent_serial;
SELECT s.id,l.software_revision,CASE WHEN c.hardware_revision IS NULL THEN 'UNCONFIRMED' ELSE 'COMPATIBLE' END diagnosis
FROM lab.serial_unit s JOIN lab.software_load l ON l.parent_serial=s.id
LEFT JOIN lab.compatibility c ON c.hardware_revision=s.revision_id AND c.software_revision=l.software_revision
WHERE l.effective_to IS NULL;
SELECT a.id,b.id FROM lab.installation a JOIN lab.installation b
 ON a.parent_serial=b.parent_serial AND a.position=b.position AND a.id<b.id
WHERE tstzrange(a.effective_from,a.effective_to,'[)') && tstzrange(b.effective_from,b.effective_to,'[)');
