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

import javax.sql.DataSource;

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
    @Qualifier("commandDataSource")
    private DataSource commandDataSource;

    private ExamCommandRepository examCommandRepository;
    private ExamQueryRepository examQueryRepository;

    @Before
    public void setUp() {
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(commandDataSource);
        examCommandRepository = new ExamCommandRepositoryImpl(commandDataSource);
        examQueryRepository = new ExamQueryRepositoryImpl(commandDataSource);
    }

    @Test
    public void shouldInsertExam() {
        Exam exam = new ExamBuilder().build();

        assertThat(examQueryRepository.getExamById(exam.getId())).isNotPresent();

        examCommandRepository.save(exam);

        assertThat(examQueryRepository.getExamById(exam.getId())).isPresent();
    }
}
