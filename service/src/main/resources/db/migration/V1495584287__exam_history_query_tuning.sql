/***********************************************************************************************************************
  File: V1495584287__exam_history_query_tuning.sql

  Desc: Schema modification to tune the exam database for performance.

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam.history
	MODIFY id CHAR(36);

ALTER TABLE exam.history
	MODIFY exam_id CHAR(36) NOT NULL;