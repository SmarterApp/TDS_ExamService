/***********************************************************************************************************************

File: V1477583565__exam_add_missing_columns.sql

Desc: Adds some missing columns from the exam table required for opening an exam

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam
  ADD COLUMN date_joined DATETIME(3) DEFAULT NULL;

ALTER TABLE exam
  ADD COLUMN environment VARCHAR(50) NOT NULL;

ALTER TABLE exam
  ADD COLUMN assessment_key varchar(250) NOT NULL;

ALTER TABLE exam
  ADD COLUMN student_key varchar(128) DEFAULT NULL;

ALTER TABLE exam
  ADD COLUMN student_name varchar(128) DEFAULT NULL;

ALTER TABLE exam
  ADD COLUMN assessment_window_id varchar(50) DEFAULT NULL;

ALTER TABLE exam
  ADD COLUMN assessment_algorithm varchar(50) NOT NULL;

ALTER TABLE exam
  ADD COLUMN segmented bit(1) NOT NULL DEFAULT 0;



