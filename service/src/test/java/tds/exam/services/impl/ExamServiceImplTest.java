package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.common.Response;
import tds.common.ValidationError;
import tds.config.ClientTestProperty;
import tds.config.TimeLimitConfiguration;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalStatus;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExamRequest;
import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamServiceImplTest {
    private ExamQueryRepository repository;
    private HistoryQueryRepository historyRepository;
    private ExamServiceImpl examService;
    private SessionService sessionService;
    private StudentService studentService;
    private AssessmentService assessmentService;
    private TimeLimitConfigurationService timeLimitConfigurationService;

    @Before
    public void setUp() {
        repository = mock(ExamQueryRepository.class);
        historyRepository = mock(HistoryQueryRepository.class);
        sessionService = mock(SessionService.class);
        studentService = mock(StudentService.class);
        assessmentService = mock(AssessmentService.class);
        timeLimitConfigurationService = mock(TimeLimitConfigurationService.class);

        examService = new ExamServiceImpl(
                repository,
                historyRepository,
                sessionService,
                studentService,
                assessmentService,
                timeLimitConfigurationService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldReturnAnExam() {
        UUID examId = UUID.randomUUID();
        when(repository.getExamById(examId)).thenReturn(Optional.of(new Exam.Builder().withId(examId).build()));

        assertThat(examService.getExam(examId).get().getId()).isEqualTo(examId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnErrorWhenSessionCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setStudentId(1);

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.empty());
        when(studentService.getStudentById(1)).thenReturn(Optional.of(new Student(1, "testId", "CA", "clientName")));

        examService.openExam(openExamRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnErrorWhenStudentCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(new Session.Builder().build()));
        when(studentService.getStudentById(1)).thenReturn(Optional.empty());

        examService.openExam(openExamRequest);
    }

    @Test
    public void shouldReturnErrorWhenPreviousSessionTypeDoesNotEqualCurrentSessionType() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(33)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_INACTIVE).build())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.SESSION_TYPE_MISMATCH);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateIfExternSessionConfigCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.empty());
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.empty());
        examService.openExam(openExamRequest);
    }

    @Test
    public void shouldReturnErrorWhenMaxOpportunitiesLessThanZeroAndEnvironmentNotSimulation() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(-1);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.empty());
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfigurationBuilder().build();
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(extSessionConfig));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.SIMULATION_ENVIRONMENT_REQUIRED);
    }

    @Test
    public void shouldNotAllowExamToOpenIfStillActive() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development", 0, 0, 0, 0);
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.CURRENT_EXAM_OPEN);
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfDayHasPassed() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .withDateChanged(Instant.now().minus(2, ChronoUnit.DAYS))
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfPreviousSessionIsClosed() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withStatus("closed")
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .withDateChanged(Instant.now())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionIdSame() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(sessionId)
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .withDateChanged(Instant.now())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionEndTimeIsBeforeNow() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withDateEnd(Instant.now().minus(1, ChronoUnit.DAYS))
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .withDateChanged(Instant.now())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
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
                UUID.randomUUID(), assessmentId, 1, Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
                UUID.randomUUID(), assessmentId, 1, Instant.now(), 50D);

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
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
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(historyRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
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
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(historyRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
                .thenReturn(Optional.empty());
        when(assessmentService.findAssessmentByKey(thisExam.getAssessmentId())).thenReturn(Optional.empty());
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

        Assessment assessment = new Assessment.Builder()
            .withKey("(SBAC)SBAC ELA 3-ELA-3-Spring-2112a")
            .withAssessmentId(assessmentId)
            .withSelectionAlgorithm("jeff-j-sort")
            .withStartAbility(assessmentAbilityVal)
            .build();

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
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(historyRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
                .thenReturn(Optional.empty());
        when(assessmentService.findAssessmentByKey(thisExam.getAssessmentId())).thenReturn(Optional.of(assessment));
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
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(historyRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
                .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        // y=mx+b
        double abilityCalulated = maybeAbility.get() * slope + intercept;
        assertThat(maybeAbilityReturned.get()).isEqualTo((float)abilityCalulated);
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
                UUID.randomUUID(), "assessmentid-2", 1, Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
                UUID.randomUUID(), "assessmentid-2", 1, Instant.now(), 50D);

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldOpenPreviousExamIfPreviousExamIsInactiveStage() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(-1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withDateEnd(Instant.now().minus(1, ChronoUnit.DAYS))
            .build();

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_INACTIVE).build())
            .withDateChanged(Instant.now())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(repository.getLastAvailableExam(-1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().build();
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldReturnExamApprovalBecauseAllRulesAreSatisfied() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .withStatus(new ExamStatusCode.Builder()
                                .withStatus("approved")
                                .build())
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().plus(60, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now())
                        .withStatus("open")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfiguration(clientName, mockEnvironment, 0, 0, 0, 0)));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .withStatus(new ExamStatusCode.Builder()
                                .withStatus("pending")
                                .build())
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().plus(30, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now())
                        .withStatus("closed")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .withStatus(new ExamStatusCode.Builder()
                                .withStatus("approved")
                                .build())
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().minus(10, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now().minus(11, ChronoUnit.MINUTES))
                        .withStatus("closed")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .withStatus(new ExamStatusCode.Builder()
                                .withStatus("approved")
                                .build())
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().minus(30, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now().minus(55, ChronoUnit.MINUTES))
                        .withStatus("closed")
                        .withProctorId(null)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(UUID.randomUUID())
                        .withAssessmentId(mockAssessmentId)
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().plus(60, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now())
                        .withStatus("open")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(UUID.randomUUID())
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().plus(60, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now())
                        .withStatus("open")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().minus(30, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now().minus(45, ChronoUnit.MINUTES))
                        .withStatus("closed")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().plus(30, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now().minus(45, ChronoUnit.MINUTES))
                        .withStatus("open")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.empty());
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().plus(30, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now().minus(45, ChronoUnit.MINUTES))
                        .withStatus("open")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.empty());
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().plus(30, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now().minus(45, ChronoUnit.MINUTES))
                        .withStatus("open")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.empty());
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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

        when(repository.getExamById(examId))
                .thenReturn(Optional.of(new Exam.Builder()
                        .withId(examId)
                        .withSessionId(sessionId)
                        .withBrowserId(browserKey)
                        .withAssessmentId(mockAssessmentId)
                        .build()));
        when(sessionService.findSessionById(sessionId))
                .thenReturn(Optional.of(new Session.Builder()
                        .withId(sessionId)
                        .withDateBegin(Instant.now().minus(60, ChronoUnit.MINUTES))
                        .withDateEnd(Instant.now().plus(30, ChronoUnit.MINUTES))
                        .withDateVisited(Instant.now().minus(45, ChronoUnit.MINUTES))
                        .withStatus("open")
                        .withProctorId(42L)
                        .build()));
        when(sessionService.findExternalSessionConfigurationByClientName(clientName))
                .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(timeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
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
                .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
                .withDateChanged(Instant.now())
                .withDateScored(Instant.now())
                .build();
    }
}
