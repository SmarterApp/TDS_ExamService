/***********************************************************************************************************************

File: V1485218648__exam_exam_page_schema_changes.sql

Desc: Update the exam_page table schema

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_page
  MODIFY exam_segment_key VARCHAR(250) NOT NULL;

ALTER TABLE exam_page
  CHANGE COLUMN `group_items_required` `are_group_items_required` BIT NOT NULL DEFAULT b'1';