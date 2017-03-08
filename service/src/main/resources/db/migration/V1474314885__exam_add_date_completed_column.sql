/***********************************************************************************************************************
  File: V1474314885__exam_add_date_completed_column.sql

  Desc: Adds a completed date column to the exam table

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam ADD COLUMN date_completed DATETIME(3) DEFAULT NULL;