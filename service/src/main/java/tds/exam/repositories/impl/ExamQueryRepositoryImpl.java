package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.common.data.mysql.UuidAdapter;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.repositories.ExamQueryRepository;

@Repository
public class ExamQueryRepositoryImpl implements ExamQueryRepository {
    private static final String BASE_SELECT_QUERY = "SELECT exam.id as examId, session_id, assessment_id, student_id, times_taken, exam.status, client_name, date_started, date_deleted, date_changed, created_at, esc.description, esc.stage \n " +
        "FROM exam.exam exam \n" +
        "JOIN exam_status_codes esc ON esc.status = exam.status \n";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamQueryRepositoryImpl(@Qualifier("queryDataSource") DataSource queryDataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(queryDataSource);
    }

    @Override
    public Optional<Exam> getExamById(UUID id) {
        final SqlParameterSource parameters = new MapSqlParameterSource("id", UuidAdapter.getBytesFromUUID(id));

        String query = BASE_SELECT_QUERY + " WHERE id = :id";

        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(query, parameters, new ExamRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }


        return examOptional;
    }

    @Override
    public Optional<Exam> getLastAvailableExam(int studentId, String assessmentId, String clientName) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("studentId", studentId);
        queryParameters.put("assessmentId", assessmentId);
        queryParameters.put("clientName", clientName);

        final SqlParameterSource parameters = new MapSqlParameterSource(queryParameters);

        String query = BASE_SELECT_QUERY +
            "WHERE date_deleted IS NULL \n" +
            "AND student_id = :studentId \n" +
            "AND assessment_id = :assessmentId \n" +
            "AND client_name = :clientName \n" +
            "ORDER BY exam.created_at \n" +
            "LIMIT 1";

        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(query, parameters, new ExamRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }

        return examOptional;
    }

    private class ExamRowMapper implements RowMapper<Exam> {
        @Override
        public Exam mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Exam.Builder()
                .withId(UuidAdapter.getUUIDFromBytes(rs.getBytes("examId")))
                .withSessionId(UuidAdapter.getUUIDFromBytes(rs.getBytes("session_id")))
                .withAssessmentId(rs.getString("assessment_id"))
                .withStudentId(rs.getLong("student_id"))
                .withTimesTaken(rs.getInt("times_taken"))
                .withClientName(rs.getString("client_name"))
                .withDateStarted(map(rs, "date_started"))
                .withDateChanged(map(rs, "date_changed"))
                .withDateDeleted(map(rs, "date_deleted"))
                .withCreatedAt(rs.getTimestamp("created_at").toInstant())
                .withStatus(new ExamStatusCode.Builder()
                    .withStatus(rs.getString("status"))
                    .withDescription(rs.getString("description"))
                    .withStage(rs.getString("stage"))
                    .build())
                .build();
        }

        private Instant map(ResultSet rs, String columnLabel) throws SQLException {
            Timestamp t = rs.getTimestamp(columnLabel);
            return t != null ? t.toInstant() : null;
        }
    }
}
