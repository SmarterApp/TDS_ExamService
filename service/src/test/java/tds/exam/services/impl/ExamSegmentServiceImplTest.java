/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.assessment.Form;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.common.Algorithm;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamSegment;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.builder.ItemBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.repositories.ExamSegmentCommandRepository;
import tds.exam.repositories.ExamSegmentQueryRepository;
import tds.exam.services.ExamPageService;
import tds.exam.services.FieldTestService;
import tds.exam.services.FormSelector;
import tds.exam.services.SegmentPoolService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamSegmentServiceImplTest {
    private ExamSegmentServiceImpl examSegmentService;

    @Mock
    private ExamSegmentCommandRepository mockExamSegmentCommandRepository;

    @Mock
    private ExamSegmentQueryRepository mockExamSegmentQueryRepository;

    @Mock
    private SegmentPoolService mockSegmentPoolService;

    @Mock
    private FieldTestService mockFieldTestService;

    @Mock
    private FormSelector mockFormSelector;

    @Mock
    private ExamPageService mockExamPageService;

    @Captor
    private ArgumentCaptor<List<ExamSegment>> examSegmentsCaptor;

    @Captor
    private ArgumentCaptor<ExamSegment> examSegmentCaptor;

    @Before
    public void setUp() {
        examSegmentService = new ExamSegmentServiceImpl(mockExamSegmentCommandRepository, mockExamSegmentQueryRepository,
            mockSegmentPoolService, mockFormSelector, mockFieldTestService, mockExamPageService);
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
            .withSegments(Collections.singletonList(segment))
            .build();
        // Empty segment pool should result in an error
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(0, 0, new HashSet<>());

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language)).thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey()))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment.getKey()))
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
            .withSegments(Collections.singletonList(segment))
            .build();
        // Empty segment pool should result in an error
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(0, 0, new HashSet<>());

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language)).thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey()))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment.getKey()))
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
            .withForms(Collections.singletonList(form1))
            .build();
        Segment segment2 = new SegmentBuilder()
            .withSelectionAlgorithm(Algorithm.FIXED_FORM)
            .withMaxItems(5)
            .withForms(Collections.singletonList(form2))
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment1, segment2))
            .build();

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
            .withForms(Collections.singletonList(enuForm))
            .withPosition(2)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment1, segment2))
            .build();
        SegmentPoolInfo segmentPoolInfo1 = new SegmentPoolInfo(3, 4,
            new HashSet<>(Arrays.asList(
                new ItemBuilder("item-1").build(),
                new ItemBuilder("item-2").build()
            )));

        // Adaptive Segment w/ field test items
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo1);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment1.getKey()))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment1.getKey()))
            .thenReturn(2);
        when(mockFormSelector.selectForm(segment2, language)).thenReturn(Optional.of(enuForm));
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(5);
        // ExamSeg 1
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment1.getKey());
        verify(mockFieldTestService).selectItemGroups(exam, assessment, segment1.getKey());
        verify(mockFormSelector).selectForm(segment2, language);
        verify(mockExamSegmentCommandRepository).insert(examSegmentsCaptor.capture());
        List<ExamSegment> examSegments = examSegmentsCaptor.getValue();
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

        assertThat(examSegment1).isNotNull();
        assertThat(examSegment2).isNotNull();

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
                new ItemBuilder("item-1").build(),
                new ItemBuilder("item-2").build()
            )));
        SegmentPoolInfo segmentPoolInfo2 = new SegmentPoolInfo(0, 0,
            new HashSet<>());

        // ExamSeg 1
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo1);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment1.getKey()))
            .thenReturn(false);
        // ExamSeg 2
        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment2, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo2);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment2.getKey()))
            .thenReturn(false);

        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(3);
        // ExamSeg 1
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment1, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment1.getKey());
        // ExamSeg 2
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment2, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment2.getKey());

        verify(mockExamSegmentCommandRepository).insert(examSegmentsCaptor.capture());
        List<ExamSegment> examSegments = examSegmentsCaptor.getValue();

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
            .withSegments(Collections.singletonList(segment))
            .build();

        when(mockFormSelector.selectForm(segment, language)).thenReturn(Optional.of(enuForm));
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(enuForm.getLength());
        verify(mockExamSegmentCommandRepository).insert(examSegmentsCaptor.capture());
        verify(mockFormSelector).selectForm(segment, language);
        List<ExamSegment> examSegments = examSegmentsCaptor.getValue();
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
            .withSegments(Collections.singletonList(segment))
            .build();
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(3, 4,
            new HashSet<>(Arrays.asList(
                new ItemBuilder("item-1").build(),
                new ItemBuilder("item-2").build(),
                new ItemBuilder("item-3").build(),
                new ItemBuilder("item-4").build()
            )));

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey()))
            .thenReturn(false);
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(3);
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey());

        verify(mockExamSegmentCommandRepository).insert(examSegmentsCaptor.capture());
        List<ExamSegment> examSegments = examSegmentsCaptor.getValue();
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
            .withSegments(Collections.singletonList(segment))
            .build();
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(5, 4,
            new HashSet<>(Arrays.asList(
                new ItemBuilder("item-1").build(),
                new ItemBuilder("item-2").build(),
                new ItemBuilder("item-3").build(),
                new ItemBuilder("item-4").build()
            )));

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey()))
            .thenReturn(true);
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(5);
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey());

        verify(mockExamSegmentCommandRepository).insert(examSegmentsCaptor.capture());
        List<ExamSegment> examSegments = examSegmentsCaptor.getValue();
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
        List<Item> items2 = Collections.singletonList(new Item("item3"));

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
        verify(mockExamSegmentCommandRepository).insert(examSegmentsCaptor.capture());

        List<ExamSegment> examSegments = examSegmentsCaptor.getValue();
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
            .withSegments(Collections.singletonList(segment))
            .build();
        SegmentPoolInfo segmentPoolInfo = new SegmentPoolInfo(segment.getMaxItems(), 4,
            new HashSet<>(Arrays.asList(
                new ItemBuilder("item-1").build(),
                new ItemBuilder("item-2").build(),
                new ItemBuilder("item-3").build(),
                new ItemBuilder("item-4").build()
            )));

        when(mockSegmentPoolService.computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language))
            .thenReturn(segmentPoolInfo);
        when(mockFieldTestService.isFieldTestEligible(exam, assessment, segment.getKey()))
            .thenReturn(true);
        when(mockFieldTestService.selectItemGroups(exam, assessment, segment.getKey()))
            .thenReturn(2);
        int totalItems = examSegmentService.initializeExamSegments(exam, assessment);
        assertThat(totalItems).isEqualTo(5);
        verify(mockSegmentPoolService).computeSegmentPool(exam.getId(), segment, assessment.getItemConstraints(),
            language);
        verify(mockFieldTestService).isFieldTestEligible(exam, assessment, segment.getKey());
        verify(mockFieldTestService).selectItemGroups(exam, assessment, segment.getKey());

        verify(mockExamSegmentCommandRepository).insert(examSegmentsCaptor.capture());
        List<ExamSegment> examSegments = examSegmentsCaptor.getValue();
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

    @Test
    public void shouldReturnExamSegmentsForExamId() {
        UUID examId = UUID.randomUUID();
        ExamSegment seg1 = new ExamSegment.Builder()
            .withSegmentKey("seg1")
            .withExamId(examId)
            .withSegmentPosition(1)
            .build();
        ExamSegment seg2 = new ExamSegment.Builder()
            .withSegmentKey("seg2")
            .withExamId(examId)
            .withSegmentPosition(2)
            .build();

        when(mockExamSegmentQueryRepository.findByExamId(examId)).thenReturn(Arrays.asList(seg1, seg2));
        List<ExamSegment> examSegments = examSegmentService.findExamSegments(examId);
        verify(mockExamSegmentQueryRepository).findByExamId(examId);

        assertThat(examSegments).hasSize(2);
    }

    @Test
    public void shouldFindExamSegmentByExamIdAndSegmentPosition() {
        Exam mockExam = new ExamBuilder().build();
        ExamSegment mockSegment = new ExamSegmentBuilder().build();

        when(mockExamSegmentQueryRepository.findByExamIdAndSegmentPosition(any(UUID.class), any(Integer.class)))
            .thenReturn(Optional.of(mockSegment));

        Optional<ExamSegment> maybeExamSegment =
            examSegmentService.findByExamIdAndSegmentPosition(mockExam.getId(), mockExam.getCurrentSegmentPosition());

        assertThat(maybeExamSegment.isPresent()).isTrue();
        assertThat(maybeExamSegment.get()).isEqualToComparingFieldByFieldRecursively(mockSegment);
    }

    @Test
    public void shouldReturnEmptyWhenAnExamSegmentCannotBeFoundForExamIdAndSegmentPosition() {
        Exam mockExam = new ExamBuilder().build();

        when(mockExamSegmentQueryRepository.findByExamIdAndSegmentPosition(any(UUID.class), any(Integer.class)))
            .thenReturn(Optional.empty());

        examSegmentService.findByExamIdAndSegmentPosition(mockExam.getId(), mockExam.getCurrentSegmentPosition());
    }

    @Test
    public void shouldExitExamSegment() {
        final UUID examId = UUID.randomUUID();
        final int segmentPosition = 1;
        ExamSegment segment = ExamSegment.Builder
            .fromSegment(random(ExamSegment.class))
            .withExitedAt(null)
            .withSegmentPosition(segmentPosition)
            .build();

        when(mockExamSegmentQueryRepository.findByExamIdAndSegmentPosition(examId, segmentPosition))
            .thenReturn(Optional.of(segment));

        Optional<ValidationError> maybeError = examSegmentService.exitSegment(examId, segmentPosition);
        assertThat(maybeError).isNotPresent();
        verify(mockExamSegmentQueryRepository).findByExamIdAndSegmentPosition(examId, segmentPosition);
        verify(mockExamSegmentCommandRepository).update(examSegmentCaptor.capture());

        ExamSegment updatedExamSegment = examSegmentCaptor.getValue();

        assertThat(updatedExamSegment.getSegmentId()).isEqualTo(segment.getSegmentId());
        assertThat(updatedExamSegment.getSegmentPosition()).isEqualTo(segment.getSegmentPosition());
        assertThat(updatedExamSegment.getExitedAt()).isNotNull();
    }

    @Test
    public void shouldFailToExitSegmentDueToExamSegmentNotFound() {
        final UUID examId = UUID.randomUUID();
        final int segmentPosition = 1;

        when(mockExamSegmentQueryRepository.findByExamIdAndSegmentPosition(examId, segmentPosition))
            .thenReturn(Optional.empty());

        Optional<ValidationError> maybeError = examSegmentService.exitSegment(examId, segmentPosition);
        assertThat(maybeError).isPresent();
        assertThat(maybeError.get().getCode()).isEqualTo(ValidationErrorCode.EXAM_SEGMENT_DOES_NOT_EXIST);
        assertThat(maybeError.get().getMessage()).isEqualTo("The exam segment does not exist");

        verify(mockExamSegmentQueryRepository).findByExamIdAndSegmentPosition(examId, segmentPosition);
        verify(mockExamSegmentCommandRepository, never()).update(isA(ExamSegment.class));
    }


    @Test
    public void shouldReturnTrueForAllSegmentsSatisfied() {
        final UUID examId = UUID.randomUUID();
        when(mockExamSegmentQueryRepository.findCountOfUnsatisfiedSegments(examId)).thenReturn(0);
        assertThat(examSegmentService.checkIfSegmentsCompleted(examId)).isTrue();
        verify(mockExamSegmentQueryRepository).findCountOfUnsatisfiedSegments(examId);
    }

    @Test
    public void shouldReturnFalseForSegmentsUnsatisfied() {
        final UUID examId = UUID.randomUUID();

        when(mockExamSegmentQueryRepository.findCountOfUnsatisfiedSegments(examId)).thenReturn(1);
        assertThat(examSegmentService.checkIfSegmentsCompleted(examId)).isFalse();
        verify(mockExamSegmentQueryRepository).findCountOfUnsatisfiedSegments(examId);
    }

    @Test
    public void shouldFindExamSegmentByExamIdAndSegmentKey() {
        final UUID examId = UUID.randomUUID();
        final String segmentKey = "segmentKey";
        final ExamSegment examSegment = new ExamSegmentBuilder().build();

        when(mockExamSegmentQueryRepository.findByExamIdAndSegmentKey(examId, segmentKey)).thenReturn(Optional.of(examSegment));
        assertThat(examSegmentService.findByExamIdAndSegmentKey(examId, segmentKey).get()).isEqualTo(examSegment);
        verify(mockExamSegmentQueryRepository).findByExamIdAndSegmentKey(examId, segmentKey);
    }
}
