package tds.exam.services.item.selection;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExpandableExamService;
import tds.exam.services.FieldTestService;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.SegmentService;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ItemCandidateServiceImplTest {
    private ItemCandidatesService itemCandidatesService;

    @Mock
    private ExpandableExamService mockExpandableExamService;

    @Mock
    private AssessmentService mockAssessmentService;

    @Mock
    private FieldTestService mockFieldTestService;

    @Mock
    private ExamSegmentService mockExamSegmentService;

    @Mock
    private SegmentService mockSegmentService;

    @Before
    public void setUp() {
        itemCandidatesService = new ItemCandidateServiceImpl(mockExpandableExamService, mockFieldTestService, mockExamSegmentService, mockAssessmentService, mockSegmentService);
    }

    @Test
    public void shouldReturnNextItemCandidates() throws ReturnStatusException {
        Segment segment = new SegmentBuilder()
            .withKey("segmentKey")
            .withSegmentId("segmentId")
            .withPosition(1)
            .build();

        Segment segment2 = new SegmentBuilder()
            .withKey("segmentKey2")
            .withSegmentId("segmentId2")
            .withPosition(2)
            .build();

        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment, segment2))
            .build();

        Exam exam = new ExamBuilder().build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(exam.getId())
            .withSegmentPosition(0)
            .withSegmentKey(segment.getKey())
            .withSegmentPosition(segment.getPosition())
            .withExamItemCount(2)
            .withIsSatisfied(false)
            .build();

        ExamSegment examSegment2 = new ExamSegmentBuilder()
            .withExamId(exam.getId())
            .withSegmentPosition(0)
            .withSegmentKey(segment2.getKey())
            .withSegmentPosition(segment2.getPosition())
            .withExamItemCount(1)
            .withIsSatisfied(false)
            .build();

        ExamPage page = new ExamPageBuilder()
            .withSegmentKey(segment.getKey())
            .withExamId(exam.getId())
            .build();

        ExamItem examItem = new ExamItemBuilder()
            .withExamPageId(page.getId())
            .build();

        ExamItem examItem2 = new ExamItemBuilder()
            .withExamPageId(page.getId())
            .build();

        ExpandableExam expandableExam = new ExpandableExam.Builder(exam)
            .withExamItems(Arrays.asList(examItem, examItem2))
            .withExamSegments(Arrays.asList(examSegment, examSegment2))
            .withExamPages(Collections.singletonList(page))
            .build();

        when(mockExpandableExamService.findExam(exam.getId(), ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)).thenReturn(Optional.of(expandableExam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));

        List<ItemCandidatesData> candidates = itemCandidatesService.getAllItemCandidates(exam.getId());

        ArgumentCaptor<ExamSegment> varArgs = ArgumentCaptor.forClass(ExamSegment.class);
        verify(mockExamSegmentService).update(varArgs.capture());

        assertThat(candidates).hasSize(1);

        ItemCandidatesData data = candidates.get(0);

        assertThat(data.getSegmentKey()).isEqualTo(segment2.getKey());

        assertThat(varArgs.getAllValues()).hasSize(1);

        ExamSegment updatedExamSegment = varArgs.getValue();

        assertThat(updatedExamSegment.getSegmentKey()).isEqualTo(examSegment.getSegmentKey());
        assertThat(updatedExamSegment.getSegmentPosition()).isEqualTo(examSegment.getSegmentPosition());
    }

    @Test
    public void shouldReturnItemCandidates() throws ReturnStatusException {
        Segment segment = new SegmentBuilder()
            .withKey("segmentKey")
            .withSegmentId("segmentId")
            .withPosition(1)
            .build();

        Assessment assessment = new AssessmentBuilder()
            .withSegments(Collections.singletonList(segment))
            .build();

        Exam exam = new ExamBuilder().build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(exam.getId())
            .withSegmentPosition(0)
            .withSegmentKey(segment.getKey())
            .withSegmentPosition(segment.getPosition())
            .withExamItemCount(2)
            .withIsSatisfied(false)
            .build();

        ExamPage page = new ExamPageBuilder()
            .withSegmentKey(segment.getKey())
            .withExamId(exam.getId())
            .build();

        ExamItem examItem = new ExamItemBuilder()
            .withExamPageId(page.getId())
            .build();

        ExpandableExam expandableExam = new ExpandableExam.Builder(exam)
            .withExamItems(Collections.singletonList(examItem))
            .withExamSegments(Collections.singletonList(examSegment))
            .withExamPages(Collections.singletonList(page))
            .build();

        when(mockExpandableExamService.findExam(exam.getId(), ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)).thenReturn(Optional.of(expandableExam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));

        ItemCandidatesData data = itemCandidatesService.getItemCandidates(exam.getId());

        assertThat(data.getSegmentKey()).isEqualTo(segment.getKey());
    }

    @Test
    public void shouldCleanUpSegments() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segment1")
            .withSegmentPosition(1)
            .withExamItemCount(2)
            .withIsSatisfied(true)
            .build();

        ExamSegment examSegment2 = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segment2")
            .withSegmentPosition(1)
            .withExamItemCount(1)
            .withIsSatisfied(false)
            .build();

        ExamSegment examSegment3 = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentPosition(2)
            .withSegmentKey("segment3")
            .withExamItemCount(1)
            .withIsSatisfied(false)
            .build();


        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(Arrays.asList(examSegment, examSegment2, examSegment3));

        itemCandidatesService.cleanupDismissedItemCandidates(1L, examId);

        ArgumentCaptor<ExamSegment> varArgs = ArgumentCaptor.forClass(ExamSegment.class);
        verify(mockExamSegmentService).update(varArgs.capture());

        assertThat(varArgs.getAllValues()).hasSize(1);
        assertThat(varArgs.getAllValues().get(0).getSegmentKey()).isEqualTo("segment2");
    }
}