/***********************************************************************************************************************
  File: V1493865701__exam_update_qa_views.sql

  Desc: Alter view creation code to return more data for validation

***********************************************************************************************************************/

USE exam;
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
    'not migrated' AS satisfiedreason,
    'not migrated' AS offgradeitems
  FROM
    exam.exam_segment s
  JOIN
    exam.qa_exam_most_recent_event_per_exam_segment last_event
    ON s.exam_id = last_event.exam_id
    AND s.segment_position = last_event.segment_position
  JOIN
    exam.exam_segment_event se
    ON last_event.exam_id = se.exam_id
    AND last_event.id = se.id
  ORDER BY s.segment_position;

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
  item.item_type AS format,
  item.is_fieldtest AS isfieldtest,
  item.created_at AS dategenerated,
  'not migrated' AS datesubmitted,
  (SELECT MIN(created_at) FROM exam_item_response WHERE exam_item_id = item.id) AS datefirstresponse,
  response.response AS response,
  response.is_marked_for_review AS mark,
  COALESCE(response.score, -1) AS score,
  'not migrated' AS hostname,
  (SELECT COUNT(id) FROM exam_item_response WHERE exam_item_id = item.id) AS numupdates,
  'not migrated' AS datesystemaltered,
  b'0' AS isinactive,
  'not migrated' AS dateinactivated,
  'not migrated' AS _fk_adminevent,
  page.item_group_key AS groupid,
  response.is_selected AS isselected,
  item.is_required AS isrequired,
  'not migrated' AS responsesequence,
  LENGTH(response.response) AS responselength,
  exam.browser_id AS _fk_browser,
  response.is_valid AS isvalid,
  response.score_latency AS scorelatency,
  CASE page.are_group_items_required
    WHEN 1 THEN -1
    ELSE 0
  END AS groupitemsrequired,
  response.scoring_status AS scorestatus,
  response.scored_at AS scoringdate,
  'not migrated' AS scoremark,
  response.scoring_rationale AS scorerationale,
  'not migrated' AS scoreattempts,
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
  AND segment.segment_key = page.segment_key
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