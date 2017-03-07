/***********************************************************************************************************************
  File: V1488868904__exam_update_status_stages_revert.sql

  Desc: Revert previous changes to mapping tables

***********************************************************************************************************************/

USE exam;

UPDATE exam_status_codes SET stage='inuse' WHERE status in ('started', 'suspended', 'approved', 'pending');