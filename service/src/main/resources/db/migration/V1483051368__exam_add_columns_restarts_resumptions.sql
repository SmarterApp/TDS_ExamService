/***********************************************************************************************************************

File: V1483051368__exam_add_columns_restarts_resumptions.sql

Desc: Adds some missing columns from the exam table required for starting an exam

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_event
  ADD COLUMN restarts_and_resumptions INT(11) DEFAULT 0 NOT NULL;

ALTER TABLE exam_event
  ADD COLUMN resumptions INT(11) DEFAULT 0 NOT NULL;