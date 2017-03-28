package tds.exam.services.impl;

import org.assertj.core.util.Lists;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.joda.time.Minutes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.assessment.AssessmentWindow;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.entity.utils.ChangeListener;
import tds.common.web.exceptions.NotFoundException;
import tds.config.ClientSystemFlag;
import tds.config.TimeLimitConfiguration;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamApproval;
import tds.exam.ExamConfiguration;
import tds.exam.ExamInfo;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.ExamineeContext;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.builder.OpenExamRequestBuilder;
import tds.exam.builder.SessionBuilder;
import tds.exam.builder.StudentBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamStatusQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamService;
import tds.exam.services.ExamineeService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.RtsStudentPackageAttribute;
import tds.student.Student;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tds.config.ClientSystemFlag.ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE;
import static tds.config.ClientSystemFlag.RESTORE_ACCOMMODATIONS_TYPE;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;
import static tds.exam.ExamStatusCode.STATUS_PENDING;
import static tds.exam.ExamStatusCode.STATUS_SUSPENDED;
import static tds.exam.ExamStatusStage.IN_USE;
import static tds.exam.ExamStatusStage.OPEN;
import static tds.session.ExternalSessionConfiguration.DEVELOPMENT_ENVIRONMENT;
import static tds.session.ExternalSessionConfiguration.SIMULATION_ENVIRONMENT;
import static tds.student.RtsStudentPackageAttribute.ACCOMMODATIONS;
import static tds.student.RtsStudentPackageAttribute.ENTITY_NAME;
import static tds.student.RtsStudentPackageAttribute.EXTERNAL_ID;

@RunWith(MockitoJUnitRunner.class)
public class ExamServiceImplTest {
    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Mock
    private ExamCommandRepository mockExamCommandRepository;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private StudentService mockStudentService;

    @Mock
    private AssessmentService mockAssessmentService;

    @Mock
    private TimeLimitConfigurationService mockTimeLimitConfigurationService;

    @Mock
    private ConfigService mockConfigService;

    @Mock
    private ExamAccommodationService mockExamAccommodationService;

    @Mock
    private ExamStatusQueryRepository mockExamStatusQueryRepository;

    @Mock
    private ExamSegmentService mockExamSegmentService;

    @Mock
    private ExamPageService mockExamPageService;

    @Mock
    private ExamItemService mockExamItemService;

    @Mock
    private ExamApprovalService mockExamApprovalService;

    @Mock
    private ExamineeService mockExamineeService;

    @Mock
    private ChangeListener<Exam> mockOnCompletedExamChangeListener;

    @Captor
    private ArgumentCaptor<Exam> examArgumentCaptor;

    private ExamService examService;

    @Before
    public void setUp() {

        examService = new ExamServiceImpl(
            mockExamQueryRepository,
            mockSessionService,
            mockStudentService,
            mockExamSegmentService,
            mockAssessmentService,
            mockTimeLimitConfigurationService,
            mockConfigService,
            mockExamCommandRepository,
            mockExamPageService,
            mockExamItemService,
            mockExamStatusQueryRepository,
            mockExamAccommodationService,
            mockExamApprovalService,
            mockExamineeService,
            Collections.singletonList(mockOnCompletedExamChangeListener));


        // Calls to get formatted message are throughout the exam service
        // Since we aren't testing that it returns anything specific in these tests I each option here for simplicity
        when(mockConfigService.getFormattedMessage(any(), any(), any())).thenReturn("Formatted message");
        when(mockConfigService.getFormattedMessage(any(), any(), any(), any())).thenReturn("Formatted message");
        when(mockConfigService.getFormattedMessage(any(), any(), any(), any(), any(), any(), any())).thenReturn("Formatted message");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldReturnAnExam() {
        UUID examId = UUID.randomUUID();
        when(mockExamQueryRepository.getExamById(examId)).thenReturn(Optional.of(new Exam.Builder().withId(examId).build()));

        assertThat(examService.findExam(examId).get().getId()).isEqualTo(examId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnErrorWhenSessionCannotBeFound() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration("SBAC_PT", SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.empty());

        examService.openExam(openExamRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnErrorWhenStudentCannotBeFound() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration("SBAC_PT", SIMULATION_ENVIRONMENT, 0, 0, 0, 0);
        Session currentSession = new SessionBuilder().build();

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(currentSession.getClientName(), openExamRequest.getStudentId())).thenReturn(Optional.empty());

        examService.openExam(openExamRequest);
    }

    @Test
    public void shouldReturnValidationErrorWhenSessionIsNoOpen() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration("SBAC_PT", SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(new SessionBuilder().withStatus("closed").build()));

        Response<Exam> response = examService.openExam(openExamRequest);

        assertThat(response.getData().isPresent()).isFalse();
        assertThat(response.getError().get().getCode()).isEqualTo(ValidationErrorCode.SESSION_NOT_OPEN);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateIfExternSessionConfigCannotBeFoundWhileOpeningExam() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(new SessionBuilder().build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.empty());

        examService.openExam(openExamRequest);
    }

    @Test
    public void shouldNotAllowExamToOpenIfStillActive() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();

        Session currentSession = new SessionBuilder()
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .build();

        Student student = new StudentBuilder()
            .withClientName("testId")
            .build();

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC_PT", "Development", 0, 0, 0, 0);

        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(currentSession.getClientName(), openExamRequest.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData().isPresent()).isFalse();
        assertThat(examResponse.hasError()).isTrue();

        ValidationError validationError = examResponse.getError().get();
        verify(mockConfigService).getFormattedMessage("SBAC_PT", "_CanOpenTestOpportunity", "Current opportunity is active");
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.CURRENT_EXAM_OPEN);
    }

    /*
     * Open New Exam Tests
     */
    @Test
    public void shouldOpenNewExamAsGuest() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(-1)
            .build();
        Instant startTestTime = Instant.now().minus(Minutes.minutes(1).toStandardDuration());
        Session currentSession = new SessionBuilder()
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfigurationBuilder()
            .withEnvironment(DEVELOPMENT_ENVIRONMENT)
            .build();
        AssessmentWindow window = new AssessmentWindow.Builder()
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .withWindowId("window1")
            .withStartTime(Instant.now())
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .build();
        ClientSystemFlag clientSystemFlag = new ClientSystemFlag.Builder().withEnabled(true).build();

        TimeLimitConfiguration configuration = new TimeLimitConfiguration.Builder().withExamDelayDays(0).build();

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockConfigService.findClientSystemFlag("SBAC_PT", ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE)).thenReturn(Optional.of(clientSystemFlag));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockAssessmentService.findAssessment("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockAssessmentService.findAssessmentWindows("SBAC_PT", assessment.getAssessmentId(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        assertThat(examResponse.hasError()).isFalse();
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        verifyZeroInteractions(mockExamineeService);
        verifyZeroInteractions(mockStudentService);

        Exam exam = examResponse.getData().get();

        assertThat(exam.getAssessmentId()).isEqualTo(assessment.getAssessmentId());
        assertThat(exam.getAssessmentAlgorithm()).isEqualTo(assessment.getSelectionAlgorithm().getType());
        assertThat(exam.getAssessmentKey()).isEqualTo(openExamRequest.getAssessmentKey());
        assertThat(exam.getAssessmentWindowId()).isEqualTo("window1");
        assertThat(exam.getAttempts()).isEqualTo(1);
        assertThat(exam.getBrowserId()).isEqualTo(openExamRequest.getBrowserId());
        assertThat(exam.getDateJoined()).isGreaterThan(startTestTime);
        assertThat(exam.getClientName()).isEqualTo("SBAC_PT");
        assertThat(exam.getStudentId()).isEqualTo(openExamRequest.getStudentId());
        assertThat(exam.getLoginSSID()).isEqualTo("GUEST");
        assertThat(exam.getStudentName()).isEqualTo("GUEST");
        assertThat(exam.getEnvironment()).isEqualTo(extSessionConfig.getEnvironment());
        assertThat(exam.getStatus().getCode()).isEqualTo(STATUS_PENDING);
        assertThat(exam.getSubject()).isEqualTo(assessment.getSubject());
        assertThat(exam.getWaitingForSegmentApprovalPosition()).isEqualTo(1);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfTimeConfigurationCannotBeFound() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(-1)
            .build();
        Session currentSession = new SessionBuilder()
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfigurationBuilder()
            .withEnvironment(DEVELOPMENT_ENVIRONMENT)
            .build();
        AssessmentWindow window = new AssessmentWindow.Builder()
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .withWindowId("window1")
            .withStartTime(Instant.now())
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .build();
        ClientSystemFlag clientSystemFlag = new ClientSystemFlag.Builder().withEnabled(true).build();

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockConfigService.findClientSystemFlag("SBAC_PT", ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE)).thenReturn(Optional.of(clientSystemFlag));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockAssessmentService.findAssessment("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockAssessmentService.findAssessmentWindows("SBAC_PT", assessment.getAssessmentId(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.empty());

        examService.openExam(openExamRequest);
    }

    @Test
    public void shouldOpenNewExamWithoutProctor() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(-1)
            .build();

        Session currentSession = new SessionBuilder()
            .withProctorId(null)
            .build();

        ClientSystemFlag clientSystemFlag = new ClientSystemFlag.Builder().withEnabled(true).build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfigurationBuilder().build();
        AssessmentWindow window = new AssessmentWindow.Builder()
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .withWindowId("window1")
            .withStartTime(Instant.now())
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .build();

        Accommodation accommodation = new Accommodation.Builder()
            .withAccommodationCode("code")
            .withAccommodationType("type")
            .withSegmentKey("segmentKey")
            .withDefaultAccommodation(true)
            .withDependsOnToolType(null)
            .build();

        TimeLimitConfiguration configuration = new TimeLimitConfiguration.Builder().withExamDelayDays(0).build();

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockConfigService.findClientSystemFlag("SBAC_PT", ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE)).thenReturn(Optional.of(clientSystemFlag));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockAssessmentService.findAssessment("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.empty());
        when(mockAssessmentService.findAssessmentWindows("SBAC_PT", assessment.getAssessmentId(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey("SBAC_PT", openExamRequest.getAssessmentKey()))
            .thenReturn(Collections.singletonList(accommodation));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_APPROVED)).thenReturn(new ExamStatusCode(STATUS_APPROVED, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        verifyZeroInteractions(mockExamineeService);
        assertThat(examResponse.hasError()).isFalse();

        Exam exam = examResponse.getData().get();
        assertThat(exam.getStatus().getCode()).isEqualTo(ExamStatusCode.STATUS_APPROVED);
    }

    @Test
    public void shouldOpenNewExamWithProctor() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(1)
            .build();

        Session currentSession = new SessionBuilder()
            .withProctorId(99L)
            .build();

        Student student = new StudentBuilder().build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfigurationBuilder().build();
        AssessmentWindow window = new AssessmentWindow.Builder()
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .withWindowId("window1")
            .withStartTime(Instant.now())
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .build();

        RtsStudentPackageAttribute externalIdAttribute = new RtsStudentPackageAttribute(EXTERNAL_ID, "External Id");
        RtsStudentPackageAttribute entityNameAttribute = new RtsStudentPackageAttribute(ENTITY_NAME, "Entity Id");
        RtsStudentPackageAttribute entityAccommodationAttribute = new RtsStudentPackageAttribute(ACCOMMODATIONS, "MATH:Code1;ELA:Code2");

        TimeLimitConfiguration configuration = new TimeLimitConfiguration.Builder().withExamDelayDays(0).build();

        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(currentSession.getClientName(), openExamRequest.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockStudentService.findStudentPackageAttributes(openExamRequest.getStudentId(), "SBAC_PT", EXTERNAL_ID, ENTITY_NAME, ACCOMMODATIONS))
            .thenReturn(Arrays.asList(externalIdAttribute, entityNameAttribute, entityAccommodationAttribute));
        when(mockAssessmentService.findAssessmentWindows(currentSession.getClientName(), assessment.getAssessmentId(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        verify(mockExamAccommodationService).initializeExamAccommodations(isA(Exam.class), isA(String.class));
        verify(mockExamineeService).insertAttributesAndRelationships(isA(Exam.class), isA(Student.class), isA(ExamineeContext.class));

        assertThat(examResponse.hasError()).isFalse();

        Exam exam = examResponse.getData().get();
        assertThat(exam.getStatus().getCode()).isEqualTo(STATUS_PENDING);
        assertThat(exam.getStudentName()).isEqualTo("Entity Id");
        assertThat(exam.getLoginSSID()).isEqualTo("External Id");
    }

    @Test
    public void shouldOpenExamWithCustomAccommodations() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(1)
            .build();

        Session currentSession = new SessionBuilder()
            .build();

        Student student = new StudentBuilder().build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfigurationBuilder().build();
        AssessmentWindow window = new AssessmentWindow.Builder()
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .withWindowId("window1")
            .withStartTime(Instant.now())
            .withAssessmentKey(openExamRequest.getAssessmentKey())
            .build();

        RtsStudentPackageAttribute externalIdAttribute = new RtsStudentPackageAttribute(EXTERNAL_ID, "External Id");
        RtsStudentPackageAttribute entityNameAttribute = new RtsStudentPackageAttribute(ENTITY_NAME, "Entity Id");
        RtsStudentPackageAttribute entityAccommodationsAttribute = new RtsStudentPackageAttribute(ACCOMMODATIONS, "MATH:CODE1;ELA:CODE2");

        TimeLimitConfiguration configuration = new TimeLimitConfiguration.Builder().withExamDelayDays(0).build();

        ExamAccommodation customExamAccommodation = new ExamAccommodationBuilder()
            .withCustom(true)
            .build();

        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(currentSession.getClientName(), openExamRequest.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockStudentService.findStudentPackageAttributes(openExamRequest.getStudentId(), "SBAC_PT", EXTERNAL_ID, ENTITY_NAME, ACCOMMODATIONS))
            .thenReturn(Arrays.asList(externalIdAttribute, entityNameAttribute, entityAccommodationsAttribute));
        when(mockAssessmentService.findAssessmentWindows(currentSession.getClientName(), assessment.getAssessmentId(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC_PT", openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockExamAccommodationService.initializeExamAccommodations(isA(Exam.class), isA(String.class))).thenReturn(Collections.singletonList(customExamAccommodation));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        verify(mockExamAccommodationService).initializeExamAccommodations(isA(Exam.class), isA(String.class));
        verify(mockExamineeService).insertAttributesAndRelationships(isA(Exam.class), isA(Student.class), isA(ExamineeContext.class));

        assertThat(examResponse.hasError()).isFalse();

        Exam exam = examResponse.getData().get();
        assertThat(exam.getStatus().getCode()).isEqualTo(STATUS_PENDING);
        assertThat(exam.isCustomAccommodations()).isTrue();
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfDayHasPassed() {
        OpenExamRequest request = new OpenExamRequestBuilder()
            .withGuestAccommodations("guest")
            .build();

        Session currentSession = new SessionBuilder()
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .build();

        Student student = new StudentBuilder().build();

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), approvedStatusDate)
            .withBrowserId(UUID.randomUUID())
            .withChangedAt(Instant.now().minus(Days.days(2).toStandardDuration()))
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();
        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(currentSession.getClientName(), request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment("SBAC_PT", request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey("SBAC_PT", request.getAssessmentKey())).thenReturn(Collections.emptyList());
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockConfigService.findClientSystemFlag("SBAC_PT", RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        verify(mockExamCommandRepository).update(isA(Exam.class));
        verify(mockExamAccommodationService).initializeAccommodationsOnPreviousExam(isA(Exam.class), isA(Assessment.class), isA(Integer.class), isA(Boolean.class), isA(String.class));

        assertThat(examResponse.hasError()).isFalse();

        Exam savedExam = examResponse.getData().get();

        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getCode()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getStatusChangedAt()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getChangedAt()).isNotNull();
        assertThat(savedExam.getStartedAt()).isEqualTo(previousExam.getStartedAt());
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfPreviousSessionIsClosed() {
        OpenExamRequest request = new OpenExamRequestBuilder()
            .withMaxAttempts(5)
            .build();

        Session currentSession = new SessionBuilder()
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withStatus("closed")
            .build();

        Student student = new StudentBuilder().build();

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), approvedStatusDate)
            .withChangedAt(Instant.now())
            .build();
        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration("SBAC_PT", SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(extSessionConfig));
        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(currentSession.getClientName(), request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment("SBAC_PT", request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockConfigService.findClientSystemFlag("SBAC_PT", RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getCode()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getStatusChangedAt()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getChangedAt()).isNotNull();
        assertThat(savedExam.getStartedAt()).isEqualTo(previousExam.getStartedAt());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionIdSame() {
        OpenExamRequest request = new OpenExamRequestBuilder().build();

        Session currentSession = new SessionBuilder()
            .build();

        Session previousSession = new SessionBuilder()
            .withId(request.getSessionId())
            .build();

        Student student = new StudentBuilder().build();

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), approvedStatusDate)
            .withChangedAt(Instant.now())
            .build();
        Assessment assessment = new AssessmentBuilder().build();

        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC_PT", SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        ExamAccommodation examAccommodation = new ExamAccommodationBuilder().withCustom(true).build();

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(currentSession.getClientName(), request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment("SBAC_PT", request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockConfigService.findClientSystemFlag("SBAC_PT", RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));
        when(mockExamAccommodationService.initializeAccommodationsOnPreviousExam(previousExam, assessment, 0, true, request.getGuestAccommodations())).
            thenReturn(Collections.singletonList(examAccommodation));

        Response<Exam> examResponse = examService.openExam(request);

        assertThat(examResponse.hasError()).isFalse();

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getCode()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getStatusChangedAt()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getChangedAt()).isNotNull();
        assertThat(savedExam.getStartedAt()).isEqualTo(previousExam.getStartedAt());
        assertThat(savedExam.isCustomAccommodations()).isTrue();
    }

    @Test
    public void shouldOpenPreviousExamIfSessionEndTimeIsBeforeNow() {
        OpenExamRequest request = new OpenExamRequestBuilder().build();

        Session currentSession = new SessionBuilder()
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withDateEnd(Instant.now().minus(Days.days(1).toStandardDuration()))
            .build();

        Student student = new StudentBuilder().build();

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), approvedStatusDate)
            .withChangedAt(Instant.now())
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();

        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(currentSession.getClientName(), request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment("SBAC_PT", request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockConfigService.findClientSystemFlag("SBAC_PT", RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        assertThat(examResponse.hasError()).isFalse();

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getCode()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getStatusChangedAt()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getChangedAt()).isNotNull();
        assertThat(savedExam.getStartedAt()).isEqualTo(previousExam.getStartedAt());
    }

    @Test
    public void shouldOpenPreviousExamIfPreviousExamIsInactiveStage() {
        OpenExamRequest request = new OpenExamRequestBuilder()
            .withSessionId(UUID.randomUUID())
            .withStudentId(-1)
            .build();

        Session currentSession = new SessionBuilder()
            .withId(request.getSessionId())
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withDateEnd(Instant.now().minus(Days.days(1).toStandardDuration()))
            .build();

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.INACTIVE), approvedStatusDate)
            .withStartedAt(Instant.now())
            .build();

        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC_PT", SIMULATION_ENVIRONMENT, 0, 0, 0, 0);
        Assessment assessment = new AssessmentBuilder().build();

        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), "SBAC_PT")).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName("SBAC_PT")).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockAssessmentService.findAssessment("SBAC_PT", request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_SUSPENDED)).thenReturn(new ExamStatusCode(STATUS_SUSPENDED, IN_USE));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_SUSPENDED)).thenReturn(new ExamStatusCode(STATUS_SUSPENDED, IN_USE));
        when(mockConfigService.findClientSystemFlag("SBAC_PT", RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getCode()).isEqualTo(STATUS_SUSPENDED);
        assertThat(savedExam.getStatusChangedAt()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getStartedAt()).isEqualTo(previousExam.getStartedAt());
        assertThat(savedExam.getSessionId()).isEqualTo(request.getSessionId());
    }

    @Test
    public void shouldPauseAnExam() {
        UUID examId = UUID.randomUUID();
        Exam mockExam = new Exam.Builder()
            .withId(examId)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PENDING, ExamStatusStage.IN_USE), Instant.now())
            .build();

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(mockExam));

        Optional<ValidationError> maybeStatusTransitionFailure = examService.updateExamStatus(examId,
            new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE));
        verify(mockOnCompletedExamChangeListener).accept(any(Exam.class), any(Exam.class)); // gets called; will do nothing (because status is "paused")

        assertThat(maybeStatusTransitionFailure).isNotPresent();
    }

    @Test
    public void shouldNotPauseAnExamDueToInvalidStatusTransition() {
        UUID examId = UUID.randomUUID();
        Exam mockExam = new Exam.Builder()
            .withId(examId)
            .withStatus(new ExamStatusCode("foo", ExamStatusStage.INACTIVE), Instant.now())
            .build();

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(mockExam));

        Optional<ValidationError> maybeStatusTransitionFailure = examService.updateExamStatus(examId,
            new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE));
        verifyZeroInteractions(mockOnCompletedExamChangeListener);

        assertThat(maybeStatusTransitionFailure).isPresent();
        ValidationError statusTransitionFailure = maybeStatusTransitionFailure.get();
        assertThat(statusTransitionFailure.getCode()).isEqualTo(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE);
        assertThat(statusTransitionFailure.getMessage()).isEqualTo("Transitioning exam status from foo to paused is not allowed");
    }

    @Test
    public void shouldReturnFailureExamConfigForNoExamFound() {
        final UUID examID = UUID.randomUUID();
        final String browserUserAgent = "007";
        when(mockExamQueryRepository.getExamById(examID)).thenReturn(Optional.empty());
        Response<ExamConfiguration> response = examService.startExam(examID, browserUserAgent);
        assertThat(response.hasError()).isTrue();
        ValidationError error = response.getError().get();
        assertThat(error.getCode()).isEqualTo(ExamStatusCode.STATUS_FAILED);
        assertThat(error.getMessage()).isNotNull();
    }

    @Test
    public void shouldReturnFailureExamConfigForExamStatusNotApproved() {
        final Exam exam = new ExamBuilder()
            .build();
        final String browserUserAgent = "007";
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.empty());
        Response<ExamConfiguration> response = examService.startExam(exam.getId(), browserUserAgent);
        assertThat(response.hasError()).isTrue();
        ValidationError error = response.getError().get();
        assertThat(error.getCode()).isEqualTo(ExamStatusCode.STATUS_FAILED);
        assertThat(error.getMessage()).isNotNull();
    }

    @Test
    public void shouldReturnFailureExamConfigForNoSessionFound() {
        final String browserUserAgent = "007";
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), Instant.now())
            .build();
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.empty());
        Response<ExamConfiguration> response = examService.startExam(exam.getId(), browserUserAgent);
        assertThat(response.hasError()).isTrue();
        ValidationError error = response.getError().get();
        assertThat(error.getCode()).isEqualTo(ExamStatusCode.STATUS_FAILED);
        assertThat(error.getMessage()).isNotNull();
    }

    @Test
    public void shouldReturnFailureExamConfigForNoAssessmentFound() {
        final String browserUserAgent = "007";
        Session session = new SessionBuilder().build();
        Exam exam = new ExamBuilder()
            .withSessionId(session.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), Instant.now())
            .build();
        TimeLimitConfiguration timeLimitConfiguration = new TimeLimitConfiguration.Builder()
            .withTaCheckinTimeMinutes(3)
            .withAssessmentId("assessmentId")
            .withExamDelayDays(2)
            .withExamRestartWindowMinutes(2)
            .withInterfaceTimeoutMinutes(4)
            .withRequestInterfaceTimeoutMinutes(5)
            .build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(exam.getClientName(),
            SIMULATION_ENVIRONMENT,
            0,
            0,
            0,
            0);

        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(exam.getClientName(), "assessmentId"))
            .thenReturn(Optional.of(timeLimitConfiguration));
        when(mockSessionService.findExternalSessionConfigurationByClientName(exam.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.of(session));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.empty());
        when(mockExamApprovalService.verifyAccess(isA(ExamInfo.class), isA(Exam.class)))
            .thenReturn(Optional.empty());

        Response<ExamConfiguration> response = examService.startExam(exam.getId(), browserUserAgent);

        assertThat(response.hasError()).isTrue();
        ValidationError error = response.getError().get();
        assertThat(error.getCode()).isEqualTo(ExamStatusCode.STATUS_FAILED);
        assertThat(error.getMessage()).isNotNull();
    }

    @Test
    public void shouldStartNewExam() throws InterruptedException {
        final String browserUserAgent = "007";
        Session session = new SessionBuilder().build();
        Instant now = org.joda.time.Instant.now().minus(5000);
        Instant approvedStatusDate = now.minus(5000);
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), approvedStatusDate)
            .withSessionId(session.getId())
            .withStartedAt(null)
            .build();
        Assessment assessment = new AssessmentBuilder().build();
        TimeLimitConfiguration timeLimitConfiguration = new TimeLimitConfiguration.Builder()
            .withTaCheckinTimeMinutes(3)
            .withAssessmentId(assessment.getAssessmentId())
            .withExamDelayDays(2)
            .withExamRestartWindowMinutes(2)
            .withInterfaceTimeoutMinutes(4)
            .withRequestInterfaceTimeoutMinutes(5)
            .build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(exam.getClientName(),
            SIMULATION_ENVIRONMENT,
            0,
            0,
            0,
            0);
        final int testLength = 10;

        when(mockSessionService.findExternalSessionConfigurationByClientName(exam.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.of(session));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey()))
            .thenReturn(Optional.of(assessment));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(exam.getClientName(), assessment.getAssessmentId()))
            .thenReturn(Optional.of(timeLimitConfiguration));
        when(mockExamSegmentService.initializeExamSegments(exam, assessment)).thenReturn(testLength);
        when(mockExamApprovalService.verifyAccess(isA(ExamInfo.class), isA(Exam.class)))
            .thenReturn(Optional.empty());

        Response<ExamConfiguration> examConfigurationResponse = examService.startExam(exam.getId(), browserUserAgent);

        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockSessionService).findSessionById(exam.getSessionId());
        verify(mockAssessmentService).findAssessment(exam.getClientName(), exam.getAssessmentKey());
        verify(mockTimeLimitConfigurationService).findTimeLimitConfiguration(exam.getClientName(), assessment.getAssessmentId());
        verify(mockExamCommandRepository).update(examArgumentCaptor.capture());

        assertThat(examConfigurationResponse.getData().isPresent()).isTrue();
        ExamConfiguration examConfiguration = examConfigurationResponse.getData().get();
        assertThat(examConfiguration.getContentLoadTimeoutMinutes()).isEqualTo(120);
        assertThat(examConfiguration.getExam().getId()).isEqualTo(exam.getId());
        assertThat(examConfiguration.getExamRestartWindowMinutes()).isEqualTo(timeLimitConfiguration.getExamRestartWindowMinutes());
        assertThat(examConfiguration.getInterfaceTimeoutMinutes()).isEqualTo(timeLimitConfiguration.getInterfaceTimeoutMinutes());
        assertThat(examConfiguration.getPrefetch()).isEqualTo(assessment.getPrefetch());

        assertThat(examConfiguration.getStartPosition()).isEqualTo(1);
        assertThat(examConfiguration.getTestLength()).isEqualTo(testLength);

        Exam updatedExam = examArgumentCaptor.getValue();
        assertThat(updatedExam).isNotNull();
        assertThat(updatedExam.getAttempts()).isEqualTo(0);
        assertThat(updatedExam.getId()).isEqualTo(exam.getId());
        assertThat(updatedExam.getMaxItems()).isEqualTo(testLength);
        assertThat(updatedExam.getStartedAt()).isNotNull();
        assertThat(updatedExam.getExpiresAt()).isNotNull();
        assertThat(updatedExam.getStatus().getStage()).isEqualTo(ExamStatusStage.IN_PROGRESS);
        assertThat(updatedExam.getStatus().getCode()).isEqualTo(ExamStatusCode.STATUS_STARTED);
        assertThat(updatedExam.getStatusChangedAt()).isGreaterThan(approvedStatusDate);
        assertThat(updatedExam.getWaitingForSegmentApprovalPosition()).isEqualTo(-1);
        assertThat(updatedExam.getBrowserUserAgent()).isEqualTo(browserUserAgent);
    }

    @Test
    public void shouldRestartExistingExamOutsideGracePeriodPausedExam() throws InterruptedException {
        final String browserUserAgent = "007";
        Session session = new SessionBuilder().build();
        final Instant now = org.joda.time.Instant.now().minus(5000);
        final Instant approvedStatusDate = now.minus(5000);
        final Instant lastStudentActivityTime = now.minus(25 * 60 * 1000); // minus 25 minutes
        final int testLength = 10;

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), approvedStatusDate)
            .withSessionId(session.getId())
            .withResumptions(3)
            .withRestartsAndResumptions(5)
            .withMaxItems(10)
            .withStartedAt(Instant.now().minus(60000))
            .build();
        Assessment assessment = new AssessmentBuilder().build();
        TimeLimitConfiguration timeLimitConfiguration = new TimeLimitConfiguration.Builder()
            .withTaCheckinTimeMinutes(3)
            .withAssessmentId(assessment.getAssessmentId())
            .withExamDelayDays(2)
            .withExamRestartWindowMinutes(20) // "grace period"
            .withInterfaceTimeoutMinutes(4)
            .withRequestInterfaceTimeoutMinutes(5)
            .build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(exam.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName(exam.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamQueryRepository.findLastStudentActivity(exam.getId())).thenReturn(Optional.of(lastStudentActivityTime));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.of(session));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey()))
            .thenReturn(Optional.of(assessment));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(exam.getClientName(), assessment.getAssessmentId()))
            .thenReturn(Optional.of(timeLimitConfiguration));
        when(mockExamSegmentService.initializeExamSegments(exam, assessment)).thenReturn(testLength);
        when(mockExamApprovalService.verifyAccess(isA(ExamInfo.class), isA(Exam.class)))
            .thenReturn(Optional.empty());

        Response<ExamConfiguration> examConfigurationResponse = examService.startExam(exam.getId(), browserUserAgent);

        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockSessionService).findSessionById(exam.getSessionId());
        verify(mockAssessmentService).findAssessment(exam.getClientName(), exam.getAssessmentKey());
        verify(mockTimeLimitConfigurationService).findTimeLimitConfiguration(exam.getClientName(), assessment.getAssessmentId());
        verify(mockExamCommandRepository).update(examArgumentCaptor.capture());
        verify(mockExamQueryRepository).findLastStudentActivity(exam.getId());

        assertThat(examConfigurationResponse.getData().isPresent()).isTrue();
        ExamConfiguration examConfiguration = examConfigurationResponse.getData().get();
        assertThat(examConfiguration.getExam().getRestartsAndResumptions()).isEqualTo(5);
        assertThat(examConfiguration.getContentLoadTimeoutMinutes()).isEqualTo(120);
        assertThat(examConfiguration.getExam().getId()).isEqualTo(exam.getId());
        assertThat(examConfiguration.getExamRestartWindowMinutes()).isEqualTo(timeLimitConfiguration.getExamRestartWindowMinutes());
        assertThat(examConfiguration.getInterfaceTimeoutMinutes()).isEqualTo(timeLimitConfiguration.getInterfaceTimeoutMinutes());
        assertThat(examConfiguration.getPrefetch()).isEqualTo(assessment.getPrefetch());
        assertThat(examConfiguration.getStartPosition()).isEqualTo(1);
        assertThat(examConfiguration.getTestLength()).isEqualTo(testLength);

        // Sleep a bit to prevent intermittent test failures due to timing
        Exam updatedExam = examArgumentCaptor.getValue();
        assertThat(updatedExam).isNotNull();
        assertThat(updatedExam.getAttempts()).isEqualTo(0);
        assertThat(updatedExam.getId()).isEqualTo(exam.getId());
        assertThat(updatedExam.getMaxItems()).isEqualTo(testLength);
        assertThat(updatedExam.getStartedAt()).isNotNull();
        assertThat(updatedExam.getResumptions()).isEqualTo(3);
        assertThat(updatedExam.getRestartsAndResumptions()).isEqualTo(6);
        assertThat(updatedExam.getExpiresAt()).isNull();
        assertThat(updatedExam.getStatus().getStage()).isEqualTo(ExamStatusStage.IN_PROGRESS);
        assertThat(updatedExam.getStatus().getCode()).isEqualTo(ExamStatusCode.STATUS_STARTED);
        assertThat(updatedExam.getStatusChangedAt()).isGreaterThan(approvedStatusDate);
        assertThat(updatedExam.getBrowserUserAgent()).isEqualTo(browserUserAgent);
    }

    @Test
    public void shouldResumeExistingExamWithinGracePeriodPausedExam() throws InterruptedException {
        final String browserUserAgent = "007";
        Session session = new SessionBuilder().build();
        final Instant now = org.joda.time.Instant.now();
        final Instant approvedStatusDate = now.minus(5000);
        final Instant lastStudentActivityTime = now.minus(15 * 60 * 1000); // minus 15 minutes, within grace period
        final int resumePosition = 5;
        final int testLength = 10;

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), approvedStatusDate)
            .withSessionId(session.getId())
            .withResumptions(3)
            .withRestartsAndResumptions(5)
            .withMaxItems(10)
            .withStartedAt(now.minus(60000))
            .build();
        Assessment assessment = new AssessmentBuilder().build();
        TimeLimitConfiguration timeLimitConfiguration = new TimeLimitConfiguration.Builder()
            .withTaCheckinTimeMinutes(3)
            .withAssessmentId(assessment.getAssessmentId())
            .withExamDelayDays(2)
            .withExamRestartWindowMinutes(20) // "grace period" of 20 mins
            .withInterfaceTimeoutMinutes(4)
            .withRequestInterfaceTimeoutMinutes(5)
            .build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(exam.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName(exam.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamQueryRepository.findLastStudentActivity(exam.getId())).thenReturn(Optional.of(lastStudentActivityTime));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.of(session));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey()))
            .thenReturn(Optional.of(assessment));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(exam.getClientName(), assessment.getAssessmentId()))
            .thenReturn(Optional.of(timeLimitConfiguration));
        when(mockExamSegmentService.initializeExamSegments(exam, assessment)).thenReturn(testLength);
        when(mockExamItemService.getExamPosition(exam.getId())).thenReturn(resumePosition);
        when(mockExamItemService.getExamPosition(exam.getId())).thenReturn(5);
        when(mockExamApprovalService.verifyAccess(isA(ExamInfo.class), isA(Exam.class)))
            .thenReturn(Optional.empty());

        Response<ExamConfiguration> examConfigurationResponse = examService.startExam(exam.getId(), browserUserAgent);

        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockSessionService).findSessionById(exam.getSessionId());
        verify(mockAssessmentService).findAssessment(exam.getClientName(), exam.getAssessmentKey());
        verify(mockTimeLimitConfigurationService).findTimeLimitConfiguration(exam.getClientName(), assessment.getAssessmentId());
        verify(mockExamCommandRepository).update(examArgumentCaptor.capture());
        verify(mockExamItemService).getExamPosition(exam.getId());
        verify(mockExamQueryRepository).findLastStudentActivity(exam.getId());
        verify(mockExamItemService).getExamPosition(exam.getId());

        assertThat(examConfigurationResponse.getData().isPresent()).isTrue();
        ExamConfiguration examConfiguration = examConfigurationResponse.getData().get();
        assertThat(examConfiguration.getExam().getRestartsAndResumptions()).isEqualTo(5);
        assertThat(examConfiguration.getContentLoadTimeoutMinutes()).isEqualTo(120);
        assertThat(examConfiguration.getExam().getId()).isEqualTo(exam.getId());
        assertThat(examConfiguration.getExamRestartWindowMinutes()).isEqualTo(timeLimitConfiguration.getExamRestartWindowMinutes());
        assertThat(examConfiguration.getInterfaceTimeoutMinutes()).isEqualTo(timeLimitConfiguration.getInterfaceTimeoutMinutes());
        assertThat(examConfiguration.getPrefetch()).isEqualTo(assessment.getPrefetch());
        assertThat(examConfiguration.getStartPosition()).isEqualTo(5);
        assertThat(examConfiguration.getTestLength()).isEqualTo(testLength);

        // Sleep a bit to prevent intermittent test failures due to timing
        Exam updatedExam = examArgumentCaptor.getValue();
        assertThat(updatedExam).isNotNull();
        assertThat(updatedExam.getAttempts()).isEqualTo(0);
        assertThat(updatedExam.getId()).isEqualTo(exam.getId());
        assertThat(updatedExam.getMaxItems()).isEqualTo(testLength);
        assertThat(updatedExam.getStartedAt()).isNotNull();
        assertThat(updatedExam.getResumptions()).isEqualTo(4);
        assertThat(updatedExam.getRestartsAndResumptions()).isEqualTo(6);
        assertThat(updatedExam.getExpiresAt()).isNull();
        assertThat(updatedExam.getStatus().getStage()).isEqualTo(ExamStatusStage.IN_PROGRESS);
        assertThat(updatedExam.getStatus().getCode()).isEqualTo(ExamStatusCode.STATUS_STARTED);
        assertThat(updatedExam.getStatusChangedAt()).isGreaterThan(approvedStatusDate);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowIllegalArgumentExceptionWhenUpdatingStatusOnAnExamThatCannotBeFound() {
        UUID examId = UUID.randomUUID();

        when(mockExamQueryRepository.getExamById(examId)).thenReturn(Optional.empty());

        examService.updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_FAILED, ExamStatusStage.CLOSED));
    }

    @Test
    public void shouldPauseAllExamsInASession() {
        UUID mockSessionId = UUID.randomUUID();
        Set<String> mockStatusTransitionSet = new HashSet<>(Arrays.asList(ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_INITIALIZING));
        List<Exam> examsInSession = Arrays.asList(
            new ExamBuilder().withSessionId(mockSessionId)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.IN_USE), Instant.now())
                .build(),
            new ExamBuilder().withSessionId(mockSessionId)
                .build(),
            new ExamBuilder().withSessionId(mockSessionId)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_USE), Instant.now())
                .build()
        );

        when(mockExamQueryRepository.findAllExamsInSessionWithStatus(mockSessionId, mockStatusTransitionSet))
            .thenReturn(examsInSession);

        examService.pauseAllExamsInSession(mockSessionId);

        verify(mockExamCommandRepository, times(3)).update(any(Exam.class));
        verify(mockOnCompletedExamChangeListener, times(3)).accept(any(Exam.class), any(Exam.class));
    }

    @Test
    public void shouldNotCallUpdateWhenThereAreNoExamsToPauseInTheSession() {
        UUID mockSessionId = UUID.randomUUID();
        Set<String> mockStatusTransitionSet = new HashSet<>();

        when(mockExamQueryRepository.findAllExamsInSessionWithStatus(mockSessionId, mockStatusTransitionSet))
            .thenReturn(Lists.emptyList());

        examService.pauseAllExamsInSession(mockSessionId);

        verify(mockExamCommandRepository, times(0)).update(Matchers.<Exam>anyVararg());
    }



    @Test
    public void shouldMarkExamAsWaitingForSegmentEntryApproval() {
        Exam exam = new Exam.Builder()
            .fromExam(random(Exam.class))
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .build();
        SegmentApprovalRequest request = new SegmentApprovalRequest(exam.getSessionId(), exam.getBrowserId(), 2, true);

        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamApprovalService.getApproval(new ExamInfo(exam.getId(), request.getSessionId(), request.getBrowserId())))
            .thenReturn(new Response<>(new ExamApproval(exam.getId(), exam.getStatus(), null)));

        Optional<ValidationError> maybeError = examService.waitForSegmentApproval(exam.getId(), request);
        assertThat(maybeError).isNotPresent();

        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockExamApprovalService).getApproval(new ExamInfo(exam.getId(), request.getSessionId(), request.getBrowserId()));
        verify(mockExamCommandRepository).update(examArgumentCaptor.capture());

        Exam updatedExam = examArgumentCaptor.getValue();
        assertThat(updatedExam.getStatus().getCode()).isEqualTo(ExamStatusCode.STATUS_SEGMENT_ENTRY);
        assertThat(updatedExam.getWaitingForSegmentApprovalPosition()).isEqualTo(2);
    }

    @Test
    public void shouldMarkExamAsWaitingForSegmentExitApproval() {
        Exam exam = new Exam.Builder()
            .fromExam(random(Exam.class))
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .build();
        SegmentApprovalRequest request = new SegmentApprovalRequest(exam.getSessionId(), exam.getBrowserId(), 1, false);

        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamApprovalService.getApproval(new ExamInfo(exam.getId(), request.getSessionId(), request.getBrowserId())))
            .thenReturn(new Response<>(new ExamApproval(exam.getId(), exam.getStatus(), null)));

        Optional<ValidationError> maybeError = examService.waitForSegmentApproval(exam.getId(), request);
        assertThat(maybeError).isNotPresent();

        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockExamApprovalService).getApproval(new ExamInfo(exam.getId(), request.getSessionId(), request.getBrowserId()));
        verify(mockExamCommandRepository).update(examArgumentCaptor.capture());

        Exam updatedExam = examArgumentCaptor.getValue();
        assertThat(updatedExam.getStatus().getCode()).isEqualTo(ExamStatusCode.STATUS_SEGMENT_EXIT);
        assertThat(updatedExam.getWaitingForSegmentApprovalPosition()).isEqualTo(1);
    }

    @Test
    public void shouldReturnValidationErrorForInvalidStatusTransition() {
        Exam exam = new Exam.Builder()
            .fromExam(random(Exam.class))
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_SUSPENDED), Instant.now())
            .build();
        SegmentApprovalRequest request = new SegmentApprovalRequest(exam.getSessionId(), exam.getBrowserId(), 1, false);

        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockExamApprovalService.getApproval(new ExamInfo(exam.getId(), request.getSessionId(), request.getBrowserId())))
            .thenReturn(new Response<>(new ExamApproval(exam.getId(), exam.getStatus(), null)));

        Optional<ValidationError> maybeError = examService.waitForSegmentApproval(exam.getId(), request);
        assertThat(maybeError).isPresent();
        assertThat(maybeError.get().getCode()).isEqualTo(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE);

        verify(mockExamQueryRepository).getExamById(exam.getId());
        verify(mockExamApprovalService).getApproval(new ExamInfo(exam.getId(), request.getSessionId(), request.getBrowserId()));
        verify(mockExamCommandRepository, never()).update(isA(Exam.class));
    }

    @Test
    public void shouldReturnValidationErrorForFailedVerifyAccess() {
        Exam exam = new Exam.Builder()
            .fromExam(random(Exam.class))
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_SUSPENDED), Instant.now())
            .build();
        SegmentApprovalRequest request = new SegmentApprovalRequest(exam.getSessionId(), exam.getBrowserId(), 1, false);

        when(mockExamApprovalService.getApproval(new ExamInfo(exam.getId(), request.getSessionId(), request.getBrowserId())))
            .thenReturn(new Response<>(new ValidationError("some", "error")));

        Optional<ValidationError> maybeError = examService.waitForSegmentApproval(exam.getId(), request);
        assertThat(maybeError).isPresent();
        assertThat(maybeError.get().getCode()).isEqualTo("some");
        assertThat(maybeError.get().getMessage()).isEqualTo("error");

        verify(mockExamApprovalService).getApproval(new ExamInfo(exam.getId(), request.getSessionId(), request.getBrowserId()));
        verify(mockExamCommandRepository, never()).update(isA(Exam.class));
    }
}
