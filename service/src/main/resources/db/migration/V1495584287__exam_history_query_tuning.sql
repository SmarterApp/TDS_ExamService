/***********************************************************************************************************************
  File: V1495584287__exam_history_query_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

DROP INDEX ix_created_at ON exam.exam_page_event;
CREATE UNIQUE INDEX uix_exam_page_event_exam_id_exam_page_id_deleted_at ON exam.exam_page_event(exam_id, exam_page_id, deleted_at);
