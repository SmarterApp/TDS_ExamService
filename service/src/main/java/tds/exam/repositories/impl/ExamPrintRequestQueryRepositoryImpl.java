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

import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.repositories.ExamPrintRequestQueryRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapTimestampToJodaInstant;

@Repository
public class ExamPrintRequestQueryRepositoryImpl implements ExamPrintRequestQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ExamPrintRequestRowMapper examPrintRequestRowMapper = new ExamPrintRequestRowMapper();

    private static final String EXAM_PRINT_REQUEST_COLUMNS =
        "SELECT \n" +
            "   PR.id AS id, \n" +
            "   PR.exam_id AS examId, \n" +
            "   PR.session_id AS sessionId, \n" +
            "   PR.type AS type, \n" +
            "   PR.value AS value, \n" +
            "   PR.item_position AS itemPosition, \n" +
            "   PR.page_position AS pagePosition, \n" +
            "   PR.parameters AS parameters,  \n" +
            "   PR.description AS description, \n" +
            "   PR.created_at AS createdAt, \n" +
            "   PRE.status AS status, \n" +
            "   PRE.created_at AS changedAt, \n" +
            "   PRE.reason_denied AS reasonDenied \n";

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
                "   PR.exam_id AS examId, \n" +
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
                "   AND status = 'SUBMITTED' \n" +
                "   AND session_id = :sessionId \n" +
                "GROUP BY exam_id";

        return jdbcTemplate.query(SQL, params, (ResultSetExtractor<Map<UUID, Integer>>) rs -> {
            HashMap<UUID, Integer> examIdResponseCounts = new HashMap<>();
            while (rs.next()) {
                examIdResponseCounts.put(UUID.fromString(rs.getString("examId")), rs.getInt("requestCount"));
            }
            return examIdResponseCounts;
        });
    }

    @Override
    public List<ExamPrintRequest> findUnfulfilledRequests(final UUID examId, final UUID sessionId) {
        final SqlParameterSource params = new MapSqlParameterSource("examId", examId.toString())
            .addValue("sessionId", sessionId.toString());

        final String SQL =
            EXAM_PRINT_REQUEST_COLUMNS +
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
                "   AND status = 'SUBMITTED' \n" +
                "   AND session_id = :sessionId \n" +
                "ORDER BY PR.created_at";

        return jdbcTemplate.query(SQL, params, examPrintRequestRowMapper);
    }

    @Override
    public Optional<ExamPrintRequest> findExamPrintRequest(final UUID id) {
        final SqlParameterSource params = new MapSqlParameterSource("id", id.toString());
        final String SQL =
            EXAM_PRINT_REQUEST_COLUMNS +
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
            EXAM_PRINT_REQUEST_COLUMNS +
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
                "   status = 'APPROVED' \n" +
                "   AND session_id = :sessionId \n" +
                "ORDER BY PR.exam_id, PR.created_at";

        return jdbcTemplate.query(SQL, params, examPrintRequestRowMapper);
    }

    private static class ExamPrintRequestRowMapper implements RowMapper<ExamPrintRequest> {
        @Override
        public ExamPrintRequest mapRow(final ResultSet rs, final int i) throws SQLException {
            return new ExamPrintRequest.Builder(UUID.fromString(rs.getString("id")))
                .withExamId(UUID.fromString(rs.getString("examId")))
                .withSessionId(UUID.fromString(rs.getString("sessionId")))
                .withType(rs.getString("type"))
                .withValue(rs.getString("value"))
                .withItemPosition(rs.getInt("itemPosition"))
                .withPagePosition(rs.getInt("pagePosition"))
                .withParameters(rs.getString("parameters"))
                .withDescription(rs.getString("description"))
                .withCreatedAt(mapTimestampToJodaInstant(rs, "createdAt"))
                .withChangedAt(mapTimestampToJodaInstant(rs, "changedAt"))
                .withStatus(ExamPrintRequestStatus.valueOf(rs.getString("status")))
                .withReasonDenied(rs.getString("reasonDenied"))
                .build();
        }
    }
}
