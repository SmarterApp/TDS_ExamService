package tds.exam.repositories.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@SqlConfig(dataSource = "queryDataSource")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:integration/sql/ExamQueryRepositoryIntegrationTest/retrieveExamBefore.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:integration/sql/ExamQueryRepositoryIntegrationTest/retrieveExamAfter.sql")
public class ExamQueryRepositoryImplIntegrationTests {
    @Autowired
    private ExamQueryRepository examQueryRepository;

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
        Optional<Exam> examOptional = examQueryRepository.getLastAvailableExam(1, "assessmentId", "clientName");
        assertThat(examOptional.isPresent()).isTrue();
        Exam exam = examOptional.get();
        assertThat(exam.getId()).isNotNull();
        assertThat(exam.getStatus()).isNotNull();
    }

    @Test
    public void shouldNotReturnLatestExamWithNonNullDeletedDate() {
        Optional<Exam> examOptional = examQueryRepository.getLastAvailableExam(1, "assessmentId2", "clientName");
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
        assertThat(myAbility.getDateScored()).isLessThan(Instant.now());

    }
}
