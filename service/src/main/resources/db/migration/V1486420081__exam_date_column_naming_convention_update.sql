/***********************************************************************************************************************
  File: V1486420081__exam_date_column_naming_convention_update.sql

  Desc: Update all date-related columns to comply to the team's naming convention of using "_at" as the column name
  suffix.

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam
  CHANGE date_joined joined_at DATETIME(3) NULL;

ALTER TABLE exam_event
  CHANGE date_changed changed_at DATETIME(3) NULL,
  CHANGE date_deleted deleted_at DATETIME(3) NULL,
  CHANGE date_completed completed_at DATETIME(3) NULL,
  CHANGE date_scored scored_at DATETIME(3) NULL,
  CHANGE date_started started_at DATETIME(3) NULL,
  CHANGE status_change_date status_changed_at DATETIME(3) NOT NULL;

ALTER TABLE exam_segment_event
  CHANGE date_exited exited_at DATETIME(3) NULL;

ALTER TABLE history
  CHANGE date_changed changed_at DATETIME(3) NULL;


