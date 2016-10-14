package tds.exam.repositories.impl;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tds.exam.repositories.HistoryQueryRepository;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for testing the {@link HistoryQueryRepositoryImpl}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@SqlConfig(dataSource = "queryDataSource")
public class HistoryQueryRepositoryImplIntegrationTest {
    @Autowired
    @Qualifier("commandDataSource")
    private DataSource dataSource;

    @Autowired
    private HistoryQueryRepository historyQueryRepository;

    private JdbcTemplate jdbcTemplate;

    private static final long STUDENT_ID1 = 1234;
    private static final long STUDENT_ID2 = 4321;
    private static final int MAX_ABILITY_VAL_FOR_STUDENT1 = 75;

    @Before
    public void initialize() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        final String SQL1 =
                "INSERT INTO history (id, client_name, student_id, subject, initial_ability, attempts, assessment_component_id, " +
                    "date_changed, admin_subject, fk_history_examid_exam, tested_grade, login_ssid, item_group_string, initial_ability_delim)\n" +
                "VALUES (X'12380054d1d24c24805c0dfdb45a0d24', 'CLIENT_TEST', " + STUDENT_ID1 + ", 'ELA', 25, 1, 'assessment-id-1', NOW(), 'admin-subject', " +
                        "X'12380054d1d24c24805c0dfdb45a0daa', '03', 'SSID1', null, null)";
        final String SQL2 =
                "INSERT INTO history (id, client_name, student_id, subject, initial_ability, attempts, assessment_component_id, " +
                        "date_changed, admin_subject, fk_history_examid_exam, tested_grade, login_ssid, item_group_string, initial_ability_delim)\n" +
                        "VALUES (X'12380054d1d24c24805c0dfdb45a0d25', 'CLIENT_TEST', " + STUDENT_ID1 + ", 'ELA', null, 1, 'assessment-id-1', NOW(), 'admin-subject', " +
                        "X'12380054d1d24c24805c0dfdb45a0daa', '03', 'SSID1', null, null)";
        final String SQL3 =
                "INSERT INTO history (id, client_name, student_id, subject, initial_ability, attempts, assessment_component_id, " +
                        "date_changed, admin_subject, fk_history_examid_exam, tested_grade, login_ssid, item_group_string, initial_ability_delim)\n" +
                        "VALUES (X'12380054d1d24c24805c0dfdb45a0d26', 'CLIENT_TEST', " + STUDENT_ID1 + ", 'ELA', " + MAX_ABILITY_VAL_FOR_STUDENT1 +", 1, " +
                        "'assessment-id-1', NOW(), 'admin-subject', X'12380054d1d24c24805c0dfdb45a0daa', '03', 'SSID1', null, null)";

        final String SQL4 =
                "INSERT INTO history (id, client_name, student_id, subject, initial_ability, attempts, assessment_component_id, " +
                        "date_changed, admin_subject, fk_history_examid_exam, tested_grade, login_ssid, item_group_string, initial_ability_delim)\n" +
                        "VALUES (X'12380054d1d24c24805c0dfdb45a0d27', 'CLIENT_TEST', " + STUDENT_ID2 + ", 'ELA', null, 1, 'assessment-id-1', NOW(), 'admin-subject', " +
                        "X'12380054d1d24c24805c0dfdb45a0daa', '03', 'SSID1', null, null)";
        jdbcTemplate.update(SQL1);
        jdbcTemplate.update(SQL2);
        jdbcTemplate.update(SQL3);
        jdbcTemplate.update(SQL4);
    }

    @After
    public void cleanUp() {
        jdbcTemplate.update("DELETE FROM history");
    }

    @Test
    public void shouldRetrieveLargestAbilityValue() {
        Optional<Double> maxAbility = historyQueryRepository.findAbilityFromHistoryForSubjectAndStudent("CLIENT_TEST", "ELA", STUDENT_ID1);
        assertThat(maxAbility.get()).isEqualTo(MAX_ABILITY_VAL_FOR_STUDENT1);
    }

    @Test
    public void shouldReturnNullForNullAbilities() {
        Optional<Double> maxAbility = historyQueryRepository.findAbilityFromHistoryForSubjectAndStudent("CLIENT_TEST", "ELA", STUDENT_ID2);
        assertThat(maxAbility).isNotPresent();
    }

    @Test
    public void shouldReturnNullForDifferentSubject() {
        Optional<Double> maxAbility = historyQueryRepository.findAbilityFromHistoryForSubjectAndStudent("CLIENT_TEST", "MATH", STUDENT_ID1);
        assertThat(maxAbility).isNotPresent();
    }
}
