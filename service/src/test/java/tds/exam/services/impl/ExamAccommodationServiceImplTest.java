package tds.exam.services.impl;

import org.assertj.core.util.Lists;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.builder.AccommodationBuilder;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.SessionBuilder;
import tds.exam.models.ExamAccommodationFilter;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.services.AssessmentService;
import tds.session.Session;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
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
    private AssessmentService mockAssessmentService;

    @Captor
    private ArgumentCaptor<List<ExamAccommodation>> examAccommodationInsertCaptor;

    @Captor
    private ArgumentCaptor<ExamAccommodation> examAccommodationUpdateCaptor;

    @Before
    public void setUp() {
        examAccommodationService = new ExamAccommodationServiceImpl(mockExamAccommodationQueryRepository,
            mockExamAccommodationCommandRepository, mockAssessmentService);
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
            .withSegmentPosition(0)
            .withDefaultAccommodation(true)
            .withDependsOnToolType(null)
            .build();

        Accommodation nonDefaultAccommodation = new Accommodation.Builder()
            .withAccommodationCode("code2")
            .withAccommodationType("type")
            .withDefaultAccommodation(false)
            .withDependsOnToolType(null)
            .build();

        Accommodation dependsOnToolTypeAccommodation = new Accommodation.Builder()
            .withAccommodationCode("code3")
            .withAccommodationType("type")
            .withDefaultAccommodation(true)
            .withDependsOnToolType("dependingSoCool")
            .build();

        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey())).thenReturn(asList(accommodation, nonDefaultAccommodation, dependsOnToolTypeAccommodation));
        examAccommodationService.initializeExamAccommodations(exam, "");
        verify(mockExamAccommodationCommandRepository).insert(examAccommodationInsertCaptor.capture());

        List<ExamAccommodation> accommodations = examAccommodationInsertCaptor.getValue();
        assertThat(accommodations).hasSize(1);
        ExamAccommodation examAccommodation = accommodations.get(0);
        assertThat(examAccommodation.getCode()).isEqualTo("code");
        assertThat(examAccommodation.getType()).isEqualTo("type");
        assertThat(examAccommodation.getSegmentPosition()).isEqualTo(0);
        assertThat(examAccommodation.getSegmentKey()).isEqualTo("segmentKey");
    }

    @Test
    public void shouldInitializeExamAccommodationsWithStudentDefaults() {
        Exam exam = new ExamBuilder()
            .withSubject("MATH")
            .build();

        Accommodation englishAccommodation = new Accommodation.Builder()
            .withAccommodationCode("ENU")
            .withAccommodationType("Language")
            .withSegmentKey("segmentKey")
            .withSegmentPosition(0)
            .withDefaultAccommodation(true)
            .withDependsOnToolType(null)
            .build();

        Accommodation spanishAccommodation = new Accommodation.Builder()
            .withAccommodationCode("ESN")
            .withAccommodationType("Language")
            .withSegmentKey("segmentKey")
            .withSegmentPosition(0)
            .withDefaultAccommodation(false)
            .withDependsOnToolType(null)
            .build();

        Accommodation nonDefaultAccommodation = new Accommodation.Builder()
            .withAccommodationCode("code1")
            .withAccommodationType("type1")
            .withDefaultAccommodation(false)
            .withDependsOnToolType(null)
            .build();

        Accommodation dependsOnToolTypeAccommodation = new Accommodation.Builder()
            .withAccommodationCode("code2")
            .withAccommodationType("type2")
            .withDefaultAccommodation(true)
            .withDependsOnToolType("dependingSoCool")
            .build();

        String studentAccommodationCodes = "MATH:ESN;MATH:TDS_CC0;ELA:ENU";

        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey())).thenReturn(asList(englishAccommodation, spanishAccommodation, nonDefaultAccommodation, dependsOnToolTypeAccommodation));
        examAccommodationService.initializeExamAccommodations(exam, studentAccommodationCodes);
        verify(mockExamAccommodationCommandRepository).insert(examAccommodationInsertCaptor.capture());

        List<ExamAccommodation> accommodations = examAccommodationInsertCaptor.getValue();
        assertThat(accommodations).hasSize(1);
        ExamAccommodation examAccommodation = accommodations.get(0);
        assertThat(examAccommodation.getCode()).isEqualTo("ESN");
        assertThat(examAccommodation.getType()).isEqualTo("Language");
        assertThat(examAccommodation.getSegmentPosition()).isEqualTo(0);
        assertThat(examAccommodation.getSegmentKey()).isEqualTo("segmentKey");
    }

    @Test
    public void shouldUpdateExamAccommodations() {
        Assessment assessment = new AssessmentBuilder()
            .build();

        Exam exam = new ExamBuilder()
            .withAssessmentId(assessment.getAssessmentId())
            .withAssessmentKey(assessment.getKey())
            .withStartedAt(null)
            .build();

        Accommodation languageAccommodationThatShouldBePresent = new AccommodationBuilder()
            .withCode("ENU")
            .withType("Language")
            .withValue("English")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .withDefaultAccommodation(false)
            .build();

        Accommodation testAccommodationThatShouldBePresent = new AccommodationBuilder()
            .withCode("TST")
            .withType("Test")
            .withValue("SO COOL")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .withDefaultAccommodation(true)
            .withDependsOnToolType("Language")
            .withAllowCombine(true)
            .withDisableOnGuestSession(false)
            .withToolTypeSortOrder(3)
            .withFunctional(true)
            .withTypeTotal(5)
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

        String guestAccommodations = "ELA;ELA:ENU;Language:ENU;ELA:TST;";

        List<Accommodation> assessmentAccommodations = asList(languageAccommodationThatShouldBePresent,
            testAccommodationThatShouldBePresent,
            accommodationWorkWithNonStartExam,
            accommodationWithIncorrectSegmentPosition,
            accommodationWithRestoreRts,
            accommodationFrench);

        ExamAccommodation existingFrenchExamAccommodation = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(exam.getId())
            .withCode("FRN")
            .withType("Language")
            .withDescription("French")
            .withAllowChange(false)
            .withSelectable(false)
            .withValue("French")
            .withSegmentPosition(0)
            .withTotalTypeCount(2)
            .withCreatedAt(Instant.now())
            .build();

        ExamAccommodation existingEnglishExamAccommodation = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(exam.getId())
            .withCode("ENU")
            .withType("Language")
            .withDescription("English")
            .withAllowChange(false)
            .withSelectable(false)
            .withValue("English")
            .withSegmentPosition(0)
            .withTotalTypeCount(2)
            .withCreatedAt(Instant.now())
            .build();

        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), assessment.getKey())).thenReturn(assessmentAccommodations);
        when(mockExamAccommodationQueryRepository.findAccommodations(exam.getId())).thenReturn(asList(existingFrenchExamAccommodation, existingEnglishExamAccommodation));

        examAccommodationService.initializeAccommodationsOnPreviousExam(exam, assessment, 0, false, guestAccommodations);

        verify(mockExamAccommodationCommandRepository, times(1)).insert(examAccommodationInsertCaptor.capture());

        List<ExamAccommodation> examAccommodations = examAccommodationInsertCaptor.getValue();

        assertThat(examAccommodations).hasSize(1);

        ExamAccommodation testExamAccommodation = examAccommodations.get(0);

        assertThat(testExamAccommodation).isNotNull();
        assertThat(testExamAccommodation.getExamId()).isEqualTo(exam.getId());
        assertThat(testExamAccommodation.getCode()).isEqualTo("TST");
        assertThat(testExamAccommodation.getType()).isEqualTo(testAccommodationThatShouldBePresent.getType());
        assertThat(testExamAccommodation.getDescription()).isEqualTo(testAccommodationThatShouldBePresent.getValue());
        assertThat(testExamAccommodation.getSegmentKey()).isEqualTo(testAccommodationThatShouldBePresent.getSegmentKey());
        assertThat(testExamAccommodation.getSegmentPosition()).isEqualTo(0);
        assertThat(testExamAccommodation.isAllowChange()).isTrue();
        assertThat(testExamAccommodation.getValue()).isEqualTo(testAccommodationThatShouldBePresent.getValue());
        assertThat(testExamAccommodation.isSelectable()).isTrue();
        assertThat(testExamAccommodation.isApproved()).isFalse();
        assertThat(testExamAccommodation.isCustom()).isFalse();
        assertThat(testExamAccommodation.getDependsOn()).isEqualTo("Language");
        assertThat(testExamAccommodation.isAllowCombine()).isTrue();
        assertThat(testExamAccommodation.isDefaultAccommodation()).isTrue();
        assertThat(testExamAccommodation.isDisabledOnGuestSession()).isFalse();
        assertThat(testExamAccommodation.isFunctional()).isTrue();
        assertThat(testExamAccommodation.getSortOrder()).isEqualTo(3);
    }

    @Test
    public void shouldSkipInsertOfDuplicateExamAccommodationPreviousExam() {
        Assessment assessment = new AssessmentBuilder()
            .build();

        Exam exam = new ExamBuilder()
            .withAssessmentId(assessment.getAssessmentId())
            .withAssessmentKey(assessment.getKey())
            .withStartedAt(null)
            .build();

        Accommodation englishLanguageAccommodation = new AccommodationBuilder()
            .withCode("ENU")
            .withType("Language")
            .withValue("English")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .withDefaultAccommodation(false)
            .build();
        Accommodation spanishLanguageAccommodation = new AccommodationBuilder()
            .withCode("ESN")
            .withType("Language")
            .withValue("Spanish")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .withDefaultAccommodation(false)
            .build();
        Accommodation audioControls1 = new AccommodationBuilder()
            .withCode("TDS_APC_PSP")
            .withType("Audio Playback Controls")
            .withValue("Play Stop and Pause")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .withDefaultAccommodation(false)
            .build();

        Accommodation audioControls2 = new AccommodationBuilder()
            .withCode("TDS_APC_SCRUBBER")
            .withType("Audio Playback Controls")
            .withValue("Scrubber")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(true)
            .withDefaultAccommodation(true)
            .build();

        String studentAccCodes = "ELA;ELA:ESN;Language:ESN;ELA:TDS_APC_SCRUBBER;ELA:TDS_APC_PSP;";

        List<Accommodation> assessmentAccommodations = asList(englishLanguageAccommodation, spanishLanguageAccommodation, audioControls1, audioControls2);

        ExamAccommodation englishExamAccommodation = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(exam.getId())
            .withCode("ENU")
            .withType("Language")
            .withDescription("English Language")
            .withAllowChange(false)
            .withSelectable(false)
            .withValue("English")
            .withSegmentPosition(0)
            .withTotalTypeCount(2)
            .withCreatedAt(Instant.now())
            .build();

        ExamAccommodation existingScrubberControl = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(exam.getId())
            .withCode("TDS_APC_SCRUBBER")
            .withType("Audio Playback Controls")
            .withDescription("Scrubber")
            .withAllowChange(false)
            .withSelectable(false)
            .withDescription("Audio Controls")
            .withValue("Scrubber")
            .withSegmentPosition(0)
            .withTotalTypeCount(2)
            .withCreatedAt(Instant.now())
            .build();

        ExamAccommodation existingAudioPlaybackControl = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(exam.getId())
            .withCode("TDS_APC_PSP")
            .withType("Audio Playback Controls")
            .withDescription("Audio Controls")
            .withAllowChange(false)
            .withSelectable(false)
            .withValue("Play Stop and Pause")
            .withSegmentPosition(0)
            .withTotalTypeCount(2)
            .withCreatedAt(Instant.now())
            .build();

        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), assessment.getKey())).thenReturn(assessmentAccommodations);
        when(mockExamAccommodationQueryRepository.findAccommodations(exam.getId())).thenReturn(asList(englishExamAccommodation, existingScrubberControl, existingAudioPlaybackControl));

        examAccommodationService.initializeAccommodationsOnPreviousExam(exam, assessment, 0, false, studentAccCodes);

        verify(mockExamAccommodationCommandRepository).insert(examAccommodationInsertCaptor.capture());
        List<ExamAccommodation> insertedExamAccommodations = examAccommodationInsertCaptor.getValue();
        assertThat(insertedExamAccommodations).hasSize(1);
        ExamAccommodation insertedAccommodation = insertedExamAccommodations.get(0);
        assertThat(insertedAccommodation.getType()).isEqualTo(spanishLanguageAccommodation.getType());
        assertThat(insertedAccommodation.getCode()).isEqualTo(spanishLanguageAccommodation.getCode());
        assertThat(insertedAccommodation.getSegmentPosition()).isEqualTo(spanishLanguageAccommodation.getSegmentPosition());

    }

    @Test
    public void shouldInsertApprovedAccommodationsTwoSegments() {
        Session session = new SessionBuilder()
            .withProctorId(null)
            .build();
        Exam exam = new ExamBuilder().withSessionId(session.getId()).build();
        Set<String> assessmentAccoms = new HashSet<>(asList("AssessmentAcc1", "AssessmentAcc2"));
        Set<String> segment1Accoms = Collections.singleton("Segment1Acc");
        Set<String> segment2Accoms = Collections.singleton("Segment2Acc");

        HashMap<Integer, Set<String>> segmentPosToAccCodes = new HashMap<>();
        segmentPosToAccCodes.put(0, assessmentAccoms);
        segmentPosToAccCodes.put(1, segment1Accoms);
        segmentPosToAccCodes.put(2, segment2Accoms);

        ApproveAccommodationsRequest approveAccommodationsRequest = new ApproveAccommodationsRequest(exam.getSessionId(), exam.getBrowserId(), true, segmentPosToAccCodes);
        when(mockExamAccommodationQueryRepository.findApprovedAccommodations(exam.getId())).thenReturn(new ArrayList<>());

        // initializePreviousAccommodations for assessment acc codes
        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey()))
            .thenReturn(asList(
                new AccommodationBuilder()
                    .withCode("AssessmentAcc1")
                    .withSegmentPosition(0)
                    .withSegmentKey("assessmentKey")
                    .build(),
                new AccommodationBuilder()
                    .withCode("AssessmentAcc2")
                    .withSegmentPosition(0)
                    .withSegmentKey("assessmentKey")
                    .build(),
                new AccommodationBuilder()
                    .withCode("Segment1Acc")
                    .withSegmentPosition(1)
                    .withSegmentKey("seg1Key")
                    .build(),
                new AccommodationBuilder()
                    .withCode("Segment2Acc")
                    .withSegmentKey("seg2Key")
                    .withSegmentPosition(2)
                    .build()
            ));

        List<ExamAccommodation> examAccommodations = examAccommodationService.approveAccommodations(exam, session, approveAccommodationsRequest);

        assertThat(examAccommodations).isNotEmpty();
        verify(mockExamAccommodationQueryRepository).findApprovedAccommodations(exam.getId());
        verify(mockAssessmentService).findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey());
        verify(mockExamAccommodationCommandRepository, times(3)).insert(examAccommodationInsertCaptor.capture());

        List<List<ExamAccommodation>> insertedExamAccoms = examAccommodationInsertCaptor.getAllValues();
        assertThat(insertedExamAccoms).hasSize(3);
        List<ExamAccommodation> insertedAssessmentAccoms = insertedExamAccoms.get(0);
        List<ExamAccommodation> insertedSeg1Accoms = insertedExamAccoms.get(1);
        List<ExamAccommodation> insertedSeg2Accoms = insertedExamAccoms.get(2);

        assertThat(insertedAssessmentAccoms).hasSize(2);
        assertThat(insertedSeg1Accoms).hasSize(1);
        assertThat(insertedSeg2Accoms).hasSize(1);

        ExamAccommodation seg2Acc = insertedSeg2Accoms.get(0);
        assertThat(seg2Acc.getSegmentKey()).isEqualTo("seg2Key");
        assertThat(seg2Acc.getCode()).isEqualTo("Segment2Acc");
        assertThat(seg2Acc.getSegmentPosition()).isEqualTo(2);
        assertThat(seg2Acc.getDeletedAt()).isNull();
    }

    @Test
    public void shouldCreateOtherExamAccommodations() {
        final Exam exam = new ExamBuilder().build();
        final String studentCodes = "ELA;ELA:ESN;Language:ESN;ELA:TDS_APC_SCRUBBER;ELA:TDS_APC_PSP;ELA:TDS_Other#test;";

        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(any(), any())).thenReturn(new ArrayList<>());
        examAccommodationService.initializeExamAccommodations(exam, studentCodes);
        verify(mockExamAccommodationCommandRepository).insert(examAccommodationInsertCaptor.capture());
        List<ExamAccommodation> insertedAccomms = examAccommodationInsertCaptor.getValue();
        assertThat(insertedAccomms).hasSize(1);
        ExamAccommodation otherExamAccomm = insertedAccomms.get(0);

        assertThat(otherExamAccomm.getCode()).isEqualTo("TDS_Other");
        assertThat(otherExamAccomm.getValue()).isEqualTo("test");
        assertThat(otherExamAccomm.getType()).isEqualTo("Other");
        assertThat(otherExamAccomm.getSegmentPosition()).isEqualTo(0);
        assertThat(otherExamAccomm.getSegmentKey()).isEqualTo(exam.getAssessmentKey());
        assertThat(otherExamAccomm.getDeletedAt()).isNull();
        assertThat(otherExamAccomm.getExamId()).isEqualTo(exam.getId());
        assertThat(otherExamAccomm.getDescription()).isEqualTo("test");
        assertThat(otherExamAccomm.isAllowChange()).isFalse();
        assertThat(otherExamAccomm.isSelectable()).isFalse();

    }

    @Test
    public void shouldDenyAccommodations() {
        final UUID examId = UUID.randomUUID();

        ExamAccommodation examAcc1 = ExamAccommodation.Builder
            .fromExamAccommodation(random(ExamAccommodation.class))
            .withId(UUID.randomUUID())
            .withDeletedAt(null)
            .withDeniedAt(null)
            .build();
        ExamAccommodation examAcc2 = ExamAccommodation.Builder
            .fromExamAccommodation(random(ExamAccommodation.class))
            .withId(UUID.randomUUID())
            .withDeletedAt(null)
            .withDeniedAt(null)
            .build();
        final Instant deniedAt = Instant.now();

        when(mockExamAccommodationQueryRepository.findAccommodations(examId)).thenReturn(asList(examAcc1, examAcc2));
        examAccommodationService.denyAccommodations(examId, deniedAt);
        verify(mockExamAccommodationQueryRepository).findAccommodations(examId);
        verify(mockExamAccommodationCommandRepository).update(examAccommodationUpdateCaptor.capture());

        List<ExamAccommodation> examAccommodations = examAccommodationUpdateCaptor.getAllValues();
        assertThat(examAccommodations).hasSize(2);

        for (ExamAccommodation updatedAccomm : examAccommodations) {
            assertThat(updatedAccomm.getDeniedAt()).isEqualTo(deniedAt);
        }
    }

    @Test
    public void shouldFindApprovedExamAccommodations() {
        ExamAccommodation accommodation = new ExamAccommodationBuilder().build();

        when(mockExamAccommodationQueryRepository.findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID)).thenReturn(Collections.singletonList(accommodation));

        List<ExamAccommodation> approvedExamAccommodations = examAccommodationService.findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);

        verify(mockExamAccommodationQueryRepository).findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);

        assertThat(approvedExamAccommodations).containsExactly(accommodation);
    }

    @Test
    public void shouldFindExamAccommmodationsWithFilters() {
        ExamAccommodation accommodation = new ExamAccommodationBuilder().build();
        UUID examId = UUID.randomUUID();
        List<ExamAccommodationFilter> filters = Collections.singletonList(new ExamAccommodationFilter("test", "test"));
        when(mockExamAccommodationQueryRepository.findAccommodations(examId, filters)).thenReturn(Collections.singletonList(accommodation));

        assertThat(examAccommodationService.findAccommodations(examId, filters)).containsExactly(accommodation);

        verify(mockExamAccommodationQueryRepository).findAccommodations(examId, filters);
    }
}
