package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tds.common.Error;
import tds.common.Response;
import tds.exam.Exam;
import tds.exam.OpenExam;
import tds.exam.error.ErrorCode;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.session.Session;
import tds.student.Student;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
class ExamServiceImpl implements ExamService {
    private final ExamQueryRepository examQueryRepository;
    private final SessionService sessionService;
    private final StudentService studentService;

    @Autowired
    public ExamServiceImpl(ExamQueryRepository examQueryRepository, SessionService sessionService, StudentService studentService) {
        this.examQueryRepository = examQueryRepository;
        this.sessionService = sessionService;
        this.studentService = studentService;
    }

    @Override
    public Optional<Exam> getExam(UUID id) {
        return examQueryRepository.getExamById(id);
    }

    @Override
    public Response<Exam> openExam(OpenExam openExam) {
        //TODO - Should be async
        Optional<Session> sessionOptional = sessionService.getSession(openExam.getSessionId());
        Optional<Student> studentOptional = studentService.getStudentById(openExam.getStudentId());

        if (!sessionOptional.isPresent() || !studentOptional.isPresent()) {
            Set<Error> errors = new HashSet<>();
            if(!sessionOptional.isPresent()) {
                errors.add(new Error(ErrorCode.SESSION_NOT_FOUND, String.format("Session could not be found for %s", openExam.getSessionId())));
            }
            if(!studentOptional.isPresent()) {
                errors.add(new Error(ErrorCode.STUDENT_NOT_FOUND, String.format("Student could not be found for %d", openExam.getStudentId())));
            }
            return new Response<>(errors);
        }

        Session currentSession = sessionOptional.get();
        Student currentStudent = studentOptional.get();

        return new Response<>(new Exam.Builder().withId(UUID.randomUUID()).build());
    }

    private boolean canOpenExam() {
        return false;
    }
}
