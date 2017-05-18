package tds.exam.repositories.impl;

import org.joda.time.Instant;
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
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapTimestampToJodaInstant;
import static tds.exam.ExamStatusCode.STATUS_PENDING;
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_ENTRY;
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_EXIT;
import static tds.exam.ExamStatusCode.STATUS_SUSPENDED;

@Repository
public class ExamQueryRepositoryImpl implements ExamQueryRepository {
    private static final RowMapper<Exam> examRowMapper = new ExamRowMapper();
    private static final RowMapper<Ability> abilityRowMapper = new AbilityRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String EXAM_QUERY_COLUMN_LIST = "e.id, \n" +
        "ee.session_id, \n" +
        "ee.browser_id, \n" +
        "e.assessment_id, \n" +
        "e.student_id, \n" +
        "e.client_name, \n" +
        "e.environment,\n" +
        "e.subject, \n" +
        "e.login_ssid,\n" +
        "e.student_name,\n" +
        "e.joined_at, \n" +
        "e.assessment_key, \n" +
        "e.assessment_window_id, \n" +
        "e.assessment_algorithm, \n" +
        "e.segmented, \n" +
        "e.msb, \n" +
        "lang.code AS language_code, \n" +
        "ee.attempts, \n" +
        "ee.status, \n" +
        "ee.status_changed_at, \n" +
        "ee.max_items, \n" +
        "ee.expires_at, \n" +
        "ee.status_change_reason, \n" +
        "ee.deleted_at, \n" +
        "ee.changed_at, \n" +
        "ee.completed_at, \n" +
        "ee.started_at, \n" +
        "ee.scored_at, \n" +
        "ee.abnormal_starts, \n" +
        "ee.waiting_for_segment_approval_position, \n" +
        "ee.current_segment_position, \n" +
        "ee.custom_accommodations, \n" +
        "ee.browser_user_agent, \n" +
        "ee.resumptions, \n" +
        "ee.restarts_and_resumptions, \n" +
        "e.created_at, \n" +
        "esc.description, \n" +
        "esc.status, \n" +
        "esc.stage \n";

    @Autowired
    public ExamQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public Optional<Exam> getExamById(final UUID id) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", id.toString());

        String querySQL =
            "SELECT \n" +
                EXAM_QUERY_COLUMN_LIST +
                "FROM exam.exam e\n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       id, \n" +
                "       exam_id \n" +
                "   FROM \n" +
                "       exam.exam_event \n" +
                "   WHERE exam_id = :examId\n" +
                "   ORDER BY id DESC \n" +
                "   LIMIT 1 \n" +
                ") last_event \n" +
                "  ON e.id = last_event.exam_id \n" +
                "JOIN exam.exam_event ee \n" +
                "  ON last_event.exam_id = ee.exam_id AND \n" +
                "     last_event.id = ee.id\n" +
                "JOIN exam.exam_status_codes esc \n" +
                "  ON esc.status = ee.status \n" +
                "LEFT JOIN exam.exam_accommodation lang \n" +
                "  ON lang.exam_id = e.id \n" +
                "  AND lang.created_at = \n" +
                "  ( \n" +
                "       SELECT \n" +
                "           MAX(eacc.created_at) \n" +
                "       FROM \n" +
                "          exam.exam_accommodation eacc \n" +
                "          WHERE \n" +
                "          eacc.exam_id = e.id \n" +
                "          AND eacc.type = 'Language'\n" +
                "  ) \n" +
                "WHERE \n" +
                "   lang.type = 'Language'";


        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(querySQL, parameters, examRowMapper));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }

        return examOptional;
    }

    @Override
    public Optional<Exam> getLastAvailableExam(final long studentId, final String assessmentId, final String clientName) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("studentId", studentId);
        queryParameters.put("assessmentId", assessmentId);
        queryParameters.put("clientName", clientName);

        final SqlParameterSource parameters = new MapSqlParameterSource(queryParameters);

        String query =
            "SELECT \n" +
                EXAM_QUERY_COLUMN_LIST +
                "FROM \n" +
                "   exam.exam e \n" +
                "JOIN \n" +
                "   exam.exam_event ee \n" +
                "   ON ee.exam_id = e.id \n" +
                "   AND ee.deleted_at IS NULL \n" +
                "JOIN \n" +
                "   exam.exam_status_codes esc \n" +
                "   ON esc.status = ee.status \n" +
                "LEFT JOIN \n" +
                "   exam.exam_accommodation lang \n" +
                "   ON lang.exam_id = e.id \n" +
                "   AND lang.type = 'Language' \n" +
                "   AND lang.created_at = ( \n" +
                "       SELECT \n" +
                "           MAX(eacc.created_at) \n" +
                "       FROM \n" +
                "           exam.exam_accommodation eacc \n" +
                "       WHERE \n" +
                "           eacc.exam_id = e.id \n" +
                "           AND eacc.type = 'Language') \n" +
                "WHERE \n" +
                "   e.student_id = :studentId \n" +
                "   AND e.assessment_id = :assessmentId \n" +
                "   AND e.client_name = :clientName \n" +
                "   AND lang.type = 'Language' \n" +
                "ORDER BY \n" +
                "   ee.id DESC, \n" +
                "   e.created_at DESC \n" +
                "LIMIT 1";


        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(query, parameters, examRowMapper));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }

        return examOptional;
    }

    @Override
    public Optional<Instant> findLastStudentActivity(final UUID id) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", id.toString());

        final String SQL =
            "SELECT \n" +
                "   MAX(ee.changed_at) AS lastStudentActivityTime \n" +
                "FROM \n" +
                "   exam e \n" +
                "JOIN \n" +
                "   exam_event ee \n" +
                "   ON e.exam_id = e.id \n" +
                "WHERE \n" +
                "   ee.status = 'paused' AND \n" +
                "   e.id = :examId \n" +
                "UNION ALL \n" +
                "SELECT \n" +
                "   MAX(IR.created_at) AS lastStudentActivityTime \n" +
                "FROM \n" +
                "   exam_item_response IR \n" +
                "JOIN \n " +
                "   exam_item I \n" +
                "   ON I.id = IR.exam_item_id \n " +
                "JOIN \n " +
                "   exam_page P \n" +
                "   ON P.id = I.exam_page_id \n" +
                "JOIN \n" +
                "   exam_page_event PE \n" +
                "ON \n" +
                "   P.id = PE.exam_page_id \n" +
                "   AND PE.deleted_at IS NULL \n" +
                "WHERE \n" +
                "   P.exam_id = :examId \n" +
                "UNION ALL \n" +
                "SELECT \n" +
                "   MAX(P.created_at) AS lastStudentActivityTime \n" +
                "FROM \n" +
                "   exam_page P \n" +
                "JOIN \n" +
                "   exam_page_event PE \n" +
                "   ON P.id = PE.exam_page_id \n" +
                "WHERE \n" +
                "   P.exam_id = :examId \n" +
                "   AND PE.deleted_at IS NULL \n" +
                "ORDER BY " +
                "   lastStudentActivityTime DESC \n" +
                "LIMIT 1";

        Optional<Instant> maybeLastStudentActivityTime;
        try {
            Timestamp lastPausedTime = jdbcTemplate.queryForObject(SQL, parameters, Timestamp.class);
            maybeLastStudentActivityTime = Optional.of(new Instant(lastPausedTime.getTime()));
        } catch (EmptyResultDataAccessException e) {
            maybeLastStudentActivityTime = Optional.empty();
        }

        return maybeLastStudentActivityTime;
    }

    @Override
    public List<Exam> findAllExamsInSessionWithoutStatus(final UUID sessionId, final Set<String> statuses) {
        return findAllExamsWithStatus(sessionId, statuses, true);
    }

    @Override
    public List<Exam> findAllExamsInSessionWithStatus(final UUID sessionId, final Set<String> statuses) {
        return findAllExamsWithStatus(sessionId, statuses, false);
    }

    private List<Exam> findAllExamsWithStatus(final UUID sessionId, final Set<String> statuses, final boolean inverse) {
        final SqlParameterSource parameters = new MapSqlParameterSource("sessionId", sessionId.toString())
            .addValue("statuses", statuses);

        final String SQL =
            "SELECT \n" +
                EXAM_QUERY_COLUMN_LIST +
                "FROM exam.exam e \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam.exam_event \n" +
                "   GROUP BY exam_id \n" +
                ") last_event \n" +
                "  ON e.id = last_event.exam_id \n" +
                "JOIN exam.exam_event ee \n" +
                "  ON last_event.exam_id = ee.exam_id AND \n" +
                "     last_event.id = ee.id\n" +
                "JOIN exam.exam_status_codes esc \n" +
                "  ON esc.status = ee.status \n" +
                "LEFT JOIN exam.exam_accommodation lang \n" +
                "  ON lang.exam_id = e.id \n" +
                "  AND lang.created_at = \n" +
                "  ( \n" +
                "       SELECT \n" +
                "           MAX(eacc.created_at) \n" +
                "       FROM \n" +
                "          exam.exam_accommodation eacc \n" +
                "          WHERE \n" +
                "          eacc.exam_id = e.id \n" +
                "          AND eacc.type = 'Language'\n" +
                "  ) \n" +
                "WHERE ee.session_id = :sessionId \n" +
                "   AND ee.status " + (inverse ? "NOT " : "") + " IN (:statuses) \n " +
                "   AND lang.type = 'Language'";

        return jdbcTemplate.query(SQL, parameters, examRowMapper);
    }

    @Override
    public List<Ability> findAbilities(final UUID exam, final String clientName, final String subject, final Long studentId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("examId", exam.toString());
        parameters.put("clientName", clientName);
        parameters.put("subject", subject);
        parameters.put("studentId", studentId);

        final String SQL =
            "SELECT\n" +
                "exam.id,\n" +
                "exam.assessment_id,\n" +
                "ee.attempts,\n" +
                "ee.scored_at,\n" +
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
                "  exam_scores \n" +
                "ON \n" +
                "  exam.id = exam_scores.exam_id \n" +
                "WHERE\n" +
                "  exam.client_name = :clientName \n" +
                "  AND exam.student_id = :studentId \n" +
                "  AND exam.subject = :subject \n" +
                "  AND ee.deleted_at IS NULL \n" +
                "  AND ee.scored_at IS NOT NULL \n" +
                "  AND exam.id <> :examId \n" +
                "  AND exam_scores.use_for_ability = 1 \n" +
                "  AND exam_scores.value IS NOT NULL \n" +
                "ORDER BY ee.scored_at DESC";

        return jdbcTemplate.query(SQL, parameters, abilityRowMapper);
    }

    @Override
    public List<Exam> getExamsPendingApproval(final UUID sessionId) {
        // create list of statuses that require proctor approval
        final List<String> pendingStatuses = Arrays.asList(
            STATUS_PENDING, STATUS_SUSPENDED, STATUS_SEGMENT_ENTRY, STATUS_SEGMENT_EXIT);

        final SqlParameterSource parameters = new MapSqlParameterSource("sessionId", sessionId.toString())
            .addValue("statusSet", pendingStatuses);

        final String SQL =
            "SELECT \n" +
                EXAM_QUERY_COLUMN_LIST +
                "FROM exam e \n" +
                "JOIN ( \n" +
                "  SELECT \n" +
                "    exam_id, \n" +
                "    MAX(id) AS id \n" +
                "  FROM exam_event \n" +
                "  GROUP BY exam_id \n" +
                ") last_event ON \n" +
                "  last_event.exam_id = e.id \n" +
                "JOIN exam_event ee \n" +
                "  ON last_event.id = ee.id \n" +
                "JOIN exam.exam_status_codes esc \n" +
                "  ON esc.status = ee.status \n" +
                "LEFT JOIN exam.exam_accommodation lang \n" +
                "  ON lang.exam_id = e.id \n" +
                "  AND lang.created_at = \n" +
                "  ( \n" +
                "       SELECT \n" +
                "           MAX(eacc.created_at) \n" +
                "       FROM \n" +
                "          exam.exam_accommodation eacc \n" +
                "          WHERE \n" +
                "          eacc.exam_id = e.id \n" +
                "          AND eacc.type = 'Language'\n" +
                "  ) \n" +
                "WHERE \n" +
                "  ee.session_id = :sessionId \n" +
                "  AND ee.status IN (:statusSet) \n" +
                "  AND lang.type = 'Language' \n";


        return jdbcTemplate.query(SQL, parameters, examRowMapper);
    }

    @Override
    public List<Exam> findAllExamsForStudent(final long studentId) {
        final SqlParameterSource parameters = new MapSqlParameterSource("studentId", studentId);

        final String SQL =
            "SELECT \n" +
                EXAM_QUERY_COLUMN_LIST +
                "FROM exam.exam e \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam.exam_event \n" +
                "   GROUP BY exam_id \n" +
                ") last_event \n" +
                "  ON e.id = last_event.exam_id \n" +
                "JOIN exam.exam_event ee \n" +
                "  ON last_event.exam_id = ee.exam_id AND \n" +
                "     last_event.id = ee.id\n" +
                "JOIN exam.exam_status_codes esc \n" +
                "  ON esc.status = ee.status \n" +
                "LEFT JOIN exam.exam_accommodation lang \n" +
                "  ON lang.exam_id = e.id \n" +
                "  AND lang.created_at = \n" +
                "  ( \n" +
                "       SELECT \n" +
                "           MAX(eacc.created_at) \n" +
                "       FROM \n" +
                "          exam.exam_accommodation eacc \n" +
                "          WHERE \n" +
                "          eacc.exam_id = e.id \n" +
                "          AND eacc.type = 'Language'\n" +
                "  ) \n" +
                "WHERE e.student_id = :studentId \n" +
                "   AND lang.type = 'Language' \n" +
                "   AND ee.deleted_at IS NULL";

        return jdbcTemplate.query(SQL, parameters, examRowMapper);
    }

    private static class AbilityRowMapper implements RowMapper<Ability> {
        @Override
        public Ability mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Ability(
                UUID.fromString(rs.getString("id")),
                rs.getString("assessment_id"),
                rs.getInt("attempts"),
                ResultSetMapperUtility.mapTimestampToInstant(rs, "scored_at"),
                rs.getDouble("score")
            );
        }
    }

    private static class ExamRowMapper implements RowMapper<Exam> {
        @Override
        public Exam mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Exam.Builder()
                .withId(UUID.fromString(rs.getString("id")))
                .withSessionId(UUID.fromString(rs.getString("session_id")))
                .withBrowserId(UuidAdapter.getUUIDFromBytes(rs.getBytes("browser_id")))
                .withAssessmentId(rs.getString("assessment_id"))
                .withAssessmentKey(rs.getString("assessment_key"))
                .withAssessmentWindowId(rs.getString("assessment_window_id"))
                .withAssessmentAlgorithm(rs.getString("assessment_algorithm"))
                .withEnvironment(rs.getString("environment"))
                .withStudentId(rs.getLong("student_id"))
                .withLoginSSID(rs.getString("login_ssid"))
                .withStudentName(rs.getString("student_name"))
                .withLanguageCode(rs.getString("language_code"))
                .withMaxItems(rs.getInt("max_items"))
                .withAttempts(rs.getInt("attempts"))
                .withClientName(rs.getString("client_name"))
                .withSubject(rs.getString("subject"))
                .withStartedAt(mapTimestampToJodaInstant(rs, "started_at"))
                .withChangedAt(mapTimestampToJodaInstant(rs, "changed_at"))
                .withDeletedAt(mapTimestampToJodaInstant(rs, "deleted_at"))
                .withScoredAt(mapTimestampToJodaInstant(rs, "scored_at"))
                .withCompletedAt(mapTimestampToJodaInstant(rs, "completed_at"))
                .withCreatedAt(mapTimestampToJodaInstant(rs, "created_at"))
                .withJoinedAt(mapTimestampToJodaInstant(rs, "joined_at"))
                .withExpiresAt(mapTimestampToJodaInstant(rs, "expires_at"))
                .withStatus(new ExamStatusCode(
                    rs.getString("status"),
                    ExamStatusStage.fromType(rs.getString("stage"))
                ), mapTimestampToJodaInstant(rs, "status_changed_at"))
                .withStatusChangeReason(rs.getString("status_change_reason"))
                .withAbnormalStarts(rs.getInt("abnormal_starts"))
                .withWaitingForSegmentApprovalPosition(rs.getInt("waiting_for_segment_approval_position"))
                .withCurrentSegmentPosition(rs.getInt("current_segment_position"))
                .withCustomAccommodation(rs.getBoolean("custom_accommodations"))
                .withBrowserUserAgent(rs.getString("browser_user_agent"))
                .withRestartsAndResumptions(rs.getInt("restarts_and_resumptions"))
                .withResumptions(rs.getInt("resumptions"))
                .withMultiStageBraille(rs.getBoolean("msb"))
                .build();
        }
    }
}
