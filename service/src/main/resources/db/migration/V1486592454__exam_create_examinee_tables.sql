/***********************************************************************************************************************
  File: V1486592454__exam_create_examinee_tables.sql

  Desc: Create tables to store student attributes and their relationships

***********************************************************************************************************************/
USE exam;

CREATE TABLE IF NOT EXISTS examinee_attribute (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  exam_id CHAR(36) NOT NULL,
  context VARCHAR(10) NOT NULL,
  attribute_name VARCHAR(50) NOT NULL,
  attribute_value VARCHAR(400) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT pk_exam_attribute_id PRIMARY KEY (id),
  CONSTRAINT fk_exam_attribute_exam_id_exam_id FOREIGN KEY (exam_id) REFERENCES exam(id)
);

CREATE TABLE IF NOT EXISTS examinee_relationship (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  exam_id CHAR(36) NOT NULL,
  attribute_name VARCHAR(50) NOT NULL,
  attribute_value VARCHAR(500) NOT NULL,
  attribute_relationship VARCHAR(100) NOT NULL,
  context VARCHAR(10) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT pk_exam_relationship_id PRIMARY KEY (id),
  CONSTRAINT fk_exam_relationship_exam_id_exam_id FOREIGN KEY (exam_id) REFERENCES exam(id)
);