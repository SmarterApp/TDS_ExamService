package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;

@Repository
public class ExamPageCommandRepositoryImpl implements ExamPageCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamPageCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(ExamPage... examPages) {
        final String examPageSQL =
            "INSERT INTO \n" +
                "exam_page (\n" +
                "   id, \n" +
                "   page_position, \n" +
                "   exam_segment_key, \n" +
                "   item_group_key, \n" +
                "   are_group_items_required, \n" +
                "   exam_id) \n" +
                "VALUES (\n" +
                "   :id, \n" +
                "   :pagePosition, \n" +
                "   :segmentKey, \n" +
                "   :itemGroupKey, \n" +
                "   :groupItemsRequired, \n" +
                "   :examId)";

        SqlParameterSource[] parameters = Stream.of(examPages).map(examPage ->
            new MapSqlParameterSource("examId", examPage.getExamId().toString())
                .addValue("id", examPage.getId().toString())
                .addValue("pagePosition", examPage.getPagePosition())
                .addValue("segmentKey", examPage.getSegmentKey())
                .addValue("itemGroupKey", examPage.getItemGroupKey())
                .addValue("groupItemsRequired", examPage.isGroupItemsRequired()))
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
                "   started_at) \n" +
                "SELECT \n" +
                "   exam_page_id, \n" +
                "   UTC_TIMESTAMP(), \n" +
                "   started_at \n" +
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
    public void update(ExamPage... examPages) {
        final String updatePageSQL =
            "INSERT INTO exam_page_event (exam_page_id, deleted_at, started_at) \n" +
                "VALUES (:examPageId, :deletedAt, :startedAt)";

        SqlParameterSource[] parameters = Stream.of(examPages).map(examPage ->
            new MapSqlParameterSource("examPageId", examPage.getId().toString())
                .addValue("startedAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(examPage.getStartedAt()))
                .addValue("deletedAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(examPage.getDeletedAt())))
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(updatePageSQL, parameters);
    }
}
