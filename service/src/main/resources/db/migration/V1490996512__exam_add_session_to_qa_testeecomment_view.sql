/***********************************************************************************************************************
  File: V1490996512__exam_add_session_to_qa_testeecomment_view.sql

  Desc: Get the session_id for the exam to which the examinee note(s) are associated

***********************************************************************************************************************/
USE exam;

CREATE OR REPLACE VIEW qa_session_testeecomment AS
  SELECT
    'not migrated' AS clientname,
    'not migrated' AS _efk_testee,
    note.exam_id AS _fk_testopportunity,
    note.item_position AS itemposition,
    note.note AS comment,
    note.created_at AS `date`,
    note.context AS context,
    last_event.session_id AS _fk_session,
    'not migrated' AS groupid
  FROM
    examinee_note AS note
  JOIN (
    SELECT
      exam_id,
      session_id,
         MAX(id) AS id
      FROM
        exam.exam_event
    GROUP BY
      exam_id,
      session_id
	) AS last_event
	ON
		note.exam_id = last_event.exam_id;