/***********************************************************************************************************************
  File: V1489427303__exam_update_print_request_status_columns.sql

  Desc: Update to remove approved_at and denied_at columns and adding "status" column

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_print_request_event
	CHANGE `approved_at` `created_at` DATETIME;

ALTER TABLE exam_print_request_event
	DROP COLUMN `denied_at`;

ALTER TABLE exam_print_request_event
	ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED';