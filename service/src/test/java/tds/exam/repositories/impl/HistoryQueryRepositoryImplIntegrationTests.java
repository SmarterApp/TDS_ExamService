package tds.exam.repositories.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.models.ItemGroupHistory;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;
import tds.exam.repositories.HistoryQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for testing the {@link HistoryQueryRepositoryImpl}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class HistoryQueryRepositoryImplIntegrationTests {
    private HistoryQueryRepository historyQueryRepository;

    @Autowired
    private ExamCommandRepository examCommandRepository;

    @Autowired
    private ExamSegmentCommandRepository examSegmentCommandRepository;

    @Autowired
    private ExamPageCommandRepository examPageCommandRepository;

    @Autowired
    private ExamItemCommandRepository examItemCommandRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private static final long STUDENT_ID1 = 1234;
    private static final long STUDENT_ID2 = 4321;
    private static final int MAX_ABILITY_VAL_FOR_STUDENT1 = 75;

    @Before
    public void initialize() {
        historyQueryRepository = new HistoryQueryRepositoryImpl(jdbcTemplate);

        final String SQL1 =
                "INSERT INTO history (id, client_name, student_id, subject, initial_ability, attempts, segment_id, " +
                        "changed_at, segment_key, exam_id, tested_grade, login_ssid, item_group_string, initial_ability_delim)\n" +
                        "VALUES (X'12380054d1d24c24805c0dfdb45a0d24', 'CLIENT_TEST', " + STUDENT_ID1 + ", 'ELA', 25, 1, 'assessment-id-1', NOW(), 'admin-subject', " +
                        "X'12380054d1d24c24805c0dfdb45a0daa', '03', 'SSID1', null, null)";
        final String SQL2 =
                "INSERT INTO history (id, client_name, student_id, subject, initial_ability, attempts, segment_id, " +
                        "changed_at, segment_key, exam_id, tested_grade, login_ssid, item_group_string, initial_ability_delim)\n" +
                        "VALUES (X'12380054d1d24c24805c0dfdb45a0d25', 'CLIENT_TEST', " + STUDENT_ID1 + ", 'ELA', null, 1, 'assessment-id-1', NOW(), 'admin-subject', " +
                        "X'12380054d1d24c24805c0dfdb45a0daa', '03', 'SSID1', null, null)";
        final String SQL3 =
                "INSERT INTO history (id, client_name, student_id, subject, initial_ability, attempts, segment_id, " +
                        "changed_at, segment_key, exam_id, tested_grade, login_ssid, item_group_string, initial_ability_delim)\n" +
                        "VALUES (X'12380054d1d24c24805c0dfdb45a0d26', 'CLIENT_TEST', " + STUDENT_ID1 + ", 'ELA', " + MAX_ABILITY_VAL_FOR_STUDENT1 + ", 1, " +
                        "'assessment-id-1', NOW(), 'admin-subject', X'12380054d1d24c24805c0dfdb45a0daa', '03', 'SSID1', null, null)";
        final String SQL4 =
                "INSERT INTO history (id, client_name, student_id, subject, initial_ability, attempts, segment_id, " +
                        "changed_at, segment_key, exam_id, tested_grade, login_ssid, item_group_string, initial_ability_delim)\n" +
                        "VALUES (X'12380054d1d24c24805c0dfdb45a0d27', 'CLIENT_TEST', " + STUDENT_ID2 + ", 'ELA', null, 1, 'assessment-id-1', NOW(), 'admin-subject', " +
                        "X'12380054d1d24c24805c0dfdb45a0daa', '03', 'SSID1', null, null)";

        jdbcTemplate.update(SQL1, new HashMap<>());
        jdbcTemplate.update(SQL2, new HashMap<>());
        jdbcTemplate.update(SQL3, new HashMap<>());
        jdbcTemplate.update(SQL4, new HashMap<>());
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

    @Test
    public void shouldFindItemGroupHistories() {
        UUID examId = UUID.randomUUID();
        Exam exam = new ExamBuilder()
            .withId(examId)
            .withStudentId(1)
            .withAssessmentId("assessmentId")
            .build();

        examCommandRepository.insert(exam);

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        examSegmentCommandRepository.insert(Collections.singletonList(examSegment));

        ExamPage examPage = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .withExamId(examId)
            .build();

        examPageCommandRepository.insert(examPage);

        ExamItem examItem = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(examPage.getId())
            .withGroupId("groupId")
            .build();

        examItemCommandRepository.insert(examItem);

        List<ItemGroupHistory> histories = historyQueryRepository.findPreviousItemGroups(1, UUID.randomUUID(), "assessmentId");

        assertThat(histories).hasSize(1);
    }
}
