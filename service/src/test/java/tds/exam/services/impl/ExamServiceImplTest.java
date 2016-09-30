package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExamRequest;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamServiceImplTest {
    private ExamQueryRepository repository;
    private ExamServiceImpl examService;
    private SessionService sessionService;
    private StudentService studentService;
    private AssessmentService assessmentService;

    @Before
    public void setUp() {
        repository = mock(ExamQueryRepository.class);
        sessionService = mock(SessionService.class);
        studentService = mock(StudentService.class);
        assessmentService = mock(AssessmentService.class);
        examService = new ExamServiceImpl(repository, sessionService, studentService, assessmentService);
    }

    @After
    public void tearDown() {}

    @Test
    public void shouldReturnAnExam() {
        UUID examId = UUID.randomUUID();
        when(repository.getExamById(examId)).thenReturn(Optional.of(new Exam.Builder().withId(examId).build()));

        assertThat(examService.getExam(examId).get().getId()).isEqualTo(examId);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldReturnErrorWhenSessionCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setStudentId(1);

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.empty());
        when(studentService.getStudentById(1)).thenReturn(Optional.of(new Student(1, "testId", "CA", "clientName")));

        examService.openExam(openExamRequest);
    }

    @Test (expected = IllegalArgumentException.class)
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
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration("SBAC-PT", "Development");
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(extSessionConfig));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.SIMULATION_ENVIRONMENT_REQUIRED);
    }

    @Test
    public void shouldNotAllowExamToOpenIfStillActive(){
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
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development");
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.CURRENT_EXAM_OPEN);
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfDayHasPassed(){
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
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development");
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfPreviousSessionIsClosed(){
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
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development");
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionIdSame(){
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
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development");
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionEndTimeIsBeforeNow(){
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
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development");
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
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
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development");
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }
}
