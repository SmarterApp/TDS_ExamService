/***********************************************************************************************************************
  File: V1482103978__exam_accommodation_event_add_multiple_types.sql

  Desc: add a multiple types flag

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_accommodation_event ADD COLUMN multiple_tool_types bit(1) not null default 0;