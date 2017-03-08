/***********************************************************************************************************************
  File: V1477420594__exam_create_exam_accommodations_table.sql

  Desc: Adds the exam_accommodations table

***********************************************************************************************************************/
USE exam;

CREATE TABLE IF NOT EXISTS exam_accommodations (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  exam_id VARBINARY(16) NOT NULL,
  segment_id VARCHAR(255) NOT NULL,
  type VARCHAR(50) NOT NULL,
  code VARCHAR(250) NOT NULL,
  description VARCHAR(250) NOT NULL,
  denied_at TIMESTAMP(3) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT pk_exam_accommodations PRIMARY KEY (id),
  KEY ix_exam_accommodations_exam_id_segment_id_type (exam_id, segment_id, type)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8;
