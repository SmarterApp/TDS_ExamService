package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.models.ExamSegment;
import tds.exam.repositories.ExamSegmentCommandRepository;

/**
 * Repository responsible for writing to the exam_segment and exam_segment_event table.
 */
@Repository
public class ExamSegmentCommandRepositoryImpl implements ExamSegmentCommandRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamSegmentCommandRepositoryImpl(final @Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate commandJdbcTemplate) {
        this.jdbcTemplate = commandJdbcTemplate;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void insert(final ExamSegment segment) {
        final SqlParameterSource parameters =
                new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(segment.getExamId()))
                        .addValue("assessmentSegmentKey", segment.getAssessmentSegmentKey())
                        .addValue("assessmentSegmentId", segment.getAssessmentSegmentId())
                        .addValue("segmentPosition", segment.getSegmentPosition())
                        .addValue("formKey", segment.getFormKey())
                        .addValue("formId", segment.getFormId())
                        .addValue("algorithm", segment.getAlgorithm())
                        .addValue("examItemCount", segment.getExamItemCount())
                        .addValue("fieldTestItemCount", segment.getFieldTestItemCount())
                        .addValue("fieldTestItems", String.join(",", segment.getFieldTestItems()))
                        .addValue("formCohort", segment.getFormCohort())
                        .addValue("poolCount", segment.getPoolCount())
                        .addValue("isSatisfied", segment.isSatisfied())
                        .addValue("isPermeable", segment.isPermeable())
                        .addValue("restorePermeableOn", segment.getRestorePermeableCondition())
                        .addValue("dateExited", ResultSetMapperUtility.mapInstantToTimestamp(segment.getDateExited()))
                        .addValue("itemPool", String.join(",", segment.getItemPool()));

        final String segmentQuery =
                "INSERT INTO exam_segment (\n" +
                "   exam_id, \n" +
                "   segment_key, \n" +
                "   segment_id, \n" +
                "   segment_position, \n" +
                "   form_key, \n" +
                "   form_id, \n" +
                "   algorithm, \n" +
                "   exam_item_count, \n" +
                "   field_test_item_count, \n" +
                "   field_test_items, \n" +
                "   form_cohort, \n" +
                "   pool_count \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :examId, \n" +
                "   :assessmentSegmentKey, \n" +
                "   :assessmentSegmentId, \n" +
                "   :segmentPosition, \n" +
                "   :formKey, \n" +
                "   :formId, \n" +
                "   :algorithm, \n" +
                "   :examItemCount, \n" +
                "   :fieldTestItemCount, \n" +
                "   :fieldTestItems, \n" +
                "   :formCohort, \n" +
                "   :poolCount \n" +
                ")";

        jdbcTemplate.update(segmentQuery, parameters);
        update(segment);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void update(final ExamSegment segment) {
        final SqlParameterSource parameters =
                new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(segment.getExamId()))
                        .addValue("segmentPosition", segment.getSegmentPosition())
                        .addValue("isSatisfied", segment.isSatisfied())
                        .addValue("isPermeable", segment.isPermeable())
                        .addValue("restorePermeableCondition", segment.getRestorePermeableCondition())
                        .addValue("dateExited", ResultSetMapperUtility.mapInstantToTimestamp(segment.getDateExited()))
                        .addValue("itemPool", String.join(",", segment.getItemPool()));

        final String segmentEventQuery =
                "INSERT INTO exam_segment_event (\n" +
                "   exam_id, \n" +
                "   segment_position, \n" +
                "   satisfied, \n" +
                "   permeable, \n" +
                "   restore_permeable_condition, \n" +
                "   date_exited, \n" +
                "   item_pool \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :examId, \n" +
                "   :segmentPosition, \n" +
                "   :isSatisfied, \n" +
                "   :isPermeable, \n" +
                "   :restorePermeableCondition, \n" +
                "   :dateExited, \n" +
                "   :itemPool \n" +
                ")";

        jdbcTemplate.update(segmentEventQuery, parameters);
    }
}
