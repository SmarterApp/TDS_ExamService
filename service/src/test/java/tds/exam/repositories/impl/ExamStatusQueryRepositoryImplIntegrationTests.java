package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamStatusQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamStatusQueryRepositoryImplIntegrationTests {
    private ExamStatusQueryRepository examStatusQueryRepository;
    private ExamCommandRepository examCommandRepository;

    @Autowired
    @Qualifier("queryJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        examStatusQueryRepository = new ExamStatusQueryRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldFindStatus() {
        //Statuses are current loaded via a migration script V1473962717__exam_create_status_codes_table.sql
        ExamStatusCode code = examStatusQueryRepository.findExamStatusCode("started");

        assertThat(code.getCode()).isEqualTo("started");
        assertThat(code.getStage()).isEqualTo(ExamStatusStage.IN_USE);
    }

    @Test
    public void shouldFindLastTimeStatusWasPaused() {
        Instant datePaused = Instant.now();
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), datePaused)
            .build();

        examCommandRepository.insert(exam);
        assertThat(examStatusQueryRepository.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_PAUSED).get()).isEqualTo(datePaused);
        Exam examRestarted = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .build();

        examCommandRepository.update(examRestarted);
        assertThat(examStatusQueryRepository.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_PAUSED).get()).isEqualTo(datePaused);
        Instant datePausedAgain = Instant.now();
        Exam examPausedAgain = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), datePausedAgain)
            .build();

        examCommandRepository.update(examPausedAgain);
        assertThat(examStatusQueryRepository.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_PAUSED).get()).isEqualTo(datePausedAgain);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldThrowIfStatusNotFound() {
        examStatusQueryRepository.findExamStatusCode("bogus");
    }
}
