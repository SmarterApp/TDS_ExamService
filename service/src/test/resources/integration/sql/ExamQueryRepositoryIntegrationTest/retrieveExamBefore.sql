/***********************************************************************************************************************
  File: retrieveExamBefore.sql

  Desc: Creates a test record in the database for integration test

***********************************************************************************************************************/
USE exam;

INSERT into exam (id, session_id, assessment_id, student_id, times_taken, client_name) values (X'af880054d1d24c24805c0dfdb45a0d24', X'244363EED4D34C02AFAE52FFE1AEAC33', 'assessmentId', 1, 0, 'clientName');