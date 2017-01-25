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

import java.util.Arrays;

import tds.exam.Exam;
import tds.exam.builder.ExamBuilder;
import tds.exam.models.ExamPage;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamPageRepositoryIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;
    private ExamPageCommandRepository examPageCommandRepository;
    private ExamPageQueryRepository examPageQueryRepository;
    private ExamCommandRepository examCommandRepository;

    @Before
    public void setUp() {
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examPageQueryRepository = new ExamPageQueryRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
    }

    @Test
    public void shouldMarkExamPagesAsDeleted() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);
        ExamPage examPage1 = new ExamPage.Builder()
            .withExamId(exam.getId())
            .withPagePosition(1)
            .withItemGroupKey("GroupKey1")
            .build();
        ExamPage examPage1a = new ExamPage.Builder()
            .withExamId(exam.getId())
            .withPagePosition(1)
            .withItemGroupKey("GroupKey1")
            .build();
        ExamPage examPage2 = new ExamPage.Builder()
            .withExamId(exam.getId())
            .withPagePosition(2)
            .withItemGroupKey("GroupKey2")
            .build();

        assertThat(examPageQueryRepository.findAll(exam.getId())).isEmpty();
        examPageCommandRepository.insert(Arrays.asList(examPage1, examPage2));

        assertThat(examPageQueryRepository.findAll(exam.getId())).hasSize(2);

        examPageCommandRepository.deleteAll(exam.getId());
        assertThat(examPageQueryRepository.findAll(exam.getId())).isEmpty();

        examPageCommandRepository.insert(Arrays.asList(examPage1a));
        assertThat(examPageQueryRepository.findAll(exam.getId())).hasSize(1);
    }

}
