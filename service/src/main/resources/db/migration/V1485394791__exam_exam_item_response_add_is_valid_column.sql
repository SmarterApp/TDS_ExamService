/***********************************************************************************************************************

File: V1485394791__exam_exam_item_response_add_is_valid_column.sql

Desc: Adds columns to the exam_item_response table for supporting the get page content feature; specifically additional
fields that are required to support mapping exam items to legacy OpportunityItems

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_item_response
  ADD is_valid BIT NOT NULL DEFAULT b'0' AFTER response;