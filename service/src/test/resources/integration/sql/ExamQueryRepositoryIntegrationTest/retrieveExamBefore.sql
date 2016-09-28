/***********************************************************************************************************************
  File: retrieveExamBefore.sql

  Desc: Creates a test record in the database for integration test

***********************************************************************************************************************/
USE exam;

INSERT into exam (exam_id, session_id, assessment_id, student_id, attempts, client_name, status) values (X'af880054d1d24c24805c0dfdb45a0d24', X'244363EED4D34C02AFAE52FFE1AEAC33', 'assessmentId', 1, 0, 'clientName', 'pending');
INSERT into exam (exam_id, session_id, assessment_id, student_id, attempts, client_name, date_deleted, status) values (X'ab880054d1d24c24805c0dfdb45a0d24', X'244363EED4D34C02AFAE52FFE1AEAC33', 'assessmentId2', 1, 0, 'clientName', current_timestamp, 'pending');