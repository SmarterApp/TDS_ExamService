/***********************************************************************************************************************
  File: V1494977225__exam_fix_waiting_and_attempts_conversion.sql

  Desc: Alter view fixing the waiting for segment and attempts conversion

***********************************************************************************************************************/

USE exam;

CREATE OR REPLACE VIEW qa_session_testopportunity AS
  SELECT
    e.student_id AS _efk_testee,
    e.assessment_id AS _efk_testid,
    ee.attempts AS opportunity,
    ee.session_id AS _fk_session,
    ee.browser_id AS _fk_browser,
    e.login_ssid AS testeeid,
    e.student_name AS testeename,
    esc.stage AS stage,
    esc.status AS status,
    (SELECT status FROM exam_event WHERE id < last_event.id AND exam_id = last_event.exam_id ORDER BY id DESC LIMIT 1) AS prevstatus,
    ee.restarts_and_resumptions AS restart,
    ee.resumptions AS graceperiodrestarts,
    ee.changed_at AS datechanged,
    e.joined_at AS datejoined,
    ee.started_at AS datestarted,
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'restarted') AS daterestarted,
    ee.completed_at AS datecompleted,
    ee.scored_at AS datescored,
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'approved') AS dateapproved,
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'expired') AS dateexpired,
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'submitted') AS datesubmitted,
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'reported') AS datereported,
    ee.status_change_reason AS comment,
    ee.abnormal_starts AS abnormalstarts,
    'not migrated' AS reportingid,
    'not migrated' AS xmlhost,
    ee.max_items AS maxitems,
    'not migrated' AS numitems,
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'invalidated') AS dateinvalidated,
    'not migrated' AS invalidatedby,
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'rescored') AS daterescored,
    'not migrated' AS ft_archived,
    'not migrated' AS items_archived,
    e.subject AS subject,
    (SELECT MAX(created_at) FROM exam.exam_event WHERE exam_id = e.id AND status = 'paused') AS datepaused,
    ee.expires_at AS expirefrom,
    'not migrated' AS scoringdate,
    'not migrated' AS scoremark,
    'not migrated' AS scorelatency,
    ee.language_code AS language,
    'not migrated' AS proctorname,
    'not migrated' AS sessid,
    e.id AS _key,
    e.client_name AS clientname,
    ee.deleted_at AS datedeleted,
    'not migrated' AS daterestored,
    'not migrated' AS _version,
    e.assessment_key AS _efk_adminsubject,
    e.environment AS environment,
    'not migrated' AS _datewiped,
    e.segmented AS issegmented,
    e.assessment_algorithm AS algorithm,
    ee.custom_accommodations AS customaccommodations,
    'not migrated' AS numresponses,
    ee.current_segment_position AS insegment,
    NULLIF(ee.waiting_for_segment_approval_position, -1) AS waitingforsegment,
    e.assessment_window_id AS windowid,
    (SELECT MAX(created_at) FROM exam.exam_event WHERE exam_id = e.id AND exam.exam_event.status = 'forceCompleted') AS dateforcecompleted,
    'not migrated' AS dateexpiredreported,
    'not migrated' AS mode,
    'not migrated' AS itemgroupstring,
    'not migrated' AS initialability,
    'not migrated' AS initialabilitydelim,
    'not migrated' AS itemstring,
    'not migrated' AS scorestring,
    'not migrated' AS scoretuples
  FROM
    exam.exam e
    JOIN
    qa_exam_most_recent_event_per_exam last_event
      ON e.id = last_event.exam_id
    JOIN
    exam_event ee
      ON last_event.exam_id = ee.exam_id
         AND last_event.id = ee.id
    JOIN
    exam_status_codes esc
      ON esc.status = ee.status;