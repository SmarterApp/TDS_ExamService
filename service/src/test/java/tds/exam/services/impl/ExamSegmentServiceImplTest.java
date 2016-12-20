package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.Form;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.models.ExamSegment;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.repositories.ExamSegmentCommandRepository;
import tds.exam.services.FieldTestService;
import tds.exam.services.FormSelector;
import tds.exam.services.SegmentPoolService;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamSegmentServiceImplTest {
    private ExamSegmentServiceImpl examSegmentService;

    @Mock
    private ExamSegmentCommandRepository mockExamSegmentCommandRepository;

    @Mock
    private SegmentPoolService mockSegmentPoolService;

    @Mock
    private FieldTestService mockFieldTestService;

    @Mock
    private FormSelector mockFormSelector;

    @Captor
    private ArgumentCaptor<List<ExamSegment>> examSegmentCaptor;

    @Before
    public void setUp() {
        examSegmentService = new ExamSegmentServiceImpl(mockExamSegmentCommandRepository,
            mockSegmentPoolService, mockFormSelector, mockFieldTestService);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNoItemsFound() {
        Exam exam = new ExamBuilder().build();
        final String language = "ENU";
        Segment segment = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withMaxItems(5)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        // Empty segment pool should result in an error
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(0, 0, new HashSet<>());

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language)).thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment.getKey(), language))
            .thenReturn(2);
        examSegmentService.initializeExamSegments(exam, assessment);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNoFormFoundFromSelector() {
        Exam exam = new ExamBuilder().build();
        final String language = "ENU";
        Segment segment = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withMaxItems(5)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        // Empty segment pool should result in an error
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(0, 0, new HashSet<>());

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language)).thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment.getKey(), language))
            .thenReturn(2);
        when(mockFormSelector.selectForm(segment, language)).thenReturn(Optional.empty());
        examSegmentService.initializeExamSegments(exam, assessment);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNoFormFoundForCohort() {
        Exam exam = new ExamBuilder().build();
        final String language = "ENU";

        Form form1 = new Form.Builder("form1")
            .withCohort("Tatooine")
            .withLanguage(language)
            .build();
        Form form2 = new Form.Builder("form2")
            .withCohort("Korriban")
            .withLanguage(language)
            .build();
        Segment segment1 = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.FIXED_FORM)
            .withMaxItems(5)
            .withForms(Arrays.asList(form1))
            .build();
        Segment segment2 = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.FIXED_FORM)
            .withMaxItems(5)
            .withForms(Arrays.asList(form2))
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment1, segment2))
            .build();
        // Empty segment pool should result in an error
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(0, 0, new HashSet<>());

        when(mockFormSelector.selectForm(segment1, language)).thenReturn(Optional.of(form1));
        examSegmentService.initializeExamSegments(exam, assessment);
    }

    @Test
    public void shouldInitializeSegmentedAssessmentWithFieldTestAdaptiveAndFixedForm() {
        Exam exam = new ExamBuilder().build();
        final String language = "ENU";
        Form enuForm = new Form.Builder("formkey-1")
            .withId("formid-1")
            .withLanguage(language)
            .withItems(Arrays.asList(new Item("item1"), new Item("item2")))
            .build();
        Segment segment1 = new SegmentBuilder()
            .withKey("segment1-key")
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .withPosition(1)
            .withMaxItems(3)
            .build();
        Segment segment2 = new SegmentBuilder()
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
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo1);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment1.getKey(), language))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment1.getKey(), language))
            .thenReturn(2);
        when(mockFormSelector.selectForm(segment2, language)).thenReturn(Optional.of(enuForm));
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(8);
        // ExamSeg 1
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment1.getKey(), language);
        verify(mockFieldTestService).selectItemGroups(exam, assessment, segment1.getKey(), language);
        verify(mockFormSelector).selectForm(segment2, language);
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
        final String language = "ENU";
        Segment segment1 = new SegmentBuilder()
            .withKey("segment1-key")
            .withSelectionAlgorithm(Algorithm.ADAPTIVE_2)
            .build();
        Segment segment2 = new SegmentBuilder()
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
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo1);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment1.getKey(), language))
            .thenReturn(false);
        // ExamSeg 2
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment2, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo2);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment2.getKey(), language))
            .thenReturn(false);

        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(segmentPoolInfo1.getPoolCount());
        // ExamSeg 1
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment1.getKey(), language);
        // ExamSeg 2
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment2, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment2.getKey(), language);

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
        final String language = "ENU";
        Form enuForm = new Form.Builder("formkey-1")
            .withId("formid-1")
            .withLanguage(language)
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

        when(mockFormSelector.selectForm(segment, language)).thenReturn(Optional.of(enuForm));
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(enuForm.getLength());
        verify(mockExamSegmentCommandRepository).insert(examSegmentCaptor.capture());
        verify(mockFormSelector).selectForm(segment, language);
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
        final String language = "ENU";
        Segment segment = new SegmentBuilder()
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

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language))
            .thenReturn(false);
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(segmentPoolInfo.getPoolCount());
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey(), language);

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
        final String language = "ENU";
        Segment segment = new SegmentBuilder()
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

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language))
            .thenReturn(true);
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(segmentPoolInfo.getPoolCount());
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey(), language);

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
    public void shouldInitializeSegmentedTestMultiFormFixedForm() {
        Exam exam = new ExamBuilder().build();
        final String language = "ENU";
        // Items are just used for formLength calculation
        List<Item> items1 = Arrays.asList(new Item("item1"), new Item("item2"));
        List<Item> items2 = Arrays.asList(new Item("item3"));

        Form enuForm1Seg1 = new Form.Builder("formKey1")
            .withCohort("churro")
            .withLanguage(language)
            .withItems(items1)
            .build();
        Form enuForm2Seg1 = new Form.Builder("formKey2")
            .withCohort("torta")
            .withLanguage(language)
            .withItems(items1)
            .build();
        Form esnFormSeg1 = new Form.Builder("formKey3")
            .withCohort("burrito")
            .withItems(items1)
            .withLanguage("ESN")
            .build();
        Form enuForm1Seg2 = new Form.Builder("formKey4")
            .withCohort("churro")
            .withItems(items2)
            .withLanguage(language)
            .build();
        Form enuForm2Seg2 = new Form.Builder("formKey5")
            .withCohort("torta")
            .withItems(items2)
            .withLanguage(language)
            .build();
        Form esnFormSeg2 = new Form.Builder("formKey6")
            .withCohort("burrito")
            .withItems(items2)
            .withLanguage("ESN")
            .build();
        Segment segment1 = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.FIXED_FORM)
            .withMaxItems(5)
            .withForms(Arrays.asList(enuForm1Seg1, enuForm2Seg1, esnFormSeg1))
            .build();
        Segment segment2 = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.FIXED_FORM)
            .withForms(Arrays.asList(enuForm1Seg2, enuForm2Seg2, esnFormSeg2))
            .withMaxItems(5)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment1, segment2))
            .build();

        when(mockFormSelector.selectForm(segment1, language)).thenReturn(Optional.of(enuForm1Seg1));
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(enuForm1Seg1.getLength() + enuForm1Seg2.getLength());
        verify(mockFormSelector).selectForm(segment1, language);
        verify(mockExamSegmentCommandRepository).insert(examSegmentCaptor.capture());

        List<ExamSegment> examSegments = examSegmentCaptor.getValue();
        assertThat(examSegments).hasSize(2);
        ExamSegment examSegment1 = examSegments.get(0);
        assertThat(examSegment1.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment1.getFormId()).isEqualTo(enuForm1Seg1.getId());
        assertThat(examSegment1.getFormKey()).isEqualTo(enuForm1Seg1.getKey());
        assertThat(examSegment1.getFormCohort()).isEqualTo(enuForm1Seg1.getCohort());
        assertThat(examSegment1.getSegmentId()).isEqualTo(segment1.getSegmentId());
        assertThat(examSegment1.getSegmentPosition()).isEqualTo(segment1.getPosition());
        assertThat(examSegment1.getSegmentKey()).isEqualTo(segment1.getKey());
        assertThat(examSegment1.getPoolCount()).isEqualTo(items1.size());
        assertThat(examSegment1.getExamItemCount()).isEqualTo(items1.size());
        assertThat(examSegment1.getAlgorithm()).isEqualTo(segment1.getSelectionAlgorithm());
        assertThat(examSegment1.getFieldTestItemCount()).isEqualTo(0);
        assertThat(examSegment1.isPermeable()).isFalse();
        assertThat(examSegment1.isSatisfied()).isFalse();

        ExamSegment examSegment2 = examSegments.get(1);
        assertThat(examSegment2.getExamId()).isEqualTo(exam.getId());
        assertThat(examSegment2.getSegmentId()).isEqualTo(segment2.getSegmentId());
        assertThat(examSegment2.getSegmentPosition()).isEqualTo(segment2.getPosition());
        assertThat(examSegment2.getSegmentKey()).isEqualTo(segment2.getKey());
        assertThat(examSegment2.getFormId()).isEqualTo(enuForm1Seg2.getId());
        assertThat(examSegment2.getFormKey()).isEqualTo(enuForm1Seg2.getKey());
        assertThat(examSegment2.getFormCohort()).isEqualTo(enuForm1Seg2.getCohort());
        assertThat(examSegment2.getPoolCount()).isEqualTo(items2.size());
        assertThat(examSegment2.getExamItemCount()).isEqualTo(items2.size());
        assertThat(examSegment2.getAlgorithm()).isEqualTo(segment2.getSelectionAlgorithm());
        assertThat(examSegment2.getFieldTestItemCount()).isEqualTo(0);
        assertThat(examSegment2.isPermeable()).isFalse();
        assertThat(examSegment2.isSatisfied()).isFalse();
    }

    @Test
    public void shouldInitializeNonSegAssessmentWithFieldTestAdaptive() {
        Exam exam = new ExamBuilder().build();
        final String language = "ENU";
        Segment segment = new SegmentBuilder()
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

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), language))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment.getKey(), language))
            .thenReturn(2);
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(segmentPoolInfo.getPoolCount() + 2);
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey(), language);
        verify(mockFieldTestService).selectItemGroups(exam, assessment, segment.getKey(), language);

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
