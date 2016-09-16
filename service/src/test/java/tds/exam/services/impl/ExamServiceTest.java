package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.exam.Exam;
import tds.exam.OpenExam;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.session.Session;
import tds.student.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamServiceTest {
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
    public void itShouldReturnAExam() {
        UUID examId = UUID.randomUUID();
        when(repository.getExamById(examId)).thenReturn(Optional.of(new Exam.Builder().withId(examId).build()));

        assertThat(examService.getExam(examId).get().getId()).isEqualTo(examId);
    }

    @Test
    public void itShouldOpenAnExam() {
        OpenExam openExam = new OpenExam();
        openExam.setSessionId(UUID.randomUUID());
        openExam.setStudentId(1);

        when(sessionService.getSession(openExam.getSessionId())).thenReturn(Optional.of(new Session()));
        when(studentService.getStudentById(openExam.getStudentId())).thenReturn(Optional.of(new Student()));

        Response<Exam> examResponse = examService.openExam(openExam);

        assertThat(examResponse.getData().isPresent()).isTrue();
    }
}
