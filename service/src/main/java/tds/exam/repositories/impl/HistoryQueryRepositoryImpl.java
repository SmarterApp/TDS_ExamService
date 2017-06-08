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

        final String SQL =
            "SELECT \n" +
                "   exam.id AS examId, \n" +
                "   item.group_id AS groupId \n" +
                "FROM \n" +
                "   exam AS exam \n" +
                "JOIN \n" +
                "   exam_page AS page \n" +
                "   ON page.exam_id = exam.id \n" +
                "JOIN \n" +
                "   exam_page_event AS page_event \n" +
                "   ON page.id = page_event.exam_page_id \n" +
                "JOIN \n" +
                "exam_item AS item \n" +
                "    ON item.exam_page_id = page.id \n" +
                "WHERE \n" +
                "   exam.id <> :examId\n" +
                "   AND exam.student_id = :studentId \n" +
                "   AND exam.assessment_id = :assessmentId \n" +
                "   AND page_event.deleted_at IS NULL \n" +
                "GROUP BY \n" +
                "   exam.id, \n" +
                "   item.group_id";

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
