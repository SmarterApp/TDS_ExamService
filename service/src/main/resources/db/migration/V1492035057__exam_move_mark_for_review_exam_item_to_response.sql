/***********************************************************************************************************************
  File: V1492035057__exam_move_mark_for_review_exam_item_to_response.sql

  Desc: Removes the mark_for_review column from exam_item to exam_item_response.

***********************************************************************************************************************/
USE exam;

ALTER TABLE
  exam_item DROP COLUMN is_marked_for_review;

ALTER TABLE
  exam_item_response ADD COLUMN is_marked_for_review BIT NOT NULL DEFAULT b'0';