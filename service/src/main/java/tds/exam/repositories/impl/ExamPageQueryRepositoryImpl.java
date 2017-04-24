package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamScoringStatus;
import tds.exam.repositories.ExamPageQueryRepository;

@Repository
public class ExamPageQueryRepositoryImpl implements ExamPageQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ExamPageRowMapper examPageRowMapper = new ExamPageRowMapper();
    private static final ExamPageResultSetExtractor examPageResultExtractor = new ExamPageResultSetExtractor();

    private static final String EXAM_PAGE_STANDARD_SELECT = "SELECT \n" +
        "   P.id, \n" +
        "   P.page_position, \n" +
        "   P.item_group_key, \n" +
        "   P.exam_id, \n" +
        "   P.created_at, \n" +
        "   P.are_group_items_required, \n" +
        "   P.segment_key, \n" +
        "   PE.started_at \n" +
        "FROM \n" +
        "   exam_page P\n" +
        "JOIN ( \n" +
        "   SELECT \n" +
        "       exam_page_id, \n" +
        "       MAX(id) AS id \n" +
        "   FROM \n" +
        "       exam_page_event \n" +
        "   GROUP BY exam_page_id \n" +
        ") last_event \n" +
        "   ON P.id = last_event.exam_page_id \n" +
        "JOIN exam_page_event PE \n" +
        "   ON last_event.id = PE.id \n";

    @Autowired
    public ExamPageQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<ExamPage> findAll(final UUID examId) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString());

        final String SQL =
            EXAM_PAGE_STANDARD_SELECT +
                "WHERE \n" +
                "   P.exam_id = :examId AND\n" +
                "   PE.deleted_at IS NULL \n" +
                "ORDER BY\n" +
                "   P.page_position";

        return jdbcTemplate.query(SQL, parameters, examPageRowMapper);
    }

    @Override
    public Optional<ExamPage> find(final UUID examId, final int position) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("position", position);

        final String SQL = EXAM_PAGE_STANDARD_SELECT +
                " WHERE \n" +
                "   P.exam_id = :examId " +
                "   AND P.page_position = :position \n" +
                "   AND PE.deleted_at IS NULL";

        Optional<ExamPage> maybeExamPage;
        try {
            maybeExamPage = Optional.of(jdbcTemplate.queryForObject(SQL, parameters, examPageRowMapper));
        } catch (EmptyResultDataAccessException e) {
            maybeExamPage = Optional.empty();
        }

        return maybeExamPage;
    }

    @Override
    public Optional<ExamPage> find(final UUID pageId) {
        final SqlParameterSource parameters = new MapSqlParameterSource("id", pageId.toString());

        final String SQL = EXAM_PAGE_STANDARD_SELECT +
            " WHERE \n" +
            "   P.id = :id " +
            "   AND PE.deleted_at IS NULL";

        Optional<ExamPage> maybeExamPage;
        try {
            maybeExamPage = Optional.of(jdbcTemplate.queryForObject(SQL, parameters, examPageRowMapper));
        } catch (EmptyResultDataAccessException e) {
            maybeExamPage = Optional.empty();
        }

        return maybeExamPage;
    }

    @Override
    public Optional<ExamPage> findPageWithItems(final UUID examId, final int position) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("position", position);

        final String SQL =
            "SELECT \n" +
                "   page.id AS page_id, \n" +
                "   page.page_position, \n" +
                "   page.item_group_key, \n" +
                "   page.are_group_items_required, \n" +
                "   page.exam_id, \n" +
                "   page.created_at, \n" +
                "   page.segment_key, \n" +
                "   page_event.started_at, \n" +
                "   item.id AS item_id, \n" +
                "   item.item_key, \n" +
                "   item.assessment_item_bank_key, \n" +
                "   item.assessment_item_key, \n" +
                "   item.item_type, \n" +
                "   item.exam_page_id, \n" +
                "   item.position AS item_position, \n" +
                "   item.is_fieldtest, \n" +
                "   item.is_required, \n" +
                "   item.item_file_path, \n" +
                "   item.stimulus_file_path, \n" +
                "   item.created_at, \n" +
                "   response.response, \n" +
                "   response.sequence, \n" +
                "   response.is_valid, \n" +
                "   response.is_selected, \n" +
                "   response.score, \n" +
                "   response.scoring_status, \n" +
                "   response.scoring_rationale, \n" +
                "   response.scoring_dimensions, \n" +
                "   response.created_at AS response_created_at, \n" +
                "   response.scored_at, \n" +
                "   response.is_marked_for_review, \n" +
                "   segment.segment_id, \n" +
                "   segment.segment_position \n" +
                "FROM \n" +
                "   exam_page page \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_page_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam_page_event \n" +
                "   GROUP BY \n" +
                "       exam_page_id \n" +
                ") last_page_event \n" +
                "   ON page.id = last_page_event.exam_page_id \n" +
                "JOIN \n" +
                "   exam_page_event page_event \n" +
                "   ON page_event.id = last_page_event.id \n" +
                "JOIN \n" +
                "   exam_segment segment \n" +
                "   ON segment.exam_id = page.exam_id \n" +
                "   AND segment.segment_key = page.segment_key \n" +
                "JOIN \n" +
                "   exam_item item \n" +
                "   ON page.id = item.exam_page_id \n" +
                "LEFT JOIN \n" +
                "   (SELECT \n" +
                "       exam_item_id, \n" +
                "       MAX(id) AS id\n" +
                "   FROM \n" +
                "       exam_item_response \n" +
                "   GROUP BY \n" +
                "       exam_item_id) most_recent_response \n" +
                "   ON item.id = most_recent_response.exam_item_id \n" +
                "LEFT JOIN \n" +
                "   exam_item_response response \n" +
                "   ON most_recent_response.id = response.id \n" +
                "WHERE \n" +
                "   page.exam_id = :examId \n" +
                "   AND page.page_position = :position \n" +
                "ORDER BY \n" +
                "   item.position";

        return jdbcTemplate.query(SQL, parameters, examPageResultExtractor);
    }

    private static class ExamPageRowMapper implements RowMapper<ExamPage> {
        @Override
        public ExamPage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExamPage.Builder()
                .withId(UUID.fromString(rs.getString("id")))
                .withPagePosition(rs.getInt("page_position"))
                .withSegmentKey(rs.getString("segment_key"))
                .withItemGroupKey(rs.getString("item_group_key"))
                .withExamId(UUID.fromString(rs.getString("exam_id")))
                .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "created_at"))
                .withStartedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "started_at"))
                .build();
        }
    }

    private static class ExamPageResultSetExtractor implements ResultSetExtractor<Optional<ExamPage>> {
        @Override
        public Optional<ExamPage> extractData(ResultSet resultExtractor) throws SQLException, DataAccessException {
            Optional<ExamPage> page = Optional.empty();
            List<ExamItem> items = new ArrayList<>();

            while (resultExtractor.next()) {
                if (!page.isPresent()) {
                    page = Optional.of(new ExamPage.Builder()
                        .withId(UUID.fromString(resultExtractor.getString("page_id")))
                        .withPagePosition(resultExtractor.getInt("page_position"))
                        .withSegmentKey(resultExtractor.getString("segment_key"))
                        .withSegmentId(resultExtractor.getString("segment_id"))
                        .withSegmentPosition(resultExtractor.getInt("segment_position"))
                        .withItemGroupKey(resultExtractor.getString("item_group_key"))
                        .withGroupItemsRequired(resultExtractor.getBoolean("are_group_items_required"))
                        .withExamId(UUID.fromString(resultExtractor.getString("exam_id")))
                        .withExamItems(items)
                        .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "created_at"))
                        .withStartedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "started_at"))
                        .build());
                }

                // An item might not have a response
                ExamItemResponse response = null;
                if (resultExtractor.getString("response") != null) {
                    response = new ExamItemResponse.Builder()
                        .withExamItemId(UUID.fromString(resultExtractor.getString("item_id")))
                        .withResponse(resultExtractor.getString("response"))
                        .withSequence(resultExtractor.getInt("sequence"))
                        .withValid(resultExtractor.getBoolean("is_valid"))
                        .withSelected(resultExtractor.getBoolean("is_selected"))
                        .withMarkedForReview(resultExtractor.getBoolean("is_marked_for_review"))
                        .withScore(new ExamItemResponseScore.Builder()
                            .withScore(resultExtractor.getInt("score"))
                            .withScoringStatus(ExamScoringStatus.fromType(resultExtractor.getString("scoring_status")))
                            .withScoringRationale(resultExtractor.getString("scoring_rationale"))
                            .withScoringDimensions(resultExtractor.getString("scoring_dimensions"))
                            .withScoredAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "scored_at"))
                            .build())
                        .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "response_created_at"))
                        .build();
                }

                items.add(new ExamItem.Builder(UUID.fromString(resultExtractor.getString("item_id")))
                    .withItemKey(resultExtractor.getString("item_key"))
                    .withAssessmentItemBankKey(resultExtractor.getLong("assessment_item_bank_key"))
                    .withAssessmentItemKey(resultExtractor.getLong("assessment_item_key"))
                    .withItemType(resultExtractor.getString("item_type"))
                    .withExamPageId(UUID.fromString(resultExtractor.getString("exam_page_id")))
                    .withPosition(resultExtractor.getInt("item_position"))
                    .withFieldTest(resultExtractor.getBoolean("is_fieldtest"))
                    .withRequired(resultExtractor.getBoolean("is_required"))
                    .withItemFilePath(resultExtractor.getString("item_file_path"))
                    .withStimulusFilePath(resultExtractor.getString("stimulus_file_path"))
                    .withResponse(response)
                    .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "created_at"))
                    .build());
            }

            return page;
        }
    }
}
