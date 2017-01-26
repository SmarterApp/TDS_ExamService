package tds.exam.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.repositories.ExamPageQueryRepository;

@Repository
public class ExamPageQueryRepositoryImpl implements ExamPageQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final Logger LOG = LoggerFactory.getLogger(ExamPageQueryRepositoryImpl.class);
    private final static ExamPageRowMapper examPageRowMapper = new ExamPageRowMapper();

    @Autowired
    public ExamPageQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<ExamPage> findAll(UUID examId) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examId));

        final String SQL =
            "SELECT \n" +
                "   P.id, \n" +
                "   P.page_position, \n" +
                "   P.item_group_key, \n" +
                "   P.exam_id, \n" +
                "   P.created_at, \n" +
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
                "   ON last_event.id = PE.id \n" +
                "WHERE \n" +
                "   P.exam_id = :examId AND\n" +
                "   PE.deleted_at IS NULL \n" +
                "ORDER BY\n" +
                "   P.page_position";

        return jdbcTemplate.query(SQL, parameters, examPageRowMapper);
    }

    @Override
    public Optional<ExamPage> findPageWithItems(UUID examId, int position) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examId))
            .addValue("position", position);

        final String SQL =
            "SELECT \n" +
                "   page.id AS page_id, \n" +
                "   page.page_position, \n" +
                "   page.item_group_key, \n" +
                "   page.are_group_items_required, \n" +
                "   page.exam_id, \n" +
                "   page.created_at, \n" +
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
                "   item.is_selected, \n" +
                "   item.is_marked_for_review, \n" +
                "   item.item_file_path, \n" +
                "   item.stimulus_file_path, \n" +
                "   response.response, \n" +
                "   response.is_valid, \n" +
                "   response.created_at AS response_created_at, \n" +
                "   segment.segment_key, \n" +
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
                "   AND segment.segment_key = page.exam_segment_key \n" +
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

        return jdbcTemplate.query(SQL, parameters, resultExtractor -> {
            Optional<ExamPage> page = Optional.empty();
            List<ExamItem> items = new ArrayList<>();

            while (resultExtractor.next()) {
                if (!page.isPresent()) {
                    page = Optional.of(new ExamPage.Builder()
                        .withId(resultExtractor.getLong("page_id"))
                        .withPagePosition(resultExtractor.getInt("page_position"))
                        .withSegmentKey(resultExtractor.getString("segment_key"))
                        .withSegmentId(resultExtractor.getString("segment_id"))
                        .withSegmentPosition(resultExtractor.getInt("segment_position"))
                        .withItemGroupKey(resultExtractor.getString("item_group_key"))
                        .withGroupItemsRequired(resultExtractor.getBoolean("are_group_items_required"))
                        .withExamId(UuidAdapter.getUUIDFromBytes(resultExtractor.getBytes("exam_id")))
                        .withExamItems(items)
                        .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "created_at"))
                        .withStartedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "started_at"))
                        .build());
                }

                // An item might not have a response
                ExamItemResponse response = null;
                if (resultExtractor.getString("response") != null) {
                    response = new ExamItemResponse.Builder()
                        .withExamItemId(resultExtractor.getLong("item_id"))
                        .withResponse(resultExtractor.getString("response"))
                        .withValid(resultExtractor.getBoolean("is_valid"))
                        .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "response_created_at"))
                        .build();
                }

                items.add(new ExamItem.Builder()
                    .withId(resultExtractor.getLong("item_id"))
                    .withItemKey(resultExtractor.getString("item_key"))
                    .withAssessmentItemBankKey(resultExtractor.getLong("assessment_item_bank_key"))
                    .withAssessmentItemKey(resultExtractor.getLong("assessment_item_key"))
                    .withItemType(resultExtractor.getString("item_type"))
                    .withExamPageId(resultExtractor.getLong("exam_page_id"))
                    .withPosition(resultExtractor.getInt("item_position"))
                    .withFieldTest(resultExtractor.getBoolean("is_fieldtest"))
                    .withRequired(resultExtractor.getBoolean("is_required"))
                    .withSelected(resultExtractor.getBoolean("is_selected"))
                    .withMarkedForReview(resultExtractor.getBoolean("is_marked_for_review"))
                    .withItemFilePath(resultExtractor.getString("item_file_path"))
                    .withStimulusFilePath(resultExtractor.getString("stimulus_file_path"))
                    .withResponse(response)
                    .build());
            }

            return page;
        });
    }

    private static class ExamPageRowMapper implements RowMapper<ExamPage> {
        @Override
        public ExamPage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExamPage.Builder()
                .withId(rs.getLong("id"))
                .withPagePosition(rs.getInt("page_position"))
                .withItemGroupKey(rs.getString("item_group_key"))
                .withExamId(UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")))
                .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "created_at"))
                .withStartedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "started_at"))
                .build();
        }
    }
}
