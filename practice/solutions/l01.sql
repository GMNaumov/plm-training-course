SET search_path=lab,public;
SELECT item_id,label,rev_order,state FROM revision WHERE item_id='HO' ORDER BY rev_order;
CREATE TABLE iteration(
 revision_id text REFERENCES revision, iteration_no integer CHECK(iteration_no>0),
 note text NOT NULL, PRIMARY KEY(revision_id,iteration_no));
INSERT INTO iteration VALUES ('HO-C',1,'Первое рабочее сохранение'),('HO-C',2,'Уточнённое рабочее сохранение');
SELECT (SELECT count(*) FROM item) AS items,
       (SELECT count(*) FROM revision) AS revisions,
       (SELECT count(*) FROM iteration) AS iterations;
