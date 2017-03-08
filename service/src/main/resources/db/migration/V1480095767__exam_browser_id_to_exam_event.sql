/***********************************************************************************************************************
  File: V1480095767__exam_browser_id_to_exam_event.sql

  Desc: Browser ID is not set across exams so it needs to be in the exam_events table.

***********************************************************************************************************************/

use exam;

ALTER TABLE exam DROP COLUMN browser_id;
ALTER TABLE exam_event ADD COLUMN browser_id VARBINARY(16);
