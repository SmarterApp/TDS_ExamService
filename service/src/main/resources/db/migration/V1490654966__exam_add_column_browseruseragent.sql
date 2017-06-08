/***********************************************************************************************************************
  File: V1490654966__exam_add_column_browseruseragent.sql

  Desc: Adds the `browser_user_agent` column to exam_event

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_event
  ADD COLUMN browser_user_agent VARCHAR(250);