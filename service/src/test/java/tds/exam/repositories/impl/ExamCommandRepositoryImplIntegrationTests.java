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
            .build();
        assertThat(examQueryRepository.getExamById(exam.getId())).isNotPresent();

        examCommandRepository.save(exam);

        Optional<Exam> maybeExam = examQueryRepository.getExamById(exam.getId());
        assertThat(maybeExam).isPresent();

        Exam savedExam = maybeExam.get();

        assertThat(savedExam.getSubject()).isEqualTo(exam.getSubject());
        assertThat(savedExam.getDateJoined()).isEqualTo(now);
        assertThat(savedExam.isSegmented()).isEqualTo(exam.isSegmented());
    }
}
