package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.models.ExamPage;
import tds.exam.repositories.ExamPageQueryRepository;

@Repository
public class ExamPageQueryRepositoryImpl implements ExamPageQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final static ExamPageRowMapper examPageRowMapper = new ExamPageRowMapper();

    @Autowired
    public ExamPageQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<ExamPage> findAll(UUID examId) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examId));

        final String SQL =
            "SELECT \n" +
                "   P.id, \n" +
                "   P.page_position, \n" +
                "   P.item_group_key, \n" +
                "   P.exam_id, \n" +
                "   P.created_at, \n" +
                "   PE.started_at \n" +
                "FROM \n" +
                "   exam_page P\n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_page_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam_page_event \n" +
                "   GROUP BY exam_page_id \n" +
                ") last_event \n" +
                "   ON P.id = last_event.exam_page_id \n" +
                "JOIN exam_page_event PE \n" +
                "   ON last_event.id = PE.id \n" +
                "WHERE \n" +
                "   P.exam_id = :examId AND\n" +
                "   PE.deleted_at IS NULL \n" +
                "ORDER BY\n" +
                "   P.page_position";

        return jdbcTemplate.query(SQL, parameters, examPageRowMapper);
    }

    private static class ExamPageRowMapper implements RowMapper<ExamPage> {
        @Override
        public ExamPage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExamPage.Builder()
                .withId(UUID.fromString(rs.getString("id")))
                .withPagePosition(rs.getInt("page_position"))
                .withItemGroupKey(rs.getString("item_group_key"))
                .withExamId(UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")))
                .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "created_at"))
                .withStartedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "started_at"))
                .build();
        }
    }
}
