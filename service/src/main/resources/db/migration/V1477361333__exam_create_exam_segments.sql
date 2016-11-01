/***********************************************************************************************************************
  File: V1477361333__exam_create_exam_segments.sql

  Desc: Creates the exam_segment and exam_segment_event database tables.

***********************************************************************************************************************/

USE exam;

CREATE TABLE exam_segment (
  fk_segment_examid_exam VARBINARY(16) NOT NULL,
  assessment_segment_key VARCHAR(250) NOT NULL,
  assessment_segment_id VARCHAR(100) DEFAULT NULL,
  segment_position INT(11) NOT NULL,
  form_key VARCHAR(50) DEFAULT NULL,
  form_id VARCHAR(200) DEFAULT NULL,
  algorithm VARCHAR(50) DEFAULT NULL,
  exam_item_count INT(11) DEFAULT NULL,
  field_test_item_count INT(11) DEFAULT NULL,
  field_test_items TEXT,
  form_cohort VARCHAR(20) DEFAULT NULL,
  pool_count INT(11) DEFAULT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  PRIMARY KEY (fk_segment_examid_exam, segment_position),
  KEY ix_segment_form_key (assessment_segment_key,form_key),
  --   CONSTRAINT `fk_exam_id_segment` FOREIGN KEY (`fk_segment_examid_exam`) REFERENCES exam (exam_id) ON DELETE CASCADE ON UPDATE NO ACTION
  INDEX ix_created_at (created_at)
);

CREATE TABLE exam_segment_event (
  id INT NOT NULL AUTO_INCREMENT,
  fk_segment_examid_exam VARBINARY(16) NOT NULL,
  segment_position INT(11) NOT NULL,
  satisfied BIT(1) NOT NULL DEFAULT b'0',
  permeable BIT(1) NOT NULL DEFAULT b'0',
  restore_permeable_on VARCHAR(50) DEFAULT NULL,
  date_exited DATETIME(3) DEFAULT NULL,
  item_pool TEXT,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  PRIMARY KEY (id),
  INDEX ix_created_at (created_at)
);
