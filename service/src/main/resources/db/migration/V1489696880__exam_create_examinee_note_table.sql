/***********************************************************************************************************************
  File: V1489696880__exam_create_examinee_note_table.sql

  Desc: Create the examinee_note table for storing exam-related and item-related notes from the notepad tool

***********************************************************************************************************************/

USE exam;

DROP TABLE IF EXISTS examinee_note;

CREATE TABLE examinee_note(
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id CHAR(36) NOT NULL,
  context VARCHAR(11) NOT NULL,
  item_position INT NOT NULL DEFAULT 0,
  note TEXT NOT NULL,
  created_at TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_examinee_notes_id PRIMARY KEY (id),
  CONSTRAINT fk_examinee_notes_exam_id FOREIGN KEY (exam_id) REFERENCES exam(id)
);

CREATE INDEX ix_examinee_note_exam_id_context ON examinee_note(exam_id, context);

CREATE OR REPLACE VIEW qa_session_testeecomment AS
  SELECT
    'not migrated' AS clientname,
    'not migrated' AS _efk_testee,
    exam_id AS _fk_testopportunity,
    item_position AS itemposition,
    note AS comment,
    created_at AS `date`,
    context AS context,
    'not migrated' AS _fk_session,
    'not migrated' AS groupid
  FROM
    examinee_note;