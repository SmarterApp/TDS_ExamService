package tds.exam.repositories.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamScoringStatus;
import tds.exam.repositories.ExamItemQueryRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ExamItemQueryRepositoryImpl implements ExamItemQueryRepository {
    private static final ExamItemRowMapper examItemRowMapper = new ExamItemRowMapper();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamItemQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public int getCurrentExamItemPosition(final UUID examId) {
        final SqlParameterSource params = new MapSqlParameterSource("examId", examId.toString());
        final String SQL =
            "SELECT \n" +
                "   MAX(I.position)\n" +
                "FROM \n" +
                "   exam_item I\n" +
                "JOIN \n" +
                "   exam_item_response IR \n" +
                "ON \n" +
                "   IR.exam_item_id = I.id\n" +
                "JOIN \n" +
                "   exam_page P \n" +
                "ON \n" +
                "   P.id = I.exam_page_id\n" +
                "JOIN\n" +
                "   exam_page_event PE\n" +
                "ON \n" +
                "   P.id = PE.exam_page_id\n" +
                "WHERE\n" +
                "   P.exam_id = :examId AND\n" +
                "   PE.deleted_at IS NULL;\n";

        return jdbcTemplate.queryForObject(SQL, params, Integer.class);
    }

    @Override
    public Map<UUID, Integer> getResponseCounts(final UUID... examIds) {
        // Needs to toString() each UUID individually
        final SqlParameterSource params = new MapSqlParameterSource("examIds",
            Arrays.stream(examIds).map(UUID::toString).collect(Collectors.toSet()));
        final String SQL =
            "SELECT \n" +
                "   exam_id, \n" +
                "   COUNT(id) AS response_count \n" +
                "FROM ( \n" +
                "   SELECT\n" +
                "       P.exam_id, \n" +
                "       I.id\n" +
                "   FROM \n" +
                "       exam.exam_item I \n" +
                "   JOIN \n" +
                "       exam.exam_item_response IR \n" +
                "   ON \n" +
                "       I.id = IR.exam_item_id \n" +
                "   JOIN \n" +
                "       exam.exam_page P \n" +
                "   ON \n" +
                "       P.id = I.exam_page_id \n" +
                "   JOIN \n" +
                "       exam.exam_page_event PE \n" +
                "   ON \n" +
                "       PE.exam_page_id = P.id \n" +
                "   WHERE \n" +
                "       PE.deleted_at IS NULL \n" +
                "       AND P.exam_id IN (:examIds) \n" +
                "   GROUP BY P.exam_id, I.id \n" +
                ") examIdCounts \n" +
                "GROUP BY exam_id";

        return jdbcTemplate.query(SQL, params, (ResultSetExtractor<Map<UUID, Integer>>) rs -> {
            HashMap<UUID, Integer> examIdResponseCounts = new HashMap<>();
            while (rs.next()) {
                examIdResponseCounts.put(UUID.fromString(rs.getString("exam_id")), rs.getInt("response_count"));
            }
            return examIdResponseCounts;
        });
    }

    @Override
    public Optional<ExamItem> findExamItemAndResponse(final UUID examId, final int position) {
        List<ExamItem> items = findExamItemAndResponses(examId, position);

        if (items.isEmpty()) {
            return Optional.empty();
        }

        //There should only be one item at a given position.  If there are multiple results there is a bug.
        return Optional.of(items.get(0));
    }

    @Override
    public List<ExamItem> findExamItemAndResponses(final UUID examId) {
        return findExamItemAndResponses(examId, null);
    }

    private List<ExamItem> findExamItemAndResponses(final UUID examId, final Integer position) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString());

        if (position != null) {
            parameters.addValue("position", position);
        }

        String SQL = "SELECT \n" +
            "  I.id AS examItemId,\n" +
            "  I.item_key,\n" +
            "  I.assessment_item_bank_key,\n" +
            "  I.assessment_item_key,\n" +
            "  I.item_type,\n" +
            "  I.exam_page_id,\n" +
            "  I.position,\n" +
            "  I.is_fieldtest,\n" +
            "  I.is_required,\n" +
            "  I.item_file_path, \n" +
            "  I.stimulus_file_path, \n" +
            "  I.created_at, \n" +
            "  R.id as responseId,\n" +
            "  R.response,\n" +
            "  R.sequence,\n" +
            "  R.is_valid,\n" +
            "  R.is_selected,\n" +
            "  R.score,\n" +
            "  R.scoring_status,\n" +
            "  R.scoring_rationale,\n" +
            "  R.scoring_dimensions,\n" +
            "  R.is_marked_for_review,\n" +
            "  R.scored_at, \n" +
            "  R.score_sent_at, \n" +
            "  R.score_latency, \n" +
            "  R.score_mark, \n" +
            "  R.created_at as response_created_at \n" +
            "FROM exam_item I\n" +
            "JOIN exam_page P ON I.exam_page_id = P.id\n" +
            "JOIN exam_page_event EPE ON P.id = EPE.exam_page_id \n" +
            "JOIN (\n" +
            "      SELECT\n" +
            "            exam_page_id,\n" +
            "            MAX(id) AS id\n" +
            "      FROM exam_page_event\n" +
            "      GROUP BY exam_page_id\n" +
            ") last_page_event ON EPE.id = last_page_event.id\n" +
            "LEFT JOIN (\n" +
            "      SELECT\n" +
            "            exam_item_id,\n" +
            "            MAX(id) AS exam_item_response_id\n" +
            "      FROM exam_item_response\n" +
            "      GROUP BY exam_item_id\n" +
            ") last_response ON I.id = last_response.exam_item_id\n" +
            "LEFT JOIN\n" +
            "      exam_item_response R ON last_response.exam_item_response_id = R.id\n" +
            "WHERE\n" +
            "  P.exam_id = :examId AND EPE.deleted_at IS NULL";

        if (position != null) {
            SQL += " AND I.position = :position ";
        }

        return jdbcTemplate.query(SQL, parameters, examItemRowMapper);
    }

    private static class ExamItemRowMapper implements RowMapper<ExamItem> {
        @Override
        public ExamItem mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            ExamItem.Builder itemBuilder = new ExamItem.Builder(UUID.fromString(rs.getString("examItemId")))
                .withAssessmentItemBankKey(rs.getLong("assessment_item_bank_key"))
                .withItemKey(rs.getString("item_key"))
                .withAssessmentItemKey(rs.getLong("assessment_item_key"))
                .withItemType(rs.getString("item_type"))
                .withExamPageId(UUID.fromString(rs.getString("exam_page_id")))
                .withFieldTest(rs.getBoolean("is_fieldtest"))
                .withRequired(rs.getBoolean("is_required"))
                .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "created_at"))
                .withPosition(rs.getInt("position"))
                .withItemFilePath(rs.getString("item_file_path"));

            if(rs.getObject("stimulus_file_path") != null) {
                itemBuilder.withStimulusFilePath(rs.getString("stimulus_file_path"));
            }

            //Since there is a left join in the query this could not have a response
            if (rs.getObject("responseId") != null) {
                ExamItemResponse.Builder responseBuilder = new ExamItemResponse.Builder()
                    .withId(rs.getLong("responseId"))
                    .withExamItemId(UUID.fromString(rs.getString("examItemId")))
                    .withResponse(rs.getString("response"))
                    .withSequence(rs.getInt("sequence"))
                    .withSelected(rs.getBoolean("is_selected"))
                    .withMarkedForReview(rs.getBoolean("is_marked_for_review"))
                    .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "response_created_at"));

                //Means that the item has been scored
                if (rs.getObject("scored_at") != null) {
                    ExamItemResponseScore score = new ExamItemResponseScore.Builder()
                        .withScore(rs.getInt("score"))
                        .withScoringStatus(ExamScoringStatus.fromType(rs.getString("scoring_status")))
                        .withScoringRationale(rs.getString("scoring_rationale"))
                        .withScoringDimensions(rs.getString("scoring_dimensions"))
                        .withScoredAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "scored_at"))
                        .withScoreSentAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "score_sent_at"))
                        .withScoreMark(StringUtils.isNotEmpty(rs.getString("score_mark")) ? UUID.fromString(rs.getString("score_mark")) : null)
                        .withScoreLatency(rs.getLong("score_latency"))
                        .build();

                    responseBuilder.withScore(score);
                }

                itemBuilder.withResponse(responseBuilder.build());
            }

            return itemBuilder.build();
        }
    }
}
