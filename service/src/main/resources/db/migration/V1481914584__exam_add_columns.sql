/***********************************************************************************************************************

File: V1481914584__exam_add_max_items_expire_from.sql

Desc: Adds some missing columns from the exam table required for initializing an exam

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_event
  ADD COLUMN expire_from DATETIME(3) DEFAULT NULL;

ALTER TABLE exam_event
  ADD COLUMN max_items INT(11) DEFAULT 0 NOT NULL;

ALTER TABLE exam_event
  ADD COLUMN language_code VARCHAR(50) DEFAULT NULL;
