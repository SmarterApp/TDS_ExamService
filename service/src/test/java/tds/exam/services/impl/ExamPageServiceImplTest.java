package tds.exam.services.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.assessment.Form;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.models.ExamItem;
import tds.exam.models.ExamItemResponse;
import tds.exam.models.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamResponseQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamPageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPageServiceImplTest {
    @Mock
    private ExamResponseQueryRepository mockExamResponseQueryRepository;

    @Mock
    private ExamPageCommandRepository mockExamPageCommandRepository;

    @Mock
    private ExamPageQueryRepository mockExamPageQueryRepository;

    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Mock
    private AssessmentService mockAssessmentService;

    private ExamPageService examPageService;

    @Before
    public void setUp() {
        examPageService = new ExamPageServiceImpl(mockExamPageQueryRepository,
            mockExamPageCommandRepository,
            mockExamResponseQueryRepository,
            mockExamQueryRepository,
            mockAssessmentService);
    }

    @Test
    public void shouldReturnLatestExamPositionForExamId() {
        final UUID examId = UUID.randomUUID();
        final int currentExamPosition = 9;
        when(mockExamResponseQueryRepository.getCurrentExamItemPosition(examId)).thenReturn(currentExamPosition);
        int examPosition = examPageService.getExamPosition(examId);
        assertThat(examPosition).isEqualTo(currentExamPosition);
        verify(mockExamResponseQueryRepository).getCurrentExamItemPosition(examId);
    }

    @Test
    public void shouldReturnAllPagesForExam() {
        final UUID examId = UUID.randomUUID();
        ExamPage examPage1 = new ExamPageBuilder()
            .build();
        ExamPage examPage2 = new ExamPageBuilder()
            .withId(79)
            .build();
        List<ExamPage> examPages = new ArrayList<>();
        examPages.add(examPage1);
        examPages.add(examPage2);

        when(mockExamPageQueryRepository.findAll(examId)).thenReturn(examPages);
        List<ExamPage> retExamPages = examPageService.findAllPages(examId);
        assertThat(retExamPages).hasSize(2);
    }

    @Test
    public void shouldDeletePagesForExamId() {
        final UUID examId = UUID.randomUUID();
        examPageService.deletePages(examId);
        verify(mockExamPageCommandRepository).deleteAll(examId);
    }

    @Test
    public void shouldInsertPagesForExamId() {
        ExamPage examPage1 = new ExamPageBuilder()
            .build();
        List<ExamPage> examPages = new ArrayList<>();
        examPages.add(examPage1);

        examPageService.insertPages(examPages);
        verify(mockExamPageCommandRepository).insert(examPages);
    }

    @Test
    public void shouldGetAnExamPageWithItems() {
        // Build Assessment
        Item mockFirstAssessmentItem = new Item("187-1234");
        mockFirstAssessmentItem.setItemFilePath("/path/to/item/187-1234.xml");
        mockFirstAssessmentItem.setStimulusFilePath("/path/to/stimulus/187-1234.xml");
        mockFirstAssessmentItem.setItemType("UNIT");
        mockFirstAssessmentItem.setRequired(true);

        Item mockSecondAssessmentItem = new Item("187-5678");
        mockSecondAssessmentItem.setItemFilePath("/path/to/item/187-5678.xml");
        mockSecondAssessmentItem.setItemType("TEST");
        mockSecondAssessmentItem.setRequired(true);

        List<Item> mockAssessmentItems = Arrays.asList(mockFirstAssessmentItem, mockSecondAssessmentItem);

         Form mockForm = new Form.Builder("test-form-1")
             .withLanguage("ENU")
             .withSegmentKey(SegmentBuilder.DEFAULT_SEGMENT_KEY)
             .withItems(mockAssessmentItems)
             .build();

        Segment mockSegment = new SegmentBuilder()
            .withForms(Arrays.asList(mockForm))
            .build();

        Assessment mockAssessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(mockSegment))
            .build();

        // Build Exam
        Exam mockExam = new ExamBuilder()
            .withAssessmentKey(mockAssessment.getKey())
            .withLanguageCode("ENU")
            .build();

        Instant respondedAtInstant = Instant.now().minus(200000);
        ExamItem mockFirstExamItem = new ExamItem.Builder()
            .withExamPageId(ExamPageBuilder.DEFAULT_ID)
            .withItemKey("187-1234")
            .withResponse(new ExamItemResponse.Builder()
                .withResponse("first item response")
                .withCreatedAt(respondedAtInstant)
                .build())
            .build();
        ExamItem mockSecondExamItem = new ExamItem.Builder()
            .withExamPageId(ExamPageBuilder.DEFAULT_ID)
            .withItemKey("187-5678")
            .build();

        List<ExamItem> mockExamItems = Arrays.asList(mockFirstExamItem, mockSecondExamItem);

        ExamPage mockExamPage = new ExamPageBuilder()
            .withExamId(mockExam.getId())
            .withSegmentKey(mockSegment.getKey())
            .withExamItems(mockExamItems)
            .build();

        mockAssessment.setSegments(Arrays.asList(mockSegment));
        mockSegment.setItems(mockAssessmentItems);

        when(mockExamQueryRepository.getExamById(mockExam.getId()))
            .thenReturn(Optional.of(mockExam));
        when(mockAssessmentService.findAssessment(mockExam.getClientName(), mockExam.getAssessmentKey()))
            .thenReturn(Optional.of(mockAssessment));
        when(mockExamPageQueryRepository.findPageWithItems(mockExamPage.getExamId(), mockExamPage.getPagePosition()))
            .thenReturn(Optional.of(mockExamPage));

        ExamPage examPage = examPageService.getPage(mockExamPage.getExamId(), mockExamPage.getPagePosition());
        verify(mockExamQueryRepository).getExamById(mockExam.getId());
        verify(mockAssessmentService).findAssessment(mockExam.getClientName(), mockExam.getAssessmentKey());
        verify(mockExamPageQueryRepository).findPageWithItems(mockExamPage.getExamId(), mockExamPage.getPagePosition());
        verify(mockExamPageCommandRepository).update(isA(ExamPage.class));

        ExamItem firstExamItem = examPage.getExamItems().get(0);
        assertThat(firstExamItem.getAssessmentBankKey()).isEqualTo(187L);
        assertThat(firstExamItem.getAssessmentItemKey()).isEqualTo(1234L);
        assertThat(firstExamItem.getAssessmentItemType()).isEqualTo("UNIT");
        assertThat(firstExamItem.getAssessmentItemIsRequired()).isTrue();
        assertThat(firstExamItem.getAssessmentItemFilePath()).isEqualTo("/path/to/item/187-1234.xml");
        assertThat(firstExamItem.getAssessmentItemStimulusPath()).isEqualTo("/path/to/stimulus/187-1234.xml");
        assertThat(firstExamItem.getRespondedAt()).isEqualTo(respondedAtInstant);
        assertThat(firstExamItem.getResponseText()).isEqualTo("first item response");
        assertThat(firstExamItem.getResponseLength()).isEqualTo(19);

        ExamItem secondExamItem = examPage.getExamItems().get(1);
        assertThat(secondExamItem.getAssessmentBankKey()).isEqualTo(187L);
        assertThat(secondExamItem.getAssessmentItemKey()).isEqualTo(5678L);
        assertThat(secondExamItem.getAssessmentItemType()).isEqualTo("TEST");
        assertThat(secondExamItem.getAssessmentItemIsRequired()).isTrue();
        assertThat(secondExamItem.getAssessmentItemFilePath()).isEqualTo("/path/to/item/187-5678.xml");
        assertThat(secondExamItem.getRespondedAt()).isEqualTo(null);
        assertThat(secondExamItem.getResponseText()).isEqualTo("");
        assertThat(secondExamItem.getResponseLength()).isEqualTo(0);
    }
}
