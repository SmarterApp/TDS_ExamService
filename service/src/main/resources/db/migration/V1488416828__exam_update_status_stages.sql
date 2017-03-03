/***********************************************************************************************************************
  File: V1488416828__exam_update_status_stages.sql

  Desc: Fixes some mappings from status to stage

***********************************************************************************************************************/

USE exam;

UPDATE exam_status_codes SET stage='inprogress' WHERE status in ('started', 'suspended');
UPDATE exam_status_codes SET stage='new' WHERE status in ('approved', 'pending');