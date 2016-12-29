package tds.exam.services.impl;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tds.assessment.Assessment;
import tds.config.Accommodation;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.builder.AccommodationBuilder;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.services.ConfigService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamAccommodationServiceImplTest {
    private ExamAccommodationServiceImpl examAccommodationService;

    @Mock
    private ExamAccommodationQueryRepository mockExamAccommodationQueryRepository;

    @Mock
    private ExamAccommodationCommandRepository mockExamAccommodationCommandRepository;

    @Mock
    private ConfigService mockConfigService;

    @Captor
    private ArgumentCaptor<List<ExamAccommodation>> examAccommodationInsertCaptor;

    @Before
    public void setUp() {
        examAccommodationService = new ExamAccommodationServiceImpl(mockExamAccommodationQueryRepository, mockExamAccommodationCommandRepository, mockConfigService);
    }

    @Test
    public void shouldReturnAnAccommodation() {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new ExamAccommodationBuilder().build());
        when(mockExamAccommodationQueryRepository.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE)).thenReturn(mockExamAccommodations);

        List<ExamAccommodation> results = examAccommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);

        assertThat(results).hasSize(1);
        ExamAccommodation examAccommodation = results.get(0);
        assertThat(examAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(examAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(examAccommodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
    }

    @Test
    public void shouldReturnTwoAccommodations() {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new ExamAccommodationBuilder().build());
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .build());
        when(mockExamAccommodationQueryRepository.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
            "closed captioning"))
            .thenReturn(mockExamAccommodations);

        List<ExamAccommodation> results = examAccommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
            "closed captioning");

        assertThat(results).hasSize(2);

        ExamAccommodation firstResult = results.get(0);
        assertThat(firstResult.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstResult.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(firstResult.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(firstResult.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);

        ExamAccommodation secondResult = results.get(1);
        assertThat(secondResult.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondResult.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(secondResult.getType()).isEqualTo("closed captioning");
        assertThat(secondResult.getCode()).isEqualTo("TDS_ClosedCap0");
    }

    @Test
    public void shouldReturnAnEmptyListWhenSearchingForAccommodationsThatDoNotExist() {
        when(mockExamAccommodationQueryRepository.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY, "foo", "bar")).thenReturn(Lists.emptyList());

        List<ExamAccommodation> result = examAccommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY, "foo", "bar");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldInitializeExamAccommodations() {
        Exam exam = new ExamBuilder().build();

        Accommodation accommodation = new Accommodation.Builder()
            .withAccommodationCode("code")
            .withAccommodationType("type")
            .withSegmentKey("segmentKey")
            .withDefaultAccommodation(true)
            .withDependsOnToolType(null)
            .build();

        Accommodation nonDefaultAccommodation = new Accommodation.Builder()
            .withDefaultAccommodation(false)
            .withDependsOnToolType(null)
            .build();

        Accommodation dependsOnToolTypeAccommodation = new Accommodation.Builder()
            .withDefaultAccommodation(true)
            .withDependsOnToolType("dependingSoCool")
            .build();

        when(mockConfigService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Arrays.asList(accommodation, nonDefaultAccommodation, dependsOnToolTypeAccommodation));
        examAccommodationService.initializeExamAccommodations(exam);
        verify(mockExamAccommodationCommandRepository).insert(examAccommodationInsertCaptor.capture());

        List<ExamAccommodation> accommodations = examAccommodationInsertCaptor.getValue();
        assertThat(accommodations).hasSize(1);
        ExamAccommodation examAccommodation = accommodations.get(0);
        assertThat(examAccommodation.getCode()).isEqualTo("code");
        assertThat(examAccommodation.getType()).isEqualTo("type");
        assertThat(examAccommodation.getSegmentKey()).isEqualTo("segmentKey");
    }

    @Test
    public void shouldUpdateExamAccommodations() {
        Assessment assessment = new AssessmentBuilder()
            .build();

        Exam exam = new ExamBuilder()
            .withAssessmentId(assessment.getAssessmentId())
            .withAssessmentKey(assessment.getKey())
            .withDateStarted(null)
            .build();

        Accommodation accommodationThatShouldBePresent = new AccommodationBuilder()
            .withCode("ENU")
            .withType("Language")
            .withValue("English")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .build();

        Accommodation accommodationFrench = new AccommodationBuilder()
            .withCode("FRN")
            .withType("Language")
            .withValue("French")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .build();

        Accommodation accommodationWorkWithNonStartExam = new AccommodationBuilder()
            .withCode("ENU")
            .withType("Language")
            .withValue("English")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(false)
            .withSelectable(true)
            .build();

        Accommodation accommodationWithIncorrectSegmentPosition = new AccommodationBuilder()
            .withSegmentPosition(99)
            .withCode("ENU")
            .withType("Language")
            .withValue("English")
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .build();

        Accommodation accommodationWithRestoreRts = new AccommodationBuilder()
            .withCode("ENU")
            .withType("Language")
            .withValue("English")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(false)
            .build();

        String guestAccommodations = "ELA;ELA:ENU;Language:ENU";

        List<Accommodation> assessmentAccommodations = Arrays.asList(accommodationThatShouldBePresent,
            accommodationWorkWithNonStartExam,
            accommodationWithIncorrectSegmentPosition,
            accommodationWithRestoreRts,
            accommodationFrench);

        ExamAccommodation existingFrenchExamAccommodation = new ExamAccommodation.Builder()
            .withExamId(exam.getId())
            .withCode("FRN")
            .withType("Language")
            .withDescription("French")
            .withAllowChange(false)
            .withSelectable(false)
            .withValue("French")
            .withSegmentPosition(0)
            .withTotalTypeCount(2)
            .build();

        ExamAccommodation existingEnglishExamAccommodation = new ExamAccommodation.Builder()
            .withExamId(exam.getId())
            .withCode("FRN")
            .withType("Language")
            .withDescription("French")
            .withAllowChange(false)
            .withSelectable(false)
            .withValue("French")
            .withSegmentPosition(0)
            .withTotalTypeCount(2)
            .build();


        when(mockConfigService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), assessment.getKey())).thenReturn(assessmentAccommodations);
        when(mockExamAccommodationQueryRepository.findAccommodations(exam.getId())).thenReturn(Arrays.asList(existingFrenchExamAccommodation, existingEnglishExamAccommodation));

        examAccommodationService.initializeAccommodationsOnPreviousExam(exam, assessment, 0, false, guestAccommodations);

        verify(mockExamAccommodationCommandRepository).insert(examAccommodationInsertCaptor.capture());

        List<ExamAccommodation> examAccommodations = examAccommodationInsertCaptor.getValue();

        assertThat(examAccommodations).hasSize(1);

        ExamAccommodation examAccommodation = examAccommodations.get(0);

        assertThat(examAccommodation.getExamId()).isEqualTo(exam.getId());
        assertThat(examAccommodation.getCode()).isEqualTo("ENU");
        assertThat(examAccommodation.getType()).isEqualTo(accommodationThatShouldBePresent.getType());
        assertThat(examAccommodation.getDescription()).isEqualTo(accommodationThatShouldBePresent.getValue());
        assertThat(examAccommodation.getSegmentKey()).isEqualTo(accommodationThatShouldBePresent.getSegmentKey());
        assertThat(examAccommodation.getSegmentPosition()).isEqualTo(0);
        assertThat(examAccommodation.isAllowChange()).isTrue();
        assertThat(examAccommodation.getValue()).isEqualTo(accommodationThatShouldBePresent.getValue());
        assertThat(examAccommodation.isSelectable()).isTrue();
    }

    @Test
    public void shouldFindApprovedExamAccommodations() {
        ExamAccommodation accommodation = new ExamAccommodationBuilder().build();

        when(mockExamAccommodationQueryRepository.findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID)).thenReturn(Collections.singletonList(accommodation));

        List<ExamAccommodation> approvedExamAccommodations = examAccommodationService.findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);

        verify(mockExamAccommodationQueryRepository).findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);

        assertThat(approvedExamAccommodations).containsExactly(accommodation);
    }
}
