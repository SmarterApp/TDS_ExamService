package tds.exam.services.item.selection;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public void shouldReturnFirstItemCandidates() throws ReturnStatusException {
        Segment segment = new SegmentBuilder()
            .withKey("segmentKey")
            .withSegmentId("segmentId")
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
//            .withExamItemCount(1)
            .withIsSatisfied(false)
            .build();

        ExamPage page = new ExamPageBuilder()
            .withSegmentId(segment.getSegmentId())
            .withSegmentKey(segment.getKey())
            .withSegmentPosition(examSegment.getSegmentPosition())
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

        List<ItemCandidatesData> candidates = itemCandidatesService.getAllItemCandidates(exam.getId());

        assertThat(candidates).hasSize(1);

        ItemCandidatesData data = candidates.get(0);

        assertThat(data.getSegmentKey()).isEqualTo(segment.getKey());
    }
}