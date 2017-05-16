package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.swing.text.html.Option;
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
import tds.exam.ExamSegment;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ItemBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamService;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.model.ItemResponse;
import tds.itemselection.services.ItemSelectionService;
import tds.student.sql.data.OpportunityItem;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tds.itemselection.model.ItemResponse.Status.SATISFIED;

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

    @Mock
    private ExamSegmentService mockExamSegmentService;

    @Before
    public void setUp() {
        examItemSelectionService = new ExamItemSelectionServiceImpl(mockItemSelectionService,
            mockExamPageCommandRepository,
            mockExamItemCommandRepository,
            mockExamService,
            mockAssessmentService,
            mockExamSegmentService);
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
        itemGroup.setNumberOfItemsRequired(-1);

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
        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, itemGroup.getSegmentPosition()))
            .thenReturn(Optional.of(random(ExamSegment.class)));

        List<OpportunityItem> items = examItemSelectionService.createNextPageGroup(examId, 1, 2);

        ArgumentCaptor<ExamPage> examPageArgumentCaptor = ArgumentCaptor.forClass(ExamPage.class);
        ArgumentCaptor<ExamItem> varArgsExamItem = ArgumentCaptor.forClass(ExamItem.class);

        verify(mockExamPageCommandRepository).insert(examPageArgumentCaptor.capture());
        verify(mockExamItemCommandRepository).insert(varArgsExamItem.capture());
        verify(mockExamSegmentService).findByExamIdAndSegmentPosition(examId, itemGroup.getSegmentPosition());

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
        assertThat(opportunityItem.getValue()).isNull();
        assertThat(opportunityItem.isVisible()).isTrue();
        assertThat(opportunityItem.getIsSelected()).isFalse();
        assertThat(opportunityItem.getIsValid()).isFalse();
        assertThat(opportunityItem.isMarkForReview()).isFalse();
        assertThat(opportunityItem.getStimulusFile()).isNull();
        assertThat(opportunityItem.getItemFile()).isNull();
        assertThat(opportunityItem.getGroupItemsRequired()).isEqualTo(-1);

        assertThat(examItem.getAssessmentItemBankKey()).isEqualTo(item.getBankKey());
        assertThat(examItem.getAssessmentItemKey()).isEqualTo(item.getItemKey());
        assertThat(examItem.getExamPageId()).isEqualTo(examPage.getId());
        assertThat(examItem.getItemFilePath()).isEqualTo(item.getItemFilePath());
        assertThat(examItem.getItemType()).isEqualTo(item.getItemType());
        assertThat(examItem.getPosition()).isEqualTo(3);
        assertThat(examItem.getResponse().isPresent()).isFalse();

        assertThat(examPage.getItemGroupKey()).isEqualTo(itemGroup.getGroupID());
        assertThat(examPage.getPagePosition()).isEqualTo(2);
        assertThat(examPage.getGroupItemsRequired()).isEqualTo(-1);
    }

    @Test
    public void shouldUpdateSegmentIfAssessmentIsSinglePage() {
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
        itemGroup.setNumberOfItemsRequired(-1);

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

        ExamSegment mockExamSegment = new ExamSegment.Builder()
            .fromSegment(random(ExamSegment.class))
            .withSatisfied(false)
            .withExamItemCount(1)
            .build();

        when(mockItemSelectionService.getNextItemGroup(examId, false)).thenReturn(new ItemResponse<>(itemGroup));
        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), assessment.getKey())).thenReturn(Optional.of(assessment));
        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, itemGroup.getSegmentPosition()))
            .thenReturn(Optional.of(mockExamSegment));

        List<OpportunityItem> items = examItemSelectionService.createNextPageGroup(examId, 1, 2);

        ArgumentCaptor<ExamPage> examPageArgumentCaptor = ArgumentCaptor.forClass(ExamPage.class);
        ArgumentCaptor<ExamItem> varArgsExamItem = ArgumentCaptor.forClass(ExamItem.class);
        ArgumentCaptor<ExamSegment> varArgsExamSegment = ArgumentCaptor.forClass(ExamSegment.class);

        verify(mockExamPageCommandRepository).insert(examPageArgumentCaptor.capture());
        verify(mockExamItemCommandRepository).insert(varArgsExamItem.capture());
        verify(mockExamSegmentService).findByExamIdAndSegmentPosition(examId, itemGroup.getSegmentPosition());
        verify(mockExamSegmentService).update(varArgsExamSegment.capture());

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
        assertThat(opportunityItem.getValue()).isNull();
        assertThat(opportunityItem.isVisible()).isTrue();
        assertThat(opportunityItem.getIsSelected()).isFalse();
        assertThat(opportunityItem.getIsValid()).isFalse();
        assertThat(opportunityItem.isMarkForReview()).isFalse();
        assertThat(opportunityItem.getStimulusFile()).isNull();
        assertThat(opportunityItem.getItemFile()).isNull();
        assertThat(opportunityItem.getGroupItemsRequired()).isEqualTo(-1);

        assertThat(examItem.getAssessmentItemBankKey()).isEqualTo(item.getBankKey());
        assertThat(examItem.getAssessmentItemKey()).isEqualTo(item.getItemKey());
        assertThat(examItem.getExamPageId()).isEqualTo(examPage.getId());
        assertThat(examItem.getItemFilePath()).isEqualTo(item.getItemFilePath());
        assertThat(examItem.getItemType()).isEqualTo(item.getItemType());
        assertThat(examItem.getPosition()).isEqualTo(3);
        assertThat(examItem.getResponse().isPresent()).isFalse();

        assertThat(examPage.getItemGroupKey()).isEqualTo(itemGroup.getGroupID());
        assertThat(examPage.getPagePosition()).isEqualTo(2);
        assertThat(examPage.getGroupItemsRequired()).isEqualTo(-1);

        ExamSegment examSegment = varArgsExamSegment.getValue();
        assertThat(examSegment.isSatisfied()).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfExamCannotBeFound() {
        UUID examId = UUID.randomUUID();
        when(mockExamService.findExam(examId)).thenReturn(Optional.empty());
        examItemSelectionService.createNextPageGroup(examId, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfAssessmentCannotBeFound() {
        UUID examId = UUID.randomUUID();
        Exam exam = new ExamBuilder().withId(examId).build();
        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.empty());
        examItemSelectionService.createNextPageGroup(examId, 1, 2);
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
        examItemSelectionService.createNextPageGroup(examId, 1, 2);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowIfThereIsNoExamSegment() {
        UUID examId = UUID.randomUUID();
        Exam exam = new ExamBuilder().withId(examId).build();
        Assessment assessment = new AssessmentBuilder()
            .build();
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setSegmentKey(assessment.getSegments().get(0).getKey());
        itemGroup.setSegmentID("segmentId");
        itemGroup.setGroupID("group");
        itemGroup.setNumberOfItemsRequired(-1);

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
        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, itemGroup.getSegmentPosition())).thenReturn(Optional.empty());
        examItemSelectionService.createNextPageGroup(examId, 1, 2);
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
        itemGroup.setNumberOfItemsRequired(-1);

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
        examItemSelectionService.createNextPageGroup(examId, 1, 2);
    }

    @Test
    public void itShouldReturnAnEmptyListIfExamIsSatisfied() {
        final UUID examId = UUID.randomUUID();
        final Exam exam = new ExamBuilder().withId(examId).build();

        final Assessment assessment = new AssessmentBuilder()
            .build();

        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockItemSelectionService.getNextItemGroup(examId, false)).thenReturn(new ItemResponse<>(SATISFIED));
        assertThat(examItemSelectionService.createNextPageGroup(examId, 1, 2)).isEmpty();
    }
}