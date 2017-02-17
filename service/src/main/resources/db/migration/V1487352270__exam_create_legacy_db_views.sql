/***********************************************************************************************************************
  File: V1487352270__exam_create_legacy_db_views.sql

  Desc: Create views in the exam database that format the exam database's tables to look like their equivalent in the
  session database.  In cases where the legacy column has not been migrated from the legacy session database to the new
  exam database, a value of 'not migrated' will be returned by the view.

***********************************************************************************************************************/
USE exam;

-- ----------------------------------------------------------------------------
-- A view for finding the latest exam_event record for an exam
--
-- NOTE:  This view is required because MySQL 5.6 does not support using
-- subqueries in the FROM clause of a view:  "Before MySQL 5.7.7, the SELECT
-- statement cannot contain a subquery in the FROM clause." (from here:
-- https://dev.mysql.com/doc/refman/5.7/en/create-view.html)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_exam_most_recent_event_per_exam AS
  SELECT
    exam_id,
    MAX(id) AS id
  FROM
    exam_event
  GROUP BY
    exam_id;

-- ----------------------------------------------------------------------------
-- A view for finding the latest exam_accommodation_event record for an
-- exam_accommodation
--
-- NOTE:  This view is required because MySQL 5.6 does not support using
-- subqueries in the FROM clause of a view:  "Before MySQL 5.7.7, the SELECT
-- statement cannot contain a subquery in the FROM clause." (from here:
-- https://dev.mysql.com/doc/refman/5.7/en/create-view.html)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_exam_most_recent_event_per_exam_accommodation AS
	SELECT
    exam_accommodation_id,
    MAX(id) AS id
  FROM
    exam_accommodation_event
  GROUP BY
    exam_accommodation_id;

-- ----------------------------------------------------------------------------
-- A view for finding the latest exam_page_event record for an exam_page
--
-- NOTE:  This view is required because MySQL 5.6 does not support using
-- subqueries in the FROM clause of a view:  "Before MySQL 5.7.7, the SELECT
-- statement cannot contain a subquery in the FROM clause." (from here:
-- https://dev.mysql.com/doc/refman/5.7/en/create-view.html)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_exam_most_recent_event_per_exam_page AS
  SELECT
    exam_page_id,
    MAX(id) AS id
  FROM
    exam_page_event
  GROUP BY
    exam_page_id;

-- ----------------------------------------------------------------------------
-- A view for finding the latest exam_item_response record for an exam_item
--
-- NOTE:  This view is required because MySQL 5.6 does not support using
-- subqueries in the FROM clause of a view:  "Before MySQL 5.7.7, the SELECT
-- statement cannot contain a subquery in the FROM clause." (from here:
-- https://dev.mysql.com/doc/refman/5.7/en/create-view.html)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_exam_most_recent_response_per_exam_item AS
	SELECT
	  exam_item_id,
    MAX(id) AS id
  FROM
    exam_item_response
  GROUP BY
    exam_item_id;

CREATE OR REPLACE VIEW qa_exam_most_recent_event_per_exam_segment AS
	SELECT
	  exam_id,
	  segment_position,
    MAX(id) AS id
  FROM
    exam_segment_event
  GROUP BY
    exam_id,
	  segment_position;

-- ----------------------------------------------------------------------------
-- Map exam to session.testopportunity
-- ----------------------------------------------------------------------------
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
    (SELECT status FROM exam_event WHERE id < last_event.id ORDER BY id DESC LIMIT 1) AS prevstatus,
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
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'paused') AS datepaused,
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
    ee.waiting_for_segment_approval AS waitingforsegment,
    e.assessment_window_id AS windowid,
    (SELECT MAX(created_at) FROM exam_event WHERE exam_id = e.id AND status = 'forceCompleted') AS dateforcecompleted,
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
    qa_exam_most_recent_event_per_exam AS last_event
    ON e.id = last_event.exam_id
  JOIN
    exam.exam_event ee
    ON last_event.exam_id = ee.exam_id
    AND last_event.id = ee.id
  JOIN
    exam.exam_status_codes esc
    ON esc.status = ee.status;

-- ----------------------------------------------------------------------------
-- Map exam_accommodation to session.testeeaccommodations
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_session_testeeaccommodations AS
	SELECT
		ea.`type` AS acctype,
		ea.value AS accvalue,
		ea.code AS acccode,
		ea.created_at AS _date,
		ea.allow_change AS allowchange,
		'not migrated' AS testeecontrol,
		ea.exam_id AS _fk_testopportunity,
		CASE
			WHEN eae.deleted_at IS NULL THEN 1
			ELSE 0
		END AS isapproved,
		eae.selectable AS isselectable,
		ea.segment_position AS segment,
		'not migrated' AS valuecount,
		'not migrated' AS recordusage
  FROM
    exam_accommodation AS ea
  JOIN
		qa_exam_most_recent_event_per_exam_accommodation AS last_event
	  ON ea.id = last_event.exam_accommodation_id
  JOIN
		exam_accommodation_event eae
		ON last_event.id = eae.id;

-- ----------------------------------------------------------------------------
-- Map exam_item and exam_item_response to session.testeeresponse
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_session_testeeresponse AS
	SELECT
		page.exam_id AS _fk_testopportunity,
		item.assessment_item_key AS _efk_itsitem,
		item.assessment_item_bank_key AS _efk_itsbank,
		exam.session_id AS _fk_session,
		exam.attempts AS opportunityrestart,
		page.page_position AS page,
		item.position AS position,
		'not migrated' AS answer,
		'not migrated' AS scorepoint,
		'not migrated' AS format,
		'not migrated' AS isfieldtest,
		'not migrated' AS dategenerated,
		'not migrated' AS datesubmitted,
		'not migrated' AS datefirstresponse,
		response.response AS response,
		'not migrated' AS mark,
		response.score AS score,
		'not migrated' AS hostname,
		'not migrated' AS numupdates,
		'not migrated' AS datesystemaltered,
		'not migrated' AS isinactive,
		'not migrated' AS dateinactivated,
		'not migrated' AS _fk_adminevent,
		page.item_group_key AS groupid,
		response.is_selected AS isselected,
		item.is_required AS isrequired,
		'not migrated' AS responsesequence,
		LENGTH(response.response) AS responselength,
		exam.browser_id AS _fk_browser,
		response.is_valid AS isvalid,
		'not migrated' AS scorelatency,
		page.are_group_items_required AS groupitemsrequired,
		response.scoring_status AS scorestatus,
		response.scored_at AS scoringdate,
		'not migrated' AS scoremark,
		response.scoring_rationale AS scorerationale,
		'not migrated' AS scoreattemts,
		item.item_key AS _efk_itemkey,
		'not migrated' AS _fk_responsesession,
		segment.segment_position AS segment,
		'not migrated' AS contentlevel,
		segment.segment_id AS segmentid,
		'not migrated' AS groupb,
		'not migrated' AS itemb,
		'not migrated' AS datelastvisited,
		'not migrated' AS visitcount,
		'not migrated' AS geosyncid,
		'not migrated' AS satellite,
		response.scoring_dimensions AS scoredimensions,
		'not migrated' AS responsedurationinsecs
	FROM
	   exam_page AS page
	JOIN
		qa_exam_most_recent_event_per_exam_page AS last_page_event
	   ON page.id = last_page_event.exam_page_id
	JOIN
	   exam_segment AS segment
	   ON segment.exam_id = page.exam_id
	   AND segment.segment_key = page.exam_segment_key
	JOIN
		qa_exam_most_recent_event_per_exam AS last_exam_event
		ON last_exam_event.exam_id = page.exam_id
	JOIN exam.exam_event exam
		ON last_exam_event.exam_id = exam.exam_id
		AND last_exam_event.id = exam.id
	JOIN
	   exam_item AS item
	   ON page.id = item.exam_page_id
	LEFT JOIN
	   qa_exam_most_recent_response_per_exam_item AS most_recent_response
	   ON item.id = most_recent_response.exam_item_id
	LEFT JOIN
	   exam_item_response response
	   ON most_recent_response.id = response.id
	ORDER BY
	   item.position;
-- ----------------------------------------------------------------------------
-- Map examinee_segment to session.testopportunitysegment
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_session_testopportunitysegment AS
	SELECT
	  s.exam_id AS _fk_testopportunity,
	  s.segment_key AS segment,
	  s.segment_position AS segmentposition,
	  s.form_key AS formkey,
	  s.form_id AS formid,
	  s.algorithm AS algorithm,
	  s.exam_item_count AS opitemcount,
	  s.field_test_item_count AS ftitemcount,
	  s.field_test_items AS ftitems,
	  se.permeable AS ispermeable,
	  se.restore_permeable_condition AS restorepermon,
	  s.segment_id AS segmentid,
		'not migrated' AS entryapproved,
		'not migrated' AS exitapproved,
	  s.form_cohort AS formcohort,
	  se.satisfied AS issatisfied,
		'not migrated' AS initialability,
		'not migrated' AS currentability,
		s.created_at AS _date,
	  se.exited_at AS dateexited,
	  se.item_pool AS itempool,
	  s.pool_count AS poolcaount,
		'not migrated' AS offgradeitems
	FROM
	  exam_segment AS s
	JOIN
	  qa_exam_most_recent_event_per_exam_segment AS last_event
		ON s.exam_id = last_event.exam_id
		AND s.segment_position = last_event.segment_position
	JOIN
	  exam_segment_event se
		ON last_event.exam_id = se.exam_id
		AND last_event.id = se.id
	ORDER BY
	  s.segment_position;

-- ----------------------------------------------------------------------------
-- Map examinee_attribute to session.testeeattribute
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_session_testeeattribute AS
	SELECT
		exam_id AS _fk_testopportunity,
		attribute_name AS tds_id,
		attribute_value AS attributevalue,
		context AS context,
		created_at AS _date
	FROM
		examinee_attribute;

-- ----------------------------------------------------------------------------
-- Map examinee_relationship to session.testeerelationship
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW qa_session_testeerelationship AS
	SELECT
		exam_id	 AS _fk_testopportunity,
		attribute_name AS tds_id,
		'not migrated' AS entitykey,
		context AS context,
		created_at AS _date,
		attribute_value AS attributevalue,
		attribute_relationship AS relationship
	FROM
		examinee_relationship;