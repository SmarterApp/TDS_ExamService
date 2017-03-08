/***********************************************************************************************************************
  File: V1482511858__exam_accommodations_add_total_type_column.sql

  Desc: use int instead of boolean for total tool type

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_accommodation_event
  DROP COLUMN multiple_tool_types;

ALTER TABLE exam_accommodation_event
  ADD COLUMN total_type_count TINYINT NOT NULL DEFAULT 0;