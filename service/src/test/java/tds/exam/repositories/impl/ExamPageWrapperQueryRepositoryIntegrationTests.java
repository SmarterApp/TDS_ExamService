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
import tds.exam.repositories.ExamPageWrapperQueryRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;
import tds.exam.wrapper.ExamPageWrapper;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamPageWrapperQueryRepositoryIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;
    private ExamItemCommandRepository examItemCommandRepository;
    private ExamPageCommandRepository examPageCommandRepository;
    private ExamPageWrapperQueryRepository examPageQueryRepository;

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
        examPageQueryRepository = new ExamPageWrapperQueryRepositoryImpl(commandJdbcTemplate);
        ExamSegmentCommandRepository examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        final ExamCommandRepository examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        ExamItemCommandRepository examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);

        // Seed the database with mock records for integration testing
        examCommandRepository.insert(mockExam);
        examSegmentCommandRepository.insert(Collections.singletonList(mockExamSegment));
        examPageCommandRepository.insert(mockExamPage);
        examItemCommandRepository.insert(mockFirstExamItem, mockSecondExamItem);
    }

    @Test
    public void shouldFindAnExamPageWithSomeItemsThatHaveResponsesAndOthersDoNot() {
        // The student gets the page for the first time...
        Optional<ExamPageWrapper> result = examPageQueryRepository.findPageWithItems(mockExam.getId(), 1);

        assertThat(result).isPresent();

        ExamPageWrapper wrapper = result.get();

        ExamPage examPage = wrapper.getExamPage();
        assertThat(examPage.getCreatedAt()).isNotNull();
        assertThat(examPage.getId()).isEqualTo(mockExamPage.getId());
        assertThat(examPage.getPagePosition()).isEqualTo(1);
        assertThat(examPage.getSegmentKey()).isEqualTo("segment-key-1");
        assertThat(examPage.getItemGroupKey()).isEqualTo("item-group-key");
        assertThat(examPage.isGroupItemsRequired()).isTrue();
        assertThat(examPage.getExamId()).isEqualTo(mockExam.getId());

        assertThat(wrapper.getExamItems()).hasSize(2);
        for (ExamItem item : wrapper.getExamItems()) {
            assertThat(item.getResponse().isPresent()).isFalse();
        }

        // ...and responds to the first item
        ExamItemResponse mockResponseForFirstItem = new ExamItemResponseBuilder()
            .withExamItemId(wrapper.getExamItems().get(0).getId())
            .withResponse("first item response")
            .withSequence(1)
            .build();

        examItemCommandRepository.insertResponses(mockResponseForFirstItem);

        Optional<ExamPageWrapper> resultWithItemResponses = examPageQueryRepository.findPageWithItems(mockExam.getId(), 1);
        assertThat(resultWithItemResponses).isPresent();

        ExamPageWrapper examPageWrapperWithResponses = resultWithItemResponses.get();

        assertThat(examPageWrapperWithResponses.getExamItems()).hasSize(2);
        assertThat(examPageWrapperWithResponses.getExamPage()).isNotNull();

        // Student responded to the first item, so it should contain a response
        ExamItem firstExamItem = examPageWrapperWithResponses.getExamItems().get(0);
        assertThat(firstExamItem.getId()).isEqualTo(mockFirstExamItem.getId());
        assertThat(firstExamItem.getExamPageId()).isEqualTo(examPage.getId());
        assertThat(firstExamItem.getItemKey()).isEqualTo("187-1234");
        assertThat(firstExamItem.getAssessmentItemBankKey()).isEqualTo(187L);
        assertThat(firstExamItem.getAssessmentItemKey()).isEqualTo(1234L);
        assertThat(firstExamItem.getItemType()).isEqualTo("MS");
        assertThat(firstExamItem.getPosition()).isEqualTo(1);
        assertThat(firstExamItem.isRequired()).isTrue();
        assertThat(firstExamItem.isFieldTest()).isFalse();
        assertThat(firstExamItem.getItemFilePath()).isEqualTo("/path/to/item/187-1234.xml");
        assertThat(firstExamItem.getStimulusFilePath().isPresent()).isFalse();
        assertThat(firstExamItem.getResponse().isPresent()).isTrue();

        ExamItemResponse firstItemResponse = firstExamItem.getResponse().get();
        assertThat(firstItemResponse.getExamItemId()).isEqualTo(firstExamItem.getId());
        assertThat(firstItemResponse.getResponse()).isEqualTo("first item response");
        assertThat(firstItemResponse.getSequence()).isEqualTo(1);
        assertThat(firstItemResponse.getCreatedAt()).isNotNull();
        assertThat(firstItemResponse.isMarkedForReview()).isFalse();

        ExamItem secondExamItem = examPageWrapperWithResponses.getExamItems().get(1);
        assertThat(secondExamItem.getId()).isEqualTo(mockSecondExamItem.getId());
        assertThat(secondExamItem.getExamPageId()).isEqualTo(examPage.getId());
        assertThat(secondExamItem.getItemKey()).isEqualTo(mockSecondExamItem.getItemKey());
        assertThat(secondExamItem.getAssessmentItemKey()).isEqualTo(mockSecondExamItem.getAssessmentItemKey());
        assertThat(secondExamItem.getItemType()).isEqualTo(mockSecondExamItem.getItemType());
        assertThat(secondExamItem.getPosition()).isEqualTo(mockSecondExamItem.getPosition());
        assertThat(secondExamItem.isRequired()).isEqualTo(mockSecondExamItem.isRequired());
        assertThat(secondExamItem.getItemFilePath()).isEqualTo(mockSecondExamItem.getItemFilePath());
        assertThat(secondExamItem.getStimulusFilePath()).isEqualTo(mockSecondExamItem.getStimulusFilePath());
    }

    @Test
    public void shouldFindExamPageWrappersForExam() {
        ExamPage otherExamPage = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(mockExamPage.getExamId())
            .withPagePosition(mockExamPage.getPagePosition() + 1)
            .build();

        ExamItem otherExamItem = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(otherExamPage.getId())
            .build();

        examPageCommandRepository.insert(otherExamPage);
        examItemCommandRepository.insert(otherExamItem);

        List<ExamPageWrapper> examPageWrappers = examPageQueryRepository.findPagesWithItems(mockExam.getId());

        assertThat(examPageWrappers).hasSize(2);

        assertThat(examPageWrappers.get(0).getExamPage().getId()).isEqualTo(mockExamPage.getId());
        assertThat(examPageWrappers.get(0).getExamItems()).hasSize(2);
        assertThat(examPageWrappers.get(0).getExamItems().get(0).getId()).isEqualTo(mockFirstExamItem.getId());
        assertThat(examPageWrappers.get(0).getExamItems().get(1).getId()).isEqualTo(mockSecondExamItem.getId());

        assertThat(examPageWrappers.get(1).getExamPage().getId()).isEqualTo(otherExamPage.getId());
        assertThat(examPageWrappers.get(1).getExamItems()).hasSize(1);
        assertThat(examPageWrappers.get(1).getExamItems().get(0).getId()).isEqualTo(otherExamItem.getId());
    }

    @Test
    public void shouldFindExamPageWrappersForExamAndSegmentKey() {
        List<ExamPageWrapper> examPageWrappers = examPageQueryRepository.findPagesForExamSegment(mockExam.getId(), mockExamSegment.getSegmentKey());

        assertThat(examPageWrappers).hasSize(1);

        assertThat(examPageWrappers.get(0).getExamPage().getId()).isEqualTo(mockExamPage.getId());
        assertThat(examPageWrappers.get(0).getExamItems()).hasSize(2);
        assertThat(examPageWrappers.get(0).getExamItems().get(0).getId()).isEqualTo(mockFirstExamItem.getId());
        assertThat(examPageWrappers.get(0).getExamItems().get(1).getId()).isEqualTo(mockSecondExamItem.getId());
    }
}
