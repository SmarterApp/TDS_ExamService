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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamPageRepositoryIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;
    private ExamItemCommandRepository examItemCommandRepository;
    private ExamPageCommandRepository examPageCommandRepository;
    private ExamPageQueryRepository examPageQueryRepository;
    private ExamCommandRepository examCommandRepository;

    private final Exam mockExam = new ExamBuilder().build();
    private final ExamSegment mockExamSegment = new ExamSegmentBuilder()
        .withExamId(mockExam.getId())
        .build();
    private final ExamPage mockExamPage = new ExamPageBuilder()
        .withExamId(mockExam.getId())
        .withSegmentKey(mockExamSegment.getSegmentKey())
        .build();
    private final ExamItem mockFirstExamItem = new ExamItemBuilder()
        .withItemKey("187-1234")
        .withExamPageId(mockExamPage.getId())
        .withRequired(true)
        .build();
    private final ExamItem mockSecondExamItem = new ExamItemBuilder()
        .withId(UUID.randomUUID())
        .withExamPageId(mockExamPage.getId())
        .withItemKey("187-5678")
        .withAssessmentItemKey(5678L)
        .withItemType("ER")
        .withPosition(2)
        .withRequired(true)
        .withItemFilePath("/path/to/item/187-5678.xml")
        .withStimulusFilePath("/path/to/stimulus/187-5678.xml")
        .build();

    @Before
    public void setUp() {
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examPageQueryRepository = new ExamPageQueryRepositoryImpl(commandJdbcTemplate);
        ExamSegmentCommandRepository examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        ExamItemCommandRepository examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);

        // Seed the database with mock records for integration testing
        examCommandRepository.insert(mockExam);
        examSegmentCommandRepository.insert(Collections.singletonList(mockExamSegment));
        examPageCommandRepository.insert(mockExamPage);
        examItemCommandRepository.insert(mockFirstExamItem, mockSecondExamItem);
    }

    @Test
    public void shouldMarkExamPagesAsDeleted() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);
        ExamPage examPage1 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam.getId())
            .withPagePosition(1)
            .withItemGroupKey("GroupKey1")
            .build();
        ExamPage examPage1a = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam.getId())
            .withPagePosition(1)
            .withItemGroupKey("GroupKey1")
            .withDuration(10000)
            .build();
        ExamPage examPage2 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam.getId())
            .withPagePosition(2)
            .withItemGroupKey("GroupKey2")
            .build();

        assertThat(examPageQueryRepository.findAll(exam.getId())).isEmpty();
        examPageCommandRepository.insert(examPage1, examPage2);

        assertThat(examPageQueryRepository.findAll(exam.getId())).hasSize(2);

        examPageCommandRepository.deleteAll(exam.getId());
        assertThat(examPageQueryRepository.findAll(exam.getId())).isEmpty();

        examPageCommandRepository.insert(examPage1a);
        List<ExamPage> pages = examPageQueryRepository.findAll(exam.getId());
        assertThat(pages).hasSize(1);

        ExamPage page = pages.get(0);

        assertThat(page.getDuration()).isEqualTo(examPage1a.getDuration());
        assertThat(page.getId()).isEqualTo(examPage1a.getId());
        assertThat(page.getExamId()).isEqualTo(examPage1a.getExamId());
        assertThat(page.getPagePosition()).isEqualTo(examPage1a.getPagePosition());
        assertThat(page.getItemGroupKey()).isEqualTo(examPage1a.getItemGroupKey());
    }

    @Test
    public void shouldFindExamPageByExamIdAndPosition() {
        Optional<ExamPage> maybeExamPage = examPageQueryRepository.find(mockExam.getId(), mockFirstExamItem.getPosition());
        assertThat(maybeExamPage).isPresent();

        ExamPage page = maybeExamPage.get();
        assertThat(page.getId()).isEqualTo(mockExamPage.getId());
    }

    @Test
    public void shouldHandleExamPageNotFoundForExamIdAndPosition() {
        Optional<ExamPage> maybeExamPage = examPageQueryRepository.find(mockExam.getId(), 99);
        assertThat(maybeExamPage).isNotPresent();
    }

    @Test
    public void shouldFindExamPageById() {
        Optional<ExamPage> maybeExamPage = examPageQueryRepository.find(mockExamPage.getId());
        assertThat(maybeExamPage).isPresent();

        ExamPage page = maybeExamPage.get();
        assertThat(page.getId()).isEqualTo(mockExamPage.getId());
    }

    @Test
    public void shouldHandleExamPageNotFoundForId() {
        Optional<ExamPage> maybeExamPage = examPageQueryRepository.find(UUID.randomUUID());
        assertThat(maybeExamPage).isNotPresent();
    }
}