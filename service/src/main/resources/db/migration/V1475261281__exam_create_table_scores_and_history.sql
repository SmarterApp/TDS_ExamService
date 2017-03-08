/***********************************************************************************************************************
  File: V1475261281__exam_create_table_scores_and_history.sql

  Desc: Creates the scores database table

***********************************************************************************************************************/
USE exam;

CREATE TABLE IF NOT EXISTS exam_scores(
  fk_scores_examid_exam VARBINARY(16) NOT NULL,
  measure_label VARCHAR(100) NOT NULL,
  value DOUBLE DEFAULT NULL,
  measure_of VARCHAR(150) NOT NULL,
  use_for_ability BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (fk_scores_examid_exam, measure_of, measure_label)
);

CREATE TABLE IF NOT EXISTS history(
  id VARBINARY(16) NOT NULL,
  client_name VARCHAR(100) NOT NULL,
  student_id BIGINT(20) NOT NULL,
  subject VARCHAR(100) NOT NULL,
  initial_ability FLOAT DEFAULT NULL,
  attempts INT(11) DEFAULT NULL,
  assessment_component_id VARCHAR(200) DEFAULT NULL,
  date_changed DATETIME(3) DEFAULT NULL,
  admin_subject VARCHAR(250) DEFAULT NULL,
  fk_history_examid_exam VARBINARY(16) NOT NULL,
  tested_grade VARCHAR(50) DEFAULT NULL,
  login_ssid VARCHAR(50) DEFAULT NULL,
  item_group_string TEXT,
  initial_ability_delim VARCHAR(400) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY ix_historyexamid (fk_history_examid_exam),
  KEY ix_historyloginssid (login_ssid),
  KEY ix_studenthistory (student_id, client_name, subject)
);