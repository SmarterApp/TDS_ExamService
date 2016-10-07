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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
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
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamQueryRepositoryImpl(@Qualifier("queryDataSource") DataSource queryDataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(queryDataSource);
    }

    @Override
    public Optional<Exam> getExamById(UUID id) {
        final SqlParameterSource parameters = new MapSqlParameterSource("id", UuidAdapter.getBytesFromUUID(id));

        String query =
                "SELECT \n" +
                "   e.exam_id, \n" +
                "   e.session_id, \n" +
                "   e.browser_id, \n" +
                "   e.assessment_id, \n" +
                "   e.student_id, \n" +
                "   e.attempts, \n" +
                "   e.status, \n" +
                "   e.status_change_reason, \n" +
                "   e.client_name, \n" +
                "   e.date_started, \n" +
                "   e.date_deleted, \n" +
                "   e.date_changed, \n" +
                "   e.date_completed, \n" +
                "   e.created_at, \n" +
                "   esc.description, \n" +
                "   esc.stage \n" +
                "FROM \n" +
                "   exam e \n" +
                "JOIN \n" +
                "   exam_status_codes esc \n" +
                "   ON esc.status = e.status \n" +
                "WHERE \n" +
                "   e.exam_id = :id \n" +
                "ORDER BY \n" +
                "   e.id DESC \n" +
                "LIMIT 1";

        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(query, parameters, new ExamRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }

        return examOptional;
    }

    @Override
    public Optional<Exam> getLastAvailableExam(long studentId, String assessmentId, String clientName) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("studentId", studentId);
        queryParameters.put("assessmentId", assessmentId);
        queryParameters.put("clientName", clientName);

        final SqlParameterSource parameters = new MapSqlParameterSource(queryParameters);

        String query =
                "SELECT \n" +
                "   e.exam_id, \n" +
                "   e.session_id, \n" +
                "   e.browser_id, \n" +
                "   e.assessment_id, \n" +
                "   e.student_id, \n" +
                "   e.attempts, \n" +
                "   e.status, \n" +
                "   e.status_change_reason, \n" +
                "   e.client_name, \n" +
                "   e.date_started, \n" +
                "   e.date_deleted, \n" +
                "   e.date_changed, \n" +
                "   e.date_completed, \n" +
                "   e.created_at, \n" +
                "   esc.description, \n" +
                "   esc.stage \n " +
                "FROM \n" +
                "   exam e \n" +
                "JOIN \n" +
                "   exam_status_codes esc \n" +
                "   ON esc.status = e.status \n" +
                "WHERE \n" +
                "   e.date_deleted IS NULL \n" +
                "   AND e.student_id = :studentId \n" +
                "   AND e.assessment_id = :assessmentId \n" +
                "   AND e.client_name = :clientName \n" +
                "ORDER BY \n" +
                "   e.id DESC \n" +
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
                .withId(UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")))
                .withSessionId(UuidAdapter.getUUIDFromBytes(rs.getBytes("session_id")))
                .withBrowserId(UuidAdapter.getUUIDFromBytes(rs.getBytes("browser_id")))
                .withAssessmentId(rs.getString("assessment_id"))
                .withStudentId(rs.getLong("student_id"))
                .withAttempts(rs.getInt("attempts"))
                .withClientName(rs.getString("client_name"))
                .withDateStarted(mapTimezoneToInstant(rs, "date_started"))
                .withDateChanged(mapTimezoneToInstant(rs, "date_changed"))
                .withDateDeleted(mapTimezoneToInstant(rs, "date_deleted"))
                .withDateCompleted(mapTimezoneToInstant(rs, "date_completed"))
                .withCreatedAt(mapTimezoneToInstant(rs, "created_at"))
                .withStatus(new ExamStatusCode.Builder()
                    .withStatus(rs.getString("status"))
                    .withDescription(rs.getString("description"))
                    .withStage(rs.getString("stage"))
                    .build())
                .build();
        }

        private Instant mapTimezoneToInstant(ResultSet rs, String columnLabel) throws SQLException {
            Timestamp t = rs.getTimestamp(columnLabel);
            return t != null ? t.toLocalDateTime().toInstant(ZoneOffset.UTC) : null;
        }
    }
}
