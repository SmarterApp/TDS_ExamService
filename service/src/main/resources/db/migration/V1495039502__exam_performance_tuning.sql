/***********************************************************************************************************************
  File: V1495039502__exam_performance_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

-- ----------------------------------------------------------------------------
-- exam_event changes
-- ----------------------------------------------------------------------------
DROP INDEX ix_created_at ON exam.exam_event;

ALTER TABLE exam.exam_event
	DROP FOREIGN KEY exam_event_ibfk_1;

DROP INDEX ix_exam_event_exam_id_status_status_change_date ON exam.exam_event;

CREATE UNIQUE INDEX uix_exam_event_id_exam_id ON exam.exam_event(id, exam_id);

ALTER TABLE exam.exam_event
	ADD CONSTRAINT fk_exam_event_exam_id FOREIGN KEY(exam_id) REFERENCES exam.exam(id);