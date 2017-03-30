/***********************************************************************************************************************
  File: V1490894289__exam_update_legacy_view_testeeaccommodations.sql

  Desc: Update to use denied_at column for "isapproved" and addition of isdeleted flag.

***********************************************************************************************************************/
USE exam;

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
			WHEN  eae.denied_at IS NULL THEN 1
			ELSE 0
		END AS isapproved,
		eae.selectable AS isselectable,
		ea.segment_position AS segment,
		'not migrated' AS valuecount,
		'not migrated' AS recordusage,
		CASE
			WHEN  eae.deleted_at IS NOT NULL THEN 1
			ELSE 0
		END AS isdeleted
  FROM
    exam_accommodation AS ea
  JOIN
		qa_exam_most_recent_event_per_exam_accommodation AS last_event
	  ON ea.id = last_event.exam_accommodation_id
  JOIN
		exam_accommodation_event eae
		ON last_event.id = eae.id;