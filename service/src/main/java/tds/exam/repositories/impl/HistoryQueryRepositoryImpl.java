package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import tds.exam.repositories.HistoryQueryRepository;

/**
 * Class for retrieving data from the exam history table.
 */
@Repository
public class HistoryQueryRepositoryImpl implements HistoryQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public HistoryQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Optional<Double> findAbilityFromHistoryForSubjectAndStudent(String clientName, String subject, Long studentId) {
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
}
