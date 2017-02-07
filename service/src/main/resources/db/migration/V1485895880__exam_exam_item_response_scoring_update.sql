/***********************************************************************************************************************
  File: V1485895880__exam_exam_item_response_scoring_update.sql

  Desc: Added scoring-related columns to exam_item_response table.  Move is_selected from exam_item to
  exam_item_response (this field gets updated during the legacy updateResponses() method).

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_item_response
  ADD is_selected BIT NOT NULL DEFAULT b'0' AFTER is_valid;

ALTER TABLE exam_item_response
  ADD score INT(11) NULL AFTER is_selected;

ALTER TABLE exam_item_response
  ADD scoring_status VARCHAR(50) NULL AFTER score;

ALTER TABLE exam_item_response
  ADD scoring_rationale TEXT NULL AFTER scoring_status;

ALTER TABLE exam_item_response
  ADD scoring_dimensions VARCHAR(4096) NULL AFTER scoring_rationale;

ALTER TABLE exam_item_response
  ADD scored_at TIMESTAMP(3) NULL AFTER created_at;

ALTER TABLE exam_item
  DROP COLUMN is_selected;