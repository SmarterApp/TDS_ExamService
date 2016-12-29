package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public void insert(final List<ExamSegment> segments) {
        final List<SqlParameterSource> parameterSources = segments.stream()
            .map(segment -> new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(segment.getExamId()))
                .addValue("segmentKey", segment.getSegmentKey())
                .addValue("segmentId", segment.getSegmentId())
                .addValue("segmentPosition", segment.getSegmentPosition())
                .addValue("formKey", segment.getFormKey())
                .addValue("formId", segment.getFormId())
                .addValue("algorithm", segment.getAlgorithm().getType())
                .addValue("examItemCount", segment.getExamItemCount())
                .addValue("fieldTestItemCount", segment.getFieldTestItemCount())
                .addValue("formCohort", segment.getFormCohort())
                .addValue("poolCount", segment.getPoolCount())
                .addValue("isSatisfied", segment.isSatisfied())
                .addValue("isPermeable", segment.isPermeable())
                .addValue("restorePermeableOn", segment.getRestorePermeableCondition())
                .addValue("dateExited", ResultSetMapperUtility.mapInstantToTimestamp(segment.getDateExited()))
                .addValue("itemPool", String.join(",", segment.getItemPool())))
            .collect(Collectors.toList());

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
                "   form_cohort, \n" +
                "   pool_count \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :examId, \n" +
                    "   :segmentKey, \n" +
                    "   :segmentId, \n" +
                "   :segmentPosition, \n" +
                "   :formKey, \n" +
                "   :formId, \n" +
                "   :algorithm, \n" +
                "   :examItemCount, \n" +
                "   :fieldTestItemCount, \n" +
                "   :formCohort, \n" +
                "   :poolCount \n" +
                ")";

        jdbcTemplate.batchUpdate(segmentQuery, parameterSources.toArray(new SqlParameterSource[parameterSources.size()]));
        update(segments);
    }

    @Override
    public void update(final ExamSegment segment) {
        update(Arrays.asList(segment));
    }

    @Override
    public void update(final List<ExamSegment> segments) {
        final List<SqlParameterSource> parameterSources = new ArrayList<>();
        segments.forEach(segment -> {
            SqlParameterSource parameters = new MapSqlParameterSource(
                "examId", UuidAdapter.getBytesFromUUID(segment.getExamId()))
                .addValue("segmentPosition", segment.getSegmentPosition())
                .addValue("isSatisfied", segment.isSatisfied())
                .addValue("isPermeable", segment.isPermeable())
                .addValue("restorePermeableCondition", segment.getRestorePermeableCondition())
                .addValue("dateExited", ResultSetMapperUtility.mapInstantToTimestamp(segment.getDateExited()))
                .addValue("itemPool", String.join(",", segment.getItemPool()));
            parameterSources.add(parameters);
        });

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

        jdbcTemplate.batchUpdate(segmentEventQuery, parameterSources.toArray(new SqlParameterSource[parameterSources.size()]));
    }
}
