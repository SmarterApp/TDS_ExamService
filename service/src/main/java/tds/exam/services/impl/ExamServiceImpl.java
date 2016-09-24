package tds.exam.services.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.data.legacy.LegacyComparer;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExamRequest;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.Student;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
class ExamServiceImpl implements ExamService {
    private static final Logger LOG = LoggerFactory.getLogger(ExamServiceImpl.class);

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
    public Response<Exam> openExam(OpenExamRequest openExamRequest) {
        Optional<Session> sessionOptional = sessionService.getSession(openExamRequest.getSessionId());
        if (!sessionOptional.isPresent()) {
            throw new IllegalArgumentException(String.format("Could not find session for %s", openExamRequest.getSessionId()));
        }

        if(!openExamRequest.isGuestStudent()) {
            Optional<Student> studentOptional = studentService.getStudentById(openExamRequest.getStudentId());
            if (!studentOptional.isPresent()) {
                throw new IllegalArgumentException(String.format("Could not find student for %s", openExamRequest.getStudentId()));
            }
        }

        Session currentSession = sessionOptional.get();

        //Previous exam is retrieved in lines 5492 - 5530 and 5605 - 5645 in StudentDLL
        Optional<Exam> previousExamOptional = examQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), openExamRequest.getAssessmentId(), openExamRequest.getClientName());

        boolean canOpenPreviousExam = false;
        if (previousExamOptional.isPresent()) {
            Pair<Boolean, Optional<ValidationError>> canOpenPreviousExamPair = canOpenPreviousExam(previousExamOptional.get(), currentSession);

            if (canOpenPreviousExamPair.getRight().isPresent()) {
                return new Response<Exam>(canOpenPreviousExamPair.getRight().get());
            }

            canOpenPreviousExam = canOpenPreviousExamPair.getLeft();
        }

        Exam exam;
        if (canOpenPreviousExam) {
            //Open previous exam
            LOG.debug("Can open previous exam");
            exam = new Exam.Builder().withId(previousExamOptional.get().getId()).build();
        } else {
            //Line 5602 in StudentDLL
            Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.getExternalSessionConfigurationByClientName(openExamRequest.getClientName());

            if (!maybeExternalSessionConfiguration.isPresent()) {
                throw new IllegalStateException("Extern could not be found for client name " + openExamRequest.getClientName());
            }

            ExternalSessionConfiguration externalSessionConfiguration = maybeExternalSessionConfiguration.get();
            Exam previousExam = previousExamOptional.isPresent() ? previousExamOptional.get() : null;
            Optional<ValidationError> openNewExamOptional = canCreateNewExam(openExamRequest, previousExam, externalSessionConfiguration);
            if (openNewExamOptional.isPresent()) {
                return new Response<Exam>(openNewExamOptional.get());
            }

            exam = new Exam.Builder().withId(UUID.randomUUID()).build();
        }

        return new Response<>(exam);
    }

    private Response<Exam> createExam(OpenExamRequest openExamRequest, Student student, Session session, ExternalSessionConfiguration externalSessionConfiguration) {
        //From OpenTestServiceImpl lines 160 -163
        String examStatus;
        if (openExamRequest.getProctorId() == null) {
            examStatus = "approved";
        } else {
            examStatus = "pending";
        }

        Instant startTime = Instant.now();



        return null;
    }

    private Pair<Boolean, Optional<ValidationError>> canOpenPreviousExam(Exam previousExam, Session currentSession) {
        //Port of Student.DLL lines 5526-5530
        if (ExamStatusCode.STAGE_CLOSED.equals(previousExam.getStatus().getStage())) {
            return Pair.of(true, Optional.empty());
        }

        //Port of Student.DLL lines 5531-5551
        //If either session type is null or if they don't match an error is returned
        Optional<Session> previousSessionOptional = sessionService.getSession(previousExam.getSessionId());
        if (!previousSessionOptional.isPresent()) {
            return Pair.of(false, Optional.of(new ValidationError(ValidationErrorCode.SESSION_TYPE_MISMATCH, "current session type and previous session type don't match")));
        }

        Session previousSession = previousSessionOptional.get();
        if (previousSession.getType() != currentSession.getType()) {
            return Pair.of(false, Optional.of(new ValidationError(ValidationErrorCode.SESSION_TYPE_MISMATCH, "current session type and previous session type don't match")));
        }

        //Port of Student.DLL lines 5555-5560
        if (ExamStatusCode.STAGE_INACTIVE.equals(previousExam.getStatus().getStage())) {
            return Pair.of(true, Optional.empty());
        }

        /*
        The below code is a straight port of the legacy StudentDLL._CanOpenExistingOpportunity_SP.  It was a little hard to follow the application
        reasons for this logic. StudentDLL lines 5569 - 5589
         */
        boolean daysSinceLastChange = false;
        if (previousExam.getDateChanged() != null) {
            daysSinceLastChange = DAYS.between(previousExam.getDateChanged(), Instant.now()) >= 1;
        }

        if (daysSinceLastChange ||
            LegacyComparer.isEqual(previousSession.getId(), currentSession.getId()) ||
            LegacyComparer.isEqual("closed", previousSession.getStatus()) ||
            LegacyComparer.greaterThan(Instant.now(), previousSession.getDateEnd())) {
            return Pair.of(true, Optional.empty());
        }

        //Port of Student.DLL line 5593
        return Pair.of(false, Optional.of(new ValidationError(ValidationErrorCode.CURRENT_EXAM_OPEN, "Current exam is active")));
    }

    private Optional<ValidationError> canCreateNewExam(OpenExamRequest openExamRequest, Exam previousExam, ExternalSessionConfiguration externalSessionConfiguration) {

        //Lines 5612 - 5618 in StudentDLL
        if (previousExam == null) {
            if (openExamRequest.getMaxOpportunities() < 0 && LegacyComparer.notEqual("SIMULATION", externalSessionConfiguration.getEnvironment())) {
                return Optional.of(new ValidationError(ValidationErrorCode.SIMULATION_ENVIRONMENT_REQUIRED, "Environment must be simulation when max opportunities less than zero"));
            }

            return Optional.empty();
        }

        //Lines 5645 - 5673 in StudentDLL
        if (ExamStatusCode.STAGE_CLOSED.equals(previousExam.getStatus().getStage())) {
            if (LegacyComparer.isEqual("SIMULATION", externalSessionConfiguration.getEnvironment())) {
                return Optional.empty();
            }

            if (previousExam.getDateCompleted() != null) {
                Duration duration = Duration.between(previousExam.getDateChanged(), Instant.now());
                if (LegacyComparer.lessThan(previousExam.getTimeTaken(), openExamRequest.getMaxOpportunities()) &&
                    LegacyComparer.greaterThan(duration.get(DAYS), openExamRequest.getNumberOfDaysToDelay())) {
                    return Optional.empty();
                } else if (LegacyComparer.greaterOrEqual(previousExam.getTimeTaken(), openExamRequest.getMaxOpportunities())) {
                    return Optional.of(new ValidationError(ValidationErrorCode.MAX_OPPORTUNITY_EXCEEDED, "Max number of opportunities for exam exceeded"));
                } else {
                    return Optional.of(new ValidationError(ValidationErrorCode.NOT_ENOUGH_DAYS_PASSED, String.format("Next exam cannot be started until %s days pass since last exam", openExamRequest.getNumberOfDaysToDelay())));
                }
            }
        }

        return Optional.empty();
    }
}
