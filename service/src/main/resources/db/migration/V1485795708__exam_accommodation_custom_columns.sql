/***********************************************************************************************************************
  File: V1485795708__exam_accommodation_custom_columns.sql

  Desc: Added an is default and a custom accommodations

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_accommodation_event
  ADD COLUMN custom BIT(1) NOT NULL DEFAULT 0;