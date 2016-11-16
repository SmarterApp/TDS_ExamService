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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.data.mysql.UuidAdapter;
import tds.exam.Exam;
import tds.exam.builder.ExamBuilder;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamQueryRepositoryImplIntegrationTests {
    private ExamQueryRepository examQueryRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        examQueryRepository = new ExamQueryRepositoryImpl(jdbcTemplate);
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
            .withId(UUID.fromString("af880054-d1d2-4c24-805c-1f0dfdb45980"))
            .withBrowserId(UUID.fromString("3C7254E4-34E1-417F-BC58-CFFC1E8D8006"))
            .withAssessmentId("assessmentId3")
            .withStudentId(9999L)
            .withAttempts(2)
            .withDateScored(Instant.now().minus(Minutes.minutes(5).toStandardDuration()))
            .build());

        exams.forEach(this::insertExamData);

        insertExamScoresData();
    }

    @Test
    public void shouldRetrieveExamForUniqueKey() {
        UUID examUniqueKey = UUID.fromString("af880054-d1d2-4c24-805c-0dfdb45a0d24");
        Optional<Exam> examOptional = examQueryRepository.getExamById(examUniqueKey);
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
    public void shouldReturnSingleAbility() {
        List<Ability> oneAbility = examQueryRepository.findAbilities(UUID.fromString("af880054-d1d2-4c24-805c-1f0dfdb45989"),
                "clientName", "ELA", 9999L);
        assertThat(oneAbility).hasSize(1);
        Ability myAbility = oneAbility.get(0);
        // Should not be the same exam
        assertThat(myAbility.getExamId()).isNotEqualTo(UUID.fromString("af880054-d1d2-4c24-805c-1f0dfdb45989"));
        assertThat(myAbility.getAssessmentId()).isEqualTo("assessmentId3");
        assertThat(myAbility.getAttempts()).isEqualTo(2);
        assertThat(myAbility.getDateScored()).isLessThan(java.time.Instant.now());
    }

    private void insertExamData(Exam exam) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(exam.getId()))
            .addValue("sessionId", UuidAdapter.getBytesFromUUID(exam.getSessionId()))
            .addValue("browserId", UuidAdapter.getBytesFromUUID(exam.getBrowserId()))
            .addValue("assessmentId", exam.getAssessmentId())
            .addValue("studentId", exam.getStudentId())
            .addValue("attempts", exam.getAttempts())
            .addValue("clientName", exam.getClientName())
            .addValue("dateDeleted", exam.getDateDeleted() == null ? null : mapJodaInstantToTimestamp(exam.getDateDeleted()))
            .addValue("dateScored", exam.getDateScored() == null ? null : mapJodaInstantToTimestamp(exam.getDateScored()))
            .addValue("dateChanged", exam.getDateChanged() == null ? null : mapJodaInstantToTimestamp(exam.getDateChanged()))
            .addValue("dateStarted", exam.getDateStarted() == null ? null : mapJodaInstantToTimestamp(exam.getDateStarted()))
            .addValue("dateCompleted", exam.getDateCompleted() == null ? null : mapJodaInstantToTimestamp(exam.getDateCompleted()))
            .addValue("status", exam.getStatus().getStatus())
            .addValue("subject", exam.getSubject());

        final String SQL =
            "INSERT INTO " +
            "   exam (exam_id, session_id, browser_id, assessment_id, student_id, attempts, client_name, date_deleted, date_scored, date_changed, date_started, date_completed, status, subject)" +
            "VALUES(:examId, :sessionId, :browserId, :assessmentId, :studentId, :attempts, :clientName, :dateDeleted, :dateScored, :dateChanged, :dateStarted, :dateCompleted, :status, :subject)";

        jdbcTemplate.update(SQL, parameters);
    }

    private void insertExamScoresData() {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(UUID.fromString("af880054-d1d2-4c24-805c-1f0dfdb45980")))
            .addValue("measureLabel", "Measure-Label")
            .addValue("value", 50)
            .addValue("measureOf", "measure-of")
            .addValue("useForAbility", 1);

        final String SQL =
            "INSERT INTO" +
            "   exam_scores (fk_scores_examid_exam, measure_label, value, measure_of, use_for_ability) " +
            "VALUES(:examId, :measureLabel, :value, :measureOf, :useForAbility)";

        jdbcTemplate.update(SQL, parameters);
    }
}
