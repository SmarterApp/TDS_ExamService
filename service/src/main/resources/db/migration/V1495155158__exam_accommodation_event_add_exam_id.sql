/***********************************************************************************************************************
  File: V1495155158__exam_accommodation_event_add_exam_id.sql

  Desc: Add exam_id column to the exam_accommodation_event table

***********************************************************************************************************************/

USE exam;

-- Add missing foreign key on exam_accommodation
ALTER TABLE exam_accommodation
  ADD CONSTRAINT fk_exam_accommodation_exam_id
  FOREIGN KEY (exam_id) REFERENCES exam(id);

ALTER TABLE exam_accommodation_event
  ADD exam_id CHAR(36) NOT NULL AFTER exam_accommodation_id;

-- Update the column w/data
start transaction;
update exam_accommodation_event
  join exam_accommodation
    on exam_accommodation.id = exam_accommodation_event.exam_accommodation_id
set exam_accommodation_event.exam_id = exam_accommodation.exam_id;
commit;

ALTER TABLE exam_accommodation_event
  ADD CONSTRAINT fk_exam_accommodation_event_exam_id
  FOREIGN KEY (exam_id) REFERENCES exam(id);
