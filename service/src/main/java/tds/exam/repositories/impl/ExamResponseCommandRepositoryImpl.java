package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import tds.exam.models.ExamResponse;
import tds.exam.repositories.ExamResponseCommandRepository;

/**
 * Handles modifications to the exam_item_response table
 */
@Repository
public class ExamResponseCommandRepositoryImpl implements ExamResponseCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ExamResponseCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(List<ExamResponse> examResponses) {
        final List<SqlParameterSource> parameterSources = examResponses.stream()
            .map(examResponse -> new MapSqlParameterSource("examItemId", examResponse.getExamItemId())
                .addValue("response", examResponse.getResponse()))
            .collect(Collectors.toList());
        final String examResponsesSQL =
            "INSERT INTO exam_item_response ( \n" +
            "   exam_item_id, response \n" +
            ") \n" +
            "VALUES (\n " +
            "   :examItemId, :response \n" +
            ")";

        jdbcTemplate.batchUpdate(examResponsesSQL, parameterSources.toArray(new SqlParameterSource[parameterSources.size()]));
    }
}
