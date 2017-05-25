/***********************************************************************************************************************
  File: V1495584287__exam_history_query_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam.exam_event
	DROP FOREIGN KEY fk_exam_event_exam_id;

ALTER TABLE exam.exam_event
	ADD student_id BIGINT(20) NOT NULL AFTER exam_id;

ALTER TABLE exam.exam_event
	ADD assessment_id VARCHAR(255) NOT NULL AFTER student_id;

UPDATE
	exam.exam_event AS ee
JOIN
	exam.exam AS e
    ON e.id = ee.exam_id
SET
	ee.student_id = e.student_id,
    ee.assessment_id = e.assessment_id;

CREATE INDEX ix_exam_id_student_id_assessment_id ON exam.exam_event(exam_id, student_id, assessment_id);