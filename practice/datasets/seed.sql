INSERT INTO unit VALUES ('ea','count',1),('kg','mass',1),('g','mass',0.001),('m','length',1),('mm','length',0.001);
INSERT INTO item VALUES
 ('DM','Демонстрационный модуль ДМ-01','assembly','ea'),('HO','Корпус','part','ea'),
 ('RO','Роторный узел','assembly','ea'),('SEN','Датчик','part','ea'),('HAR','Жгут','part','ea'),
 ('ECU','Блок управления','assembly','ea'),('FW','Прошивка','software','ea'),
 ('BOLT','Крепёж','part','ea'),('SEAL','Герметик','material','kg');
INSERT INTO revision(id,item_id,label,rev_order,state,valid_from,valid_to,series,serial_from,serial_to) VALUES
 ('DM-A','DM','A',1,'released','2026-01-01',NULL,'DM',1,NULL),
 ('HO-A','HO','A',1,'released','2026-01-01',NULL,'DM',1,100),
 ('HO-B','HO','B',2,'released','2026-07-01',NULL,'DM',101,NULL),
 ('HO-C','HO','C',3,'draft','2026-07-01',NULL,'DM',1,NULL),
 ('RO-A','RO','A',1,'released','2026-01-01',NULL,'DM',1,NULL),
 ('SEN-A','SEN','A',1,'released','2026-01-01','2026-07-01','DM',1,NULL),
 ('SEN-B','SEN','B',2,'released','2026-07-01',NULL,'DM',1,NULL),
 ('HAR-A','HAR','A',1,'released','2026-01-01',NULL,'DM',1,NULL),
 ('ECU-A','ECU','A',1,'released','2026-01-01',NULL,'DM',1,NULL),
 ('FW-A','FW','A',1,'released','2026-01-01',NULL,'DM',1,NULL),
 ('FW-B','FW','B',2,'draft','2026-07-01',NULL,'DM',1,NULL),
 ('BOLT-A','BOLT','A',1,'released','2026-01-01',NULL,'DM',1,NULL),
 ('SEAL-A','SEAL','A',1,'released','2026-01-01',NULL,'DM',1,NULL);
INSERT INTO occurrence VALUES
 ('o10','DM-A','10','HO','HO-A',1,'ea',NULL),
 ('o20','DM-A','20','RO','RO-A',1,'ea',NULL),
 ('o30','DM-A','30','SEN','SEN-A',1,'ea',NULL),
 ('o35','DM-A','35','SEN','SEN-A',1,'ea','REDUNDANT'),
 ('o40','DM-A','40','HAR','HAR-A',1,'ea',NULL),
 ('o50','DM-A','50','ECU','ECU-A',1,'ea',NULL),
 ('h10','HO-A','10','BOLT','BOLT-A',6,'ea',NULL),
 ('r10','RO-A','10','BOLT','BOLT-A',4,'ea',NULL),
 ('e10','ECU-A','10','FW','FW-A',1,'ea',NULL),
 ('e20','ECU-A','20','BOLT','BOLT-A',2,'ea',NULL);
INSERT INTO baseline
 SELECT 'BASE-01',id,fixed_revision,'{"date":"2026-06-01","series":"DM","serial":100,"options":"BASE","rule":"fixed-v1"}'::jsonb
 FROM occurrence WHERE option_code IS NULL;
INSERT INTO document VALUES
 ('DOC-1','A','released','HO-A','urn:training:HO-A:drawing'),
 ('DOC-2','A','released','HO-A','urn:training:HO-A:test'),
 ('DOC-3','A','draft','HO-B','urn:training:HO-B:test');
INSERT INTO requirement VALUES
 ('REQ-1','Корпус имеет подтверждённый контроль для выбранной ревизии','HO-B'),
 ('REQ-2','Базовый датчик имеет положительный результат проверки','SEN-A'),
 ('REQ-3','Жгут имеет подтверждённую проверку интерфейса','HAR-A');
INSERT INTO evidence VALUES
 ('EV-1','REQ-1','HO-A','DOC-2','pass'),
 ('EV-2','REQ-1','HO-B','DOC-3','pending'),
 ('EV-3','REQ-2','SEN-A','DOC-1','pass');
-- EV-3 намеренно ссылается на документ другой ревизии: FK не проверяет этот смысл.
INSERT INTO change_order VALUES ('CH-1','approved','HO-A','HO-B',0);
INSERT INTO change_action VALUES
 ('ACT-1','CH-1','engineering','done',NULL),('ACT-2','CH-1','plant-P1','pending',NULL),
 ('ACT-3','CH-1','service-DM-100','excluded','Внедрение только на новых экземплярах начиная с DM-101');
INSERT INTO mline VALUES
 ('M-HO','P1','BASE-01','HO-A',1,'ea','product',NULL),
 ('M-RO','P1','BASE-01','RO-A',1,'ea','product',NULL),
 ('M-SEN','P1','BASE-01','SEN-A',1,'ea','product',NULL),
 ('M-HAR','P1','BASE-01','HAR-A',1,'ea','product',NULL),
 ('M-ECU','P1','BASE-01','ECU-A',1,'ea','product',NULL),
 ('M-FW','P1','BASE-01','FW-A',1,'ea','product',NULL),
 ('M-BOLT','P1','BASE-01','BOLT-A',12,'ea','product',NULL),
 ('M-SEAL','P1','BASE-01','SEAL-A',0.05,'kg','consumable','Производственный расход, не инструмент');
INSERT INTO mapping VALUES
 ('o10','M-HO',1),('o20','M-RO',1),('o30','M-SEN',1),('o40','M-HAR',1),
 ('o50','M-ECU',1),('o50/e10','M-FW',1),
 ('o10/h10','M-BOLT',6),('o20/r10','M-BOLT',4),('o50/e20','M-BOLT',1);
INSERT INTO resource VALUES
 ('PRESS','blank',true),('CNC','machining',true),('FURNACE','thermal',true),
 ('COAT','coating',false),('BENCH','assembly',true),('ELEC','electrical',true),('TEST','inspection',true);
INSERT INTO operation VALUES
 ('OP10','ROUTE-A','blank','PRESS','INS-BLANK-A'),
 ('OP20','ROUTE-A','machining','CNC','INS-MACH-A'),
 ('OP30','ROUTE-A','thermal','FURNACE','INS-HEAT-A'),
 ('OP40','ROUTE-A','coating','COAT','INS-COAT-A'),
 ('OP50','ROUTE-A','assembly','BENCH','INS-ASSY-A'),
 ('OP60','ROUTE-A','electrical','ELEC','INS-ELEC-A'),
 ('OP70','ROUTE-A','inspection','TEST','INS-TEST-A');
INSERT INTO precedence VALUES ('OP10','OP20'),('OP20','OP30'),('OP30','OP40'),('OP40','OP50'),('OP50','OP60'),('OP60','OP70');
INSERT INTO allocation VALUES
 ('M-HO','OP50',1),('M-RO','OP50',1),('M-BOLT','OP50',12),
 ('M-SEN','OP60',1),('M-HAR','OP60',1),('M-ECU','OP60',1),('M-SEAL','OP50',0.05);
INSERT INTO lot VALUES ('LOT-1','Условная партия материала','urn:training:cert:LOT-1');
INSERT INTO serial_unit VALUES ('DM-100','DM-A',NULL),('S-001','SEN-A','LOT-1'),('S-002','SEN-B','LOT-1'),('EC-001','ECU-A',NULL);
INSERT INTO installation VALUES
 ('I-1','DM-100','30','S-001','2026-06-01Z','2026-08-10Z','2026-06-01Z','Первичная установка'),
 ('I-2','DM-100','30','S-002','2026-08-10Z',NULL,'2026-08-12Z','Замена при обслуживании'),
 ('I-3','DM-100','50','EC-001','2026-06-01Z',NULL,'2026-06-01Z','Первичная установка');
INSERT INTO software_load VALUES ('EC-001','FW-A','2026-06-01Z',NULL,'training-digest-not-real-binary');
INSERT INTO compatibility VALUES ('ECU-A','FW-A');
INSERT INTO outbox VALUES ('EVT-1','MBOM:DM:P1',1,'BASE-01');
