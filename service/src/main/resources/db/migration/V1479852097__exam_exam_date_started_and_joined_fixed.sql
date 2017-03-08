/***********************************************************************************************************************
  File: V1479852097__exam_exam_date_started_and_joined_fixed.sql

  Desc: date_started was added to exam when it should be in exam event.  date_joined should be in exam instead of exam
  event since it doesn't change.  We can simply rename the two columns at this point in time.

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam CHANGE COLUMN date_started date_joined datetime(3);

ALTER TABLE exam_event CHANGE COLUMN date_joined date_started datetime(3);