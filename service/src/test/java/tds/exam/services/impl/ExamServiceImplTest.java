package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExam;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
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

    @Before
    public void setUp() {
        repository = mock(ExamQueryRepository.class);
        sessionService = mock(SessionService.class);
        studentService = mock(StudentService.class);
        examService = new ExamServiceImpl(repository, sessionService, studentService);
    }

    @After
    public void tearDown() {}

    @Test
    public void shouldReturnAnExam() {
        UUID examId = UUID.randomUUID();
        when(repository.getExamById(examId)).thenReturn(Optional.of(new Exam.Builder().withId(examId).build()));

        assertThat(examService.getExam(examId).get().getId()).isEqualTo(examId);
    }

    @Test
    public void shouldReturnErrorWhenSessionCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExam openExam = new OpenExam();
        openExam.setSessionId(sessionId);
        openExam.setStudentId(1);

        when(sessionService.getSession(sessionId)).thenReturn(Optional.empty());
        when(studentService.getStudentById(1)).thenReturn(Optional.of(new Student()));

        Response<Exam> examResponse = examService.openExam(openExam);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.SESSION_NOT_FOUND);
    }

    @Test
    public void shouldReturnErrorWhenStudentCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExam openExam = new OpenExam();
        openExam.setStudentId(1);
        openExam.setSessionId(sessionId);

        when(sessionService.getSession(sessionId)).thenReturn(Optional.of(new Session()));
        when(studentService.getStudentById(1)).thenReturn(Optional.empty());

        Response<Exam> examResponse = examService.openExam(openExam);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.STUDENT_NOT_FOUND);
    }

    @Test
    public void shouldReturnErrorWhenPreviousSessionTypeDoesNotEqualCurrentSessionType() {
        UUID sessionId = UUID.randomUUID();
        OpenExam openExam = new OpenExam();
        openExam.setStudentId(1);
        openExam.setSessionId(sessionId);
        openExam.setAssessmentId("assessmentId");
        openExam.setClientName("SBAC-PT");

        Session currentSession = new Session();
        currentSession.setType(2);

        Session previousSession = new Session();
        previousSession.setId(UUID.randomUUID());
        previousSession.setType(33);

        Student student = new Student();
        student.setId(1);

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_INACTIVE).build())
            .build();

        when(sessionService.getSession(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.getSession(previousSession.getId())).thenReturn(Optional.of(previousSession));

        Response<Exam> examResponse = examService.openExam(openExam);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.SESSION_TYPE_MISMATCH);
    }

//    @Test
//    public void itShouldOpenAnExam() {
//        OpenExam openExam = new OpenExam();
//        openExam.setSessionId(UUID.randomUUID());
//        openExam.setStudentId(1);
//
//        when(sessionService.getSession(openExam.getSessionId())).thenReturn(Optional.of(new Session()));
//        when(studentService.getStudentById(openExam.getStudentId())).thenReturn(Optional.of(new Student()));
//
//        Exam exam = examService.openExam(openExam);
//
//        assertThat(exam).isNotNull();
//    }
}
