/***********************************************************************************************************************
  File: V1475701532__exam_move_exam_status_to_join_table.sql

  Desc: Adds a status_change_reason column and rename the browser_key column to browser_id

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam
    ADD COLUMN status_change_reason VARCHAR(255) NULL;

ALTER TABLE exam
    CHANGE `browser_key` `browser_id` VARBINARY(16) NOT NULL;