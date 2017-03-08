/***********************************************************************************************************************
  File: V1475278170__exam_add_browser_id_column.sql

  Desc: Adds a browser_key column between session_id and assessment_id

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam
  ADD COLUMN browser_key VARBINARY(16) NOT NULL AFTER session_id;
