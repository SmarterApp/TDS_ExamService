/***********************************************************************************************************************
  File: V1479514472__exam_create_exam_event_table.sql

  Desc: To keep the immutability pattern in exam we need to have a single record that represents an exam.  The exam table
  contains those things that never change once created.  exam_event contains the data that may change over time.  For
  example

***********************************************************************************************************************/
USE exam;

DROP TABLE IF EXISTS exam;

CREATE TABLE exam (
  id varbinary(16) NOT NULL,
  client_name varchar(100) NOT NULL,
  environment varchar(50) NOT NULL,
  session_id varbinary(16) NOT NULL,
  browser_id varbinary(16) NOT NULL,
  subject varchar(20) NOT NULL,
  login_ssid varchar(128) DEFAULT NULL,
  student_id bigint(20) NOT NULL,
  student_key varchar(128) DEFAULT NULL,
  student_name varchar(128) DEFAULT NULL,
  assessment_id varchar(255) NOT NULL,
  assessment_key varchar(250) NOT NULL,
  assessment_window_id varchar(50) DEFAULT NULL,
  assessment_algorithm varchar(50) NOT NULL,
  segmented bit(1) NOT NULL DEFAULT b'0',
  date_started datetime(3) DEFAULT NULL,
  created_at timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS exam_event;

CREATE TABLE exam_event (
  id int(11) NOT NULL AUTO_INCREMENT,
  exam_id varbinary(16) NOT NULL,
  attempts int(11) NOT NULL DEFAULT 0,
  status varchar(50) NOT NULL DEFAULT 'pending',
  status_change_reason varchar(255) DEFAULT NULL,
  date_changed datetime(3) DEFAULT NULL,
  date_deleted datetime(3) DEFAULT NULL,
  date_completed datetime(3) DEFAULT NULL,
  date_scored datetime(3) DEFAULT NULL,
  date_joined datetime(3) DEFAULT NULL,
  created_at timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  FOREIGN KEY fk_exam_event_examid_exam (exam_id) REFERENCES exam(id),
  INDEX ix_created_at (created_at)
);