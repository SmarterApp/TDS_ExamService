/***********************************************************************************************************************
  File: V1494292728__exam_page_group_items_required.sql

  Desc: item group required number of items needs to be a number

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_page DROP COLUMN are_group_items_required;
ALTER TABLE exam_page ADD COLUMN group_items_required INT NOT NULL DEFAULT -1;