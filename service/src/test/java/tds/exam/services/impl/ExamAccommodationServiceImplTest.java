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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.common.ValidationError;
import tds.exam.ExamInfo;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.builder.AccommodationBuilder;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.SessionBuilder;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.SessionService;
import tds.session.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
    private SessionService mockSessionService;
    
    @Mock
    private AssessmentService mockAssessmentService;
    
    @Mock
    private ExamApprovalService mockExamApprovalService;
    
    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Captor
    private ArgumentCaptor<List<ExamAccommodation>> examAccommodationInsertCaptor;

    @Before
    public void setUp() {
        examAccommodationService = new ExamAccommodationServiceImpl(mockExamAccommodationQueryRepository,
            mockExamAccommodationCommandRepository, mockAssessmentService, mockSessionService,  mockExamApprovalService, mockExamQueryRepository);
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

        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Arrays.asList(accommodation, nonDefaultAccommodation, dependsOnToolTypeAccommodation));
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

        List<Accommodation> assessmentAccommodations = Arrays.asList(languageAccommodationThatShouldBePresent,
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

        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), assessment.getKey())).thenReturn(assessmentAccommodations);
        when(mockExamAccommodationQueryRepository.findAccommodations(exam.getId())).thenReturn(Arrays.asList(existingFrenchExamAccommodation, existingEnglishExamAccommodation));

        examAccommodationService.initializeAccommodationsOnPreviousExam(exam, assessment, 0, false, guestAccommodations);

        verify(mockExamAccommodationCommandRepository).insert(examAccommodationInsertCaptor.capture());

        List<ExamAccommodation> examAccommodations = examAccommodationInsertCaptor.getValue();

        assertThat(examAccommodations).hasSize(2);

        ExamAccommodation testExamAccommodation = null;
        ExamAccommodation englishExamAccommodation = null;

        for (ExamAccommodation ea : examAccommodations) {
            switch (ea.getCode()) {
                case "ENU":
                    englishExamAccommodation = ea;
                    break;
                case "TST":
                    testExamAccommodation = ea;
                    break;
                default:
                    fail("Unexpected exam accommodation with code " + ea.getCode());
                    break;
            }
        }

        assertThat(testExamAccommodation).isNotNull();
        assertThat(testExamAccommodation.isApproved()).isFalse();

        assertThat(englishExamAccommodation).isNotNull();
        assertThat(englishExamAccommodation.getExamId()).isEqualTo(exam.getId());
        assertThat(englishExamAccommodation.getCode()).isEqualTo("ENU");
        assertThat(englishExamAccommodation.getType()).isEqualTo(languageAccommodationThatShouldBePresent.getType());
        assertThat(englishExamAccommodation.getDescription()).isEqualTo(languageAccommodationThatShouldBePresent.getValue());
        assertThat(englishExamAccommodation.getSegmentKey()).isEqualTo(languageAccommodationThatShouldBePresent.getSegmentKey());
        assertThat(englishExamAccommodation.getSegmentPosition()).isEqualTo(0);
        assertThat(englishExamAccommodation.isAllowChange()).isTrue();
        assertThat(englishExamAccommodation.getValue()).isEqualTo(languageAccommodationThatShouldBePresent.getValue());
        assertThat(englishExamAccommodation.isSelectable()).isTrue();
        assertThat(englishExamAccommodation.isApproved()).isTrue();
        assertThat(englishExamAccommodation.isCustom()).isTrue();
    }
    
    @Test
    public void shouldReturnValidationErrorForNoExamPresent() {
        Exam exam = new ExamBuilder().build();
        ApproveAccommodationsRequest request = new ApproveAccommodationsRequest(exam.getSessionId(), exam.getBrowserId(), new HashMap<>());
        
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.empty());
        Optional<ValidationError> maybeError = examAccommodationService.approveAccommodations(exam.getId(), request);
        assertThat(maybeError).isPresent();
        verify(mockExamQueryRepository).getExamById(exam.getId());
    }
    
    @Test
    public void shouldReturnValidationErrorForFailedVerifyAccess() {
        Exam exam = new ExamBuilder().build();
        ApproveAccommodationsRequest approveAccommodationsRequest = new ApproveAccommodationsRequest(exam.getSessionId(), exam.getBrowserId(), new HashMap<>());
        ExamInfo examInfo = new ExamInfo(exam.getId(), exam.getSessionId(), exam.getBrowserId());
        
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamApprovalService.verifyAccess(examInfo, exam)).thenReturn(Optional.of(new ValidationError("ErrorCode", "Error Message")));
        Optional<ValidationError> maybeError = examAccommodationService.approveAccommodations(exam.getId(), approveAccommodationsRequest);
        assertThat(maybeError).isPresent();
        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockExamApprovalService).verifyAccess(examInfo, exam);
    }
    
    @Test
    public void shouldReturnValidationErrorForNoSessionFound() {
        Exam exam = new ExamBuilder().build();
        ApproveAccommodationsRequest approveAccommodationsRequest = new ApproveAccommodationsRequest(exam.getSessionId(), exam.getBrowserId(), new HashMap<>());
        ExamInfo examInfo = new ExamInfo(exam.getId(), exam.getSessionId(), exam.getBrowserId());
        
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamApprovalService.verifyAccess(examInfo, exam)).thenReturn(Optional.empty());
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.empty());
        Optional<ValidationError> maybeError = examAccommodationService.approveAccommodations(exam.getId(), approveAccommodationsRequest);
        assertThat(maybeError).isPresent();
        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockExamApprovalService).verifyAccess(examInfo, exam);
        verify(mockSessionService).findSessionById(exam.getSessionId());
    }
    
    @Test
    public void shouldReturnValidationErrorForProctoredSessionFound() {
        Session session = new SessionBuilder().withProctorId(1L).build();
        Exam exam = new ExamBuilder().withSessionId(session.getId()).build();
        ApproveAccommodationsRequest approveAccommodationsRequest = new ApproveAccommodationsRequest(exam.getSessionId(), exam.getBrowserId(), new HashMap<>());
        ExamInfo examInfo = new ExamInfo(exam.getId(), exam.getSessionId(), exam.getBrowserId());
        
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamApprovalService.verifyAccess(examInfo, exam)).thenReturn(Optional.empty());
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.of(session));
        Optional<ValidationError> maybeError = examAccommodationService.approveAccommodations(exam.getId(), approveAccommodationsRequest);
        assertThat(maybeError).isPresent();
        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockExamApprovalService).verifyAccess(examInfo, exam);
        verify(mockSessionService).findSessionById(exam.getSessionId());
    }
    
    @Test
    public void shouldInsertApprovedAccommodationsTwoSegments() {
        Session session = new SessionBuilder()
            .withProctorId(null)
            .build();
        Exam exam = new ExamBuilder().withSessionId(session.getId()).build();
        Set<String> assessmentAccoms = new HashSet<>(Arrays.asList("AssessmentAcc1", "AssessmentAcc2"));
        Set<String> segment1Accoms = new HashSet<>(Arrays.asList("Segment1Acc"));
        Set<String> segment2Accoms = new HashSet<>(Arrays.asList("Segment2Acc"));
        
        HashMap<Integer, Set<String>> segmentPosToAccCodes = new HashMap<>();
        segmentPosToAccCodes.put(0, assessmentAccoms);
        segmentPosToAccCodes.put(1, segment1Accoms);
        segmentPosToAccCodes.put(2, segment2Accoms);
    
        ApproveAccommodationsRequest approveAccommodationsRequest = new ApproveAccommodationsRequest(exam.getSessionId(), exam.getBrowserId(), segmentPosToAccCodes);
        ExamInfo examInfo = new ExamInfo(exam.getId(), exam.getSessionId(), exam.getBrowserId());
        
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamApprovalService.verifyAccess(examInfo, exam)).thenReturn(Optional.empty());
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.of(session));
        when(mockExamAccommodationQueryRepository.findApprovedAccommodations(exam.getId())).thenReturn(new ArrayList<>());
        
        // initializePreviousAccommodations for assessment acc codes
        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey()))
              .thenReturn(Arrays.asList(
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
        
        Optional<ValidationError> maybeError = examAccommodationService.approveAccommodations(exam.getId(), approveAccommodationsRequest);
        
        assertThat(maybeError).isNotPresent();
        
        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockExamApprovalService).verifyAccess(examInfo, exam);
        verify(mockExamAccommodationQueryRepository).findApprovedAccommodations(exam.getId());
        verify(mockAssessmentService, times(3)).findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey());
        verify(mockSessionService).findSessionById(session.getId());
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
    public void shouldFindApprovedExamAccommodations() {
        ExamAccommodation accommodation = new ExamAccommodationBuilder().build();

        when(mockExamAccommodationQueryRepository.findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID)).thenReturn(Collections.singletonList(accommodation));

        List<ExamAccommodation> approvedExamAccommodations = examAccommodationService.findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);

        verify(mockExamAccommodationQueryRepository).findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);

        assertThat(approvedExamAccommodations).containsExactly(accommodation);
    }
}
