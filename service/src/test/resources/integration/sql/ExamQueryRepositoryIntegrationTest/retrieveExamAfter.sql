/***********************************************************************************************************************
  File: V1473371684__exam_populate_test_record.sql

  Desc: Creates a test record in the database for integration tests

***********************************************************************************************************************/
USE exam;

DELETE FROM exam where unique_key = X'af880054d1d24c24805c0dfdb45a0d24';