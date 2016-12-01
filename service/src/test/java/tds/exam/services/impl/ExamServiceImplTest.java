package tds.exam.services.impl;

import org.joda.time.Days;
import org.joda.time.Instant;
import org.joda.time.Minutes;
import org.junit.After;
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
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.common.Response;
import tds.common.ValidationError;
import tds.config.Accommodation;
import tds.config.AssessmentWindow;
import tds.config.ClientSystemFlag;
import tds.config.ClientTestProperty;
import tds.config.TimeLimitConfiguration;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalStatus;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.builder.OpenExamRequestBuilder;
import tds.exam.builder.SessionBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamStatusQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ConfigService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tds.config.ClientSystemFlag.ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;
import static tds.exam.ExamStatusCode.STATUS_PENDING;
import static tds.exam.ExamStatusCode.STATUS_SUSPENDED;
import static tds.exam.ExamStatusStage.INUSE;
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
    private ExamAccommodationCommandRepository mockExamAccommodationCommandRepository;

    @Mock
    private ExamStatusQueryRepository mockExamStatusQueryRepository;

    @Captor
    private ArgumentCaptor<List<ExamAccommodation>> examAccommodationCaptor;

    private ExamService examService;

    @Before
    public void setUp() {
        examService = new ExamServiceImpl(
            mockExamQueryRepository,
            mockHistoryRepository,
            mockSessionService,
            mockStudentService,
            mockAssessmentService,
            mockTimeLimitConfigurationService,
            mockConfigService,
            mockExamCommandRepository,
            mockExamAccommodationCommandRepository,
            mockExamStatusQueryRepository);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldReturnAnExam() {
        UUID examId = UUID.randomUUID();
        when(mockExamQueryRepository.getExamById(examId)).thenReturn(Optional.of(new Exam.Builder().withId(examId).build()));

        assertThat(examService.getExam(examId).get().getId()).isEqualTo(examId);
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

        assertThat(response.getData()).isNotPresent();
        assertThat(response.getErrors().get()[0].getCode()).isEqualTo(ValidationErrorCode.SESSION_NOT_OPEN);
    }

    @Test
    public void shouldReturnErrorWhenOpenExamPreviousSessionTypeDoesNotEqualCurrentSessionType() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();

        Session currentSession = new SessionBuilder()
            .withType(2)
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withType(33)
            .build();

        Student student = new Student(openExamRequest.getStudentId(), "testId", "CA", openExamRequest.getClientName());

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.INACTIVE))
            .build();

        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(openExamRequest.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        Assessment assessment = new AssessmentBuilder().build();

        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockAssessmentService.findAssessmentByKey(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.of(student));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.SESSION_TYPE_MISMATCH);
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
            .withType(2)
            .build();


        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration(openExamRequest.getClientName(), "Development", 0, 0, 0, 0);

        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessmentByKey(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
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
            .withType(2)
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
        when(mockAssessmentService.findAssessmentByKey(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockConfigService.findAssessmentWindows(openExamRequest.getClientName(), assessment.getAssessmentId(), currentSession.getType(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockConfigService.findAssessmentAccommodations(openExamRequest.getAssessmentKey()))
            .thenReturn(Collections.singletonList(accommodation));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        assertThat(examResponse.getErrors()).isEmpty();
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        verify(mockExamAccommodationCommandRepository).insert(examAccommodationCaptor.capture());

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

        assertThat(examAccommodationCaptor.getValue()).hasSize(1);
    }

    @Test
    public void shouldOpenNewExamWithoutProctor() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(-1)
            .withProctorId(null)
            .build();

        Session currentSession = new SessionBuilder()
            .withType(2)
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
        when(mockAssessmentService.findAssessmentByKey(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.empty());
        when(mockConfigService.findAssessmentWindows(openExamRequest.getClientName(), assessment.getAssessmentId(), currentSession.getType(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockConfigService.findAssessmentAccommodations(openExamRequest.getAssessmentKey()))
            .thenReturn(Collections.singletonList(accommodation));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_APPROVED)).thenReturn(new ExamStatusCode(STATUS_APPROVED, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        verify(mockExamAccommodationCommandRepository).insert(examAccommodationCaptor.capture());
        assertThat(examResponse.getErrors()).isEmpty();

        Exam exam = examResponse.getData().get();
        assertThat(exam.getStatus().getStatus()).isEqualTo(ExamStatusCode.STATUS_APPROVED);

        assertThat(examAccommodationCaptor.getValue()).hasSize(1);
    }

    @Test
    public void shouldOpenNewExamWithProctor() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder()
            .withStudentId(1)
            .withProctorId(99L)
            .build();
        Session currentSession = new SessionBuilder()
            .withType(2)
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

        TimeLimitConfiguration configuration = new TimeLimitConfiguration.Builder().withExamDelayDays(0).build();

        when(mockSessionService.findSessionById(openExamRequest.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(openExamRequest.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessmentByKey(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName())).thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName())).thenReturn(Optional.of(extSessionConfig));
        when(mockStudentService.findStudentPackageAttributes(openExamRequest.getStudentId(), openExamRequest.getClientName(), EXTERNAL_ID, ENTITY_NAME, ACCOMMODATIONS))
            .thenReturn(Arrays.asList(externalIdAttribute, entityNameAttribute));
        when(mockConfigService.findAssessmentWindows(openExamRequest.getClientName(), assessment.getAssessmentId(), currentSession.getType(), openExamRequest.getStudentId(), extSessionConfig))
            .thenReturn(Collections.singletonList(window));
        when(mockConfigService.findAssessmentAccommodations(openExamRequest.getAssessmentKey())).thenReturn(Arrays.asList(accommodation, nonDefaultAccommodation, dependsOnToolTypeAccommodation));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(openExamRequest.getClientName(), openExamRequest.getAssessmentKey())).thenReturn(Optional.of(configuration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(openExamRequest);
        verify(mockExamCommandRepository).insert(isA(Exam.class));
        verify(mockExamAccommodationCommandRepository).insert(examAccommodationCaptor.capture());

        assertThat(examResponse.getErrors()).isEmpty();

        Exam exam = examResponse.getData().get();
        assertThat(exam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(exam.getStudentName()).isEqualTo("Entity Id");
        assertThat(exam.getLoginSSID()).isEqualTo("External Id");

        List<ExamAccommodation> accommodations = examAccommodationCaptor.getValue();
        assertThat(accommodations).hasSize(1);
        ExamAccommodation examAccommodation = accommodations.get(0);
        assertThat(examAccommodation.getCode()).isEqualTo("code");
        assertThat(examAccommodation.getType()).isEqualTo("type");
        assertThat(examAccommodation.getSegmentKey()).isEqualTo("segmentKey");
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfDayHasPassed() {
        OpenExamRequest request = new OpenExamRequestBuilder().build();

        Session currentSession = new SessionBuilder()
            .withType(2)
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withType(2)
            .build();

        Student student = new Student(1, "loginSSD", "CA", request.getClientName());

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
            .withBrowserId(UUID.randomUUID())
            .withDateChanged(Instant.now().minus(Days.days(2).toStandardDuration()))
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();

        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessmentByKey(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockConfigService.findAssessmentAccommodations(request.getAssessmentKey()))
            .thenReturn(Collections.emptyList());
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(request);

        verify(mockExamCommandRepository).update(isA(Exam.class));

        assertThat(examResponse.getErrors()).isNotPresent();

        Exam savedExam = examResponse.getData().get();

        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfPreviousSessionIsClosed() {
        OpenExamRequest request = new OpenExamRequestBuilder()
            .withMaxAttempts(5)
            .build();

        Session currentSession = new SessionBuilder()
            .withType(2)
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withStatus("closed")
            .build();

        Student student = new Student(request.getStudentId(), "testId", "CA", request.getClientName());

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
            .withDateChanged(Instant.now())
            .build();
        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(request.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(extSessionConfig));

        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessmentByKey(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(request);

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionIdSame() {
        OpenExamRequest request = new OpenExamRequestBuilder().build();

        Session currentSession = new SessionBuilder()
            .withType(2)
            .build();

        Session previousSession = new SessionBuilder()
            .withId(request.getSessionId())
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", request.getClientName());

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
            .withDateChanged(Instant.now())
            .build();
        Assessment assessment = new AssessmentBuilder().build();

        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration(request.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);

        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(extSessionConfig));

        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessmentByKey(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(request);

        assertThat(examResponse.getErrors()).isNotPresent();

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionEndTimeIsBeforeNow() {
        OpenExamRequest request = new OpenExamRequestBuilder().build();

        Session currentSession = new SessionBuilder()
            .withType(2)
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withDateEnd(Instant.now().minus(Days.days(1).toStandardDuration()))
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
            .withDateChanged(Instant.now())
            .build();

        Assessment assessment = new AssessmentBuilder().build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();

        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockStudentService.getStudentById(request.getStudentId())).thenReturn(Optional.of(student));
        when(mockAssessmentService.findAssessmentByKey(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_PENDING)).thenReturn(new ExamStatusCode(STATUS_PENDING, OPEN));

        Response<Exam> examResponse = examService.openExam(request);

        assertThat(examResponse.getErrors()).isNotPresent();

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(savedExam.getDateChanged()).isNotNull();
        assertThat(savedExam.getDateStarted()).isEqualTo(previousExam.getDateStarted());
    }

    @Test
    public void shouldOpenPreviousExamIfPreviousExamIsInactiveStage() {
        OpenExamRequest request = new OpenExamRequestBuilder()
            .withStudentId(-1)
            .build();

        Session currentSession = new SessionBuilder()
            .withType(2)
            .build();

        Session previousSession = new SessionBuilder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withDateEnd(Instant.now().minus(Days.days(1).toStandardDuration()))
            .build();

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.INACTIVE))
            .withDateStarted(Instant.now())
            .build();
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration(request.getClientName(), SIMULATION_ENVIRONMENT, 0, 0, 0, 0);
        Assessment assessment = new AssessmentBuilder().build();

        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockSessionService.findSessionById(request.getSessionId())).thenReturn(Optional.of(currentSession));
        when(mockExamQueryRepository.getLastAvailableExam(request.getStudentId(), assessment.getAssessmentId(), request.getClientName())).thenReturn(Optional.of(previousExam));
        when(mockSessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        when(mockSessionService.findExternalSessionConfigurationByClientName(request.getClientName())).thenReturn(Optional.of(externalSessionConfiguration));
        when(mockAssessmentService.findAssessmentByKey(request.getClientName(), request.getAssessmentKey())).thenReturn(Optional.of(assessment));
        when(mockExamStatusQueryRepository.findExamStatusCode(STATUS_SUSPENDED)).thenReturn(new ExamStatusCode(STATUS_SUSPENDED, INUSE));

        Response<Exam> examResponse = examService.openExam(request);

        Exam savedExam = examResponse.getData().get();
        assertThat(savedExam.getId()).isEqualTo(previousExam.getId());
        assertThat(savedExam.getBrowserId()).isEqualTo(request.getBrowserId());
        assertThat(savedExam.getBrowserId()).isNotEqualTo(previousExam.getBrowserId());
        assertThat(savedExam.getStatus().getStatus()).isEqualTo(STATUS_SUSPENDED);
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

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
            .withClientName(clientName)
            .withAssessmentId(assessmentId)
            .withMaxOpportunities(3)
            .withPrefetch(2)
            .withIsSelectable(true)
            .withLabel("Grades 3 - 5 MATH")
            .withSubjectName("ELA")
            .withAccommodationFamily("MATH")
            .withRtsFormField("tds-testform")
            .withRequireRtsWindow(true)
            .withRtsModeField("tds-testmode")
            .withRequireRtsMode(true)
            .withRequireRtsModeWindow(true)
            .withDeleteUnansweredItems(true)
            .withInitialAbilityBySubject(true)
            .withAbilitySlope(1D)
            .withAbilityIntercept(2D)
            .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        Ability sameAssessmentAbility = new Ability(
            UUID.randomUUID(), assessmentId, 1, java.time.Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
            UUID.randomUUID(), assessmentId, 1, java.time.Instant.now(), 50D);

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);

        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldGetInitialAbilityFromHistoryWithoutSlopeIntercept() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST4";
        final long studentId = 9897L;

        // Null slope/intercept for this test case
        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
            .withClientName(clientName)
            .withAssessmentId(assessmentId)
            .withMaxOpportunities(3)
            .withPrefetch(2)
            .withIsSelectable(true)
            .withLabel("Grades 3 - 5 MATH")
            .withSubjectName("ELA")
            .withAccommodationFamily("MATH")
            .withRtsFormField("tds-testform")
            .withRequireRtsWindow(true)
            .withRtsModeField("tds-testmode")
            .withRequireRtsMode(true)
            .withRequireRtsModeWindow(true)
            .withDeleteUnansweredItems(true)
            .withInitialAbilityBySubject(true)
            .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);
        List<Ability> abilities = new ArrayList<>();
        Optional<Double> maybeAbility = Optional.of(66D);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        assertThat(maybeAbilityReturned.get()).isEqualTo(maybeAbility.get());
    }

    @Test
    public void shouldGetNullInitialAbility() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST7";
        final long studentId = 9898L;
        final Double slope = 2D;
        final Double intercept = 1D;

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
            .withClientName(clientName)
            .withAssessmentId(assessmentId)
            .withMaxOpportunities(3)
            .withPrefetch(2)
            .withIsSelectable(true)
            .withLabel("Grades 3 - 5 MATH")
            .withSubjectName("ELA")
            .withAccommodationFamily("MATH")
            .withRtsFormField("tds-testform")
            .withRequireRtsWindow(true)
            .withRtsModeField("tds-testmode")
            .withRequireRtsMode(true)
            .withRequireRtsModeWindow(true)
            .withDeleteUnansweredItems(true)
            .withInitialAbilityBySubject(true)
            .withAbilitySlope(slope)
            .withAbilityIntercept(intercept)
            .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        List<Ability> abilities = new ArrayList<>();
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(Optional.empty());
        when(mockAssessmentService.findAssessmentByKey(thisExam.getClientName(), thisExam.getAssessmentId())).thenReturn(Optional.empty());
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        assertThat(maybeAbilityReturned).isNotPresent();
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

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
            .withClientName(clientName)
            .withAssessmentId(assessmentId)
            .withMaxOpportunities(3)
            .withPrefetch(2)
            .withIsSelectable(true)
            .withLabel("Grades 3 - 5 MATH")
            .withSubjectName("ELA")
            .withAccommodationFamily("MATH")
            .withRtsFormField("tds-testform")
            .withRequireRtsWindow(true)
            .withRtsModeField("tds-testmode")
            .withRequireRtsMode(true)
            .withRequireRtsModeWindow(true)
            .withDeleteUnansweredItems(true)
            .withInitialAbilityBySubject(true)
            .withAbilitySlope(slope)
            .withAbilityIntercept(intercept)
            .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        List<Ability> abilities = new ArrayList<>();
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(Optional.empty());
        when(mockAssessmentService.findAssessmentByKey(thisExam.getClientName(), thisExam.getAssessmentId())).thenReturn(Optional.of(assessment));
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldGetInitialAbilityFromHistoryWithSlopeIntercept() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST3";
        final long studentId = 9898L;
        final Double slope = 2D;
        final Double intercept = 1D;

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
            .withClientName(clientName)
            .withAssessmentId(assessmentId)
            .withMaxOpportunities(3)
            .withPrefetch(2)
            .withIsSelectable(true)
            .withLabel("Grades 3 - 5 MATH")
            .withSubjectName("ELA")
            .withAccommodationFamily("MATH")
            .withRtsFormField("tds-testform")
            .withRequireRtsWindow(true)
            .withRtsModeField("tds-testmode")
            .withRequireRtsMode(true)
            .withRequireRtsModeWindow(true)
            .withDeleteUnansweredItems(true)
            .withInitialAbilityBySubject(true)
            .withAbilitySlope(slope)
            .withAbilityIntercept(intercept)
            .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);
        List<Ability> abilities = new ArrayList<>();
        Optional<Double> maybeAbility = Optional.of(66D);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        // y=mx+b
        double abilityCalulated = maybeAbility.get() * slope + intercept;
        assertThat(maybeAbilityReturned.get()).isEqualTo((float) abilityCalulated);
    }

    @Test
    public void shouldGetInitialAbilityFromScoresForDifferentAssessment() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST2";
        final long studentId = 9899L;
        final double assessmentAbilityVal = 75D;

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
            .withClientName(clientName)
            .withAssessmentId(assessmentId)
            .withMaxOpportunities(3)
            .withPrefetch(2)
            .withIsSelectable(true)
            .withLabel("Grades 3 - 5 MATH")
            .withSubjectName("ELA")
            .withAccommodationFamily("MATH")
            .withRtsFormField("tds-testform")
            .withRequireRtsWindow(true)
            .withRtsModeField("tds-testmode")
            .withRequireRtsMode(true)
            .withRequireRtsModeWindow(true)
            .withDeleteUnansweredItems(true)
            .withInitialAbilityBySubject(true)
            .withAbilitySlope(1D)
            .withAbilityIntercept(2D)
            .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        Ability sameAssessmentAbility = new Ability(
            UUID.randomUUID(), "assessmentid-2", 1, java.time.Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
            UUID.randomUUID(), "assessmentid-2", 1, java.time.Instant.now(), 50D);

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
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
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
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

        assertThat(result.getErrors()).isNotPresent();
        assertThat(result.getData()).isPresent();
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
                .withStatus(new ExamStatusCode(STATUS_PENDING, OPEN))
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

        assertThat(result.getErrors()).isNotPresent();
        assertThat(result.getData()).isPresent();
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
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
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

        assertThat(result.getErrors()).isNotPresent();
        assertThat(result.getData()).isPresent();
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
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
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

        assertThat(result.getErrors()).isNotPresent();
        assertThat(result.getData()).isPresent();
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

        assertThat(result.getErrors()).isPresent();
        assertThat(result.getErrors().get().length).isEqualTo(1);
        assertThat(result.getErrors().get()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH);
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

        assertThat(result.getErrors()).isPresent();
        assertThat(result.getErrors().get().length).isEqualTo(1);
        assertThat(result.getErrors().get()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH);
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

        assertThat(result.getErrors()).isPresent();
        assertThat(result.getErrors().get().length).isEqualTo(1);
        assertThat(result.getErrors().get()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED);
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

        assertThat(result.getErrors()).isPresent();
        assertThat(result.getErrors().get().length).isEqualTo(1);
        assertThat(result.getErrors().get()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_TA_CHECKIN_TIMEOUT);
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

    private Exam createExam(UUID sessionId, UUID thisExamId, String assessmentId, String clientName, long studentId) {
        return new Exam.Builder()
            .withId(thisExamId)
            .withClientName(clientName)
            .withSessionId(sessionId)
            .withAssessmentId(assessmentId)
            .withSubject("ELA")
            .withStudentId(studentId)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN))
            .withDateChanged(Instant.now())
            .withDateScored(Instant.now())
            .build();
    }
}
