package tds.exam.services.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.ItemProperty;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.FieldTestService;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldTestServiceImplTest {
    private FieldTestService fieldTestService;

    @Before
    public void setUp() {
        fieldTestService = new FieldTestServiceImpl();
    }

    @Test
    public void shouldReturnFalseForNoFieldTestItems() {
        final String segmentKey = "segment-key";
        final String language = "ENU";

        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(false);

        Segment seg1 = new Segment(segmentKey);
        seg1.setItems(items);
        seg1.setFieldTestMinItems(1);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnTrueForSimulationWithFieldTestItems() {
        final String segmentKey = "segment-key";
        final String language = "ENU";
        final Exam exam = new ExamBuilder()
            .withEnvironment("SIMULATION")
            .build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldReturnTrueForNonSegmentedAssessmentWithFieldTestItemsNullWindow() {
        final String segmentKey = "segment-key";
        final String language = "ENU";
        final Exam exam = new ExamBuilder().build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldReturnFalseForNonSegmentedAssessmentWithFieldTestItemsNullStartDate() {
        final String segmentKey = "segment-key";
        final String language = "ENU";
        final Exam exam = new ExamBuilder().build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestEndDate(Instant.now().minus(50000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnFalseForNonSegmentedAssessmentWithFieldTestItemsNullEndDate() {
        final String segmentKey = "segment-key";
        final String language = "ENU";
        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().plus(50000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnTrueForNonSegmentedAssessmentWithFieldTestItemsNullEndDate() {
        final String segmentKey = "segment-key";
        final String language = "ENU";
        final Exam exam = new ExamBuilder().build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().minus(50000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldReturnFalseForNonSegmentedAssessmentOutOfFTWindow() {
        final String segmentKey = "segment-key";
        final String language = "ENU";
        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey);
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

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnFalseForSegmentedAssessmentOutOfSegmentFTWindow() {
        final String segmentKey = "segment-key";
        final String segmentId = "segment-id";
        final String language = "ENU";
        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey);
        seg1.setSegmentId(segmentId);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);
        // Non-eligible FT window
        seg1.setFieldTestStartDate(Instant.now().minus(100000));
        seg1.setFieldTestEndDate(Instant.now().minus(2000000));

        Segment seg2 = new Segment("anotherSegment");

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);
        segments.add(seg2);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().minus(100000))
            .withFieldTestEndDate(Instant.now().plus(2000000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isFalse();
    }

    @Test
    public void shouldReturnTrueForSegmentedAssessmentNullSegFTWindow() {
        final String segmentKey = "segment-key";
        final String segmentId = "segment-id";
        final String language = "ENU";
        final Exam exam = new ExamBuilder().build();
        List<Item> items = createTestItems(true);

        // Null field test start/end dates - eligible FT window
        Segment seg1 = new Segment(segmentKey);
        seg1.setSegmentId(segmentId);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);

        Segment seg2 = new Segment("anotherSegment");

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);
        segments.add(seg2);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestStartDate(Instant.now().minus(100000))
            .withFieldTestEndDate(Instant.now().plus(2000000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isTrue();
    }

    @Test
    public void shouldReturnTrueForSegmentedAssessmentInSegFTWindow() {
        final String segmentKey = "segment-key";
        final String language = "ENU";

        final Exam exam = new ExamBuilder().build();

        List<Item> items = createTestItems(true);

        Segment seg1 = new Segment(segmentKey);
        seg1.setFieldTestMinItems(1);
        seg1.setItems(items);
        seg1.setFieldTestStartDate(Instant.now().minus(100000));
        seg1.setFieldTestEndDate(Instant.now().plus(2000000));
        Segment seg2 = new Segment("anotherSegment");

        List<Segment> segments = new ArrayList<>();
        segments.add(seg1);
        segments.add(seg2);

        Assessment assessment = new AssessmentBuilder()
            .withSegments(segments)
            // This is an eligible FT window
            .withFieldTestEndDate(Instant.now().plus(2000000))
            .build();

        boolean isEligible = fieldTestService.isFieldTestEligible(exam, assessment, segmentKey, language);
        assertThat(isEligible).isTrue();
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
