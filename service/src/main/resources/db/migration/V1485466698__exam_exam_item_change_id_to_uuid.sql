/***********************************************************************************************************************

File: V1485394791__exam_exam_item_response_add_is_valid_column.sql

Desc: Change the exam_item.id column to a UUID from a BIGINT and add sequence column to exam_item_response

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam_item_response
  DROP FOREIGN KEY fk_exam_item_response_exam_item_id;

ALTER TABLE exam_item_response
  MODIFY exam_item_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;

ALTER TABLE exam_item_response
  ADD sequence INT(11) NOT NULL AFTER response;

ALTER TABLE exam_item
  MODIFY id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;

ALTER TABLE exam_item_response
  ADD CONSTRAINT fk_exam_item_response_exam_item_id
    FOREIGN KEY (exam_item_id)
    REFERENCES exam_item(id);