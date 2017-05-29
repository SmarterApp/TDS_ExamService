/***********************************************************************************************************************
  File: V1496077806__exam_field_test_item_group_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

DROP INDEX ix_created_at ON exam.field_test_item_group_event;

CREATE INDEX ix_field_test_item_group_exam_id_segment_key_position ON exam.field_test_item_group(exam_id, segment_key, position);