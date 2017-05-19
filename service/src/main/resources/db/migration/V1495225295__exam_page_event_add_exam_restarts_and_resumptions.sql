/***********************************************************************************************************************
  File: V1495225295__exam_page_event_add_exam_restarts_and_resumptions.sql

  Desc: Schema modification to add the exam_restarts_and_resumptions column to exam_page_event

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_page_event ADD COLUMN exam_restarts_and_resumptions INT(11) NOT NULL DEFAULT 0;