/***********************************************************************************************************************
  File: V1483051338__exam_create_item_response_page_tables.sql

  Desc: Creates the item, page, and response tables

***********************************************************************************************************************/

USE exam;


DROP TABLE IF EXISTS exam_page;

CREATE TABLE exam_page (
  id BIGINT NOT NULL AUTO_INCREMENT,
  page_position INT(11) NOT NULL,
  item_group_key VARCHAR(25) NOT NULL,
  exam_id VARBINARY(16) NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  INDEX ix_created_at (created_at),
  PRIMARY KEY (id),
  KEY ix_exam_page (page_position, exam_id),
  CONSTRAINT fk_exam_page_examid_exam FOREIGN KEY (exam_id) REFERENCES exam (id)
);

DROP TABLE IF EXISTS exam_page_event;

CREATE TABLE exam_page_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_page_id BIGINT NOT NULL,
  deleted_at DATETIME(3) DEFAULT NULL,
  started_at DATETIME(3) DEFAULT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  PRIMARY KEY (id),
  INDEX ix_created_at (created_at),
  CONSTRAINT fk_exam_page_id FOREIGN KEY (exam_page_id) REFERENCES exam_page (id)
);

DROP TABLE IF EXISTS exam_item;

CREATE TABLE exam_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  item_key VARCHAR(25) NOT NULL,
  exam_page_id BIGINT NOT NULL,
  position INT(11) NOT NULL,
  type VARCHAR(10) NOT NULL,
  is_fieldtest BIT(1) DEFAULT b'0' NOT NULL,
  segment_id VARCHAR(100) DEFAULT NULL,
  is_required BIT(1) DEFAULT b'0' NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  PRIMARY KEY (id),
  INDEX ix_created_at (created_at),
  CONSTRAINT fk_exam_item_page_id_exam_page_id FOREIGN KEY (exam_page_id) REFERENCES exam_page(id)
);

DROP TABLE IF EXISTS exam_item_response;

CREATE TABLE exam_item_response (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_item_id BIGINT NOT NULL,
  response TEXT NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  PRIMARY KEY (id),
  INDEX ix_created_at (created_at),
  CONSTRAINT fk_exam_item_response_exam_item_id FOREIGN KEY (exam_item_id) REFERENCES exam_item (id)
);
