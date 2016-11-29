/***********************************************************************************************************************
  File: V1480369079__exam_exam_accommodations_event.sql

  Desc: Exam Accommodations requires an event table that can be updated

***********************************************************************************************************************/

use exam;

CREATE TABLE IF NOT EXISTS exam_accommodation_event (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  exam_accommodation_id BIGINT(20) NOT NULL,
  denied_at TIMESTAMP(3) NULL,
  deleted_at TIMESTAMP(3) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  FOREIGN KEY (exam_accommodation_id) REFERENCES exam_accommodations(id)
);

ALTER TABLE exam_accommodations DROP COLUMN denied_at;

ALTER TABLE exam_accommodations RENAME exam_accommodation;