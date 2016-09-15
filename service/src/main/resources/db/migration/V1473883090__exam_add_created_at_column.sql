/***********************************************************************************************************************
  File: V1473883090__exam_add_created_at_column.sql

  Desc: Adds a created_at field for audit

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam
  ADD COLUMN created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL;