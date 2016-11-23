package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.repositories.ExamStatusQueryRepository;

@Repository
public class ExamStatusQueryRepositoryImpl implements ExamStatusQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamStatusQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public ExamStatusCode findExamStatusCode(String code) {
        SqlParameterSource parameters = new MapSqlParameterSource("code", code);

        String SQL = "SELECT \n" +
            "status, \n" +
            "stage \n" +
            "FROM exam_status_codes \n" +
            "WHERE status = :code";

        return jdbcTemplate.queryForObject(SQL, parameters, (resultSet, i) ->
            new ExamStatusCode(resultSet.getString("status"),
                ExamStatusStage.fromType(resultSet.getString("stage")))
        );
    }
}
