/***********************************************************************************************************************
  File: V1479850580_exam_add_abnormal_attempt_count.sql

  Desc: The system needs to track when a user reopens an exam that has already been started.

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_event ADD COLUMN abnormal_starts int(11) NOT NULL DEFAULT 0;