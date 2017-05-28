package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.stream.Stream;

import tds.exam.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@Repository
public class ExamPageCommandRepositoryImpl implements ExamPageCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamPageCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final ExamPage... examPages) {
        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        final String examPageSQL =
            "INSERT INTO \n" +
                "exam_page (\n" +
                "   id, \n" +
                "   page_position, \n" +
                "   segment_key, \n" +
                "   item_group_key, \n" +
                "   group_items_required, \n" +
                "   exam_id, \n" +
                "   created_at) \n" +
                "VALUES (\n" +
                "   :id, \n" +
                "   :pagePosition, \n" +
                "   :segmentKey, \n" +
                "   :itemGroupKey, \n" +
                "   :groupItemsRequired, \n" +
                "   :examId, \n" +
                "   :createdAt)";

        SqlParameterSource[] parameters = Stream.of(examPages).map(examPage ->
            new MapSqlParameterSource("examId", examPage.getExamId().toString())
                .addValue("id", examPage.getId().toString())
                .addValue("pagePosition", examPage.getPagePosition())
                .addValue("segmentKey", examPage.getSegmentKey())
                .addValue("itemGroupKey", examPage.getItemGroupKey())
                .addValue("groupItemsRequired", examPage.getGroupItemsRequired())
                .addValue("createdAt", createdAt))
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(examPageSQL, parameters);
        update(examPages);
    }

    @Override
    public void deleteAll(final UUID examId) {
        final SqlParameterSource params = new MapSqlParameterSource("examId", examId.toString());

        final String SQL =
            "INSERT INTO \n" +
                "exam_page_event (\n" +
                "   exam_page_id, \n" +
                "   exam_id, \n" +
                "   exam_restarts_and_resumptions, \n" +
                "   page_duration, \n" +
                "   deleted_at, \n" +
                "   started_at, \n" +
                "   created_at) \n" +
                "SELECT \n" +
                "   PE.exam_page_id, \n" +
                "   PE.exam_id, \n" +
                "   PE.exam_restarts_and_resumptions, \n" +
                "   PE.page_duration, \n" +
                "   UTC_TIMESTAMP(), \n" +
                "   PE.started_at, \n " +
                "   UTC_TIMESTAMP() \n" +
                "FROM \n" +
                "   exam_page_event PE\n" +
                "JOIN \n" +
                "   exam_page P\n " +
                "ON \n" +
                "   PE.exam_page_id = P.id \n" +
                "WHERE \n " +
                "   P.exam_id = :examId";

        jdbcTemplate.update(SQL, params);
    }

    @Override
    public void update(final ExamPage... examPages) {
        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        final String updatePageSQL =
            "INSERT INTO \n" +
                "exam_page_event (\n" +
                "   exam_page_id, \n" +
                "   exam_id, \n" +
                "   exam_restarts_and_resumptions, \n" +
                "   page_duration, \n" +
                "   visible, \n" +
                "   deleted_at, \n" +
                "   started_at, \n" +
                "   created_at) \n" +
                "VALUES ( \n" +
                "   :examPageId, \n" +
                "   :examId, \n" +
                "   :examRestartsAndResumptions, \n" +
                "   :pageDuration, \n" +
                "   :visible, \n" +
                "   :deletedAt, \n" +
                "   :startedAt, \n" +
                "   :createdAt)";

        SqlParameterSource[] parameters = Stream.of(examPages).map(examPage ->
            new MapSqlParameterSource("examPageId", examPage.getId().toString())
                .addValue("examRestartsAndResumptions", examPage.getExamRestartsAndResumptions())
                .addValue("pageDuration", examPage.getDuration())
                .addValue("visible", examPage.isVisible())
                .addValue("examId", examPage.getExamId().toString())
                .addValue("startedAt", mapJodaInstantToTimestamp(examPage.getStartedAt()))
                .addValue("deletedAt", mapJodaInstantToTimestamp(examPage.getDeletedAt()))
                .addValue("visible", examPage.isVisible())
                .addValue("createdAt", createdAt))
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(updatePageSQL, parameters);
    }
}
