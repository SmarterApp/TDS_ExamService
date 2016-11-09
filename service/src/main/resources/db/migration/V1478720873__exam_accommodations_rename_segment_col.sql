/***********************************************************************************************************************
  File: V1478720873__exam_accommodations_rename_segment_col.sql

  Desc: Renames the segment_id column in exam_accommodations to segment_key and alters it to be nullable.

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_accommodations
  CHANGE segment_id segment_key VARCHAR(255) NULL;