/***********************************************************************************************************************
  File: V1487121221__exam_examinee_attribute_null_value.sql

  Desc: examinee attribute value should be nullable as it is in legacy

***********************************************************************************************************************/
USE exam;

ALTER TABLE examinee_attribute MODIFY COLUMN attribute_value VARCHAR(400);