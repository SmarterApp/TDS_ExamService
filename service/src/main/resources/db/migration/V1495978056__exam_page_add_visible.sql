/***********************************************************************************************************************
  File: V1495978056__exam_page_add_visible.sql

  Desc: Add a visible flag on exam_page_event so pages can be marked not visible

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_page_event ADD COLUMN visible BIT(1) NOT NULL DEFAULT 1;