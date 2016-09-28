/***********************************************************************************************************************
  File: V1475074781__exam_alter_add_rename_columns.sql

  Desc: Adds an auto incrementing primary int id and renames times_taken to attempts

***********************************************************************************************************************/

USE exam;

/*
Since this is an early migration there should be no data in the exam table except for development
purposes.  We need to change the primary key and alter the name.  Due to compatibility issues between
database engines the easiest way to do this right now is:

1. Drop the exam table
2. Recreate it

*/
DROP TABLE exam;

CREATE TABLE IF NOT EXISTS exam(
  id INT NOT NULL AUTO_INCREMENT,
  exam_id VARBINARY(16) NOT NULL,
  session_id VARBINARY(16) NOT NULL,
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
