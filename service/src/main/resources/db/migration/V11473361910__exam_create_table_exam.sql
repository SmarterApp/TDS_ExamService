/***********************************************************************************************************************
  File: V11473361910_exam_create_table_exam.sql

  Desc: Creates the initial exam database table

***********************************************************************************************************************/
USE exam;

CREATE TABLE IF NOT EXISTS exam(
  id INT NOT NULL AUTO_INCREMENT,
  unique_key VARBINARY(16) NOT NULL,
  session_id VARBINARY(16) NOT NULL,
  assessment_id VARCHAR(255) NOT NULL,
  student_id BIGINT(20) NOT NULL,
  times_taken INT(11) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'pending',
  client_name VARCHAR(100),
  date_started DATETIME(3) DEFAULT NULL,
  date_changed DATETIME(3) DEFAULT NULL,
  date_deleted DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE (unique_key)
);
