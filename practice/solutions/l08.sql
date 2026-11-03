SELECT r.id,r.target_revision,
 EXISTS(SELECT FROM lab.evidence e JOIN lab.document d ON d.id=e.document_id
 WHERE e.requirement_id=r.id AND e.tested_revision=r.target_revision
 AND d.source_revision=r.target_revision AND d.status='released' AND e.result='pass') AS confirmed
FROM lab.requirement r ORDER BY r.id;
SELECT r.id AS requirement_id,e.id AS evidence_id,
 CASE WHEN e.id IS NULL THEN 'MISSING_EVIDENCE'
      WHEN e.tested_revision<>r.target_revision THEN 'WRONG_TESTED_REVISION'
      WHEN d.source_revision<>r.target_revision THEN 'WRONG_DOCUMENT_REVISION'
      WHEN d.status<>'released' THEN 'DOCUMENT_NOT_RELEASED'
      WHEN e.result<>'pass' THEN 'NOT_PASSED' ELSE 'CONFIRMED' END AS diagnosis
FROM lab.requirement r LEFT JOIN lab.evidence e ON e.requirement_id=r.id
LEFT JOIN lab.document d ON d.id=e.document_id ORDER BY r.id,e.id;
INSERT INTO lab.document VALUES
 ('DOC-4','A','released','HO-B','urn:training:HO-B:approved-test'),
 ('DOC-5','A','released','SEN-A','urn:training:SEN-A:approved-test');
INSERT INTO lab.evidence VALUES ('EV-4','REQ-1','HO-B','DOC-4','pass'),('EV-5','REQ-2','SEN-A','DOC-5','pass');
SELECT r.id,EXISTS(SELECT FROM lab.evidence e JOIN lab.document d ON d.id=e.document_id
 WHERE e.requirement_id=r.id AND e.tested_revision=r.target_revision
 AND d.source_revision=r.target_revision AND d.status='released' AND e.result='pass') AS confirmed
FROM lab.requirement r ORDER BY r.id;
