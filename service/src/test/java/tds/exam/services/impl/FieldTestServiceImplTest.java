package tds.exam.services.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.ItemProperty;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.FieldTestItemGroupBuilder;
import tds.exam.builder.ItemBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.FieldTestItemGroupCommandRepository;
import tds.exam.repositories.FieldTestItemGroupQueryRepository;
import tds.exam.services.FieldTestItemGroupSelector;
import tds.exam.services.FieldTestService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FieldTestServiceImplTest {
    private FieldTestService fieldTestService;

    @Mock
    private FieldTestItemGroupCommandRepository mockFieldTestItemGroupCommandRepository;

    @Mock
    private FieldTestItemGroupQueryRepository mockFieldTestItemGroupQueryRepository;

    @Mock
    private FieldTestItemGroupSelector mockFieldTestItemGroupSelector;

    @Captor
    private ArgumentCaptor<List<FieldTestItemGroup>> fieldTestItemGroupInsertCaptor;

    @Before
    public void setUp() {
        fieldTestService = new FieldTestServiceImpl(mockFieldTestItemGroupQueryRepository, mockFieldTestItemGroupCommandRepository,
            mockFieldTestItemGroupSelector);
    }

    @Test
    public void shouldReturnFalseForNoFieldTestItems() {
        final String segmentKey = "segment-key";

        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(false);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setItems(items);
        seg1.setFieldTestMinItems(1);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnTrueForSimulationWithFieldTestItems() {
        final String segmentKey = "segment-key";
        final Exam exam = new ExamBuilder()
            .withEnvironment("SIMULATION")
            .build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldReturnTrueForNonSegmentedAssessmentWithFieldTestItemsNullWindow() {
        final String segmentKey = "segment-key";
        final String language = "ENU";
        final Exam exam = new ExamBuilder().build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldReturnFalseForNonSegmentedAssessmentWithFieldTestItemsNullStartDate() {
        final String segmentKey = "segment-key";
        final Exam exam = new ExamBuilder().build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestEndDate(Instant.now().minus(50000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnFalseForNonSegmentedAssessmentWithFieldTestItemsNullEndDate() {
        final String segmentKey = "segment-key";
        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().plus(50000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnTrueForNonSegmentedAssessmentWithFieldTestItemsNullEndDate() {
        final String segmentKey = "segment-key";
        final Exam exam = new ExamBuilder().build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().minus(50000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldReturnFalseForNonSegmentedAssessmentOutOfFTWindow() {
        final String segmentKey = "segment-key";
        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().plus(100000))
            .withFieldTestEndDate(Instant.now().plus(2000000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnFalseForSegmentedAssessmentOutOfSegmentFTWindow() {
        final String segmentKey = "segment-key";
        final String segmentId = "segment-id";
        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setSegmentId(segmentId);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);
        // Non-eligible FT window
        seg1.setFieldTestStartDate(Instant.now().minus(100000));
        seg1.setFieldTestEndDate(Instant.now().minus(2000000));

        Segment seg2 = new Segment("anotherSegment", Algorithm.ADAPTIVE_2);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);
        segments.add(seg2);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().minus(100000))
            .withFieldTestEndDate(Instant.now().plus(2000000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnTrueForSegmentedAssessmentNullSegFTWindow() {
        final String segmentKey = "segment-key";
        final String segmentId = "segment-id";
        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(true);

        // Null field test start/end dates - eligible FT window
        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setSegmentId(segmentId);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        Segment seg2 = new Segment("anotherSegment", Algorithm.ADAPTIVE_2);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);
        segments.add(seg2);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().minus(100000))
            .withFieldTestEndDate(Instant.now().plus(2000000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldReturnTrueForSegmentedAssessmentInSegFTWindow() {
        final String segmentKey = "segment-key";

        final Exam exam = new ExamBuilder().build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);
        seg1.setFieldTestStartDate(Instant.now().minus(100000));
        seg1.setFieldTestEndDate(Instant.now().plus(2000000));
        Segment seg2 = new Segment("anotherSegment", Algorithm.ADAPTIVE_2);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);
        segments.add(seg2);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestEndDate(Instant.now().plus(2000000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldSelectAndInsertFieldTestItemGroupsWithPreviousSelected() {
        Exam exam = new ExamBuilder().build();
        final String assessmentKey = "assessment-key123";
        Segment segment = new SegmentBuilder()
            .withAssessmentKey(assessmentKey)
            .withFieldTestStartPosition(3)
            .withFieldTestEndPosition(7)
            .withFieldTestMinItems(4)
            .withFieldTestMaxItems(4)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        FieldTestItemGroup ftItemGroup1 = new FieldTestItemGroupBuilder("group-key-1")
            .withGroupId("group-id-1")
            .withNumItems(1)
            .withExamId(exam.getId())
            .withSegmentKey(segment.getKey())
            .build();
        FieldTestItemGroup ftItemGroup2 = new FieldTestItemGroupBuilder("group-key-2")
            .withGroupId("group-id-2")
            .withExamId(exam.getId())
            .withSegmentKey(segment.getKey())
            .withNumItems(1)
            .build();
        FieldTestItemGroup ftItemGroup3 = new FieldTestItemGroupBuilder("group-key-3")
            .withGroupId("group-id-3")
            .withExamId(exam.getId())
            .withSegmentKey(segment.getKey())
            .withNumItems(1)
            .build();
        // This field test item group should be excluded (it is already selected for this exam)s
        FieldTestItemGroup ftItemGroup4 = new FieldTestItemGroupBuilder("group-key-4")
            .withGroupId("group-id-4")
            .withExamId(exam.getId())
            .withSegmentKey(segment.getKey())
            .withNumItems(1)
            .build();

        when(mockFieldTestItemGroupQueryRepository.find(exam.getId(), segment.getKey()))
            .thenReturn(Arrays.asList(ftItemGroup4));
        when(mockFieldTestItemGroupSelector.selectLeastUsedItemGroups(eq(exam), any(),
            any(), eq(segment), eq(segment.getFieldTestMinItems())))
            .thenReturn(Arrays.asList(ftItemGroup1, ftItemGroup2, ftItemGroup3));
        int totalItems = fieldTestService.selectItemGroups(exam, assessment, segment.getKey());
        verify(mockFieldTestItemGroupQueryRepository).find(exam.getId(), segment.getKey());
        verify(mockFieldTestItemGroupCommandRepository).insert(fieldTestItemGroupInsertCaptor.capture());
        verify(mockFieldTestItemGroupSelector).selectLeastUsedItemGroups(eq(exam), any(),
            eq(assessment), eq(segment), eq(segment.getFieldTestMinItems()));

        List<FieldTestItemGroup> insertedItemGroups = fieldTestItemGroupInsertCaptor.getValue();

        assertThat(insertedItemGroups).containsExactlyInAnyOrder(ftItemGroup1, ftItemGroup2, ftItemGroup3);

        FieldTestItemGroup insertedFtGroup1 = null;
        FieldTestItemGroup insertedFtGroup2 = null;
        FieldTestItemGroup insertedFtGroup3 = null;
        FieldTestItemGroup notInsertedFtGroup = null;
        for (FieldTestItemGroup itemGroup : insertedItemGroups) {
            if (itemGroup.getGroupKey().equals(ftItemGroup1.getGroupKey())) {
                insertedFtGroup1 = itemGroup;
            } else if (itemGroup.getGroupKey().equals(ftItemGroup2.getGroupKey())) {
                insertedFtGroup2 = itemGroup;
            } else if (itemGroup.getGroupKey().equals(ftItemGroup3.getGroupKey())) {
                insertedFtGroup3 = itemGroup;
            } else if (itemGroup.getGroupKey().equals(ftItemGroup4.getGroupKey())) {
                notInsertedFtGroup = itemGroup;
            }
        }
        assertThat(totalItems).isEqualTo(4);
        assertThat(insertedFtGroup1.getGroupId()).isEqualTo(ftItemGroup1.getGroupId());
        assertThat(insertedFtGroup1.getBlockId()).isEqualTo(ftItemGroup1.getBlockId());
        assertThat(insertedFtGroup1.getPosition()).isGreaterThanOrEqualTo(segment.getFieldTestStartPosition());
        assertThat(insertedFtGroup1.getLanguageCode()).isEqualTo(exam.getLanguageCode());
        assertThat(insertedFtGroup1.getItemCount()).isEqualTo(1);
        assertThat(insertedFtGroup1.getExamId()).isEqualTo(exam.getId());
        assertThat(insertedFtGroup1.getSegmentId()).isEqualTo(segment.getSegmentId());
        assertThat(insertedFtGroup1.getSegmentKey()).isEqualTo(segment.getKey());
        assertThat(insertedFtGroup1.getSessionId()).isEqualTo(exam.getSessionId());
        assertThat(insertedFtGroup1.getDeletedAt()).isNull();

        assertThat(insertedFtGroup2.getPosition()).isGreaterThanOrEqualTo(segment.getFieldTestStartPosition());
        assertThat(insertedFtGroup2.getItemCount()).isEqualTo(1);

        assertThat(insertedFtGroup3.getPosition()).isGreaterThanOrEqualTo(segment.getFieldTestStartPosition());
        assertThat(insertedFtGroup3.getItemCount()).isEqualTo(1);

        assertThat(notInsertedFtGroup).isNull();
    }

    /* This method will test the case when there are field test item groups that have more items
    *  than there are field test positions for those items in the segment. In this case, the expected number of field test items is
    *  2, but ftItemGroup1 below has 3 items (and therefore should never be selected).
    * */
    @Test
    public void shouldSelectAndInsertFieldTestItemGroupsWithoutEnoughPositionsForAllItems() {
        Exam exam = new ExamBuilder().build();
        final String assessmentKey = "assessment-key123";
        Segment segment = new SegmentBuilder()
            .withAssessmentKey(assessmentKey)
            .withFieldTestStartPosition(3)
            .withFieldTestEndPosition(7)
            .withFieldTestMinItems(2)
            .withFieldTestMaxItems(2)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        FieldTestItemGroup ftItemGroup1 = new FieldTestItemGroupBuilder("group-key-1")
            .withGroupId("group-id-1")
            .withExamId(exam.getId())
            .withSegmentKey(segment.getKey())
            .withNumItems(3)
            .build();
        FieldTestItemGroup ftItemGroup2 = new FieldTestItemGroupBuilder("group-key-2")
            .withGroupId("group-id-2")
            .withExamId(exam.getId())
            .withSegmentKey(segment.getKey())
            .withNumItems(1)
            .build();

        when(mockFieldTestItemGroupQueryRepository.find(exam.getId(), segment.getKey()))
            .thenReturn(new ArrayList<>());
        when(mockFieldTestItemGroupSelector.selectLeastUsedItemGroups(eq(exam), any(),
            any(), eq(segment), eq(segment.getFieldTestMinItems())))
            .thenReturn(Arrays.asList(ftItemGroup1, ftItemGroup2));
        int totalItems = fieldTestService.selectItemGroups(exam, assessment, segment.getKey());
        verify(mockFieldTestItemGroupQueryRepository).find(exam.getId(), segment.getKey());
        verify(mockFieldTestItemGroupCommandRepository).insert(fieldTestItemGroupInsertCaptor.capture());
        verify(mockFieldTestItemGroupSelector).selectLeastUsedItemGroups(eq(exam), any(),
            eq(assessment), eq(segment), eq(segment.getFieldTestMinItems()));

        List<FieldTestItemGroup> insertedItemGroups = fieldTestItemGroupInsertCaptor.getValue();

        assertThat(insertedItemGroups).containsExactly(ftItemGroup2);

        FieldTestItemGroup insertedFtGroup1 = null;
        FieldTestItemGroup insertedFtGroup2 = null;
        for (FieldTestItemGroup itemGroup : insertedItemGroups) {
            if (itemGroup.getGroupKey().equals(ftItemGroup1.getGroupKey())) {
                insertedFtGroup1 = itemGroup;
            } else if (itemGroup.getGroupKey().equals(ftItemGroup2.getGroupKey())) {
                insertedFtGroup2 = itemGroup;
            }
        }

        assertThat(insertedFtGroup1).isNull();

        assertThat(totalItems).isEqualTo(1);
        assertThat(insertedFtGroup2.getGroupId()).isEqualTo(ftItemGroup2.getGroupId());
        assertThat(insertedFtGroup2.getBlockId()).isEqualTo(ftItemGroup2.getBlockId());
        assertThat(insertedFtGroup2.getPosition()).isGreaterThanOrEqualTo(segment.getFieldTestStartPosition());
        assertThat(insertedFtGroup2.getLanguageCode()).isEqualTo(exam.getLanguageCode());
        assertThat(insertedFtGroup2.getItemCount()).isEqualTo(1);
        assertThat(insertedFtGroup2.getExamId()).isEqualTo(exam.getId());
        assertThat(insertedFtGroup2.getSegmentId()).isEqualTo(segment.getSegmentId());
        assertThat(insertedFtGroup2.getSegmentKey()).isEqualTo(segment.getKey());
        assertThat(insertedFtGroup2.getSessionId()).isEqualTo(exam.getSessionId());
        assertThat(insertedFtGroup2.getDeletedAt()).isNull();
    }

    @Test
    public void shouldSelectAndInsertFieldTestItemGroupsMultiItems() {
        Exam exam = new ExamBuilder().build();
        final String assessmentKey = "assessment-key123";
        Item item1G1 = new ItemBuilder("item1group1")
            .withGroupKey("group-key-1")
            .withItemProperties(Arrays.asList(new ItemProperty("Language", "ENU")))
            .build();
        Item item2G1 = new ItemBuilder("item2group1")
            .withGroupKey("group-key-1")
            .withItemProperties(Arrays.asList(new ItemProperty("Language", "ENU")))
            .build();
        Item item3G1 = new ItemBuilder("item3group1")
            .withGroupKey("group-key-1")
            .withItemProperties(Arrays.asList(new ItemProperty("Language", "ENU")))
            .build();
//        Item nonFieldTestItemG1 = new ItemBuilder("nonFtItem-group1")
//            .withGroupKey("group-key-1")
//            .withItemProperties(Arrays.asList(new ItemProperty("Language", "ENU")))
//            .build();
        Item item1G2 = new ItemBuilder("item1group2")
            .withGroupKey("group-key-2")
            .withItemProperties(Arrays.asList(new ItemProperty("Language", "ENU")))
            .build();
        Segment segment = new SegmentBuilder()
            .withAssessmentKey(assessmentKey)
            .withItems(Arrays.asList(item1G1, item2G1, item3G1, item1G2))
//            .withItems(Arrays.asList(item1G1, item2G1, item3G1, item1G2, nonFieldTestItemG1))
            .withFieldTestStartPosition(3)
            .withFieldTestEndPosition(7)
            .withFieldTestMinItems(4)
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withFieldTestMaxItems(4)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        FieldTestItemGroup ftItemGroup1 = new FieldTestItemGroupBuilder("group-key-1")
            .withGroupId("group-id-1")
            .withNumItems(3)
            .withExamId(exam.getId())
            .withSegmentKey(segment.getKey())
            .build();
        FieldTestItemGroup ftItemGroup2 = new FieldTestItemGroupBuilder("group-key-2")
            .withGroupId("group-id-2")
            .withNumItems(1)
            .withExamId(exam.getId())
            .withSegmentKey(segment.getKey())
            .build();
        when(mockFieldTestItemGroupQueryRepository.find(exam.getId(), segment.getKey())).thenReturn(new ArrayList<>());
        when(mockFieldTestItemGroupSelector.selectLeastUsedItemGroups(eq(exam), any(),
            any(), eq(segment), eq(segment.getFieldTestMinItems())))
            .thenReturn(Arrays.asList(ftItemGroup1, ftItemGroup2));
        int totalItems = fieldTestService.selectItemGroups(exam, assessment, segment.getKey());
        verify(mockFieldTestItemGroupQueryRepository).find(exam.getId(), segment.getKey());
        verify(mockFieldTestItemGroupCommandRepository).insert(fieldTestItemGroupInsertCaptor.capture());
        verify(mockFieldTestItemGroupSelector).selectLeastUsedItemGroups(eq(exam), any(),
            eq(assessment), eq(segment), eq(segment.getFieldTestMinItems()));

        List<FieldTestItemGroup> insertedItemGroups = fieldTestItemGroupInsertCaptor.getValue();

        assertThat(insertedItemGroups).containsExactlyInAnyOrder(ftItemGroup1, ftItemGroup2);

        FieldTestItemGroup insertedFtGroup1 = null;
        FieldTestItemGroup insertedFtGroup2 = null;
        for (FieldTestItemGroup itemGroup : insertedItemGroups) {
            if (itemGroup.getGroupKey().equals(ftItemGroup1.getGroupKey())) {
                insertedFtGroup1 = itemGroup;
            } else if (itemGroup.getGroupKey().equals(ftItemGroup2.getGroupKey())) {
                insertedFtGroup2 = itemGroup;
            }
        }
        assertThat(totalItems).isEqualTo(segment.getFieldTestMinItems());

        assertThat(insertedFtGroup1.getGroupId()).isEqualTo(ftItemGroup1.getGroupId());
        assertThat(insertedFtGroup1.getBlockId()).isEqualTo(ftItemGroup1.getBlockId());
        assertThat(insertedFtGroup1.getPosition()).isGreaterThanOrEqualTo(segment.getFieldTestStartPosition());
        assertThat(insertedFtGroup1.getLanguageCode()).isEqualTo(exam.getLanguageCode());
        assertThat(insertedFtGroup1.getItemCount()).isEqualTo(3);
        assertThat(insertedFtGroup1.getExamId()).isEqualTo(exam.getId());
        assertThat(insertedFtGroup1.getSegmentId()).isEqualTo(segment.getSegmentId());
        assertThat(insertedFtGroup1.getSegmentKey()).isEqualTo(segment.getKey());
        assertThat(insertedFtGroup1.getSessionId()).isEqualTo(exam.getSessionId());
        assertThat(insertedFtGroup1.getDeletedAt()).isNull();

        assertThat(insertedFtGroup2.getPosition()).isGreaterThanOrEqualTo(segment.getFieldTestStartPosition());
        assertThat(insertedFtGroup2.getItemCount()).isEqualTo(1);
    }

    private List<Item> createTestItems(boolean isFieldTest) {
        Item item1 = new Item("item-1");
        List<ItemProperty> props1 = new ArrayList<>();
        props1.add(new ItemProperty("Language", "ENU"));
        item1.setItemProperties(props1);
        item1.setFieldTest(isFieldTest);

        Item item2 = new Item("item-2");
        List<ItemProperty> props2 = new ArrayList<>();
        props2.add(new ItemProperty("Language", "ESN"));
        item2.setItemProperties(props2);
        item1.setFieldTest(isFieldTest);

        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        return items;
    }
}
