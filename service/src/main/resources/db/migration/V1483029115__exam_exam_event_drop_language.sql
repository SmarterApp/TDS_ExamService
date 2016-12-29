/***********************************************************************************************************************

File: V1483029115__exam_exam_event_drop_language.sql

Desc: Drops the language column since we are using language code instead

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_event
  DROP COLUMN language;