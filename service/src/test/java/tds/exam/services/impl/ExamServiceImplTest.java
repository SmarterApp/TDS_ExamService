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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.config.Accommodation;
import tds.config.AssessmentWindow;
import tds.config.ClientSystemFlag;
import tds.config.TimeLimitConfiguration;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalStatus;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.builder.OpenExamRequestBuilder;
import tds.exam.builder.SessionBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamStatusQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.RtsStudentPackageAttribute;
import tds.student.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private HistoryQueryRepository mockHistoryRepository;

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
    private ExamItemService mockExamItemService;

    @Captor
    private ArgumentCaptor<Exam> examArgumentCaptor;

    private ExamService examService;

    @Before
    public void setUp() {
        examService = new ExamServiceImpl(
            mockExamQueryRepository,
            mockHistoryRepository,
            mockSessionService,
            mockStudentService,
            mockExamSegmentService,
            mockAssessmentService,
            mockTimeLimitConfigurationService,
            mockConfigService,
            mockExamCommandRepository,
            mockExamItemService,
            mockExamStatusQueryRepository,
            mockExamAccommodationService);
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
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(openExamRequest.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.empty());
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.of(new Student(1, "testId", "CA", "clientName")));

        examService.openExam(openExamRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnErrorWhenStudentCannotBeFound() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(openExamRequest.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(new SessionBuilder().build()));
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.empty());

        examService.openExam(openExamRequest);
    }

    @Test
    public void shouldReturnValidationErrorWhenSessionIsNoOpen() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(openExamRequest.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(new SessionBuilder().withStatus("closed").build()));

        Response<Exam> response = examService.openExam(openExamRequest);

        assertThat(response.getData().isPresent()).isFalse();
        assertThat(response.getErrors()[0].getCode()).isEqualTo(ValidationErrorCode.SESSION_NOT_OPEN);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateIfExternSessionConfigCannotBeFoundWhileOpeningExam() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();
        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.empty());
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

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration(openExamRequest.getClientName(), "Development", 0, 0, 0, 0);

        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData().isPresent()).isFalse();
        assertThat(examResponse.getErrors()).hasSize(1);

        ValidationError validationError = examResponse.getErrors()[0];
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

        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockConfigService.findClientSystemFlag(openExamRequest.getClientName(), ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE)).thenReturn(Optional.of(clientSystemFlag));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.empty());
        when(mockAssessmentService.findAssessment(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockConfigService.findAssessmentWindows(openExamRequest.getClientName(), assessment.getAssessmentId(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        assertThat(examResponse.getErrors()).isEmpty();
        verify(mockExamCommandRepository).insert(isA(Exam.class));

        Exam exam = examResponse.getData().get();

        assertThat(exam.getAssessmentId()).isEqualTo(assessment.getAssessmentId());
        assertThat(exam.getAssessmentAlgorithm()).isEqualTo(assessment.getSelectionAlgorithm().getType());
        assertThat(exam.getAssessmentKey()).isEqualTo(openExamRequest.getAssessmentKey());
        assertThat(exam.getAssessmentWindowId()).isEqualTo("window1");
        assertThat(exam.getAttempts()).isEqualTo(1);
        assertThat(exam.getBrowserId()).isEqualTo(openExamRequest.getBrowserId());
        assertThat(exam.getDateJoined()).isGreaterThan(startTestTime);
        assertThat(exam.getClientName()).isEqualTo(openExamRequest.getClientName());
        assertThat(exam.getStudentId()).isEqualTo(openExamRequest.getStudentId());
        assertThat(exam.getLoginSSID()).isEqualTo("GUEST");
        assertThat(exam.getStudentName()).isEqualTo("GUEST");
        assertThat(exam.getEnvironment()).isEqualTo(extSessionConfig.getEnvironment());
        assertThat(exam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(exam.getSubject()).isEqualTo(assessment.getSubject());
    }

    @Test
    public void shouldOpenNewExamWithoutProctor() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(-1)
            .withProctorId(null)
            .build();

        Session currentSession = new SessionBuilder()
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

        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockConfigService.findClientSystemFlag(openExamRequest.getClientName(), ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE)).thenReturn(Optional.of(clientSystemFlag));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.empty());
        when(mockAssessmentService.findAssessment(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.empty());
        when(mockConfigService.findAssessmentWindows(openExamRequest.getClientName(), assessment.getAssessmentId(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockConfigService.findAssessmentAccommodationsByAssessmentKey(openExamRequest.getClientName(), openExamRequest.getAssessmentKey()))
            .thenReturn(Collections.singletonList(accommodation));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_APPROVED)).thenReturn(new ExamStatusCode(STATUS_APPROVED, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        assertThat(examResponse.getErrors()).isEmpty();

        Exam exam = examResponse.getData().get();
        assertThat(exam.getStatus().getStatus()).isEqualTo(ExamStatusCode.STATUS_APPROVED);
    }

    @Test
    public void shouldOpenNewExamWithProctor() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(1)
            .withProctorId(99L)
            .build();
        Session currentSession = new SessionBuilder()
            .build();

        Student student = new Student(1, "loginSSD", "CA", openExamRequest.getClientName());
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

        TimeLimitConfiguration configuration = new TimeLimitConfiguration.Builder().withExamDelayDays(0).build();

        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockStudentService.findStudentPackageAttributes(openExamRequest.getStudentId(), openExamRequest.getClientName(), EXTERNAL_ID, ENTITY_NAME, ACCOMMODATIONS))
            .thenReturn(Arrays.asList(externalIdAttribute, entityNameAttribute));
        when(mockConfigService.findAssessmentWindows(openExamRequest.getClientName(), assessment.getAssessmentId(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        verify(mockExamAccommodationService).initializeExamAccommodations(isA(Exam.class));

        assertThat(examResponse.getErrors()).isEmpty();

        Exam exam = examResponse.getData().get();
        assertThat(exam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(exam.getStudentName()).isEqualTo("Entity Id");
        assertThat(exam.getLoginSSID()).isEqualTo("External Id");
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

        Student student = new Student(1, "loginSSD", "CA", request.getClientName());

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), approvedStatusDate)
            .withBrowserId(UUID.randomUUID())
            .withDateChanged(Instant.now().minus(Days.days(2).toStandardDuration()))
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();
        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockConfigService.findAssessmentAccommodationsByAssessmentKey(request.getClientName(), request.getAssessmentKey())).thenReturn(Collections.emptyList());
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockConfigService.findClientSystemFlag(request.getClientName(), RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        verify(mockExamCommandRepository).update(isA(Exam.class));
        verify(mockExamAccommodationService).initializeAccommodationsOnPreviousExam(isA(Exam.class), isA(Assessment.class), isA(Integer.class), isA(Boolean.class), isA(String.class));

        assertThat(examResponse.getErrors()).isEmpty();

        Exam savedExam = examResponse.getData().get();

        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getStatusChangeDate()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
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

        Student student = new Student(request.getStudentId(), "testId", "CA", request.getClientName());

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), approvedStatusDate)
            .withDateChanged(Instant.now())
            .build();
        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(request.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockConfigService.findClientSystemFlag(request.getClientName(), RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getStatusChangeDate()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionIdSame() {
        OpenExamRequest request = new OpenExamRequestBuilder().build();

        Session currentSession = new SessionBuilder()
            .build();

        Session previousSession = new SessionBuilder()
            .withId(request.getSessionId())
            .build();

        Student student = new Student(1, "testId", "CA", request.getClientName());

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), approvedStatusDate)
            .withDateChanged(Instant.now())
            .build();
        Assessment assessment = new AssessmentBuilder().build();

        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration(request.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockConfigService.findClientSystemFlag(request.getClientName(), RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        assertThat(examResponse.getErrors()).isEmpty();

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getStatusChangeDate()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
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

        Student student = new Student(1, "testId", "CA", "clientName");

        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), approvedStatusDate)
            .withDateChanged(Instant.now())
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();

        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessment(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));
        when(mockConfigService.findClientSystemFlag(request.getClientName(), RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        assertThat(examResponse.getErrors()).isEmpty();

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getStatusChangeDate()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
    }

    @Test
    public void shouldOpenPreviousExamIfPreviousExamIsInactiveStage() {
        OpenExamRequest request = new OpenExamRequestBuilder()
            .withStudentId(-1)
            .build();

        Session currentSession = new SessionBuilder()
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
            .withDateStarted(Instant.now())
            .build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration(request.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);
        Assessment assessment = new AssessmentBuilder().build();

        ClientSystemFlag restoreAccommodations = new ClientSystemFlag.Builder()
            .withAuditObject(RESTORE_ACCOMMODATIONS_TYPE)
            .withEnabled(true)
            .build();

        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockAssessmentService.findAssessment(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_SUSPENDED)).thenReturn(new ExamStatusCode(STATUS_SUSPENDED, IN_USE));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_SUSPENDED)).thenReturn(new ExamStatusCode(STATUS_SUSPENDED, IN_USE));
        when(mockConfigService.findClientSystemFlag(request.getClientName(), RESTORE_ACCOMMODATIONS_TYPE)).thenReturn(Optional.of(restoreAccommodations));

        Response<Exam> examResponse = examService.openExam(request);

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_SUSPENDED);
        assertThat(savedExam.getStatusChangeDate()).isGreaterThan(approvedStatusDate);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
    }

    @Test
    public void shouldGetInitialAbilityFromScoresForSameAssessment() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST1";
        final long studentId = 9898L;
        final double assessmentAbilityVal = 99D;

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);
        Assessment assessment = new AssessmentBuilder()
            .withSubject("ELA")
            .build();

        Ability sameAssessmentAbility = new Ability(
            UUID.randomUUID(), assessmentId, 1, java.time.Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
            UUID.randomUUID(), assessmentId, 1, java.time.Instant.now(), 50D);

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);

        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, assessment);

        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldGetInitialAbilityFromHistoryWithoutSlopeIntercept() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST4";
        final long studentId = 9897L;

        Assessment assessment = new AssessmentBuilder()
            .withSubject("ELA")
            .withAssessmentId(assessmentId)
            .withAbilitySlope(1)
            .withAbilityIntercept(0)
            .withInitialAbilityBySubject(true)
            .build();
        // Null slope/intercept for this test case

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);
        List<Ability> abilities = new ArrayList<>();
        Optional<Double> maybeAbility = Optional.of(66D);

        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, assessment);
        verify(mockHistoryRepository).findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId);
        assertThat(maybeAbilityReturned.get()).isEqualTo(maybeAbility.get());
    }

    @Test
    public void shouldGetInitialAbilityFromItembank() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST6";
        final long studentId = 9898L;
        final float assessmentAbilityVal = 99F;
        final Double slope = 2D;
        final Double intercept = 1D;

        Assessment assessment = new Assessment();
        assessment.setKey("(SBAC)SBAC ELA 3-ELA-3-Spring-2112a");
        assessment.setAssessmentId(assessmentId);
        assessment.setSelectionAlgorithm(Algorithm.FIXED_FORM);
        assessment.setStartAbility(assessmentAbilityVal);

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        List<Ability> abilities = new ArrayList<>();
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(Optional.empty());
        when(mockAssessmentService.findAssessment(thisExam.getClientName(), thisExam.getAssessmentId())).thenReturn(Optional.of(assessment));
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, assessment);
        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldGetInitialAbilityFromHistoryWithSlopeIntercept() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST3";
        final long studentId = 9898L;
        final float slope = 2f;
        final float intercept = 1f;

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        Assessment assessment = new AssessmentBuilder()
            .withSubject("ELA")
            .withAbilitySlope(slope)
            .withAbilityIntercept(intercept)
            .withInitialAbilityBySubject(true)
            .build();

        List<Ability> abilities = new ArrayList<>();
        Optional<Double> maybeAbility = Optional.of(66D);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, assessment);
        // y=mx+b
        double abilityCalculated = maybeAbility.get() * slope + intercept;
        assertThat(maybeAbilityReturned.get()).isEqualTo((float) abilityCalculated);
    }

    @Test
    public void shouldGetInitialAbilityFromScoresForDifferentAssessment() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST2";
        final long studentId = 9899L;
        final double assessmentAbilityVal = 75D;


        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        Ability sameAssessmentAbility = new Ability(
            UUID.randomUUID(), "assessmentid-2", 1, java.time.Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
            UUID.randomUUID(), "assessmentid-2", 1, java.time.Instant.now(), 50D);

        Assessment assessment = new AssessmentBuilder()
            .withAssessmentId(assessmentId)
            .withInitialAbilityBySubject(true)
            .withSubject("ELA")
            .build();

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, assessment);
        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldReturnExamApprovalBecauseAllRulesAreSatisfied() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(60).toStandardDuration()))
                .withDateVisited(Instant.now())
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfiguration(clientName, mockEnvironment, 0, 0, 0, 0)));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        Response<ExamApproval> result = examService.getApproval(approvalRequest);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().isPresent()).isTrue();
        assertThat(result.getData().get().getExamId()).isEqualTo(examId);
        assertThat(result.getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
    }

    @Test
    public void shouldReturnExamApprovalWithWaitingStatusBecauseEnvironmentIsDevelopment() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "development";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .withStatus(new ExamStatusCode(STATUS_PENDING, OPEN), Instant.now())
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now())
                .withStatus("closed")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        Response<ExamApproval> result = examService.getApproval(approvalRequest);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().isPresent()).isTrue();
        assertThat(result.getData().get().getExamId()).isEqualTo(examId);
        assertThat(result.getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.WAITING);
    }

    @Test
    public void shouldReturnExamApprovalWithApprovedStatusBecauseEnvironmentIsSimulation() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "SimUlaTIon";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().minus(Minutes.minutes(10).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(11).toStandardDuration()))
                .withStatus("closed")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        Response<ExamApproval> result = examService.getApproval(approvalRequest);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().isPresent()).isTrue();
        assertThat(result.getData().get().getExamId()).isEqualTo(examId);
        assertThat(result.getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
    }

    @Test
    public void shouldReturnExamApprovalWithCorrectExamStatusBecauseSessionIsProctorless() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "development";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().minus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(55).toStandardDuration()))
                .withStatus("closed")
                .withProctorId(null)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        Response<ExamApproval> result = examService.getApproval(approvalRequest);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().isPresent()).isTrue();
        assertThat(result.getData().get().getExamId()).isEqualTo(examId);
        assertThat(result.getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
    }

    @Test
    public void shouldReturnValidationErrorDueToBrowserKeyMismatch() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(UUID.randomUUID())
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(60).toStandardDuration()))
                .withDateVisited(Instant.now())
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        Response<ExamApproval> result = examService.getApproval(approvalRequest);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH);
    }

    @Test
    public void shouldReturnValidationErrorDueToSessionKeyMismatch() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(UUID.randomUUID())
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(60).toStandardDuration()))
                .withDateVisited(Instant.now())
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        Response<ExamApproval> result = examService.getApproval(approvalRequest);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH);
    }

    @Test
    public void shouldReturnValidationErrorDueToClosedSession() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().minus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("closed")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        Response<ExamApproval> result = examService.getApproval(approvalRequest);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED);
    }

    @Test
    public void shouldReturnValidationErrorDueToTaCheckinTimeExpired() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        Response<ExamApproval> result = examService.getApproval(approvalRequest);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_TA_CHECKIN_TIMEOUT);
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

        Optional<ValidationError> maybeStatusTransitionFailure = examService.pauseExam(examId);

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

        Optional<ValidationError> maybeStatusTransitionFailure = examService.pauseExam(examId);

        assertThat(maybeStatusTransitionFailure).isPresent();
        ValidationError statusTransitionFailure = maybeStatusTransitionFailure.get();
        assertThat(statusTransitionFailure.getCode()).isEqualTo(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE);
        assertThat(statusTransitionFailure.getMessage()).isEqualTo("Bad status transition from foo to paused");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenExamIsNotPresent() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.empty());
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        examService.getApproval(approvalRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSessionIsNotPresent() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        examService.getApproval(approvalRequest);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenExternalSessionConfigurationIsNotPresent() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.empty());
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        examService.getApproval(approvalRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenTimeLimitConfigurationIsNotPresent() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.empty());

        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserKey, clientName);

        examService.getApproval(approvalRequest);
    }

    @Test
    public void shouldReturnFailureExamConfigForNoExamFound() {
        UUID examID = UUID.randomUUID();
        when(mockExamQueryRepository.getExamById(examID)).thenReturn(Optional.empty());
        Response<ExamConfiguration> response = examService.startExam(examID);
        assertThat(response.getErrors()).hasSize(1);
        ValidationError error = response.getErrors()[0];
        assertThat(error.getCode()).isEqualTo(ExamStatusCode.STATUS_FAILED);
        assertThat(error.getMessage()).isNotNull();
    }

    @Test
    public void shouldReturnFailureExamConfigForExamStatusNotApproved() {
        Exam exam = new ExamBuilder()
            .build();
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.empty());
        Response<ExamConfiguration> response = examService.startExam(exam.getId());
        assertThat(response.getErrors()).hasSize(1);
        ValidationError error = response.getErrors()[0];
        assertThat(error.getCode()).isEqualTo(ExamStatusCode.STATUS_FAILED);
        assertThat(error.getMessage()).isNotNull();
    }

    @Test
    public void shouldReturnFailureExamConfigForNoSessionFound() {
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), Instant.now())
            .build();
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.empty());
        Response<ExamConfiguration> response = examService.startExam(exam.getId());
        assertThat(response.getErrors()).hasSize(1);
        ValidationError error = response.getErrors()[0];
        assertThat(error.getCode()).isEqualTo(ExamStatusCode.STATUS_FAILED);
        assertThat(error.getMessage()).isNotNull();
    }

    @Test
    public void shouldReturnFailureExamConfigForNoAssessmentFound() {
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
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(exam.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(exam.getClientName(), "assessmentId"))
            .thenReturn(Optional.of(timeLimitConfiguration));
        when(mockSessionService.findExternalSessionConfigurationByClientName(exam.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.of(session));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())).thenReturn(Optional.empty());
        Response<ExamConfiguration> response = examService.startExam(exam.getId());
        assertThat(response.getErrors()).hasSize(1);
        ValidationError error = response.getErrors()[0];
        assertThat(error.getCode()).isEqualTo(ExamStatusCode.STATUS_FAILED);
        assertThat(error.getMessage()).isNotNull();
    }

    @Test
    public void shouldStartNewExam() throws InterruptedException {
        Session session = new SessionBuilder().build();
        Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), approvedStatusDate)
            .withSessionId(session.getId())
            .withDateChanged(Instant.now().minus(50000))
            .withDateStarted(null)
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
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(exam.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);
        final int testLength = 10;

        when(mockSessionService.findExternalSessionConfigurationByClientName(exam.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        when(mockSessionService.findSessionById(exam.getSessionId())).thenReturn(Optional.of(session));
        when(mockAssessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey()))
            .thenReturn(Optional.of(assessment));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(exam.getClientName(), assessment.getAssessmentId()))
            .thenReturn(Optional.of(timeLimitConfiguration));
        when(mockExamSegmentService.initializeExamSegments(exam, assessment)).thenReturn(testLength);
        Response<ExamConfiguration> examConfigurationResponse = examService.startExam(exam.getId());
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
        assertThat(updatedExam.getDateStarted()).isNotNull();
        assertThat(updatedExam.getDateChanged()).isGreaterThan(exam.getDateChanged());
        assertThat(updatedExam.getExpireFrom()).isNotNull();
        assertThat(updatedExam.getStatus().getStage()).isEqualTo(ExamStatusStage.IN_PROGRESS);
        assertThat(updatedExam.getStatus().getStatus()).isEqualTo(ExamStatusCode.STATUS_STARTED);
        assertThat(updatedExam.getStatusChangeDate()).isGreaterThan(approvedStatusDate);
    }

    @Test
    public void shouldRestartExistingExamOutsideGracePeriodPausedExam() throws InterruptedException {
        Session session = new SessionBuilder().build();
        final Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        final Instant lastStudentActivityTime = org.joda.time.Instant.now().minus(25 * 60 * 1000); // minus 25 minutes
        final int testLength = 10;

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), approvedStatusDate)
            .withSessionId(session.getId())
            .withResumptions(3)
            .withDateChanged(Instant.now().minus(50000))
            .withRestartsAndResumptions(5)
            .withMaxItems(10)
            .withDateStarted(Instant.now().minus(60000))
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

        Response<ExamConfiguration> examConfigurationResponse = examService.startExam(exam.getId());

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
        assertThat(updatedExam.getDateStarted()).isNotNull();
        assertThat(updatedExam.getResumptions()).isEqualTo(3);
        assertThat(updatedExam.getRestartsAndResumptions()).isEqualTo(6);
        assertThat(updatedExam.getDateChanged()).isGreaterThan(exam.getDateChanged());
        assertThat(updatedExam.getExpireFrom()).isNull();
        assertThat(updatedExam.getStatus().getStage()).isEqualTo(ExamStatusStage.IN_PROGRESS);
        assertThat(updatedExam.getStatus().getStatus()).isEqualTo(ExamStatusCode.STATUS_STARTED);
        assertThat(updatedExam.getStatusChangeDate()).isGreaterThan(approvedStatusDate);
    }

    @Test
    public void shouldResumeExistingExamWithinGracePeriodPausedExam() throws InterruptedException {
        Session session = new SessionBuilder().build();
        final Instant approvedStatusDate = org.joda.time.Instant.now().minus(5000);
        final Instant lastStudentActivityTime = org.joda.time.Instant.now().minus(15 * 60 * 1000); // minus 15 minutes, within grace period
        final int resumePosition = 5;
        final int testLength = 10;

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), approvedStatusDate)
            .withSessionId(session.getId())
            .withResumptions(3)
            .withDateChanged(Instant.now().minus(50000))
            .withRestartsAndResumptions(5)
            .withMaxItems(10)
            .withDateStarted(Instant.now().minus(60000))
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

        Response<ExamConfiguration> examConfigurationResponse = examService.startExam(exam.getId());

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
        assertThat(updatedExam.getDateStarted()).isNotNull();
        assertThat(updatedExam.getResumptions()).isEqualTo(4);
        assertThat(updatedExam.getRestartsAndResumptions()).isEqualTo(6);
        assertThat(updatedExam.getDateChanged()).isGreaterThan(exam.getDateChanged());
        assertThat(updatedExam.getExpireFrom()).isNull();
        assertThat(updatedExam.getStatus().getStage()).isEqualTo(ExamStatusStage.IN_PROGRESS);
        assertThat(updatedExam.getStatus().getStatus()).isEqualTo(ExamStatusCode.STATUS_STARTED);
        assertThat(updatedExam.getStatusChangeDate()).isGreaterThan(approvedStatusDate);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowIllegalArgumentExceptionWhenPausingAnExamThatCannotBeFound() {
        UUID examId = UUID.randomUUID();

        when(mockExamQueryRepository.getExamById(examId)).thenReturn(Optional.empty());

        examService.pauseExam(examId);
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

        verify(mockExamCommandRepository).update(Matchers.<Exam>anyVararg());
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

    private Exam createExam(UUID sessionId, UUID thisExamId, String assessmentId, String clientName, long studentId) {
        return new Exam.Builder()
            .withId(thisExamId)
            .withClientName(clientName)
            .withSessionId(sessionId)
            .withAssessmentId(assessmentId)
            .withSubject("ELA")
            .withStudentId(studentId)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
            .withDateChanged(Instant.now())
            .withDateScored(Instant.now())
            .build();
    }
}
