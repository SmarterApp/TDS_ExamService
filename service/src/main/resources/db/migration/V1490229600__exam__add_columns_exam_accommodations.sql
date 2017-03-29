/***********************************************************************************************************************
  File: V1490229600__exam__add_columns_exam_accommodations.sql

  Desc: Adds additional columns to the exam_accommodation table

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_accommodation
  ADD COLUMN visible BIT(1) NOT NULL DEFAULT 1;

ALTER TABLE exam_accommodation
  ADD COLUMN student_controlled BIT(1) NOT NULL DEFAULT 1;

ALTER TABLE exam_accommodation
  ADD COLUMN disabled_on_guest_session BIT(1) NOT NULL DEFAULT 0;

ALTER TABLE exam_accommodation
  ADD COLUMN default_accommodation BIT(1) NOT NULL;

ALTER TABLE exam_accommodation
  ADD COLUMN allow_combine BIT(1) NOT NULL;

ALTER TABLE exam_accommodation
  ADD COLUMN depends_on VARCHAR(50) DEFAULT NULL;

ALTER TABLE exam_accommodation
  ADD COLUMN sort_order INT(11) NOT NULL;