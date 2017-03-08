/***********************************************************************************************************************

File: V1484676942__exam_add_exam_item_columns.sql

Desc: Adds columns to the exam_item table for supporting the get page content feature

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_item
  ADD is_selected BIT NOT NULL DEFAULT b'0' AFTER is_required;

ALTER TABLE exam_item
  ADD is_marked_for_review BIT NULL AFTER is_selected;

ALTER TABLE exam_item
  DROP COLUMN is_required;

ALTER TABLE exam_item
  DROP COLUMN `type`;