/***********************************************************************************************************************
  File: V1475278170__exam_add_browser_id_column.sql

  Desc: Adds a browser_key column between session_id and assessment_id

***********************************************************************************************************************/

USE exam;

/*
Since this is an early migration there should be no data in the exam table except for development purposes.  Due to
compatibility issues between database engines the easiest way to do this right now is:

1. Drop the exam table
2. Recreate it

*/
DROP TABLE exam;

CREATE TABLE IF NOT EXISTS exam(
  id INT NOT NULL AUTO_INCREMENT,
  exam_id VARBINARY(16) NOT NULL,
  session_id VARBINARY(16) NOT NULL,
  browser_key VARBINARY(16) NOT NULL,
  assessment_id VARCHAR(255) NOT NULL,
  student_id BIGINT(20) NOT NULL,
  attempts INT(11) NOT NULL DEFAULT 0,
  status VARCHAR(50) NOT NULL DEFAULT 'pending',
  client_name VARCHAR(100),
  date_started DATETIME(3) DEFAULT NULL,
  date_changed DATETIME(3) DEFAULT NULL,
  date_deleted DATETIME(3) DEFAULT NULL,
  date_completed DATETIME(3) DEFAULT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  PRIMARY KEY (id)
);
