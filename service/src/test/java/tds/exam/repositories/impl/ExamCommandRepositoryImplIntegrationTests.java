package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamCommandRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamCommandRepository examCommandRepository;
    private ExamQueryRepository examQueryRepository;

    @Before
    public void setUp() {
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        examQueryRepository = new ExamQueryRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldInsertExam() {
        Instant now = Instant.now();
        Exam exam = new ExamBuilder()
            .withDateJoined(now)
            .withAbnormalStarts(5)
            .build();
        assertThat(examQueryRepository.getExamById(exam.getId())).isNotPresent();

        examCommandRepository.insert(exam);

        Optional<Exam> maybeExam = examQueryRepository.getExamById(exam.getId());
        assertThat(maybeExam).isPresent();

        Exam savedExam = maybeExam.get();

        assertThat(savedExam.getSubject()).isEqualTo(exam.getSubject());
        assertThat(savedExam.isSegmented()).isEqualTo(exam.isSegmented());
        assertThat(savedExam.getStatus()).isEqualTo(exam.getStatus());
        assertThat(savedExam.getId()).isEqualByComparingTo(exam.getId());
        assertThat(savedExam.getSessionId()).isEqualTo(exam.getSessionId());
        assertThat(savedExam.getEnvironment()).isEqualTo(exam.getEnvironment());
        assertThat(savedExam.getClientName()).isEqualTo(exam.getClientName());
        assertThat(savedExam.getAssessmentAlgorithm()).isEqualTo(exam.getAssessmentAlgorithm());
        assertThat(savedExam.getAssessmentId()).isEqualTo(exam.getAssessmentId());
        assertThat(savedExam.getAssessmentKey()).isEqualTo(exam.getAssessmentKey());
        assertThat(savedExam.getAssessmentWindowId()).isEqualTo(exam.getAssessmentWindowId());
        assertThat(savedExam.getDateJoined()).isEqualTo(now);
        assertThat(savedExam.getDateChanged()).isEqualTo(exam.getDateChanged());
        assertThat(savedExam.getDateStarted()).isEqualTo(exam.getDateStarted());
        assertThat(savedExam.getDateScored()).isEqualTo(exam.getDateScored());
        assertThat(savedExam.getDateCompleted()).isEqualTo(exam.getDateCompleted());
        assertThat(savedExam.getDateDeleted()).isEqualTo(exam.getDateDeleted());
        assertThat(savedExam.getBrowserId()).isEqualTo(exam.getBrowserId());
        assertThat(savedExam.getLoginSSID()).isEqualTo(exam.getLoginSSID());
        assertThat(savedExam.getStatusChangeReason()).isEqualTo(exam.getStatusChangeReason());
        assertThat(savedExam.getAbnormalStarts()).isEqualTo(5);
        assertThat(savedExam.isWaitingForSegmentApproval()).isEqualTo(exam.isWaitingForSegmentApproval());
        assertThat(savedExam.getCurrentSegmentPosition()).isEqualTo(exam.getCurrentSegmentPosition());
    }

    @Test
    public void shouldUpdateAnExam() {
        Exam mockExam = new ExamBuilder().build();
        assertThat(examQueryRepository.getExamById(mockExam.getId())).isNotPresent();

        examCommandRepository.insert(mockExam);

        Optional<Exam> maybeExam = examQueryRepository.getExamById(mockExam.getId());
        assertThat(maybeExam).isPresent();

        Exam insertedExam = maybeExam.get();

        ExamStatusCode pausedStatus = new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE);
        Instant pausedStatusDate = Instant.now();
        Exam examWithChanges = new Exam.Builder()
            .fromExam(insertedExam)
            .withStatus(pausedStatus, pausedStatusDate)
            .build();

        examCommandRepository.update(examWithChanges);

        Optional<Exam> maybeUpdatedExam = examQueryRepository.getExamById(examWithChanges.getId());
        assertThat(maybeUpdatedExam).isPresent();

        Exam updatedExam = maybeUpdatedExam.get();
        assertThat(updatedExam.getStatus()).isEqualTo(pausedStatus);
        assertThat(updatedExam.getStatusChangeDate()).isEqualTo(pausedStatusDate);
    }
}
