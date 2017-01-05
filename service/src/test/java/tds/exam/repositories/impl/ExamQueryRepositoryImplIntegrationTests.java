package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.joda.time.Minutes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.common.data.mysql.UuidAdapter;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamQueryRepositoryImplIntegrationTests {
    private ExamQueryRepository examQueryRepository;
    private ExamCommandRepository examCommandRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private UUID currentExamId = UUID.fromString("af880054-d1d2-4c24-805c-1f0dfdb45980");
    private UUID mockSessionId = UUID.randomUUID();
    private Set<String> statusesThatCanTransitionToPaused;

    private List<Exam> examsInSession;

    @Before
    public void setUp() {
        examQueryRepository = new ExamQueryRepositoryImpl(jdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        List<Exam> exams = new ArrayList<>();
        // Build a basic exam record
        exams.add(new ExamBuilder().build());

        // Build an exam record that has been marked as deleted
        exams.add(new ExamBuilder()
            .withId(UUID.fromString("ab880054-d1d2-4c24-805c-0dfdb45a0d24"))
            .withAssessmentId("assementId2")
            .withDateDeleted(Instant.now().minus(Minutes.minutes(5).toStandardDuration()))
            .build());

        // Build an exam record that is a subsequent attempt of an exam
        exams.add(new ExamBuilder()
            .withId(currentExamId)
            .withBrowserId(UUID.fromString("3C7254E4-34E1-417F-BC58-CFFC1E8D8006"))
            .withAssessmentId("assessmentId3")
            .withStudentId(9999L)
            .withAttempts(2)
            .withDateScored(Instant.now().minus(Minutes.minutes(5).toStandardDuration()))
            .build());

        exams.forEach(exam -> examCommandRepository.insert(exam));

        insertExamScoresData();

        // Build exams that belong to the same session
        examsInSession = new ArrayList<>();
        examsInSession.add(new ExamBuilder().withSessionId(mockSessionId)
            .withStudentId(5L)
            .withAssessmentId("assessmentId5")
            .build());
        examsInSession.add(new ExamBuilder().withSessionId(mockSessionId)
            .withStudentId(6L)
            .withAssessmentId("assessmentId5")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.INACTIVE), Instant.now())
            .withStudentId(7L)
            .withAssessmentId("assessmentId5")
            .build());
        examsInSession.add(new ExamBuilder().withSessionId(mockSessionId)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.INACTIVE), Instant.now())
            .withStudentId(8L)
            .withAssessmentId("assessmentId6")
            .build());
        examsInSession.add(new ExamBuilder().withSessionId(mockSessionId)
            .withStudentId(9L)
            .withAssessmentId("assessmentId6")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_FAILED, ExamStatusStage.INACTIVE), Instant.now())
            .build());

        examsInSession.forEach(exam -> examCommandRepository.insert(exam));

        statusesThatCanTransitionToPaused = new HashSet<>(Arrays.asList(ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_INITIALIZING));
    }

    @Test
    public void shouldRetrieveExamForUniqueKey() {
        Optional<Exam> examOptional = examQueryRepository.getExamById(currentExamId);
        assertThat(examOptional.isPresent()).isTrue();
    }

    @Test
    public void shouldReturnEmptyOptionalForNotFoundUniqueKey() {
        UUID examUniqueKey = UUID.fromString("12345678-d1d2-4c24-805c-0dfdb45a0d24");
        Optional<Exam> sessionOptional = examQueryRepository.getExamById(examUniqueKey);
        assertThat(sessionOptional.isPresent()).isFalse();
    }

    @Test
    public void shouldRetrieveLatestExam() {
        Optional<Exam> examOptional = examQueryRepository.getLastAvailableExam(1L, "assessmentId", "clientName");
        assertThat(examOptional.isPresent()).isTrue();
        Exam exam = examOptional.get();
        assertThat(exam.getId()).isNotNull();
        assertThat(exam.getStatus()).isNotNull();
    }

    @Test
    public void shouldNotReturnLatestExamWithNonNullDeletedDate() {
        Optional<Exam> examOptional = examQueryRepository.getLastAvailableExam(1L, "assessmentId2", "clientName");
        assertThat(examOptional.isPresent()).isFalse();
    }

    @Test
    public void shouldReturnEmptyListOfAbilities() {
        List<Ability> noAbilities = examQueryRepository.findAbilities(UUID.fromString("12345678-d1d2-4c24-805c-0dfdb45a0999"),
            "otherclient", "ELA", 9999L);
        assertThat(noAbilities).isEmpty();
    }

    @Test
    public void shouldReturnLastPausedDate() {
        Instant datePaused = Instant.now().minus(99999);
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), datePaused)
            .withDateChanged(datePaused)
            .build();
        examCommandRepository.insert(exam);

        Exam approvedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.IN_PROGRESS), Instant.now().minus(5000))
            .build();

        examCommandRepository.update(approvedExam);

        Optional<Instant> maybeLastTimePaused = examQueryRepository.findLastStudentActivity(exam.getId());
        assertThat(maybeLastTimePaused).isPresent();
        assertThat(maybeLastTimePaused.get()).isEqualTo(datePaused);
    }

    @Test
    public void shouldReturnLastResponseDate() {
        Instant datePaused = Instant.now().minus(50000);
        Instant datePageCreated = Instant.now().minus(70000);
        Instant dateLastResponseSubmitted = Instant.now().minus(20000);
        Instant dateEarlierResponseSubmitted = Instant.now().minus(30000);

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), datePaused)
            .withDateChanged(datePaused)
            .build();
        examCommandRepository.insert(exam);

        insertTestDataForResponses(datePageCreated, dateLastResponseSubmitted, dateEarlierResponseSubmitted, exam);

        Optional<Instant> maybeLastTimeStudentResponded = examQueryRepository.findLastStudentActivity(exam.getId());
        assertThat(maybeLastTimeStudentResponded).isPresent();
        assertThat(maybeLastTimeStudentResponded.get()).isEqualTo(dateLastResponseSubmitted);
    }

    @Test
    public void shouldReturnLastPageCreated() {
        Instant datePaused = Instant.now().minus(50000);
        Instant datePageCreated = Instant.now().minus(20000);
        Instant dateLastResponseSubmitted = Instant.now().minus(40000);
        Instant dateEarlierResponseSubmitted = Instant.now().minus(30000);

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), datePaused)
            .withDateChanged(datePaused)
            .build();
        examCommandRepository.insert(exam);

        insertTestDataForResponses(datePageCreated, dateLastResponseSubmitted, dateEarlierResponseSubmitted, exam);

        Optional<Instant> maybeLastTimeStudentResponded = examQueryRepository.findLastStudentActivity(exam.getId());
        assertThat(maybeLastTimeStudentResponded).isPresent();
        assertThat(maybeLastTimeStudentResponded.get()).isEqualTo(datePageCreated);
    }

    private void insertTestDataForResponses(Instant datePageCreated, Instant dateLastResponseSubmitted, Instant dateEarlierResponseSubmitted, Exam exam) {
        MapSqlParameterSource testParams = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(exam.getId()))
            .addValue("datePageCreated", new Timestamp(datePageCreated.getMillis()))
            .addValue("dateLastResponseSubmitted", new Timestamp(dateLastResponseSubmitted.getMillis()))
            .addValue("dateEarlierResponseSubmitted", new Timestamp(dateEarlierResponseSubmitted.getMillis()));

        final String insertPageSQL =
            "INSERT INTO exam_page (id, page_position, item_group_key, exam_id, created_at) " +
            "VALUES (805, 1, 'GroupKey1', :examId, :datePageCreated)";
        final String insertPageEventSQL =
            "INSERT INTO exam_page_event (exam_page_id, started_at) VALUES (805, now())";
        final String insertItemSQL =
            "INSERT INTO exam_item (id, item_key, exam_page_id, position, type, is_fieldtest, segment_id, is_required)" +
            "VALUES (2112, 'item-1', 805, 1, 'MI', 0, 'seg-id', 0)";
        final String insertResponsesSQL =
            "INSERT INTO exam_item_response (id, exam_item_id, response, created_at) " +
            "VALUES (1337, 2112, 'Response 1', :dateLastResponseSubmitted), (1338, 2112, 'Response 2', :dateEarlierResponseSubmitted)";

        jdbcTemplate.update(insertPageSQL, testParams);
        jdbcTemplate.update(insertPageEventSQL, testParams);
        jdbcTemplate.update(insertItemSQL, testParams);
        jdbcTemplate.update(insertResponsesSQL, testParams);
    }

    @Test
    public void shouldReturnSingleAbility() {
        UUID examId = UUID.randomUUID();
        List<Ability> oneAbility = examQueryRepository.findAbilities(examId,
            "clientName", "ELA", 9999L);
        assertThat(oneAbility).hasSize(1);
        Ability myAbility = oneAbility.get(0);
        // Should not be the same exam
        assertThat(myAbility.getExamId()).isNotEqualTo(examId);
        assertThat(myAbility.getAssessmentId()).isEqualTo("assessmentId3");
        assertThat(myAbility.getAttempts()).isEqualTo(2);
        assertThat(myAbility.getDateScored()).isLessThan(java.time.Instant.now());
    }

    @Test
    public void shouldGetAllExamsInASession() {
        List<Exam> exams = examQueryRepository.findAllExamsInSessionWithStatus(mockSessionId, statusesThatCanTransitionToPaused);

        assertThat(exams).hasSize(3);
        assertThat(exams).doesNotContain(examsInSession.stream().filter(exam -> exam.getStatus().getCode().equals(ExamStatusCode.STATUS_FAILED)).findAny().get());
    }

    @Test
    public void shouldReturnAnEmptyListWhenFindingAllExamsForASessionIdThatDoesNotExist() {
        List<Exam> exams = examQueryRepository.findAllExamsInSessionWithStatus(UUID.randomUUID(), statusesThatCanTransitionToPaused);

        assertThat(exams).isEmpty();
    }

    private void insertExamScoresData() {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(currentExamId))
            .addValue("measureLabel", "Measure-Label")
            .addValue("value", 50)
            .addValue("measureOf", "measure-of")
            .addValue("useForAbility", 1);

        final String SQL =
            "INSERT INTO" +
                "   exam_scores (exam_id, measure_label, value, measure_of, use_for_ability) " +
                "VALUES(:examId, :measureLabel, :value, :measureOf, :useForAbility)";

        jdbcTemplate.update(SQL, parameters);
    }
}
