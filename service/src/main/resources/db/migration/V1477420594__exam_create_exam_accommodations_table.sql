/***********************************************************************************************************************
  File: V1477420594__exam_create_exam_accommodations_table.sql

  Desc: Adds the exam_accommodations table

***********************************************************************************************************************/
USE exam;

CREATE TABLE IF NOT EXISTS exam_accommodations (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  exam_id VARBINARY(16) NOT NULL,
  segment_id int(11) NOT NULL,
  accommodation_type VARCHAR(50) NOT NULL,
  accommodation_value VARCHAR(250) NOT NULL,
  accommodation_code VARCHAR(250) NOT NULL,
  CONSTRAINT pk_exam_accommodations PRIMARY KEY (id),
  UNIQUE KEY ux_exam_accommodations (exam_id, segment_id, accommodation_type, accommodation_code)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8;
