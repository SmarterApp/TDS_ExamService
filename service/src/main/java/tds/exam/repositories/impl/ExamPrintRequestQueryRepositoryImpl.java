package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.Exam;
import tds.exam.ExamPrintRequest;
import tds.exam.repositories.ExamPrintRequestQueryRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapTimestampToJodaInstant;

@Repository
public class ExamPrintRequestQueryRepositoryImpl implements ExamPrintRequestQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ExamPrintRequestRowMapper examPrintRequestRowMapper = new ExamPrintRequestRowMapper();

    @Autowired
    public ExamPrintRequestQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public Map<UUID, Integer> findRequestCountsForExamIds(final UUID sessionId, final UUID... examIds) {
        final SqlParameterSource params = new MapSqlParameterSource("sessionId", sessionId.toString())
            .addValue("examIds", Arrays.stream(examIds).map(UUID::toString).collect(Collectors.toSet()));

        final String SQL =
            "SELECT \n" +
                "   PR.exam_id, \n" +
                "   COUNT(PR.id) AS requestCount \n" +
                "FROM  \n" +
                "   exam.exam_print_request PR \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_print_request_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam.exam_print_request_event \n" +
                "   GROUP BY exam_print_request_id \n" +
                ") last_event \n" +
                "   ON PR.id = last_event.exam_print_request_id \n" +
                "JOIN exam.exam_print_request_event PRE \n" +
                "   ON last_event.exam_print_request_id = PRE.exam_print_request_id \n" +
                "   AND last_event.id = PRE.id \n" +
                "WHERE \n" +
                "   exam_id IN (:examIds) \n" +
                "   AND approved_at IS NULL \n" +
                "   AND denied_at IS NULL \n" +
                "   AND session_id = :sessionId \n" +
                "GROUP BY exam_id";

        return jdbcTemplate.query(SQL, params, (ResultSetExtractor<Map<UUID, Integer>>) rs -> {
            HashMap<UUID, Integer> examIdResponseCounts = new HashMap<>();
            while (rs.next()) {
                examIdResponseCounts.put(UUID.fromString(rs.getString("exam_id")), rs.getInt("requestCount"));
            }
            return examIdResponseCounts;
        });
    }

    @Override
    public List<ExamPrintRequest> findUnfulfilledRequests(final UUID examId, final UUID sessionId) {
        final SqlParameterSource params = new MapSqlParameterSource("examId", examId.toString())
            .addValue("sessionId", sessionId.toString());

        final String SQL =
            "SELECT \n" +
                "   PR.id, \n" +
                "   PR.exam_id, \n" +
                "   PR.session_id, \n" +
                "   PR.type, \n" +
                "   PR.value, \n" +
                "   PR.item_position, \n" +
                "   PR.page_position, \n" +
                "   PR.parameters, \n" +
                "   PR.description, \n" +
                "   PR.created_at, \n" +
                "   PRE.approved_at, \n" +
                "   PRE.denied_at, \n" +
                "   PRE.reason_denied \n" +
                "FROM  \n" +
                "   exam.exam_print_request PR \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_print_request_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam.exam_print_request_event \n" +
                "   GROUP BY exam_print_request_id \n" +
                ") last_event \n" +
                "   ON PR.id = last_event.exam_print_request_id \n" +
                "JOIN exam.exam_print_request_event PRE \n" +
                "   ON last_event.exam_print_request_id = PRE.exam_print_request_id \n" +
                "   AND last_event.id = PRE.id \n" +
                "WHERE \n" +
                "   exam_id = :examId \n" +
                "   AND approved_at IS NULL \n" +
                "   AND denied_at IS NULL \n" +
                "   AND session_id = :sessionId \n" +
                "ORDER BY PR.created_at";

        return jdbcTemplate.query(SQL, params, examPrintRequestRowMapper);
    }

    @Override
    public Optional<ExamPrintRequest> findExamPrintRequest(final UUID id) {
        final SqlParameterSource params = new MapSqlParameterSource("id", id.toString());
        final String SQL =
            "SELECT \n" +
                "   PR.id, \n" +
                "   PR.exam_id, \n" +
                "   PR.session_id, \n" +
                "   PR.type, \n" +
                "   PR.value, \n" +
                "   PR.item_position, \n" +
                "   PR.page_position, \n" +
                "   PR.parameters, \n" +
                "   PR.description, \n" +
                "   PR.created_at, \n" +
                "   PRE.approved_at, \n" +
                "   PRE.denied_at, \n" +
                "   PRE.reason_denied \n" +
                "FROM  \n" +
                "   exam.exam_print_request PR \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_print_request_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam.exam_print_request_event \n" +
                "   GROUP BY exam_print_request_id \n" +
                ") last_event \n" +
                "   ON PR.id = last_event.exam_print_request_id \n" +
                "JOIN exam.exam_print_request_event PRE \n" +
                "   ON last_event.exam_print_request_id = PRE.exam_print_request_id \n" +
                "   AND last_event.id = PRE.id \n" +
                "WHERE \n" +
                "   PR.id = :id";

        Optional<ExamPrintRequest> maybeExamPrintRequest;

        try {
            maybeExamPrintRequest = Optional.of(jdbcTemplate.queryForObject(SQL, params, examPrintRequestRowMapper));
        } catch (EmptyResultDataAccessException e) {
            maybeExamPrintRequest = Optional.empty();
        }

        return maybeExamPrintRequest;
    }

    @Override
    public List<ExamPrintRequest> findApprovedRequests(final UUID sessionId) {
        final SqlParameterSource params = new MapSqlParameterSource("sessionId", sessionId.toString());

        final String SQL =
            "SELECT \n" +
                "   PR.id, \n" +
                "   PR.exam_id, \n" +
                "   PR.session_id, \n" +
                "   PR.type, \n" +
                "   PR.value, \n" +
                "   PR.item_position, \n" +
                "   PR.page_position, \n" +
                "   PR.parameters, \n" +
                "   PR.description, \n" +
                "   PR.created_at, \n" +
                "   PRE.approved_at, \n" +
                "   PRE.denied_at, \n" +
                "   PRE.reason_denied \n" +
                "FROM  \n" +
                "   exam.exam_print_request PR \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_print_request_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam.exam_print_request_event \n" +
                "   GROUP BY exam_print_request_id \n" +
                ") last_event \n" +
                "   ON PR.id = last_event.exam_print_request_id \n" +
                "JOIN exam.exam_print_request_event PRE \n" +
                "   ON last_event.exam_print_request_id = PRE.exam_print_request_id \n" +
                "   AND last_event.id = PRE.id \n" +
                "WHERE \n" +
                "   approved_at IS NOT NULL \n" +
                "   AND denied_at IS NULL \n" +
                "   AND session_id = :sessionId \n" +
                "ORDER BY PR.exam_id, PR.created_at";

        return jdbcTemplate.query(SQL, params, examPrintRequestRowMapper);
    }

    private static class ExamPrintRequestRowMapper implements RowMapper<ExamPrintRequest> {
        @Override
        public ExamPrintRequest mapRow(final ResultSet rs, final int i) throws SQLException {
            return new ExamPrintRequest.Builder(UUID.fromString(rs.getString("id")))
                .withExamId(UUID.fromString(rs.getString("exam_id")))
                .withSessionId(UUID.fromString(rs.getString("session_id")))
                .withType(rs.getString("type"))
                .withValue(rs.getString("value"))
                .withItemPosition(rs.getInt("item_position"))
                .withPagePosition(rs.getInt("page_position"))
                .withParameters(rs.getString("parameters"))
                .withDescription(rs.getString("description"))
                .withCreatedAt(mapTimestampToJodaInstant(rs, "created_at"))
                .withApprovedAt(mapTimestampToJodaInstant(rs, "approved_at"))
                .withDeniedAt(mapTimestampToJodaInstant(rs, "denied_at"))
                .withReasonDenied(rs.getString("reason_denied"))
                .build();
        }
    }
}
