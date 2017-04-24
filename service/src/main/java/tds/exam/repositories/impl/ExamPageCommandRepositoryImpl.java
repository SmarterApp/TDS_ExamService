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
                "   exam_segment_key, \n" +
                "   item_group_key, \n" +
                "   are_group_items_required, \n" +
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
                .addValue("segmentKey", examPage.getExamSegmentKey())
                .addValue("itemGroupKey", examPage.getItemGroupKey())
                .addValue("groupItemsRequired", examPage.isGroupItemsRequired())
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
                "   deleted_at, \n" +
                "   started_at, \n" +
                "   created_at) \n" +
                "SELECT \n" +
                "   exam_page_id, \n" +
                "   UTC_TIMESTAMP(), \n" +
                "   started_at, \n " +
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
            "INSERT INTO exam_page_event (exam_page_id, deleted_at, started_at, created_at) \n" +
                "VALUES (:examPageId, :deletedAt, :startedAt, :createdAt)";

        SqlParameterSource[] parameters = Stream.of(examPages).map(examPage ->
            new MapSqlParameterSource("examPageId", examPage.getId().toString())
                .addValue("startedAt", mapJodaInstantToTimestamp(examPage.getStartedAt()))
                .addValue("deletedAt", mapJodaInstantToTimestamp(examPage.getDeletedAt()))
                .addValue("createdAt", createdAt))
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(updatePageSQL, parameters);
    }
}
