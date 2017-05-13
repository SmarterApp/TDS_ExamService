package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.stream.Stream;

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.repositories.ExamItemCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@Repository
public class ExamItemCommandRepositoryImpl implements ExamItemCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamItemCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final ExamItem... examItems) {
        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        final SqlParameterSource[] batchParameters = Stream.of(examItems)
            .map(examItem -> new MapSqlParameterSource("id", examItem.getId().toString())
                .addValue("itemKey", examItem.getItemKey())
                .addValue("assessmentItemBankKey", examItem.getAssessmentItemBankKey())
                .addValue("assessmentItemKey", examItem.getAssessmentItemKey())
                .addValue("itemType", examItem.getItemType())
                .addValue("examPageId", examItem.getExamPageId().toString())
                .addValue("position", examItem.getPosition())
                .addValue("isFieldTest", examItem.isFieldTest())
                .addValue("isRequired", examItem.isRequired())
                .addValue("itemFilePath", examItem.getItemFilePath())
                .addValue("stimulusFilePath", examItem.getStimulusFilePath().orNull())
                .addValue("groupId", examItem.getGroupId())
                .addValue("createdAt", createdAt))
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO exam_item ( \n" +
                "   id, \n" +
                "   item_key, \n" +
                "   assessment_item_bank_key, \n" +
                "   assessment_item_key, \n" +
                "   item_type, \n" +
                "   exam_page_id, \n" +
                "   position, \n" +
                "   is_fieldtest, \n" +
                "   is_required, \n" +
                "   item_file_path, \n" +
                "   stimulus_file_path, \n" +
                "   group_id, \n" +
                "   created_at) \n" +
                "VALUES( " +
                "   :id, \n" +
                "   :itemKey, \n" +
                "   :assessmentItemBankKey, \n" +
                "   :assessmentItemKey, \n" +
                "   :itemType, \n" +
                "   :examPageId, \n" +
                "   :position, \n" +
                "   :isFieldTest, \n" +
                "   :isRequired, \n" +
                "   :itemFilePath, \n" +
                "   :stimulusFilePath, \n" +
                "   :groupId, \n" +
                "   :createdAt)";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }

    @Override
    public void insertResponses(final ExamItemResponse... responses) {
        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        final SqlParameterSource[] batchParameters = Stream.of(responses)
            .map(response -> {
                MapSqlParameterSource sqlParameterSource =
                    new MapSqlParameterSource("examItemId", response.getExamItemId().toString())
                        .addValue("response", response.getResponse())
                        .addValue("sequence", response.getSequence())
                        .addValue("isValid", response.isValid())
                        .addValue("isSelected", response.isSelected())
                        .addValue("createdAt", createdAt)
                        .addValue("isMarkedForReview", response.isMarkedForReview());

                // It's possible that a response has not yet been scored
                if (response.getScore().isPresent()) {
                    ExamItemResponseScore score = response.getScore().get();
                    sqlParameterSource.addValue("score", score.getScore())
                        .addValue("scoringStatus", score.getScoringStatus().toString())
                        .addValue("scoringRationale", score.getScoringRationale())
                        .addValue("scoringDimensions", score.getScoringDimensions())
                        .addValue("scoredAt", mapJodaInstantToTimestamp(score.getScoredAt()))
                        .addValue("scoreSentAt", mapJodaInstantToTimestamp(score.getScoreSentAt()))
                        .addValue("scoreMark", score.getScoreMark() != null ? score.getScoreMark().toString() : null)
                        .addValue("scoreLatency", score.getScoreLatency());
                } else {
                    //This is necessary because we always include the values in the query
                    sqlParameterSource.addValue("score", null)
                        .addValue("scoringStatus", null)
                        .addValue("scoringRationale", null)
                        .addValue("scoringDimensions", null)
                        .addValue("scoredAt", null)
                        .addValue("scoreSentAt", null)
                        .addValue("scoreMark", null)
                        .addValue("scoreLatency", 0);
                }

                return sqlParameterSource;
            })
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO exam_item_response ( \n" +
                "   exam_item_id, \n" +
                "   response, \n" +
                "   sequence, \n" +
                "   is_valid, \n" +
                "   is_selected, \n" +
                "   is_marked_for_review, \n" +
                "   score, \n" +
                "   scoring_status, \n" +
                "   scoring_rationale, \n" +
                "   scoring_dimensions, \n" +
                "   created_at, \n" +
                "   score_sent_at, \n" +
                "   score_latency, \n" +
                "   score_mark, \n" +
                "   scored_at) \n" +
                "VALUES ( \n" +
                "   :examItemId, \n" +
                "   :response, \n" +
                "   :sequence, \n" +
                "   :isValid, \n" +
                "   :isSelected, \n" +
                "   :isMarkedForReview, \n" +
                "   :score, \n" +
                "   :scoringStatus, \n" +
                "   :scoringRationale, \n" +
                "   :scoringDimensions, \n" +
                "   :createdAt, \n" +
                "   :scoreSentAt, \n" +
                "   :scoreLatency, \n" +
                "   :scoreMark, \n" +
                "   :scoredAt)";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }
}
