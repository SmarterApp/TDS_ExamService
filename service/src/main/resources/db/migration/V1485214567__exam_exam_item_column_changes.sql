/***********************************************************************************************************************

File: V1485214567__exam_exam_item_column_changes.sql

Desc: Adds columns to the exam_item table for supporting the get page content feature; specifically additional fields
that are required to support mapping exam items to legacy OpportunityItems

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_item
  MODIFY is_marked_for_review BIT NOT NULL DEFAULT b'0';

ALTER TABLE exam_item
  ADD assessment_item_bank_key BIGINT(19) NOT NULL AFTER item_key;

ALTER TABLE exam_item
  ADD assessment_item_key BIGINT(19) NOT NULL AFTER assessment_item_bank_key;

ALTER TABLE exam_item
  ADD item_type VARCHAR(50) NOT NULL AFTER assessment_item_key;

ALTER TABLE exam_item
  ADD item_file_path VARCHAR(500) NOT NULL AFTER is_marked_for_review;

ALTER TABLE exam_item
  ADD stimulus_file_path VARCHAR(500) NULL AFTER item_file_path;

ALTER TABLE exam_item
  ADD is_required BIT NOT NULL DEFAULT b'0' AFTER is_fieldtest;

ALTER TABLE exam_item
  DROP COLUMN segment_id;