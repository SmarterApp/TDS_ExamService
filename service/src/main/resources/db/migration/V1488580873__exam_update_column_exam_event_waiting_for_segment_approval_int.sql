/***********************************************************************************************************************
  File: V1488580873__exam_update_column_exam_event_waiting_for_segment_approval_int.sql

  Desc: Updates the waiting_for_segment_approval column from a BIT to INT and renames it.
  This column stores a segment position of the segment waiting for approval.

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam.exam_event
    CHANGE waiting_for_segment_approval waiting_for_segment_approval_position INT DEFAULT -1;