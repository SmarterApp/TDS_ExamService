package tds.exam.repositories.impl;

import com.google.common.base.Optional;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.UUID;

import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.repositories.ExamStatusQueryRepository;

@Repository
public class ExamStatusQueryRepositoryImpl implements ExamStatusQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamStatusQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public ExamStatusCode findExamStatusCode(final String code) {
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

    @Override
    public Optional<Instant> findRecentTimeAtStatus(final UUID examId, final String examStatus) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("status", examStatus);

        final String SQL =
            "SELECT \n" +
                "    ee.status_changed_at AS changedAt\n" +
                "FROM \n" +
                "   exam.exam e \n" +
                "JOIN \n" +
                "   exam.exam_event ee \n" +
                "ON \n" +
                "   e.id = ee.exam_id \n" +
                "WHERE \n" +
                "   e.id = :examId \n" +
                "   AND ee.status = :status \n" +
                "ORDER BY status_changed_at \n" +
                "DESC LIMIT 1;";

        Optional<Instant> maybeLastStatusChangedAt;

        try {
            Timestamp lastStatusChangedAtTimestamp = jdbcTemplate.queryForObject(SQL, parameters, Timestamp.class);
            maybeLastStatusChangedAt = Optional.of(new Instant(lastStatusChangedAtTimestamp.getTime()));
        } catch (EmptyResultDataAccessException e) {
            maybeLastStatusChangedAt = Optional.absent();
        }

        return maybeLastStatusChangedAt;
    }
}
