/***********************************************************************************************************************
  File: V1490384492__exam_accommodations_add_functional_column.sql

  Desc: Adds the `functional` column to exam_accommodations

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_accommodation
  ADD COLUMN functional BIT(1) NOT NULL DEFAULT 1;
