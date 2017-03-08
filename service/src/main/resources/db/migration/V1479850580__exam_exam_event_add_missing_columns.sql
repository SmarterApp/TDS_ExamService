/***********************************************************************************************************************
  File: V1479850580_exam_add_abnormal_attempt_count.sql

  Desc: A few columns were missed when creating the exam event table.  abnormal_starts contains the number of times the
  exam was reopened after being started.  waiting_for_segment_approval is a binary column indicating whether the proctor
  needs to approve going to the next segment.  current_segment_position indicates which segment is the current active
  segment.

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_event ADD COLUMN abnormal_starts int(11) NOT NULL DEFAULT 0;

ALTER TABLE exam_event ADD COLUMN waiting_for_segment_approval BIT(1);

ALTER TABLE exam_event ADD COLUMN current_segment_position int(11);