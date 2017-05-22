package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.models.ItemGroupHistory;
import tds.exam.repositories.HistoryQueryRepository;

/**
 * Class for retrieving data from the exam history table.
 */
@Repository
public class HistoryQueryRepositoryImpl implements HistoryQueryRepository {
    private final static ItemGroupHistoryResultExtractor itemGroupHistoryResultExtractor = new ItemGroupHistoryResultExtractor();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public HistoryQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Optional<Double> findAbilityFromHistoryForSubjectAndStudent(final String clientName, final String subject, final Long studentId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientName", clientName);
        parameters.put("subject", subject);
        parameters.put("studentId", studentId);

        final String SQL =
                "SELECT\n" +
                    "MAX(initial_ability) \n" +
                "FROM \n" +
                    "history \n" +
                "WHERE \n" +
                    "client_name = :clientName AND \n" +
                    "student_id = :studentId AND \n" +
                    "subject = :subject AND \n" +
                    "initial_ability IS NOT NULL;";

        Optional<Double> maybeAbility;
        try {
            maybeAbility = Optional.ofNullable(jdbcTemplate.queryForObject(SQL, parameters, Double.class));
        } catch (EmptyResultDataAccessException e) {
            maybeAbility = Optional.empty();
        }

        return maybeAbility;
    }

    @Override
    public List<ItemGroupHistory> findPreviousItemGroups(final long studentId, final UUID excludedExamId, final String assessmentId) {
        SqlParameterSource parameters = new MapSqlParameterSource("examId", excludedExamId)
            .addValue("studentId", studentId)
            .addValue("assessmentId", assessmentId);

        final String SQL = "SELECT e.id as examId, item.group_id as groupId FROM exam_item\n" +
            "JOIN exam e\n" +
            "JOIN ( \n" +
            "  SELECT\n" +
            "    exam_id,\n" +
            "    MAX(id) AS id \n" +
            "  FROM exam.exam_event\n" +
            "  WHERE exam_id <> :examId \n" +
            "  GROUP BY exam_id \n" +
            ") last_event \n" +
            "ON e.id = last_event.exam_id \n" +
            "JOIN exam.exam_event ee \n" +
            "  ON last_event.exam_id = ee.exam_id \n" +
            "  AND last_event.id = ee.id\n" +
            "JOIN exam_segment segment ON e.id = segment.exam_id\n" +
            "JOIN exam_page page on segment.segment_key = page.segment_key\n" +
            "JOIN ( \n" +
            "  SELECT \n" +
            "    exam_page_id, \n" +
            "    MAX(id) AS id\n" +
            "  FROM exam_page_event \n" +
            "  WHERE exam_id <> :examId \n" +
            "  GROUP BY exam_page_id \n" +
            ") last_page_event \n" +
            "    ON page.id = last_page_event.exam_page_id \n" +
            "JOIN \n" +
            "  exam_page_event page_event \n" +
            "  ON page_event.id = last_page_event.id \n" +
            "JOIN exam_item item\n" +
            "  ON item.exam_page_id = page.id\n" +
            "WHERE\n" +
            "  e.id <> :examId \n" +
            "  AND page_event.deleted_at IS NULL\n" +
            "  AND student_id = :studentId \n" +
            "  AND assessment_id = :assessmentId \n" +
            "ORDER BY \n" +
            "  ee.started_at;";

        return jdbcTemplate.query(SQL, parameters, itemGroupHistoryResultExtractor);
    }

    private static class ItemGroupHistoryResultExtractor implements ResultSetExtractor<List<ItemGroupHistory>> {
        @Override
        public List<ItemGroupHistory> extractData(final ResultSet resultSet) throws SQLException, DataAccessException {
            UUID examId = null;
            Set<String> itemGroups = new HashSet<>();
            List<ItemGroupHistory> itemGroupHistories = new ArrayList<>();

            while(resultSet.next()) {
                UUID currentExamId = UUID.fromString(resultSet.getString("examId"));

                if(examId != null && !examId.equals(currentExamId)) {
                    itemGroupHistories.add(new ItemGroupHistory(examId, itemGroups));
                    examId = currentExamId;
                    itemGroups = new HashSet<>();
                }

                itemGroups.add(resultSet.getString("groupId"));
            }

            //Add the last item group history
            if(!itemGroups.isEmpty()) {
                itemGroupHistories.add(new ItemGroupHistory(examId, itemGroups));
            }

            return itemGroupHistories;
        }
    }
}
