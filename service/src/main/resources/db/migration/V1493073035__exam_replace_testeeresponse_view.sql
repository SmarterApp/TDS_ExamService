/***********************************************************************************************************************
  File: V1493073035__exam_replace_testeeresponse_view.sql

  Desc: Alter view creation code after table changed

***********************************************************************************************************************/


USE exam;
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