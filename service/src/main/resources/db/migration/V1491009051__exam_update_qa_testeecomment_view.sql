/***********************************************************************************************************************
  File: V1491009051__exam_update_qa_testeecomment_view.sql

  Desc: Update the qa_session_testeecomment view to be MySQL 5.6 compliant.  MySQL versions < 5.7 do not allow
  subqueries in views, meaning the normal approach to getting the most recent event record will not work.  The
  qa_session_testeecomment view has been updated to use another view that will get the most recent exam_event record.

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
    event.session_id AS _fk_session,
    'not migrated' AS groupid
  FROM
    examinee_note AS note
  JOIN
    qa_exam_most_recent_event_per_exam AS last_event
    ON note.exam_id = last_event.exam_id
  JOIN
	  exam_event AS event
	  ON last_event.id = event.id;