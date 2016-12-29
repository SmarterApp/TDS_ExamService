package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.assessment.Algorithm;
import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.models.ExamSegment;
import tds.exam.repositories.ExamSegmentQueryRepository;

/**
 * Repository implementation for reading from the {@link ExamSegment} related tables.
 */
@Repository
public class ExamSegmentQueryRepositoryImpl implements ExamSegmentQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamSegmentQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<ExamSegment> findByExamId(UUID examId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("examId", UuidAdapter.getBytesFromUUID(examId));

        final String SQL =
                "SELECT \n" +
                "   s.exam_id, \n" +
                "   s.segment_key, \n" +
                "   s.segment_id, \n" +
                "   s.segment_position, \n" +
                "   s.form_key, \n" +
                "   s.form_id, \n" +
                "   s.algorithm, \n" +
                "   s.exam_item_count, \n" +
                "   s.field_test_item_count, \n" +
                "   s.field_test_items, \n" +
                "   se.permeable, \n" +
                "   se.restore_permeable_condition, \n" +
                "   s.form_cohort, \n" +
                "   se.satisfied, \n" +
                "   se.date_exited, \n" +
                "   se.item_pool, \n" +
                "   s.pool_count, \n" +
                "   s.created_at \n" +
                "FROM \n" +
                "   exam_segment s \n" +
                "INNER JOIN ( \n" +
                "   SELECT \n" +
                "       exam_id, \n" +
                "       segment_position, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam_segment_event \n" +
                "   WHERE exam_id = :examId \n" +
                "   GROUP BY \n" +
                "       exam_id, segment_position \n" +
                ") last_event \n" +
                "ON s.exam_id = last_event.exam_id AND \n" +
                "   s.segment_position = last_event.segment_position \n" +
                "INNER JOIN \n" +
                "   exam_segment_event se \n" +
                "ON \n" +
                "   last_event.exam_id = se.exam_id AND \n" +
                "   last_event.id = se.id \n" +
                "ORDER BY \n" +
                "   segment_position \n";

        return jdbcTemplate.query(SQL, parameters, new ExamSegmentRowMapper());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Optional<ExamSegment> findByExamIdAndSegmentPosition(UUID examId, int segmentPosition) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("examId", UuidAdapter.getBytesFromUUID(examId));
        parameters.put("segmentPosition", segmentPosition);

        final String SQL =
                "SELECT \n" +
                "   s.exam_id as exam_id, \n" +
                "   s.segment_key, \n" +
                "   s.segment_id, \n" +
                "   s.segment_position, \n" +
                "   s.form_key, \n" +
                "   s.form_id, \n" +
                "   s.algorithm, \n" +
                "   s.exam_item_count, \n" +
                "   s.field_test_item_count, \n" +
                "   se.permeable, \n" +
                "   se.restore_permeable_condition, \n" +
                "   s.form_cohort, \n" +
                "   se.satisfied, \n" +
                "   se.date_exited, \n" +
                "   se.item_pool, \n" +
                "   s.pool_count, \n" +
                "   s.created_at \n" +
                "FROM \n" +
                "   exam_segment s \n" +
                "JOIN exam_segment_event se \n" +
                "   ON s.exam_id = se.exam_id AND \n" +
                "       s.segment_position = se.segment_position \n" +
                "WHERE \n" +
                "   s.exam_id = :examId AND \n " +
                "   s.segment_position = :segmentPosition \n" +
                "ORDER BY \n" +
                "   se.id \n" +
                "DESC \n" +
                "LIMIT 1";

        Optional<ExamSegment> maybeExamSegment;

        try {
            maybeExamSegment = Optional.of(jdbcTemplate.queryForObject(SQL, parameters, new ExamSegmentRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            maybeExamSegment = Optional.empty();
        }

        return maybeExamSegment;
    }

    private Set<String> createItemsFromString(String itemListStr) {
        // Check if the value is an empty string - otherwise, return a set of
        return itemListStr.equals("") ? new HashSet<>() : new HashSet<>(Arrays.asList(itemListStr.split(",")));
    }

    private class ExamSegmentRowMapper implements RowMapper<ExamSegment> {
        @Override
        public ExamSegment mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExamSegment.Builder()
                    .withExamId(UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")))
                .withSegmentId(rs.getString("segment_id"))
                .withSegmentKey(rs.getString("segment_key"))
                    .withSegmentPosition(rs.getInt("segment_position"))
                    .withFormKey(rs.getString("form_key"))
                    .withFormId(rs.getString("form_id"))
                .withAlgorithm(Algorithm.fromType(rs.getString("algorithm")))
                    .withExamItemCount(rs.getInt("exam_item_count"))
                    .withFieldTestItemCount(rs.getInt("field_test_item_count"))
                    .withIsPermeable(rs.getBoolean("permeable"))
                    .withRestorePermeableCondition(rs.getString("restore_permeable_condition"))
                    .withFormCohort(rs.getString("form_cohort"))
                    .withIsSatisfied(rs.getBoolean("satisfied"))
                    .withDateExited(ResultSetMapperUtility.mapTimestampToInstant(rs, "date_exited"))
                .withItemPool(createItemsFromString(rs.getString("item_pool")))
                    .withPoolCount(rs.getInt("pool_count"))
                    .withCreatedAt(ResultSetMapperUtility.mapTimestampToInstant(rs, "created_at"))
                    .build();
        }
    }
}
