/***********************************************************************************************************************
  File: V1495496675__exam_exam_segment_query_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;
ALTER TABLE exam_segment_event
  DROP FOREIGN KEY fk_exam_segment_event_pk_exam_segment;

  DROP INDEX ix_created_at ON exam_segment_event;

  ALTER TABLE exam_segment_event
	ADD CONSTRAINT fk_exam_segment_event_exam_segment_id_segment_position
    FOREIGN KEY (exam_id, segment_position) REFERENCES exam_segment(exam_id, segment_position);