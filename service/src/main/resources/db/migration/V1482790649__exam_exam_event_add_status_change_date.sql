/***********************************************************************************************************************

File: V1482790649__exam_exam_event_add_status_change_date.sql

Desc: Adds status_change_date to the exam_event table to facilitate tracking when an exam's status changes

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_event
  ADD COLUMN status_change_date DATETIME(3) NOT NULL AFTER `status`;

CREATE INDEX ix_exam_event_exam_id_status_status_change_date ON exam_event(exam_id, `status`, status_change_date);