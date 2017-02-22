package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.repositories.ExamItemQueryRepository;

@Repository
public class ExamItemQueryRepositoryImpl implements ExamItemQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamItemQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public int getCurrentExamItemPosition(final UUID examId) {
        final SqlParameterSource params = new MapSqlParameterSource("examId", examId.toString());
        final String SQL =
            "SELECT \n" +
                "   MAX(I.position)\n" +
                "FROM \n" +
                "   exam_item I\n" +
                "JOIN \n" +
                "   exam_item_response IR \n" +
                "ON \n" +
                "   IR.exam_item_id = I.id\n" +
                "JOIN \n" +
                "   exam_page P \n" +
                "ON \n" +
                "   P.id = I.exam_page_id\n" +
                "JOIN\n" +
                "   exam_page_event PE\n" +
                "ON \n" +
                "   P.id = PE.exam_page_id\n" +
                "WHERE\n" +
                "   P.exam_id = :examId AND\n" +
                "   PE.deleted_at IS NULL;\n";

        return jdbcTemplate.queryForObject(SQL, params, Integer.class);
    }

    @Override
    public Map<UUID, Integer> getResponseCounts(final UUID... examIds) {
        // Needs to toString() each UUID individually
        final SqlParameterSource params = new MapSqlParameterSource("examIds",
            Arrays.stream(examIds).map(UUID::toString).collect(Collectors.toSet()));
        final String SQL =
            "SELECT \n" +
                "   exam_id, \n" +
                "   COUNT(id) as response_count \n" +
                "FROM ( \n" +
                "   SELECT\n" +
                "       P.exam_id, \n" +
                "       I.id\n" +
                "   FROM \n" +
                "       exam.exam_item I \n" +
                "   JOIN \n" +
                "       exam.exam_item_response IR \n" +
                "   ON \n" +
                "       I.id = IR.exam_item_id \n" +
                "   JOIN \n" +
                "       exam.exam_page P \n" +
                "   ON \n" +
                "       P.id = I.exam_page_id \n" +
                "   JOIN \n" +
                "       exam.exam_page_event PE \n" +
                "   ON \n" +
                "       PE.exam_page_id = P.id \n" +
                "   WHERE \n" +
                "       PE.deleted_at IS NULL \n" +
                "       AND P.exam_id IN (:examIds) \n" +
                "   GROUP BY P.exam_id, I.id \n" +
                ") examIdCounts \n" +
                "GROUP BY exam_id";

        return jdbcTemplate.query(SQL, params, (ResultSetExtractor<Map<UUID, Integer>>) rs -> {
            HashMap<UUID, Integer> examIdResponseCounts = new HashMap<>();
            while(rs.next()){
                examIdResponseCounts.put(UUID.fromString(rs.getString("exam_id")), rs.getInt("response_count"));
            }
            return examIdResponseCounts;
        });
    }
}
