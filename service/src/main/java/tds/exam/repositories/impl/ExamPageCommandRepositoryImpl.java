package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.models.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;

import static tds.common.data.mysql.UuidAdapter.getBytesFromUUID;

@Repository
public class ExamPageCommandRepositoryImpl implements ExamPageCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamPageCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(List<ExamPage> examPages) {
        final String examPageSQL =
            "INSERT INTO exam_page (\n" +
            "   id, page_position, item_group_key, exam_id\n" +
            ") \n" +
            "VALUES (\n" +
            "   :id, \n" +
            "   :pagePosition, \n" +
            "   :itemGroupKey, \n" +
            "   :examId\n" +
            ")";

        SqlParameterSource[] parameters = examPages.stream().map(examPage ->
            new MapSqlParameterSource("examId", examPage.getExamId().toString())
                .addValue("id", examPage.getId().toString())
                .addValue("pagePosition", examPage.getPagePosition())
                .addValue("itemGroupKey", examPage.getItemGroupKey()))
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(examPageSQL, parameters);
        update(examPages);
    }

    @Override
    public void deleteAll(final UUID examId) {
        final SqlParameterSource params = new MapSqlParameterSource("examId", examId.toString());

        final String SQL =
            "INSERT INTO \n" +
            "   exam_page_event (exam_page_id, deleted_at, started_at) \n" +
            "SELECT \n" +
            "   exam_page_id, UTC_TIMESTAMP(), started_at \n" +
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

    private void update(List<ExamPage> examPages) {
        final String updatePageSQL =
            "INSERT INTO exam_page_event (exam_page_id, deleted_at, started_at) \n" +
            "VALUES (:examPageId, :deletedAt, :startedAt)";

        SqlParameterSource[] parameters = examPages.stream().map(examPage ->
            new MapSqlParameterSource("examPageId", examPage.getId().toString())
                .addValue("startedAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(examPage.getStartedAt()))
                .addValue("deletedAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(examPage.getDeletedAt())))
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(updatePageSQL, parameters);
    }
}
