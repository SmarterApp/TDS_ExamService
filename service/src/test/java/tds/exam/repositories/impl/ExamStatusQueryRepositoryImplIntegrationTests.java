package tds.exam.repositories.impl;

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

import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.repositories.ExamStatusQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamStatusQueryRepositoryImplIntegrationTests {
    private ExamStatusQueryRepository examStatusQueryRepository;

    @Autowired
    @Qualifier("queryJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        examStatusQueryRepository = new ExamStatusQueryRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldFindStatus() {
        //Statuses are current loaded via a migration script V1473962717__exam_create_status_codes_table.sql
        ExamStatusCode code = examStatusQueryRepository.findExamStatusCode("started");

        assertThat(code.getStatus()).isEqualTo("started");
        assertThat(code.getStage()).isEqualTo(ExamStatusStage.INUSE);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldThrowIfStatusNotFound() {
        examStatusQueryRepository.findExamStatusCode("bogus");
    }
}
