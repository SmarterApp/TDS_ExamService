/***********************************************************************************************************************
  File: V1487976556__exam_create_table_exam_requests.sql

  Desc: Create tables to store exam requests, such as print or embossing requests

***********************************************************************************************************************/
USE exam;

DROP TABLE  IF EXISTS exam_requests;
DROP TABLE  IF EXISTS exam_requests_event;

CREATE TABLE exam_print_request (
  id CHAR(36) NOT NULL,
  exam_id CHAR(36) NOT NULL,
  session_id CHAR(36) NOT NULL,
  type VARCHAR(50) NOT NULL,
  value VARCHAR(250) NOT NULL,
  item_position INT(11) NOT NULL,
  page_position INT(11) NOT NULL,
  parameters VARCHAR(255) DEFAULT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT pk_exam_request_id PRIMARY KEY (id),
  CONSTRAINT fk_exam_request_exam_id_exam_id FOREIGN KEY (exam_id) REFERENCES exam(id)
);

CREATE TABLE exam_print_request_event (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  exam_print_request_id CHAR(36) NOT NULL,
  approved_at DATETIME(3) DEFAULT NULL,
  denied_at DATETIME(3) DEFAULT NULL,
  reason_denied VARCHAR(250) DEFAULT NULL,
  CONSTRAINT pk_exam_request_id PRIMARY KEY (id),
  CONSTRAINT fk_exam_request_event_exam_request_id FOREIGN KEY (exam_print_request_id) REFERENCES exam_print_request(id)
);