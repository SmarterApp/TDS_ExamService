/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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
                "JOIN exam.exam_print_request_event PRE \n" +
                "   ON PRE.exam_print_request_id = PRE.exam_print_request_id \n" +
                "   AND PRE.id = (SELECT MAX(id) FROM exam_print_request_event WHERE exam_print_request_id = PR.id) \n" +
                "WHERE \n" +
                "   PR.exam_id IN (:examIds) \n" +
                "   AND PR.session_id = :sessionId \n" +
                "   AND PRE.status = 'SUBMITTED' \n" +
                "GROUP BY exam_id;";

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
            "SELECT \n" +
                "   NULL as itemResponse, \n" +
                EXAM_PRINT_REQUEST_COLUMNS +
                "FROM  \n" +
                "   exam.exam_print_request PR \n" +
                "JOIN exam.exam_print_request_event PRE \n" +
                "   ON PRE.exam_print_request_id = PRE.exam_print_request_id \n" +
                "   AND PRE.id = (SELECT MAX(id) FROM exam_print_request_event WHERE exam_print_request_id = PR.id) \n" +
                "WHERE \n" +
                "   exam_id = :examId \n" +
                "   AND status = 'SUBMITTED' \n" +
                "   AND session_id = :sessionId \n" +
                "ORDER BY \n" +
                "   PR.id";

        return jdbcTemplate.query(SQL, params, examPrintRequestRowMapper);
    }

    @Override
    public Optional<ExamPrintRequest> findExamPrintRequest(final UUID id) {
        final SqlParameterSource params = new MapSqlParameterSource("id", id.toString());
        final String SQL =
            "SELECT \n" +
                "   IR.response AS itemResponse, \n" +
                EXAM_PRINT_REQUEST_COLUMNS +
                "FROM  \n" +
                "   exam.exam_print_request PR \n" +
                "JOIN exam_print_request_event PRE \n" +
                "   ON PRE.exam_print_request_id = PR.id \n" +
                "   AND PRE.id = (SELECT MAX(id) FROM exam_print_request_event WHERE exam_print_request_id = PR.id) \n" +
                "LEFT JOIN exam_page P \n" +
                "   ON P.exam_id = PR.exam_id \n" +
                "LEFT JOIN exam_page_event PE \n" +
                "   ON PE.exam_page_id = P.id \n" +
                "LEFT JOIN exam_item I \n" +
                "   ON I.exam_page_id = P.id \n" +
                "   AND PR.item_position = I.position \n" +
                "LEFT JOIN exam.exam_item_response IR \n" +
                "   ON IR.exam_item_id = I.id \n" +
                "WHERE \n" +
                "   PR.id = :id \n" +
                "   AND PE.deleted_at IS NULL \n" +
                "ORDER BY \n" +
                "   IR.id DESC LIMIT 1";

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
                "   NULL as itemResponse, \n" +
                EXAM_PRINT_REQUEST_COLUMNS +
                "FROM  \n" +
                "   exam.exam_print_request PR \n" +
                "JOIN exam.exam_print_request_event PRE \n" +
                "   ON PRE.exam_print_request_id = PR.id \n" +
                "   AND PRE.id = (SELECT MAX(id) \n" +
                "                 FROM exam_print_request_event \n" +
                "                 WHERE exam_print_request_id = PR.id \n" +
                "                 AND status = 'APPROVED') \n" +
                "WHERE \n" +
                "   PR.session_id = :sessionId \n" +
                "ORDER BY \n" +
                "   PR.exam_id, \n" +
                "   PR.id";

        return jdbcTemplate.query(SQL, params, examPrintRequestRowMapper);
    }

    @Override
    public int findCountOfUnfulfilledRequestsForExamAndItemPosition(final UUID examId, final int itemPosition, final int pagePosition) {
        final SqlParameterSource params = new MapSqlParameterSource("examId", examId.toString())
            .addValue("itemPosition", itemPosition)
            .addValue("pagePosition", pagePosition);

        final String SQL =
            "SELECT \n" +
                "   COUNT(PR.id) \n" +
                "FROM  \n" +
                "   exam.exam_print_request PR \n" +
                "JOIN exam.exam_print_request_event PRE \n" +
                "   ON PRE.exam_print_request_id = PR.id \n" +
                "   AND PRE.id = (SELECT MAX(id) FROM exam_print_request_event WHERE exam_print_request_id = PR.id) \n" +
                "WHERE \n" +
                "   PRE.status = 'SUBMITTED' \n" +
                "   AND PR.exam_id = :examId\n" +
                "   AND PR.item_position = :itemPosition \n" +
                "   AND PR.page_position = :pagePosition \n" +
                "ORDER BY \n" +
                "   PR.exam_id, \n" +
                "   PR.created_at";

        return jdbcTemplate.queryForObject(SQL, params, Integer.class);
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
                .withItemResponse(rs.getString("itemResponse"))
                .build();
        }
    }
}
