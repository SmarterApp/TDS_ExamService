package tds.exam.repositories.impl;

import org.joda.time.Instant;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.models.ExamSegment;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemResponseCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamItemCommandRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;

    private ExamItemCommandRepository examItemCommandRepository;
    private ExamPageQueryRepository examPageQueryRepository;

    private Exam mockExam = new ExamBuilder().build();
    private ExamPage mockPage = new ExamPageBuilder()
        .withExamId(mockExam.getId())
        .build();

    @Before
    public void SetUp() {
        ExamCommandRepository examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        ExamSegmentCommandRepository examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        ExamPageCommandRepository examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examPageQueryRepository = new ExamPageQueryRepositoryImpl(commandJdbcTemplate);
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);

        ExamSegment mockExamSegment = new ExamSegmentBuilder()
            .withSegmentId(mockPage.getSegmentId())
            .withSegmentKey(mockPage.getSegmentKey())
            .withSegmentPosition(mockPage.getSegmentPosition())
            .withExamId(mockExam.getId())
            .build();

        examCommandRepository.insert(mockExam);
        examSegmentCommandRepository.insert(Arrays.asList(mockExamSegment));
        examPageCommandRepository.insert(mockPage);
    }

    @Test
    public void shouldInsertAnExamItem() {
        List<ExamPage> pages = examPageQueryRepository.findAll(mockExam.getId());
        assertThat(pages).hasSize(1);

        ExamPage insertedPage = pages.get(0);
        ExamItem mockExamItem = new ExamItemBuilder()
            .withExamPageId(insertedPage.getId())
            .build();

        examItemCommandRepository.insert(mockExamItem);
    }
}
