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
