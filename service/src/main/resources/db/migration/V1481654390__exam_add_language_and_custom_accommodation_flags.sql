/***********************************************************************************************************************
  File: V1481654390__exam_add_language_and_custom_accommodation_flags.sql

  Desc: Add language and custom accommodations flag

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_event ADD COLUMN custom_accommodations bit(1) not null default 0;

ALTER TABLE exam_event ADD COLUMN language varchar(25);