package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.UUID;

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
}
