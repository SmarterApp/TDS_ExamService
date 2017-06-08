/***********************************************************************************************************************
  File: V1494607731__exam_item_group_id.sql

  Desc: Adds a group id column to the exam item

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_item ADD COLUMN group_id VARCHAR(100);

UPDATE exam_item set group_id = '' where group_id is null;

ALTER TABLE exam_item MODIFY group_id VARCHAR(100) NOT NULL;