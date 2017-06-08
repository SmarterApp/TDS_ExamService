/***********************************************************************************************************************
  File: V1492035057__exam_move_mark_for_review_exam_item_to_response.sql

  Desc: Add msb column to exam

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam ADD COLUMN msb bit(1) NOT NULL DEFAULT b'0';