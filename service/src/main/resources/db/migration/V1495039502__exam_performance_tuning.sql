/***********************************************************************************************************************
  File: V1495039502__exam_performance_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

-- ----------------------------------------------------------------------------
-- exam changes
-- ----------------------------------------------------------------------------

CREATE INDEX ix_exam_student_id_assessment_id_client_name ON exam(student_id, assessment_id, client_name);

-- ----------------------------------------------------------------------------
-- exam_event changes
-- ----------------------------------------------------------------------------
DROP INDEX ix_created_at ON exam_event;

ALTER TABLE exam_event
	DROP FOREIGN KEY exam_event_ibfk_1;

DROP INDEX ix_exam_event_exam_id_status_status_change_date ON exam_event;

CREATE UNIQUE INDEX uix_exam_event_id_exam_id_deleted_at ON exam.exam_event(id, exam_id, deleted_at);

ALTER TABLE exam_event
	ADD CONSTRAINT fk_exam_event_exam_id FOREIGN KEY(exam_id) REFERENCES exam(id);

-- ----------------------------------------------------------------------------
-- exam_item_response changes
-- ----------------------------------------------------------------------------
CREATE INDEX ix_exam_item_response_exam_item_id_created_at ON exam.exam_item_response(exam_item_id, created_at);