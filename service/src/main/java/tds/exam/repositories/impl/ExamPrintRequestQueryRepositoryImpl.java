package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.repositories.ExamPrintRequestQueryRepository;

@Repository
public class ExamPrintRequestQueryRepositoryImpl implements ExamPrintRequestQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamPrintRequestQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public Map<UUID, Integer> findRequestCountsForExamIds(final UUID sessionId, final UUID... examIds) {
        final MapSqlParameterSource params = new MapSqlParameterSource("sessionId", sessionId.toString())
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
                "       MAX(id) AS id \n"+
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
}
