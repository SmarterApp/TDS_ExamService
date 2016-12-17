package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import tds.assessment.AdaptiveSegment;
import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.FixedFormSegment;
import tds.assessment.Form;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.config.Accommodation;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.models.ExamSegment;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.repositories.ExamSegmentCommandRepository;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.FieldTestService;
import tds.exam.services.SegmentPoolService;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamSegmentServiceImplTest {
    private ExamSegmentServiceImpl examSegmentService;
    private ExamSegmentCommandRepository mockExamSegmentCommandRepository;
    private ExamAccommodationService mockExamAccommodationService;
    private SegmentPoolService mockSegmentPoolService;
    private FieldTestService mockFieldTestService;

    @Captor
    private ArgumentCaptor<List<ExamSegment>> examSegmentCaptor;

    @Before
    public void setUp() {
        mockExamSegmentCommandRepository = mock(ExamSegmentCommandRepository.class);
        mockExamAccommodationService = mock(ExamAccommodationService.class);
        mockSegmentPoolService = mock(SegmentPoolService.class);
        mockFieldTestService = mock(FieldTestService.class);
        examSegmentService = new ExamSegmentServiceImpl(mockExamSegmentCommandRepository, mockExamAccommodationService,
            mockSegmentPoolService, mockFieldTestService);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNoLanguageFound() {
        Exam exam = new ExamBuilder().build();
        Segment segment = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();

        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(new ArrayList<>());
        examSegmentService.initializeExamSegments(exam, assessment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNoFormFoundForLanguage() {
        Exam exam = new ExamBuilder().build();
        ExamAccommodation language = new ExamAccommodation.Builder()
            .withCode("ENU")
            .build();
        Segment segment = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.FIXED_FORM)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();

        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        examSegmentService.initializeExamSegments(exam, assessment);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNoItemsFound() {
        Exam exam = new ExamBuilder().build();
        ExamAccommodation language = new ExamAccommodation.Builder()
            .withCode("ENU")
            .build();
        AdaptiveSegment segment = (AdaptiveSegment) new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withMaxItems(5)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        // Empty segment pool should result in an error
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(0, 0, new HashSet<>());

        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language.getCode())).thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language.getCode()))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment.getKey(), language.getCode()))
            .thenReturn(2);
        examSegmentService.initializeExamSegments(exam, assessment);
    }

    @Test
    public void shouldInitializeSegmentedAssessmentWithFieldTestAdaptiveAndFixedForm() {
        Exam exam = new ExamBuilder().build();
        ExamAccommodation language = new ExamAccommodation.Builder()
            .withCode("ENU")
            .build();
        Form enuForm = new Form.Builder("formkey-1")
            .withId("formid-1")
            .withLanguage(language.getCode())
            .withItems(Arrays.asList(new Item("item1"), new Item("item2")))
            .build();
        AdaptiveSegment segment1 = (AdaptiveSegment) new SegmentBuilder()
            .withKey("segment1-key")
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withPosition(1)
            .withMaxItems(3)
            .build();
        FixedFormSegment segment2 = (FixedFormSegment) new SegmentBuilder()
            .withKey("segment2-key")
            .withSelectionAlgorithm(Algorithm.FIXED_FORM)
            .withForms(Arrays.asList(enuForm))
            .withPosition(2)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment1, segment2))
            .build();
        SegmentPoolInfo segmentPoolInfo1 = new SegmentPoolInfo(3, 4,
            new HashSet<>(Arrays.asList(
                new Item("item-1"),
                new Item("item-2")
            )));

        // Adaptive Segment w/ field test items
        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment1.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language.getCode()))
            .thenReturn(segmentPoolInfo1);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment1.getKey(), language.getCode()))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment1.getKey(), language.getCode()))
            .thenReturn(2);
        examSegmentService.initializeExamSegments(exam, assessment);
        // ExamSeg 1
        verify(mockExamAccommodationService).findAccommodations(exam.getId(), segment1.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE });
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language.getCode());
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment1.getKey(), language.getCode());
        verify(mockFieldTestService).selectItemGroups(exam, assessment, segment1.getKey(), language.getCode());
        verify(mockExamSegmentCommandRepository).insert(examSegmentCaptor.capture());
        List<ExamSegment> examSegments = examSegmentCaptor.getValue();
        assertThat(examSegments).hasSize(2);

        ExamSegment examSegment1 = null;
        ExamSegment examSegment2 = null;

        for (ExamSegment seg : examSegments) {
            if (seg.getSegmentKey().equals(segment1.getKey())) {
                examSegment1 = seg;
            } else if (seg.getSegmentKey().equals(segment2.getKey())) {
                examSegment2 = seg;
            }
        }

        assertThat(examSegment1.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment1.getSegmentId()).isEqualTo(segment1.getSegmentId());
        assertThat(examSegment1.getSegmentPosition()).isEqualTo(segment1.getPosition());
        assertThat(examSegment1.getSegmentKey()).isEqualTo(segment1.getKey());
        assertThat(examSegment1.getAlgorithm()).isEqualTo(segment1.getSelectionAlgorithm());
        assertThat(examSegment1.getExamItemCount()).isEqualTo(segmentPoolInfo1.getLength());
        assertThat(examSegment1.getFieldTestItemCount()).isEqualTo(2);
        assertThat(examSegment1.isPermeable()).isFalse();
        assertThat(examSegment1.isSatisfied()).isFalse();
        assertThat(examSegment1.getPoolCount()).isEqualTo(segmentPoolInfo1.getPoolCount());
        assertThat(examSegment1.getItemPool()).containsExactlyInAnyOrder("item-1", "item-2");

        assertThat(examSegment2.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment2.getSegmentId()).isEqualTo(segment2.getSegmentId());
        assertThat(examSegment2.getSegmentPosition()).isEqualTo(segment2.getPosition());
        assertThat(examSegment2.getSegmentKey()).isEqualTo(segment2.getKey());
        assertThat(examSegment2.getAlgorithm()).isEqualTo(segment2.getSelectionAlgorithm());
        assertThat(examSegment2.getExamItemCount()).isEqualTo(enuForm.getLength());
        assertThat(examSegment2.getFieldTestItemCount()).isEqualTo(0);
        assertThat(examSegment2.isPermeable()).isFalse();
        assertThat(examSegment2.isSatisfied()).isFalse();
        assertThat(examSegment2.getPoolCount()).isEqualTo(enuForm.getLength());
        assertThat(examSegment2.getItemPool()).isEmpty();
    }

    @Test
    public void shouldInitializeSegmentedAssessmentWithNoItemsNoFieldTestAdaptive() {
        Exam exam = new ExamBuilder().build();
        ExamAccommodation language = new ExamAccommodation.Builder()
            .withCode("ENU")
            .build();
        AdaptiveSegment segment1 = (AdaptiveSegment) new SegmentBuilder()
            .withKey("segment1-key")
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .build();
        AdaptiveSegment segment2 = (AdaptiveSegment) new SegmentBuilder()
            .withKey("segment2-key")
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment1, segment2))
            .build();
        SegmentPoolInfo segmentPoolInfo1 = new SegmentPoolInfo(3, 4,
            new HashSet<>(Arrays.asList(
                new Item("item-1"),
                new Item("item-2")
            )));
        SegmentPoolInfo segmentPoolInfo2 = new SegmentPoolInfo(0, 0,
            new HashSet<>());

        // ExamSeg 1
        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment1.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language.getCode()))
            .thenReturn(segmentPoolInfo1);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment1.getKey(), language.getCode()))
            .thenReturn(false);
        // ExamSeg 2
        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment2.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment2, assessment.getItemConstraints(),
            language.getCode()))
            .thenReturn(segmentPoolInfo2);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment2.getKey(), language.getCode()))
            .thenReturn(false);

        examSegmentService.initializeExamSegments(exam, assessment);
        // ExamSeg 1
        verify(mockExamAccommodationService).findAccommodations(exam.getId(), segment1.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE });
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language.getCode());
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment1.getKey(), language.getCode());
        // ExamSeg 2
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment2, assessment.getItemConstraints(),
            language.getCode());
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment2.getKey(), language.getCode());

        verify(mockExamSegmentCommandRepository).insert(examSegmentCaptor.capture());
        List<ExamSegment> examSegments = examSegmentCaptor.getValue();

        assertThat(examSegments).hasSize(2);
        Optional<ExamSegment> maybeExamSegment2 = examSegments.stream()
            .filter(seg -> seg.getSegmentKey().equals(segment2.getKey()))
            .findFirst();
        assertThat(maybeExamSegment2.isPresent()).isTrue();
        ExamSegment examSegment2 = maybeExamSegment2.get();
        assertThat(examSegment2.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment2.getSegmentId()).isEqualTo(segment2.getSegmentId());
        assertThat(examSegment2.getSegmentPosition()).isEqualTo(segment2.getPosition());
        assertThat(examSegment2.getSegmentKey()).isEqualTo(segment2.getKey());
        assertThat(examSegment2.getAlgorithm()).isEqualTo(segment2.getSelectionAlgorithm());
        assertThat(examSegment2.getExamItemCount()).isEqualTo(segmentPoolInfo2.getLength());
        assertThat(examSegment2.getFieldTestItemCount()).isEqualTo(0);
        assertThat(examSegment2.isPermeable()).isFalse();
        assertThat(examSegment2.isSatisfied()).isTrue();
        assertThat(examSegment2.getPoolCount()).isEqualTo(segmentPoolInfo2.getPoolCount());
        assertThat(examSegment2.getItemPool()).isEmpty();
    }

    @Test
    public void shouldInitializeNonSegAssessmentFixedForm() {
        Exam exam = new ExamBuilder().build();
        ExamAccommodation language = new ExamAccommodation.Builder()
            .withCode("ENU")
            .build();
        Form enuForm = new Form.Builder("formkey-1")
            .withId("formid-1")
            .withLanguage(language.getCode())
            .withItems(Arrays.asList(new Item("item1"), new Item("item2")))
            .build();
        Form esnForm = new Form.Builder("formkey-2")
            .withId("formid-2")
            .withLanguage("ESN")
            .build();
        Segment segment = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.FIXED_FORM)
            .withForms(Arrays.asList(enuForm, esnForm))
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();

        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        examSegmentService.initializeExamSegments(exam, assessment);
        verify(mockExamSegmentCommandRepository).insert(examSegmentCaptor.capture());
        List<ExamSegment> examSegments = examSegmentCaptor.getValue();
        ExamSegment examSegment = examSegments.get(0);
        assertThat(examSegments).hasSize(1);
        assertThat(examSegment.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment.getSegmentId()).isEqualTo(segment.getSegmentId());
        assertThat(examSegment.getSegmentPosition()).isEqualTo(segment.getPosition());
        assertThat(examSegment.getSegmentKey()).isEqualTo(segment.getKey());
        assertThat(examSegment.getAlgorithm()).isEqualTo(segment.getSelectionAlgorithm());
        assertThat(examSegment.getFieldTestItemCount()).isEqualTo(0);
        assertThat(examSegment.getFormId()).isEqualTo(enuForm.getId());
        assertThat(examSegment.getFormKey()).isEqualTo(enuForm.getKey());
        assertThat(examSegment.getExamItemCount()).isEqualTo(enuForm.getItems().size());
        assertThat(examSegment.isPermeable()).isFalse();
        assertThat(examSegment.isSatisfied()).isFalse();
    }

    @Test
    public void shouldInitializeNonSegAssessmentNoFieldTestAdaptive() {
        Exam exam = new ExamBuilder().build();
        ExamAccommodation language = new ExamAccommodation.Builder()
            .withCode("ENU")
            .build();
        AdaptiveSegment segment = (AdaptiveSegment) new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(3, 4,
            new HashSet<>(Arrays.asList(
                new Item("item-1"),
                new Item("item-2"),
                new Item("item-3"),
                new Item("item-4")
            )));

        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language.getCode()))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language.getCode()))
            .thenReturn(false);
        examSegmentService.initializeExamSegments(exam, assessment);
        verify(mockExamAccommodationService).findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE });
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language.getCode());
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey(), language.getCode());

        verify(mockExamSegmentCommandRepository).insert(examSegmentCaptor.capture());
        List<ExamSegment> examSegments = examSegmentCaptor.getValue();
        ExamSegment examSegment = examSegments.get(0);
        assertThat(examSegments).hasSize(1);
        assertThat(examSegment.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment.getSegmentId()).isEqualTo(segment.getSegmentId());
        assertThat(examSegment.getSegmentPosition()).isEqualTo(segment.getPosition());
        assertThat(examSegment.getSegmentKey()).isEqualTo(segment.getKey());
        assertThat(examSegment.getAlgorithm()).isEqualTo(segment.getSelectionAlgorithm());
        assertThat(examSegment.getExamItemCount()).isEqualTo(segmentPoolInfo.getLength());
        assertThat(examSegment.getFieldTestItemCount()).isEqualTo(0);
        assertThat(examSegment.isPermeable()).isFalse();
        assertThat(examSegment.isSatisfied()).isFalse();
        assertThat(examSegment.getPoolCount()).isEqualTo(segmentPoolInfo.getPoolCount());
        assertThat(examSegment.getItemPool()).containsExactlyInAnyOrder("item-1", "item-2", "item-3", "item-4");
    }

    @Test
    public void shouldInitializeNonSegAssessmentWithFieldTestAdaptiveMaxItemsNotEqualToPool() {
        Exam exam = new ExamBuilder().build();
        ExamAccommodation language = new ExamAccommodation.Builder()
            .withCode("ENU")
            .build();
        AdaptiveSegment segment = (AdaptiveSegment) new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withMaxItems(7)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(5, 4,
            new HashSet<>(Arrays.asList(
                new Item("item-1"),
                new Item("item-2"),
                new Item("item-3"),
                new Item("item-4")
            )));

        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language.getCode()))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language.getCode()))
            .thenReturn(true);
        examSegmentService.initializeExamSegments(exam, assessment);
        verify(mockExamAccommodationService).findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE });
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language.getCode());
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey(), language.getCode());

        verify(mockExamSegmentCommandRepository).insert(examSegmentCaptor.capture());
        List<ExamSegment> examSegments = examSegmentCaptor.getValue();
        ExamSegment examSegment = examSegments.get(0);
        assertThat(examSegments).hasSize(1);
        assertThat(examSegment.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment.getSegmentId()).isEqualTo(segment.getSegmentId());
        assertThat(examSegment.getSegmentPosition()).isEqualTo(segment.getPosition());
        assertThat(examSegment.getSegmentKey()).isEqualTo(segment.getKey());
        assertThat(examSegment.getAlgorithm()).isEqualTo(segment.getSelectionAlgorithm());
        assertThat(examSegment.getExamItemCount()).isEqualTo(segmentPoolInfo.getLength());
        assertThat(examSegment.getFieldTestItemCount()).isEqualTo(0);
        assertThat(examSegment.isPermeable()).isFalse();
        assertThat(examSegment.isSatisfied()).isFalse();
        assertThat(examSegment.getPoolCount()).isEqualTo(segmentPoolInfo.getPoolCount());
        assertThat(examSegment.getItemPool()).containsExactlyInAnyOrder("item-1", "item-2", "item-3", "item-4");
    }

    @Test
    public void shouldInitializeNonSegAssessmentWithFieldTestAdaptive() {
        Exam exam = new ExamBuilder().build();
        ExamAccommodation language = new ExamAccommodation.Builder()
            .withCode("ENU")
            .build();
        AdaptiveSegment segment = (AdaptiveSegment) new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withMaxItems(5)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(segment.getMaxItems(), 4,
            new HashSet<>(Arrays.asList(
                new Item("item-1"),
                new Item("item-2"),
                new Item("item-3"),
                new Item("item-4")
            )));

        when(mockExamAccommodationService.findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE })).thenReturn(Arrays.asList(language));
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language.getCode()))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language.getCode()))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment.getKey(), language.getCode()))
            .thenReturn(2);
        examSegmentService.initializeExamSegments(exam, assessment);
        verify(mockExamAccommodationService).findAccommodations(exam.getId(), segment.getKey(),
            new String[] { Accommodation.ACCOMMODATION_TYPE_LANGUAGE });
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language.getCode());
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey(), language.getCode());
        verify(mockFieldTestService).selectItemGroups(exam, assessment, segment.getKey(), language.getCode());

        verify(mockExamSegmentCommandRepository).insert(examSegmentCaptor.capture());
        List<ExamSegment> examSegments = examSegmentCaptor.getValue();
        ExamSegment examSegment = examSegments.get(0);
        assertThat(examSegments).hasSize(1);
        assertThat(examSegment.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment.getSegmentId()).isEqualTo(segment.getSegmentId());
        assertThat(examSegment.getSegmentPosition()).isEqualTo(segment.getPosition());
        assertThat(examSegment.getSegmentKey()).isEqualTo(segment.getKey());
        assertThat(examSegment.getAlgorithm()).isEqualTo(segment.getSelectionAlgorithm());
        assertThat(examSegment.getExamItemCount()).isEqualTo(segmentPoolInfo.getLength());
        assertThat(examSegment.getFieldTestItemCount()).isEqualTo(2);
        assertThat(examSegment.isPermeable()).isFalse();
        assertThat(examSegment.isSatisfied()).isFalse();
        assertThat(examSegment.getPoolCount()).isEqualTo(segmentPoolInfo.getPoolCount());
        assertThat(examSegment.getItemPool()).containsExactlyInAnyOrder("item-1", "item-2", "item-3", "item-4");
    }
}
