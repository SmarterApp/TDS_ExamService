/***********************************************************************************************************************
  File: V1485222723__exam_UUID_primary_key_type.sql

  Desc:
  Changed primary key to char(36) for UUID values:
  exam_accommodation
  exam_page

  Changed tables with foreign key references:
  exam_accommodation_event
  exam_item
  exam_page_event

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_accommodation_event drop foreign key exam_accommodation_event_ibfk_1;

ALTER TABLE exam_accommodation MODIFY id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;
ALTER TABLE exam_accommodation_event MODIFY exam_accommodation_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;

ALTER TABLE exam_accommodation_event ADD CONSTRAINT exam_accommodation_event_ibfk_1 FOREIGN KEY (exam_accommodation_id) REFERENCES exam_accommodation(id);

ALTER TABLE exam_item drop foreign key fk_exam_item_page_id_exam_page_id;
ALTER TABLE exam_page_event drop foreign key fk_exam_page_id;

ALTER TABLE exam_page MODIFY id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;
ALTER TABLE exam_item MODIFY exam_page_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;
ALTER TABLE exam_page_event MODIFY exam_page_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;

ALTER TABLE exam_item ADD CONSTRAINT fk_exam_item_page_id_exam_page_id FOREIGN KEY (exam_page_id) REFERENCES exam_page(id);
ALTER TABLE exam_page_event ADD CONSTRAINT fk_exam_page_id FOREIGN KEY (exam_page_id) REFERENCES exam_page(id);