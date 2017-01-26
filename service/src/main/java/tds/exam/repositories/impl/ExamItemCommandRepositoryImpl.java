package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.stream.Stream;

import tds.exam.ExamItem;
import tds.exam.repositories.ExamItemCommandRepository;

public class ExamItemCommandRepositoryImpl implements ExamItemCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamItemCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(ExamItem... examItems) {
        final SqlParameterSource[] batchParameters = Stream.of(examItems)
            .map(examItem -> new MapSqlParameterSource("itemKey", examItem.getItemKey())
                .addValue("examPageId", examItem.getExamPageId())
                .addValue("position", examItem.getPosition())
                .addValue("assessmentItemBankKey", examItem.getAssessmentItemBankKey())
                .addValue("assessmentItemKey", examItem.getAssessmentItemKey())
                .addValue("itemType", examItem.getItemType())
                .addValue("isRequired", examItem.isRequired())
                .addValue("isSelected", examItem.isSelected())
                .addValue("isMarkedForReview", examItem.isMarkedForReview())
                .addValue("isFieldTest", examItem.isFieldTest())
                .addValue("itemFilePath", examItem.getItemFilePath())
                .addValue("stimulusFilePath", examItem.getStimulusFilePath().orNull()))
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO exam_item ( \n" +
                "   item_key, \n" +
                "   assessment_item_bank_key, \n" +
                "   assessment_item_key, \n" +
                "   item_type, \n" +
                "   exam_page_id, \n" +
                "   position, \n" +
                "   is_fieldtest, \n" +
                "   is_required, \n" +
                "   is_selected, \n" +
                "   is_marked_for_review, \n" +
                "   item_file_path, \n" +
                "   stimulus_file_path) \n" +
                "VALUES( \n" +
                "   :itemKey, \n" +
                "   :assessmentItemBankKey, \n" +
                "   :assessmentItemKey, \n" +
                "   :itemType, \n" +
                "   :examPageId, \n" +
                "   :position, \n" +
                "   :isFieldTest, \n" +
                "   :isRequired, \n" +
                "   :isSelected, \n" +
                "   :isMarkedForReview, \n" +
                "   :itemFilePath, \n" +
                "   :stimulusFilePath)";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }
}
