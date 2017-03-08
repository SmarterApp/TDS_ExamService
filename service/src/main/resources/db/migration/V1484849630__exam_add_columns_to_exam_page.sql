/***********************************************************************************************************************

File: V1484849630__exam_add_columns_to_exam_page.sql

Desc: Adds columns to the exam_page table for supporting the get page content feature

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_page
  ADD group_items_required INT NOT NULL DEFAULT -1 AFTER item_group_key;

ALTER TABLE exam_page
  ADD exam_segment_key VARCHAR(250) NULL AFTER page_position;

ALTER TABLE exam_page
  ADD CONSTRAINT fk_exam_page_exam_segment_segment_key
    FOREIGN KEY (exam_segment_key)
    REFERENCES exam_segment(segment_key);