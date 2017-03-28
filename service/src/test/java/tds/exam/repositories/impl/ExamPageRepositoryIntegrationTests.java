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
import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
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
        examSegmentCommandRepository.insert(Arrays.asList(mockExamSegment));
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
        assertThat(examPageQueryRepository.findAll(exam.getId())).hasSize(1);
    }

    @Test
    public void shouldFindAnExamPageWithSomeItemsThatHaveResponsesAndOthersDoNot() {
        // The student gets the page for the first time...
        Optional<ExamPage> result = examPageQueryRepository.findPageWithItems(mockExam.getId(), 1);

        assertThat(result).isPresent();

        ExamPage examPage = result.get();
        assertThat(examPage.getCreatedAt()).isNotNull();
        assertThat(examPage.getId()).isEqualTo(mockExamPage.getId());
        assertThat(examPage.getPagePosition()).isEqualTo(1);
        assertThat(examPage.getSegmentKey()).isEqualTo("segment-key-1");
        assertThat(examPage.getSegmentId()).isEqualTo("segment-id-1");
        assertThat(examPage.getSegmentPosition()).isEqualTo(1);
        assertThat(examPage.getItemGroupKey()).isEqualTo("item-group-key");
        assertThat(examPage.isGroupItemsRequired()).isTrue();
        assertThat(examPage.getExamId()).isEqualTo(mockExam.getId());

        assertThat(examPage.getExamItems()).hasSize(2);
        for (ExamItem item : examPage.getExamItems()) {
            assertThat(item.getResponse().isPresent()).isFalse();
        }

        // ...and responds to the first item
        ExamItemResponse mockResponseForFirstItem = new ExamItemResponseBuilder()
            .withExamItemId(examPage.getExamItems().get(0).getId())
            .withResponse("first item response")
            .withSequence(1)
            .build();

        examItemCommandRepository.insertResponses(mockResponseForFirstItem);

        Optional<ExamPage> resultWithItemResponses = examPageQueryRepository.findPageWithItems(mockExam.getId(), 1);
        assertThat(resultWithItemResponses).isPresent();

        ExamPage examPageWithResponses = resultWithItemResponses.get();
        assertThat(examPageWithResponses.getExamItems()).hasSize(2);

        // Student responded to the first item, so it should contain a response
        ExamItem firstExamItem = examPageWithResponses.getExamItems().get(0);
        assertThat(firstExamItem.getId()).isEqualTo(mockFirstExamItem.getId());
        assertThat(firstExamItem.getExamPageId()).isEqualTo(examPage.getId());
        assertThat(firstExamItem.getItemKey()).isEqualTo("187-1234");
        assertThat(firstExamItem.getAssessmentItemBankKey()).isEqualTo(187L);
        assertThat(firstExamItem.getAssessmentItemKey()).isEqualTo(1234L);
        assertThat(firstExamItem.getItemType()).isEqualTo("MS");
        assertThat(firstExamItem.getPosition()).isEqualTo(1);
        assertThat(firstExamItem.isRequired()).isTrue();
        assertThat(firstExamItem.isMarkedForReview()).isFalse();
        assertThat(firstExamItem.isFieldTest()).isFalse();
        assertThat(firstExamItem.getItemFilePath()).isEqualTo("/path/to/item/187-1234.xml");
        assertThat(firstExamItem.getStimulusFilePath().isPresent()).isFalse();
        assertThat(firstExamItem.getResponse().isPresent()).isTrue();

        ExamItemResponse firstItemResponse = firstExamItem.getResponse().get();
        assertThat(firstItemResponse.getExamItemId()).isEqualTo(firstExamItem.getId());
        assertThat(firstItemResponse.getResponse()).isEqualTo("first item response");
        assertThat(firstItemResponse.getSequence()).isEqualTo(1);
        assertThat(firstItemResponse.getCreatedAt()).isNotNull();

        ExamItem secondExamItem = examPage.getExamItems().get(1);
        assertThat(secondExamItem).isEqualToComparingFieldByField(mockSecondExamItem);
    }

    @Test
    public void shouldFindExamPageByExamIdAndPosition() {
        Optional<ExamPage> maybeExamPage = examPageQueryRepository.find(mockExam.getId(), mockFirstExamItem.getPosition());
        assertThat(maybeExamPage).isPresent();

        ExamPage page = maybeExamPage.get();
        assertThat(page.getId()).isEqualTo(mockExamPage.getId());
        assertThat(page.getExamItems()).isEmpty();
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
        assertThat(page.getExamItems()).isEmpty();
    }

    @Test
    public void shouldHandleExamPageNotFoundForId() {
        Optional<ExamPage> maybeExamPage = examPageQueryRepository.find(UUID.randomUUID());
        assertThat(maybeExamPage).isNotPresent();
    }
}