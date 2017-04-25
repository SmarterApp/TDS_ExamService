package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ItemBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamService;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.model.ItemResponse;
import tds.itemselection.services.ItemSelectionService;
import tds.student.sql.data.OpportunityItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamItemSelectionServiceImplTest {
    private ExamItemSelectionService examItemSelectionService;

    @Mock
    private ItemSelectionService mockItemSelectionService;

    @Mock
    private ExamPageCommandRepository mockExamPageCommandRepository;

    @Mock
    private ExamItemCommandRepository mockExamItemCommandRepository;

    @Mock
    private ExamService mockExamService;

    @Mock
    private AssessmentService mockAssessmentService;

    @Before
    public void setUp() {
        examItemSelectionService = new ExamItemSelectionServiceImpl(mockItemSelectionService,
            mockExamPageCommandRepository,
            mockExamItemCommandRepository,
            mockExamService,
            mockAssessmentService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldSelectItems() {
        UUID examId = UUID.randomUUID();

        Item item = new ItemBuilder("187-2345")
            .withGroupId("group")
            .withBlockId("block")
            .withFieldTest(false)
            .withStrand("strand")
            .build();

        Segment segment = new SegmentBuilder()
            .withKey("segmentKey")
            .withSegmentId("segmentId")
            .withPosition(1)
            .withItems(Collections.singletonList(item))
            .build();

        Assessment assessment = new AssessmentBuilder()
            .withSegments(Collections.singletonList(segment))
            .build();

        Exam exam = new ExamBuilder()
            .withId(examId)
            .withAssessmentKey(assessment.getKey())
            .build();

        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setSegmentKey("segmentKey");
        itemGroup.setSegmentID("segmentId");
        itemGroup.setGroupID("group");

        TestItem testItem = new TestItem(
            "187-2345",
            "group",
            1,
            true,
            false,
            "strand",
            true,
            1.2,
            1.3,
            1.4,
            "irtModel",
            "1.5"
        );

        itemGroup.setItems(Collections.singletonList(testItem));

        when(mockItemSelectionService.getNextItemGroup(examId, false)).thenReturn(new ItemResponse<>(itemGroup));
        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), assessment.getKey())).thenReturn(Optional.of(assessment));

        List<OpportunityItem> items = examItemSelectionService.createNextPageGroup(examId, 1);

        ArgumentCaptor<ExamPage> examPageArgumentCaptor = ArgumentCaptor.forClass(ExamPage.class);
        ArgumentCaptor<ExamItem> varArgsExamItem = ArgumentCaptor.forClass(ExamItem.class);

        verify(mockExamPageCommandRepository).insert(examPageArgumentCaptor.capture());
        verify(mockExamItemCommandRepository).insert(varArgsExamItem.capture());

        assertThat(items).hasSize(1);
        assertThat(varArgsExamItem.getAllValues()).hasSize(1);
        assertThat(examPageArgumentCaptor.getValue()).isNotNull();

        OpportunityItem opportunityItem = items.get(0);
        ExamItem examItem = varArgsExamItem.getValue();
        ExamPage examPage = examPageArgumentCaptor.getValue();

        assertThat(opportunityItem.getIsSelected()).isFalse();
        assertThat(opportunityItem.getSequence()).isEqualTo(0);
        assertThat(opportunityItem.getFormat()).isEqualTo(item.getItemType());
        assertThat(opportunityItem.getPage()).isEqualTo(examPage.getPagePosition());
        assertThat(opportunityItem.getPosition()).isEqualTo(examItem.getPosition());
        assertThat(opportunityItem.getGroupItemsRequired()).isEqualTo(0);
        assertThat(opportunityItem.getValue()).isNull();
        assertThat(opportunityItem.isVisible()).isTrue();
        assertThat(opportunityItem.getIsSelected()).isFalse();
        assertThat(opportunityItem.getIsValid()).isFalse();
        assertThat(opportunityItem.isMarkForReview()).isFalse();
        assertThat(opportunityItem.getStimulusFile()).isNull();
        assertThat(opportunityItem.getItemFile()).isNull();

        assertThat(examItem.getAssessmentItemBankKey()).isEqualTo(item.getBankKey());
        assertThat(examItem.getAssessmentItemKey()).isEqualTo(item.getItemKey());
        assertThat(examItem.getExamPageId()).isEqualTo(examPage.getId());
        assertThat(examItem.getItemFilePath()).isEqualTo(item.getItemFilePath());
        assertThat(examItem.getItemType()).isEqualTo(item.getItemType());
        assertThat(examItem.getPosition()).isEqualTo(testItem.position);
        assertThat(examItem.getResponse().isPresent()).isFalse();

        assertThat(examPage.getItemGroupKey()).isEqualTo(itemGroup.getGroupID());
        assertThat(examPage.getPagePosition()).isEqualTo(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfExamCannotBeFound() {
        UUID examId = UUID.randomUUID();
        when(mockExamService.findExam(examId)).thenReturn(Optional.empty());
        examItemSelectionService.createNextPageGroup(examId, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfAssessmentCannotBeFound() {
        UUID examId = UUID.randomUUID();
        Exam exam = new ExamBuilder().withId(examId).build();
        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.empty());
        examItemSelectionService.createNextPageGroup(examId, 1);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowIfThereIsErrorInItemSelection() {
        UUID examId = UUID.randomUUID();
        Exam exam = new ExamBuilder().withId(examId).build();
        Assessment assessment = new AssessmentBuilder()
            .build();

        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockItemSelectionService.getNextItemGroup(examId, false)).thenReturn(new ItemResponse<>("Failed"));
        examItemSelectionService.createNextPageGroup(examId, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfThereIsError() {
        UUID examId = UUID.randomUUID();
        Exam exam = new ExamBuilder().withId(examId).build();

        Assessment assessment = new AssessmentBuilder()
            .build();

        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setSegmentKey(assessment.getSegments().get(0).getKey());
        itemGroup.setSegmentID("segmentId");
        itemGroup.setGroupID("group");

        TestItem testItem = new TestItem(
            "187-2345",
            "group",
            1,
            true,
            false,
            "strand",
            true,
            1.2,
            1.3,
            1.4,
            "irtModel",
            "1.5"
        );

        itemGroup.setItems(Collections.singletonList(testItem));

        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockItemSelectionService.getNextItemGroup(examId, false)).thenReturn(new ItemResponse<>(itemGroup));
        examItemSelectionService.createNextPageGroup(examId, 1);
    }
}