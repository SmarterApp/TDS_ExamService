/***********************************************************************************************************************
  File: V1481304301__exam_exam_accommodations_new_columns.sql

  Desc: Exam Accommodations needs additional columns

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_accommodation ADD COLUMN allow_change bit(1) NOT NULL DEFAULT 0;

ALTER TABLE exam_accommodation ADD COLUMN value VARCHAR(256) NOT NULL;

ALTER TABLE exam_accommodation ADD COLUMN segment_position INT(11) NOT NULL;

ALTER TABLE exam_accommodation_event ADD COLUMN selectable bit(1) NOT NULL DEFAULT 0;

