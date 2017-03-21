/***********************************************************************************************************************
  File: VV1490121506__exam_create_legacy_db_view_testopprequest.sql

  Desc: Create the QA view for testopprequest -> exam_print_request tables.

***********************************************************************************************************************/

use exam;

CREATE OR REPLACE VIEW qa_exam_most_recent_event_per_exam_print_request AS
	SELECT
		exam_print_request_id,
		MAX(id) AS id
	FROM
		exam_print_request_event
	GROUP BY
		exam_print_request_id;

CREATE OR REPLACE VIEW qa_session_testopprequest AS
	SELECT
		r.exam_id AS _fk_testopportunity,
		r.session_id AS _fk_session,
		r.type AS requesttype,
		r.value AS requesvalue,
		r.created_at AS datesubmitted,
		CASE WHEN re.status = 'DENIED' OR re.status = 'APPROVED'
			THEN re.created_at END AS datefulfilled,
		re.reason_denied AS denied,
		r.page_position AS itempage,
		r.item_position AS itemposition,
		r.parameters AS requestparameters,
		r.description AS requestdescription,
		CASE WHEN re.status = 'DENIED' THEN re.created_at END AS datedenied
	FROM
		exam_print_request r
	JOIN
		qa_exam_most_recent_event_per_exam_print_request AS last_event
		ON r.id = last_event.exam_print_request_id
	JOIN
		exam_print_request_event re
		ON last_event.exam_print_request_id = re.exam_print_request_id
		AND last_event.id = re.id
