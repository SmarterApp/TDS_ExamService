/***********************************************************************************************************************
  File: V1495823498__exam_exam_print_request_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

CREATE INDEX ix_exam_print_request_exam_id_session_id ON exam.exam_print_request(exam_id, session_id);
CREATE INDEX ix_exam_print_request_event_print_request_id_status ON exam.exam_print_request_event(exam_print_request_id, status);