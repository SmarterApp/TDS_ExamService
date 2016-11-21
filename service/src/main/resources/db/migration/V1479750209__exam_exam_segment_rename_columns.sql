/***********************************************************************************************************************
  File: V1479750209_exam_exam_segment_rename_columns.sql

  Desc: We tweaked our team naming standards for foreign keys and their constraints.  This will update the exam_segments
  table to follow the new convention.  In addition this script will use better names for assessment related columns.

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_segment CHANGE COLUMN assessment_segment_key segment_key VARCHAR(250);
ALTER TABLE exam_segment CHANGE COLUMN assessment_segment_id segment_id VARCHAR(100);
ALTER TABLE exam_segment CHANGE COLUMN fk_segment_examid_exam exam_id VARBINARY(16);
ALTER TABLE exam_segment ADD CONSTRAINT fk_exam_segment_examid_exam FOREIGN KEY (exam_id) REFERENCES exam(id);

ALTER TABLE exam_segment_event CHANGE COLUMN fk_segment_examid_exam exam_id VARBINARY(16);

ALTER TABLE exam_segment_event ADD CONSTRAINT fk_exam_segment_event_pk_exam_segment FOREIGN KEY (exam_id, segment_position) REFERENCES exam_segment(exam_id, segment_position);