/***********************************************************************************************************************
  File: V1490816547__exam_response_add_missing_columns.sql

  Desc: Adds missing columns

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_item_response ADD COLUMN score_sent_at datetime(3) DEFAULT NULL;
ALTER TABLE exam_item_response ADD COLUMN score_mark char(36) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE exam_item_response ADD COLUMN score_latency bigint(20) NOT NULL DEFAULT '0';