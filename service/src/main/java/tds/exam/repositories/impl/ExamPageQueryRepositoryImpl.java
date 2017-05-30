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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamPage;
import tds.exam.repositories.ExamPageQueryRepository;

@Repository
public class ExamPageQueryRepositoryImpl implements ExamPageQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ExamPageRowMapper examPageRowMapper = new ExamPageRowMapper();

    private static final String EXAM_PAGE_STANDARD_SELECT =
        "SELECT \n" +
            "   P.id, \n" +
            "   P.page_position, \n" +
            "   P.item_group_key, \n" +
            "   P.exam_id, \n" +
            "   P.created_at, \n" +
            "   P.group_items_required, \n" +
            "   P.segment_key, \n" +
            "   PE.started_at, \n" +
            "   PE.page_duration, \n" +
            "   PE.visible \n" +
            "FROM \n" +
            "   exam_page P\n";

    private static final String JOIN_EXAM_PAGE_EVENT_GROUP_BY_EXAM_ID =
        "JOIN ( \n" +
            "   SELECT \n" +
            "       exam_page_id, \n" +
            "       MAX(id) AS id \n" +
            "   FROM \n" +
            "       exam_page_event \n" +
            "   WHERE \n" +
            "       exam_id = :examId \n" +
            "   GROUP BY exam_page_id \n" +
            ") last_event \n" +
            "   ON P.id = last_event.exam_page_id \n" +
            "JOIN exam_page_event PE \n" +
            "   ON last_event.id = PE.id \n";

    @Autowired
    public ExamPageQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<ExamPage> findAll(final UUID examId) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString());

        final String SQL = EXAM_PAGE_STANDARD_SELECT +
            JOIN_EXAM_PAGE_EVENT_GROUP_BY_EXAM_ID +
            "WHERE \n" +
            "   P.exam_id = :examId \n" +
            "   AND PE.deleted_at IS NULL \n" +
            "ORDER BY \n" +
            "   P.page_position";

        return jdbcTemplate.query(SQL, parameters, examPageRowMapper);
    }

    @Override
    public Optional<ExamPage> find(final UUID examId, final int position) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("position", position);

        final String SQL = EXAM_PAGE_STANDARD_SELECT +
            JOIN_EXAM_PAGE_EVENT_GROUP_BY_EXAM_ID +
            "WHERE \n" +
            "   P.exam_id = :examId \n" +
            "   AND P.page_position = :position \n" +
            "   AND PE.deleted_at IS NULL";

        Optional<ExamPage> maybeExamPage;
        try {
            maybeExamPage = Optional.of(jdbcTemplate.queryForObject(SQL, parameters, examPageRowMapper));
        } catch (EmptyResultDataAccessException e) {
            maybeExamPage = Optional.empty();
        }

        return maybeExamPage;
    }

    @Override
    public Optional<ExamPage> find(final UUID pageId) {
        final SqlParameterSource parameters = new MapSqlParameterSource("id", pageId.toString());

        final String SQL =
            EXAM_PAGE_STANDARD_SELECT +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_page_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam_page_event \n" +
                "   WHERE \n" +
                "       exam_page_id = :id \n" +
                "   GROUP BY exam_page_id \n" +
                ") last_event \n" +
                "   ON P.id = last_event.exam_page_id \n" +
                "JOIN exam_page_event PE \n" +
                "   ON last_event.id = PE.id \n" +
                " WHERE \n" +
                "   P.id = :id \n" +
                "   AND PE.deleted_at IS NULL";

        Optional<ExamPage> maybeExamPage;
        try {
            maybeExamPage = Optional.of(jdbcTemplate.queryForObject(SQL, parameters, examPageRowMapper));
        } catch (EmptyResultDataAccessException e) {
            maybeExamPage = Optional.empty();
        }

        return maybeExamPage;
    }

    private static class ExamPageRowMapper implements RowMapper<ExamPage> {
        @Override
        public ExamPage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExamPage.Builder()
                .withId(UUID.fromString(rs.getString("id")))
                .withPagePosition(rs.getInt("page_position"))
                .withSegmentKey(rs.getString("segment_key"))
                .withItemGroupKey(rs.getString("item_group_key"))
                .withExamId(UUID.fromString(rs.getString("exam_id")))
                .withDuration(rs.getLong("page_duration"))
                .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "created_at"))
                .withStartedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "started_at"))
                .withVisible(rs.getBoolean("visible"))
                .build();
        }
    }
}
