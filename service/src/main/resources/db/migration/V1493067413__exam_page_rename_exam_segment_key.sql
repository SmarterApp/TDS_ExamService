/***********************************************************************************************************************
  File: V1493067413__exam_page_rename_exam_segment_key.sql

  Desc: Rename exam_segment_key column because it is really the segment_key.

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_page CHANGE COLUMN exam_segment_key segment_key varchar(250) NOT NULL;