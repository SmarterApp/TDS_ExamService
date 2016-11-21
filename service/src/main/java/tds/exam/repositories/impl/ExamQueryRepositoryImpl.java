package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapTimestampToJodaInstant;

@Repository
public class ExamQueryRepositoryImpl implements ExamQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public Optional<Exam> getExamById(UUID id) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(id));

        String querySQL = "SELECT \n" +
            "   e.id, \n" +
            "   e.session_id, \n" +
            "   e.browser_id, \n" +
            "   e.assessment_id, \n" +
            "   e.student_id, \n" +
            "   e.client_name, \n" +
            "   e.environment,\n" +
            "   e.subject,    \n" +
            "   e.login_ssid,\n" +
            "   e.student_name,\n" +
            "   e.date_started,    \n" +
            "   e.assessment_key,\n" +
            "   e.assessment_window_id,\n" +
            "   e.assessment_algorithm,\n" +
            "   e.segmented,\n" +
            "   ee.attempts, \n" +
            "   ee.status, \n" +
            "   ee.status_change_reason, \n" +
            "   ee.date_deleted, \n" +
            "   ee.date_changed, \n" +
            "   ee.date_completed, \n" +
            "   ee.date_joined, \n" +
            "   ee.date_scored, \n" +
            "   e.created_at, \n" +
            "   esc.description, \n" +
            "   esc.stage\n" +
            "FROM exam.exam e\n" +
            "JOIN ( \n" +
            "   SELECT \n" +
            "       exam_id, \n" +
            "       MAX(id) AS id \n" +
            "   FROM \n" +
            "       exam.exam_event \n" +
            "   WHERE exam_id = :examId\n" +
            "   GROUP BY exam_id \n" +
            ") last_event \n" +
            "  ON e.id = last_event.exam_id \n" +
            "JOIN exam.exam_event ee \n" +
            "  ON last_event.exam_id = ee.exam_id AND \n" +
            "     last_event.id = ee.id\n" +
            "JOIN exam.exam_status_codes esc \n" +
            "  ON esc.status = ee.status;";

        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(querySQL, parameters, new ExamRowMapper()));
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
            "SELECT " +
            "   e.id, \n" +
            "   e.session_id, \n" +
            "   e.browser_id, \n" +
            "   e.assessment_id, \n" +
            "   e.student_id, \n" +
            "   e.client_name, \n" +
            "   e.environment,\n" +
            "   e.subject,    \n" +
            "   e.login_ssid,\n" +
            "   e.student_name,\n" +
            "   e.date_started,    \n" +
            "   e.assessment_key,\n" +
            "   e.assessment_window_id,\n" +
            "   e.assessment_algorithm,\n" +
            "   e.segmented,\n" +
            "   ee.attempts, \n" +
            "   ee.status, \n" +
            "   ee.status_change_reason, \n" +
            "   ee.date_deleted, \n" +
            "   ee.date_changed, \n" +
            "   ee.date_completed, \n" +
            "   ee.date_joined, \n" +
            "   ee.date_scored, \n" +
            "   e.created_at, \n" +
            "   esc.description, \n" +
            "   esc.stage\n" +
            "FROM exam.exam e\n" +
            "JOIN ( \n" +
            "   SELECT \n" +
            "       exam_id, \n" +
            "       MAX(id) AS id \n" +
            "   FROM \n" +
            "       exam.exam_event \n" +
            "   WHERE date_deleted IS NULL \n" +
            "   GROUP BY exam_id \n" +
            ") last_event \n" +
            "  ON e.id = last_event.exam_id \n" +
            "JOIN exam.exam_event ee \n" +
            "  ON last_event.exam_id = ee.exam_id AND \n" +
            "     last_event.id = ee.id \n" +
            "JOIN exam.exam_status_codes esc \n" +
            "  ON esc.status = ee.status \n" +
            "WHERE \n" +
            "   e.student_id = :studentId \n" +
            "   AND e.assessment_id = :assessmentId \n" +
            "   AND e.client_name = :clientName \n" +
            "ORDER BY \n" +
            "   e.created_at DESC";

        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(query, parameters, new ExamRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }

        return examOptional;
    }

    @Override
    public List<Ability> findAbilities(UUID exam, String clientName, String subject, Long studentId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("examId", UuidAdapter.getBytesFromUUID(exam));
        parameters.put("clientName", clientName);
        parameters.put("subject", subject);
        parameters.put("studentId", studentId);

        final String SQL =
            "SELECT\n" +
                "exam.id,\n" +
                "exam.assessment_id,\n" +
                "ee.attempts,\n" +
                "ee.date_scored,\n" +
                "exam_scores.value AS score\n" +
            "FROM exam exam \n" +
            "JOIN ( \n" +
            "   SELECT \n" +
            "       exam_id, \n" +
            "       MAX(id) AS id \n" +
            "   FROM \n" +
            "       exam.exam_event \n" +
            "   GROUP BY exam_id \n" +
            ") last_event \n" +
            "  ON exam.id = last_event.exam_id \n" +
            "JOIN exam.exam_event ee \n" +
            "  ON last_event.exam_id = ee.exam_id AND \n" +
            "     last_event.id = ee.id \n" +
            "JOIN exam.exam_status_codes esc \n" +
            "  ON esc.status = ee.status \n" +
            "INNER JOIN \n" +
                "exam_scores \n" +
            "ON \n" +
                "exam.id = exam_scores.fk_scores_examid_exam \n" +
            "WHERE\n" +
                "exam.client_name = :clientName AND\n" +
                "exam.student_id = :studentId AND\n" +
                "exam.subject = :subject AND\n" +
                "ee.date_deleted IS NULL AND\n" +
                "ee.date_scored IS NOT NULL AND\n" +
                "exam.id <> :examId AND\n" +
                "exam_scores.use_for_ability = 1 AND\n" +
                "exam_scores.value IS NOT NULL \n" +
            "ORDER BY ee.date_scored DESC";

        return jdbcTemplate.query(SQL, parameters, new AbilityRowMapper());
    }

    private class AbilityRowMapper implements RowMapper<Ability> {
        @Override
        public Ability mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Ability(
                    UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")),
                    rs.getString("assessment_id"),
                    rs.getInt("attempts"),
                    ResultSetMapperUtility.mapTimestampToInstant(rs, "date_scored"),
                    rs.getDouble("score")
            );
        }
    }

    private class ExamRowMapper implements RowMapper<Exam> {
        @Override
        public Exam mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Exam.Builder()
                .withId(UuidAdapter.getUUIDFromBytes(rs.getBytes("id")))
                .withSessionId(UuidAdapter.getUUIDFromBytes(rs.getBytes("session_id")))
                .withBrowserId(UuidAdapter.getUUIDFromBytes(rs.getBytes("browser_id")))
                .withAssessmentId(rs.getString("assessment_id"))
                .withAssessmentKey(rs.getString("assessment_key"))
                .withAssessmentWindowId(rs.getString("assessment_window_id"))
                .withEnvironment(rs.getString("environment"))
                .withStudentId(rs.getLong("student_id"))
                .withLoginSSID(rs.getString("login_ssid"))
                .withStudentName(rs.getString("student_name"))
                .withAttempts(rs.getInt("attempts"))
                .withClientName(rs.getString("client_name"))
                .withSubject(rs.getString("subject"))
                .withDateStarted(mapTimestampToJodaInstant(rs, "date_started"))
                .withDateChanged(mapTimestampToJodaInstant(rs, "date_changed"))
                .withDateDeleted(mapTimestampToJodaInstant(rs, "date_deleted"))
                .withDateScored(mapTimestampToJodaInstant(rs, "date_scored"))
                .withDateCompleted(mapTimestampToJodaInstant(rs, "date_completed"))
                .withCreatedAt(mapTimestampToJodaInstant(rs, "created_at"))
                .withDateJoined(mapTimestampToJodaInstant(rs, "date_joined"))
                .withStatus(new ExamStatusCode.Builder()
                    .withStatus(rs.getString("status"))
                    .withDescription(rs.getString("description"))
                    .withStage(rs.getString("stage"))
                    .build())
                .withStatusChangeReason(rs.getString("status_change_reason"))
                .build();
        }
    }
}
