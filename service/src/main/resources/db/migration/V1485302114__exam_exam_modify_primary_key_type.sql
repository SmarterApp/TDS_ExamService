/***********************************************************************************************************************
  File: V1485302114__exam_exam_modify_primary_key_type.sql

  Desc:
  Changed exam primary key to from bin(16) to char(36)

  Changed tables with foreign key references:
  exam_accommodation
  exam_page

***********************************************************************************************************************/

use exam;

ALTER TABLE exam_accommodation MODIFY exam_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;

ALTER TABLE exam_event drop foreign key exam_event_ibfk_1;
ALTER TABLE exam_page drop foreign key fk_exam_page_examid_exam;
ALTER TABLE exam_segment drop foreign key fk_exam_segment_examid_exam;
ALTER TABLE exam_segment_event drop foreign key fk_exam_segment_event_pk_exam_segment;
ALTER TABLE field_test_item_group drop foreign key fk_field_test_item_group_exam_id_exam;
ALTER TABLE exam_scores drop foreign key fk_exam_scores_examid_exam;

ALTER TABLE exam_event MODIFY exam_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;
ALTER TABLE exam_page MODIFY exam_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;
ALTER TABLE exam_segment MODIFY exam_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;
ALTER TABLE exam_segment_event MODIFY exam_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;
ALTER TABLE field_test_item_group MODIFY exam_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;
ALTER TABLE exam_scores MODIFY exam_id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;

ALTER TABLE exam MODIFY id CHAR(36) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL;

ALTER TABLE exam_event ADD CONSTRAINT exam_event_ibfk_1 FOREIGN KEY (exam_id) REFERENCES exam (id);
ALTER TABLE exam_page ADD CONSTRAINT fk_exam_page_examid_exam FOREIGN KEY (exam_id) REFERENCES exam (id);
ALTER TABLE exam_segment ADD CONSTRAINT fk_exam_segment_examid_exam FOREIGN KEY (exam_id) REFERENCES exam (id);
ALTER TABLE exam_segment_event ADD CONSTRAINT fk_exam_segment_event_pk_exam_segment FOREIGN KEY (exam_id, segment_position) REFERENCES exam_segment (exam_id, segment_position);
ALTER TABLE field_test_item_group ADD CONSTRAINT fk_field_test_item_group_exam_id_exam FOREIGN KEY (exam_id) REFERENCES exam (id);
ALTER TABLE exam_scores ADD CONSTRAINT fk_exam_scores_examid_exam FOREIGN KEY (exam_id) REFERENCES exam (id);
