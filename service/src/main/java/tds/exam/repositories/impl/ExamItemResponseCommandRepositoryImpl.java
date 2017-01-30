package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamItemResponse;
import tds.exam.repositories.ExamItemResponseCommandRepository;

/**
 * Handles modifications to the exam_item_response table
 */
@Repository
public class ExamItemResponseCommandRepositoryImpl implements ExamItemResponseCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamItemResponseCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(ExamItemResponse... responses) {
        final SqlParameterSource[] batchParameters = Stream.of(responses)
            .map(response -> new MapSqlParameterSource("examItemId", response.getExamItemId().toString())
                .addValue("response", response.getResponse())
                .addValue("sequence", response.getSequence())
                .addValue("isValid", response.isValid())
                .addValue("createdAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(response.getCreatedAt())))
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO exam_item_response ( \n" +
                "   exam_item_id, \n" +
                "   response, \n" +
                "   sequence, \n" +
                "   is_valid, \n" +
                "   created_at) \n" +
                "VALUES ( \n" +
                "   :examItemId, \n" +
                "   :response, \n" +
                "   :sequence, \n" +
                "   :isValid, \n" +
                "   :createdAt)";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }
}
