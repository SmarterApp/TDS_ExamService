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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import tds.exam.Exam;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.models.ExamItem;
import tds.exam.models.ExamItemResponse;
import tds.exam.models.ExamPage;
import tds.exam.models.ExamSegment;
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
    private ExamPageCommandRepository examPageCommandRepository;
    private ExamPageQueryRepository examPageQueryRepository;
    private ExamSegmentCommandRepository examSegmentCommandRepository;
    private ExamCommandRepository examCommandRepository;
    private ExamItemCommandRepository examItemCommandRepository;

    private final Exam mockExam = new ExamBuilder().build();

    @Before
    public void setUp() {
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examPageQueryRepository = new ExamPageQueryRepositoryImpl(commandJdbcTemplate);
        examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);

        // Create mock data for testing fetching exam pages
        examCommandRepository.insert(mockExam);

        ExamSegment mockExamSegment = new ExamSegmentBuilder()
            .withExamId(mockExam.getId())
            .build();
        examSegmentCommandRepository.insert(Arrays.asList(mockExamSegment));

        ExamPage mockExamPage = new ExamPageBuilder()
            .withExamId(mockExam.getId())
            .withSegmentKey(mockExamSegment.getSegmentKey())
            .build();

        assertThat(examPageQueryRepository.findAll(mockExam.getId())).isEmpty();
        examPageCommandRepository.insert(Arrays.asList(mockExamPage));

        List<ExamPage> mockExamPages = examPageQueryRepository.findAll(mockExam.getId());
        ExamPage mockFirstExamPage = mockExamPages.get(0);

        ExamItem mockFirstExamItem = new ExamItemBuilder()
            .withItemKey("187-1234")
            .withExamPageId(mockFirstExamPage.getId())
            .build();

        ExamItem mockSecondExamItem = new ExamItemBuilder()
            .withExamPageId(mockFirstExamPage.getId())
            .withItemKey("187-5678")
            .withPosition(2)
            .build();

        examItemCommandRepository.insert(mockFirstExamItem, mockSecondExamItem);
    }

    @Test
    public void shouldMarkExamPagesAsDeleted() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);
        ExamPage examPage1 = new ExamPageBuilder()
            .withExamId(exam.getId())
            .withPagePosition(1)
            .withItemGroupKey("GroupKey1")
            .build();
        ExamPage examPage2 = new ExamPageBuilder()
            .withExamId(exam.getId())
            .withPagePosition(2)
            .withItemGroupKey("GroupKey2")
            .build();

        assertThat(examPageQueryRepository.findAll(exam.getId())).isEmpty();
        examPageCommandRepository.insert(Arrays.asList(examPage1, examPage2));

        assertThat(examPageQueryRepository.findAll(exam.getId())).hasSize(2);

        examPageCommandRepository.deleteAll(exam.getId());
        assertThat(examPageQueryRepository.findAll(exam.getId())).isEmpty();

        examPageCommandRepository.insert(Arrays.asList(examPage1));
        assertThat(examPageQueryRepository.findAll(exam.getId())).hasSize(1);
    }

    @Test
    public void shouldFindAnExamPageWithItemsThatHaveNoResponses() {
        // The student gets the page for the first time...
        Optional<ExamPage> result = examPageQueryRepository.findPageWithItems(mockExam.getId(), 1);

        assertThat(result).isPresent();

        ExamPage examPage = result.get();
        assertThat(examPage.getId()).isGreaterThan(0L);
        assertThat(examPage.getPagePosition()).isEqualTo(1);
        assertThat(examPage.getSegmentKey()).isEqualTo("segment-key-1");
        assertThat(examPage.getSegmentId()).isEqualTo("segment-id-1");
        assertThat(examPage.getSegmentPosition()).isEqualTo(1);
        assertThat(examPage.getItemGroupKey()).isEqualTo("item-group-key");
        assertThat(examPage.getGroupItemsRequired()).isEqualTo(-1);
        assertThat(examPage.getExamId()).isEqualTo(mockExam.getId());
        assertThat(examPage.getCreatedAt()).isNotNull();

        assertThat(examPage.getExamItems()).hasSize(2);
        for (ExamItem item : examPage.getExamItems()) {
            assertThat(item.getResponse()).isNull();
        }

        // ...and responds to the first item
        ExamPage mockExamPage = result.get();
        Instant responseCreatedAt = Instant.now();
        ExamItemResponse mockResponseForFirstItem = new ExamItemResponse.Builder()
            .withExamItemId(mockExamPage.getExamItems().get(0).getId())
            .withResponse("first item response")
            .withCreatedAt(responseCreatedAt)
            .build();

        examItemCommandRepository.insertResponses(mockResponseForFirstItem);

        Optional<ExamPage> resultWithItemResponses = examPageQueryRepository.findPageWithItems(mockExam.getId(), 1);
        assertThat(resultWithItemResponses).isPresent();

        ExamPage examPageWithResponses = resultWithItemResponses.get();
        assertThat(examPageWithResponses.getExamItems()).hasSize(2);

        // Student responded to the first item, so it should contain a response
        ExamItem firstExamItem = examPageWithResponses.getExamItems().get(0);
        assertThat(firstExamItem.getId()).isGreaterThan(0L);
        assertThat(firstExamItem.getExamPageId()).isEqualTo(examPage.getId());
        assertThat(firstExamItem.getItemKey()).isEqualTo("187-1234");
        assertThat(firstExamItem.getPosition()).isEqualTo(1);
        assertThat(firstExamItem.isSelected()).isFalse();
        assertThat(firstExamItem.isMarkedForReview()).isFalse();
        assertThat(firstExamItem.isFieldTest()).isFalse();

        ExamItemResponse firstItemResponse = firstExamItem.getResponse();
        assertThat(firstItemResponse.getExamItemId()).isEqualTo(firstExamItem.getId());
        assertThat(firstItemResponse.getResponse()).isEqualTo("first item response");
        assertThat(firstItemResponse.getCreatedAt()).isEqualTo(responseCreatedAt);

        // Student did not respond to the second item, so it should not contain a response
        ExamItem secondExamItem = examPage.getExamItems().get(1);
        assertThat(secondExamItem.getId()).isGreaterThan(0L);
        assertThat(secondExamItem.getExamPageId()).isEqualTo(examPage.getId());
        assertThat(secondExamItem.getItemKey()).isEqualTo("187-5678");
        assertThat(secondExamItem.getPosition()).isEqualTo(2);
        assertThat(secondExamItem.isSelected()).isFalse();
        assertThat(secondExamItem.isMarkedForReview()).isFalse();
        assertThat(secondExamItem.isFieldTest()).isFalse();
        assertThat(secondExamItem.getResponse()).isNull();
    }
}