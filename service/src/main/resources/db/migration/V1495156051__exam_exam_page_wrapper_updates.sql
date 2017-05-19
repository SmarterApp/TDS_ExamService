/***********************************************************************************************************************
  File: V1495156051__exam_exam_page_wrapper_updates.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

-- ----------------------------------------------------------------------------
-- Add exam_id to exam_page_event table
-- ----------------------------------------------------------------------------
ALTER TABLE exam.exam_page_event
	ADD exam_id CHAR(36) NOT NULL AFTER exam_page_id;

-- Migrate existing data
UPDATE
  exam.exam_page_event
JOIN
  exam.exam_page
	ON exam.exam_page.id = exam.exam_page_event.exam_page_id
SET
  exam.exam_page_event.exam_id = exam_page.exam_id;

ALTER TABLE exam.exam_page_event
	ADD CONSTRAINT fk_exam_page_event_exam_id
    FOREIGN KEY (exam_id) REFERENCES exam(id);

CREATE UNIQUE INDEX ix_exam_page_event_id_exam_id ON exam.exam_page_event(id, exam_id);
CREATE INDEX ix_exam_page_event_exam_id_exam_page_id ON exam.exam_page_event(exam_id, exam_page_id);

-- ----------------------------------------------------------------------------
-- Add exam_id to exam_item_response table
-- ----------------------------------------------------------------------------
ALTER TABLE exam.exam_item_response
	ADD exam_id CHAR(36) NOT NULL AFTER exam_item_id;

-- Migrate existing data
UPDATE
	exam.exam_item_response AS response
JOIN
	exam_item AS item
	ON item.id = response.exam_item_id
JOIN
	exam_page AS page
    ON page.id = item.exam_page_id
JOIN
	exam AS exam
    ON exam.id = page.exam_id
SET
	response.exam_id = exam.id;

ALTER TABLE exam.exam_item_response
	ADD CONSTRAINT fk_exam_item_response_exam_id
		FOREIGN KEY (exam_id) REFERENCES exam(id);

CREATE INDEX ix_exam_item_response_exam_id ON exam.exam_item_response(exam_id);
CREATE INDEX ix_exam_item_response_exam_id_exam_item_id ON exam.exam_item_response(exam_id, exam_item_id);