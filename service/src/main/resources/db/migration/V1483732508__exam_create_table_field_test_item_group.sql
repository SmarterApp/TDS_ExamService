/***********************************************************************************************************************
  File: V1483732508__exam_create_table_field_test_item_group.sql

  Desc: Creates the field_test_item_group table

  The purpose of this table is to store field test item groups assigned to exam segments.

***********************************************************************************************************************/

USE exam;

DROP TABLE  IF EXISTS field_test_item_group_event;
DROP TABLE IF EXISTS field_test_item_group;

CREATE TABLE field_test_item_group (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id VARBINARY(16) NOT NULL,
  position INT(11) NOT NULL,
  item_count INT(11) DEFAULT NULL,
  segment_id VARCHAR(100) NOT NULL,
  segment_key VARCHAR(200) NOT NULL,
  group_id VARCHAR(50) NOT NULL,
  group_key VARCHAR(60) NOT NULL,
  block_id VARCHAR(10) NOT NULL,
  session_id VARBINARY(16) NOT NULL,
  language_code VARCHAR(50) NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  INDEX ix_created_at (created_at),
  PRIMARY KEY (id),
  KEY ix_ftitem_cluster (exam_id, group_id),
  KEY ix_ftexamitemgroup_pk (segment_key, language_code, group_key),
  CONSTRAINT fk_field_test_item_group_exam_id_exam FOREIGN KEY (exam_id) REFERENCES exam (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE field_test_item_group_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  field_test_item_group_id BIGINT(20) NOT NULL,
  deleted_at DATETIME(3) DEFAULT NULL,
  position_administered INT(11) DEFAULT NULL,
  administered_at DATETIME(3) DEFAULT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  PRIMARY KEY(id),
  INDEX ix_created_at (created_at),
  FOREIGN KEY (field_test_item_group_id) REFERENCES field_test_item_group(id)
);