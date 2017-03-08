/***********************************************************************************************************************
  File: V1480007481__exam_history_update_column_names.sql

  Desc: Updating the history table with new column names to match conventions.

***********************************************************************************************************************/

USE exam;

ALTER TABLE history CHANGE COLUMN assessment_component_id segment_id VARCHAR(200);
ALTER TABLE history CHANGE COLUMN admin_subject segment_key VARCHAR(250);
ALTER TABLE history CHANGE COLUMN fk_history_examid_exam exam_id VARBINARY(16);