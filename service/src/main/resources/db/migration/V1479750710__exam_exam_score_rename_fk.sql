/***********************************************************************************************************************
  File: V1479750710_exam_exam_score_rename_fk.sql

  Desc: We tweaked our team naming standards for foreign keys and their constraints.  This will update the exam_scores
  table to follow the new convention.

***********************************************************************************************************************/

USE exam;

ALTER TABLE exam_scores CHANGE COLUMN fk_scores_examid_exam exam_id VARBINARY(16);
ALTER TABLE exam_scores ADD CONSTRAINT fk_exam_scores_examid_exam FOREIGN KEY (exam_id) REFERENCES exam(id);