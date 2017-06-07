package tds.exam.services.item.selection;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.assessment.Form;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.common.Algorithm;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.builder.FieldTestItemGroupBuilder;
import tds.exam.builder.ItemBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.models.ExamAccommodationFilter;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.models.ItemGroupHistory;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamHistoryService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamService;
import tds.exam.services.ExpandableExamService;
import tds.exam.services.FieldTestService;
import tds.exam.services.ItemPoolService;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.model.AlgorithmType;
import tds.itemselection.model.OffGradeResponse;
import tds.itemselection.services.ItemCandidatesService;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.isA;
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
    private ExamHistoryService mockExamHistoryService;

    @Mock
    private ExamAccommodationService mockExamAccommodationService;

    @Mock
    private ItemPoolService mockItemPoolService;

    @Mock
    private ExamService mockExamService;

    @Captor
    private ArgumentCaptor<Collection<ExamAccommodationFilter>> examAccommodationFiltersCapture;

    @Before
    public void setUp() {
        itemCandidatesService = new ItemCandidateServiceImpl(mockExpandableExamService,
            mockFieldTestService,
            mockExamSegmentService,
            mockAssessmentService,
            mockExamHistoryService,
            mockExamAccommodationService,
            mockItemPoolService,
            mockExamService);
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

        assertThat(candidates).hasSize(2);

        ItemCandidatesData data = candidates.get(0);

        assertThat(data.getSegmentKey()).isEqualTo(segment.getKey());

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
        assertThat(data.isActive()).isTrue();
    }

    @Test
    public void shouldReturnASatisfiedItemCandidateIfThereAreNoItemCandidates() throws ReturnStatusException {
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

        ExpandableExam expandableExam = new ExpandableExam.Builder(exam)
            .withExamItems(Collections.singletonList(examItem))
            .withExamSegments(Collections.singletonList(examSegment))
            .withExamPages(Collections.singletonList(page))
            .build();

        when(mockExpandableExamService.findExam(exam.getId(), ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)).thenReturn(Optional.of(expandableExam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));

        ItemCandidatesData data = itemCandidatesService.getItemCandidates(exam.getId());

        assertThat(data).isNotNull();
        assertThat(data.getAlgorithm()).isEqualTo(AlgorithmType.SATISFIED.getType());
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
        assertThat(varArgs.getAllValues().get(0).getSegmentKey()).isEqualTo("segment3");
    }

    @Test
    public void shouldSetExamSegmentSatisifed() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segment1")
            .withSegmentPosition(1)
            .withExamItemCount(2)
            .withIsSatisfied(false)
            .build();

        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, 1)).thenReturn(Optional.of(examSegment));

        assertThat(itemCandidatesService.setSegmentSatisfied(examId, 1, "reason")).isTrue();

        ArgumentCaptor<ExamSegment> varArgs = ArgumentCaptor.forClass(ExamSegment.class);
        verify(mockExamSegmentService).update(varArgs.capture());

        assertThat(varArgs.getAllValues()).hasSize(1);
        assertThat(varArgs.getValue().getSegmentKey()).isEqualTo("segment1");
    }

    @Test
    public void shouldProcessFieldTestItems() throws ReturnStatusException {
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
            .withExamItemCount(1)
            .withIsSatisfied(false)
            .withAlgorithm(Algorithm.ADAPTIVE_2)
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
            .withFieldTest(true)
            .build();

        ExpandableExam expandableExam = new ExpandableExam.Builder(exam)
            .withExamItems(Arrays.asList(examItem, examItem2))
            .withExamSegments(Collections.singletonList(examSegment))
            .withExamPages(Collections.singletonList(page))
            .build();

        FieldTestItemGroup fieldTestItemGroup = new FieldTestItemGroupBuilder("groupKey")
            .withSegmentKey("segmentKey")
            .withAdministeredAt(Instant.now())
            .build();

        when(mockFieldTestService.findUsageInExam(exam.getId())).thenReturn(Collections.singletonList(fieldTestItemGroup));

        when(mockExpandableExamService.findExam(exam.getId(), ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)).thenReturn(Optional.of(expandableExam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));

        itemCandidatesService.getAllItemCandidates(exam.getId());

        ArgumentCaptor<ExamSegment> varArgs = ArgumentCaptor.forClass(ExamSegment.class);
        verify(mockExamSegmentService).update(varArgs.capture());

        assertThat(varArgs.getValue().getSegmentKey()).isEqualTo(examSegment.getSegmentKey());
    }

    @Test
    public void shouldGetTestItemGroupForFixedFormWithNoSegmentItemGroup() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();

        Item item = new ItemBuilder("187-2345")
            .withGroupId("")
            .build();

        Item item2 = new ItemBuilder("187-6789")
            .withGroupId("")
            .build();

        Form form = new Form.Builder("form-123")
            .withCohort("cohort")
            .withSegmentKey("segmentKey")
            .withLanguage("ENU")
            .withItems(Arrays.asList(item, item2))
            .build();

        Segment segment = new SegmentBuilder()
            .withKey("segmentKey")
            .withSegmentId("segmentId")
            .withPosition(1)
            .withForms(Collections.singletonList(form))
            .withItems(Arrays.asList(item, item2))
            .build();

        Assessment assessment = new AssessmentBuilder()
            .withKey("(SBAC) MATH 3")
            .withSegments(Collections.singletonList(segment))
            .build();

        Exam exam = new ExamBuilder()
            .withId(examId)
            .withClientName("client")
            .withAssessmentKey(assessment.getKey())
            .withLanguageCode("ENU")
            .build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(exam.getId())
            .withSegmentPosition(0)
            .withSegmentKey(segment.getKey())
            .withSegmentPosition(segment.getPosition())
            .withExamItemCount(2)
            .withIsSatisfied(false)
            .withFormCohort("cohort")
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

        when(mockExpandableExamService.findExam(examId, ExpandableExamAttributes.EXAM_SEGMENTS)).thenReturn(Optional.of(expandableExam));
        when(mockAssessmentService.findAssessment("client", assessment.getKey())).thenReturn(Optional.of(assessment));

        ItemGroup itemGroup = itemCandidatesService.getItemGroup(examId, segment.getKey(), "I-187-6789", "A", false);

        assertThat(itemGroup.getGroupID()).isEqualTo("I-187-6789");
        assertThat(itemGroup.getItemCount()).isEqualTo(1);
        assertThat(itemGroup.getItems().get(0).getItemID()).isEqualTo(item2.getId());
    }

    @Test
    public void shouldFindStudentHistory() throws ItemSelectionException {
        String segmentKey = "segmentKey";
        Exam exam = new ExamBuilder().build();
        ExamSegment examSegment = new ExamSegmentBuilder()
            .withSegmentKey(segmentKey)
            .withExamId(exam.getId())
            .build();

        ExamPage examPage = new ExamPageBuilder()
            .withExamId(exam.getId())
            .withSegmentKey(segmentKey)
            .build();

        ExamItem examItem = new ExamItemBuilder()
            .withExamPageId(examPage.getId())
            .withGroupId("groupId")
            .build();

        ExamItem examItemScored = new ExamItemBuilder()
            .withExamPageId(examPage.getId())
            .withId(UUID.randomUUID())
            .withItemKey("score-item-key")
            .withResponse(new ExamItemResponseBuilder()
                .withScore(new ExamItemResponseScore.Builder()
                    .withScore(1)
                    .build())
                .build())
            .withGroupId("groupId")
            .build();

        ExpandableExam expandableExam = new ExpandableExam.Builder(exam)
            .withExamPages(Collections.singletonList(examPage))
            .withExamSegments(Collections.singletonList(examSegment))
            .withExamItems(Arrays.asList(examItem, examItemScored))
            .build();

        Assessment assessment = new AssessmentBuilder().build();

        when(mockExpandableExamService.findExam(exam.getId(), ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)).thenReturn(Optional.of(expandableExam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockFieldTestService.findUsageInExam(exam.getId())).thenReturn(Collections.emptyList());

        when(mockExamHistoryService.findPreviousItemGroups(exam.getStudentId(), exam.getId(), exam.getAssessmentId()))
            .thenReturn(Collections.singletonList(new ItemGroupHistory(UUID.randomUUID(), Collections.singleton("G-123-11"))));

        StudentHistory2013 history = itemCandidatesService.loadOppHistory(exam.getId(), segmentKey);

        assertThat(history.get_itemPool()).containsOnly(examSegment.getItemPool().toArray(new String[examSegment.getItemPool().size()]));
        assertThat(history.get_previousFieldTestItemGroups()).isEmpty();
        assertThat(history.get_previousResponses()).hasSize(2);

        ItemResponse response = history.get_previousResponses().get(0);
        assertThat(response.itemID).isEqualTo(examItem.getItemKey());
        assertThat(response.getScore()).isEqualTo(-1);

        response = history.get_previousResponses().get(1);
        assertThat(response.itemID).isEqualTo(examItemScored.getItemKey());
        assertThat(response.getScore()).isEqualTo(1);
    }

    @Test
    public void shouldIgnoreOffGradeWithoutAccommodationPresent() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();

        OffGradeResponse response = itemCandidatesService.addOffGradeItems(examId, "Grade 3", "segmentKey");

        assertThat(response.getStatus()).isEqualTo(OffGradeResponse.FAILED);
        assertThat(response.getReason()).isEqualTo("offgrade accommodation not exists");
    }

    @Test
    public void shouldIgnoreOffGradeIfAlreadyAdded() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();

        ExamAccommodation examAccommodation = new ExamAccommodationBuilder().withCode("Grade 3 IN").build();

        when(mockExamAccommodationService.findAccommodations(isA(UUID.class), examAccommodationFiltersCapture.capture())).thenReturn(Collections.singletonList(examAccommodation));

        OffGradeResponse response = itemCandidatesService.addOffGradeItems(examId, "Grade 3", "segmentKey");

        assertThat(response.getStatus()).isEqualTo(OffGradeResponse.SUCCESS);
        assertThat(response.getReason()).isEqualTo("already set");
    }

    @Test
    public void shouldAddOffGradeItems() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();

        Exam exam = new ExamBuilder()
            .withId(examId)
            .build();
        ExamAccommodation examAccommodation = new ExamAccommodationBuilder().withCode("Grade 3 OUT").build();
        Segment segment = new SegmentBuilder().withKey("SegKey").withSelectionAlgorithm(Algorithm.ADAPTIVE_2).build();
        Assessment assessment = new AssessmentBuilder()
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withSegments(Collections.singletonList(segment))
            .build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withIsSatisfied(false)
            .withSegmentKey("SegKey")
            .withAlgorithm(Algorithm.ADAPTIVE_2)
            .withItemPool(Collections.singleton("187-0000"))
            .build();

        Item item = new ItemBuilder("187-1234").build();

        when(mockExamAccommodationService.findAccommodations(isA(UUID.class), examAccommodationFiltersCapture.capture())).thenReturn(Collections.singletonList(examAccommodation));
        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(Collections.singletonList(examSegment));
        when(mockItemPoolService.getItemPool(examId, assessment.getItemConstraints(), segment.getItems(exam.getLanguageCode()))).thenReturn(Collections.singleton(item));

        OffGradeResponse response = itemCandidatesService.addOffGradeItems(examId, "Grade 3", "segmentKey");

        ArgumentCaptor<ExamSegment> examSegmentArgumentCaptor = ArgumentCaptor.forClass(ExamSegment.class);
        verify(mockExamSegmentService).update(examSegmentArgumentCaptor.capture());

        assertThat(response.getStatus()).isEqualTo(OffGradeResponse.SUCCESS);
        assertThat(response.getReason()).isEqualTo("");
        assertThat(examSegmentArgumentCaptor.getValue().getItemPool()).containsExactlyInAnyOrder("187-1234", "187-0000");
    }
}