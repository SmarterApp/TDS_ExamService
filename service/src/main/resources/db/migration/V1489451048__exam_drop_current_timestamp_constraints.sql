/***********************************************************************************************************************
  File: V1489451048__exam_drop_current_timestamp_constraints.sql

  Desc: Update to remove DEFAULT CURRENT_TIMESTAMP constraints from columns.

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_accommodation
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_accommodation_event
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_event
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_item
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_item_response
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_page
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_page_event
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_print_request
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_segment
  ALTER created_at DROP DEFAULT;

ALTER TABLE exam_segment_event
  ALTER created_at DROP DEFAULT;

ALTER TABLE examinee_attribute
  ALTER created_at DROP DEFAULT;

ALTER TABLE examinee_relationship
  ALTER created_at DROP DEFAULT;

ALTER TABLE field_test_item_group
  ALTER created_at DROP DEFAULT;

ALTER TABLE field_test_item_group_event
  ALTER created_at DROP DEFAULT;