/***********************************************************************************************************************
  File: V1495487791__exam_exam_page_query_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

DROP INDEX ix_exam_page ON exam.exam_page;
DROP INDEX ix_created_at ON exam.exam_page;

CREATE INDEX ix_exam_page_exam_id_page_position ON exam.exam_page(exam_id, page_position);

-- Rename foreign key constraint to be consistent with other names
ALTER TABLE exam.exam_page
	DROP FOREIGN KEY fk_exam_page_examid_exam;

ALTER TABLE exam.exam_page
	ADD CONSTRAINT fk_exam_page_exam_id
    FOREIGN KEY (exam_id) REFERENCES exam(id);