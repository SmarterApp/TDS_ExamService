package tds.exam.repositories.impl;

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

    public ExamPageCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(List<ExamPage> examPages) {
        final String examPageSQL =
            "INSERT INTO exam_page (\n" +
            "   page_position, item_group_key, exam_id\n" +
            ") \n" +
            "VALUES (\n" +
            "   :pagePosition, \n" +
            "   :itemGroupKey, \n" +
            "   :examId\n" +
            ")";

        examPages.forEach(examPage -> {
            SqlParameterSource parameterSources = new MapSqlParameterSource("examId", getBytesFromUUID(examPage.getExamId()))
                .addValue("pagePosition", examPage.getPagePosition())
                .addValue("itemGroupKey", examPage.getItemGroupKey());

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(examPageSQL, parameterSources, keyHolder);
            examPage.setId(keyHolder.getKey().longValue());

            update(examPage);
        });
    }

    @Override
    public void deleteAll(final UUID examId) {
        final SqlParameterSource params = new MapSqlParameterSource("examId", getBytesFromUUID(examId));

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

    private void update(ExamPage examPage) {
        final String updatePageSQL =
            "INSERT INTO exam_page_event (exam_page_id, deleted_at, started_at) \n" +
            "VALUES (:examPageId, :deletedAt, :startedAt)";

        final SqlParameterSource parameterSources = new MapSqlParameterSource("examPageId", examPage.getId())
                .addValue("startedAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(examPage.getStartedAt()))
                .addValue("deletedAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(examPage.getDeletedAt()));

        jdbcTemplate.update(updatePageSQL, parameterSources);
    }
}
