/***********************************************************************************************************************
  File: V1473962717__exam_create_status_codes_table.sql.sql

  Desc: Adds a status codes table

***********************************************************************************************************************/

USE exam;

CREATE TABLE exam_status_codes (
  status varchar(50) NOT NULL,
  description varchar(255) DEFAULT NULL,
  stage varchar(50) DEFAULT NULL,
  PRIMARY KEY (status)
);

INSERT INTO exam_status_codes VALUES ('suspended','an existing exam is waiting for proctor approval to restart','inuse');
INSERT INTO exam_status_codes VALUES ('scored','completed exam has been scored','closed');
INSERT INTO exam_status_codes VALUES ('reported','exam scores have been reported to reporting system','closed');
INSERT INTO exam_status_codes VALUES ('approved','exam has been approved for start/restart by proctor','inuse');
INSERT INTO exam_status_codes VALUES ('paused','exam has been put on hold by the user','inactive');
INSERT INTO exam_status_codes VALUES ('pending','a new exam is waiting for proctor approval','inuse');
INSERT INTO exam_status_codes VALUES ('expired','incomplete exam has expired','closed');
INSERT INTO exam_status_codes VALUES ('segmentExit','request to leave a segment','inuse');
INSERT INTO exam_status_codes VALUES ('denied','exam start was denied by the proctor','inactive');
INSERT INTO exam_status_codes VALUES ('segmentEntry','request to enter a segment','inuse');
INSERT INTO exam_status_codes VALUES ('review','student is reviewing completed exam items','inuse');
INSERT INTO exam_status_codes VALUES ('completed','exam has been completed and submitted for scoring by student','closed');
INSERT INTO exam_status_codes VALUES ('submitted','exam scores have been submitted to QA','closed');
INSERT INTO exam_status_codes VALUES ('rescored','the exam was rescored','closed');
INSERT INTO exam_status_codes VALUES ('invalidated','exam results have been invalidated','closed');
INSERT INTO exam_status_codes VALUES ('started','exam is in use','inuse');

ALTER TABLE exam ADD FOREIGN KEY (status) REFERENCES exam_status_codes(status);
ALTER TABLE exam ALTER COLUMN status DROP DEFAULT;
