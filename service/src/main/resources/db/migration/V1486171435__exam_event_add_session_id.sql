/***********************************************************************************************************************
  File: V1486171435__exam_event_add_session_id.sql

  Desc: Added session_id to exam_event and removed from exam

***********************************************************************************************************************/

ALTER TABLE exam_event ADD COLUMN session_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;

ALTER TABLE exam DROP COLUMN session_id;