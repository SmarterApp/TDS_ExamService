package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.PKIXRevocationChecker;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.data.legacy.LegacyComparer;
import tds.config.TimeLimitConfiguration;
import tds.exam.*;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.*;
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
    private final AssessmentService assessmentService;
    private final TimeLimitConfigurationService timeLimitConfigurationService;

    @Autowired
    public ExamServiceImpl(ExamQueryRepository examQueryRepository,
                           SessionService sessionService,
                           StudentService studentService,
                           AssessmentService assessmentService,
                           TimeLimitConfigurationService timeLimitConfigurationService) {
        this.examQueryRepository = examQueryRepository;
        this.sessionService = sessionService;
        this.studentService = studentService;
        this.assessmentService = assessmentService;
        this.timeLimitConfigurationService = timeLimitConfigurationService;
    }

    @Override
    public Optional<Exam> getExam(UUID id) {
        return examQueryRepository.getExamById(id);
    }

    @Override
    public Response<Exam> openExam(OpenExamRequest openExamRequest) {
        Optional<Session> maybeSession = sessionService.findSessionById(openExamRequest.getSessionId());
        if (!maybeSession.isPresent()) {
            throw new IllegalArgumentException(String.format("Could not find session for id %s", openExamRequest.getSessionId()));
        }

        if (!openExamRequest.isGuestStudent()) {
            Optional<Student> maybeStudent = studentService.getStudentById(openExamRequest.getStudentId());
            if (!maybeStudent.isPresent()) {
                throw new IllegalArgumentException(String.format("Could not find student for id %s", openExamRequest.getStudentId()));
            }
        }

        Session currentSession = maybeSession.get();

        //Previous exam is retrieved in lines 5492 - 5530 and 5605 - 5645 in StudentDLL
        Optional<Exam> maybePreviousExam = examQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), openExamRequest.getAssessmentId(), openExamRequest.getClientName());

        boolean canOpenPreviousExam = false;
        if (maybePreviousExam.isPresent()) {
            Optional<ValidationError> canOpenPreviousExamError = canOpenPreviousExam(maybePreviousExam.get(), currentSession);

            if (canOpenPreviousExamError.isPresent()) {
                return new Response<Exam>(canOpenPreviousExamError.get());
            }

            canOpenPreviousExam = true;
        }

        Exam exam;
        if (canOpenPreviousExam) {
            //Open previous exam
            LOG.debug("Can open previous exam");
            exam = new Exam.Builder().withId(maybePreviousExam.get().getId()).build();
        } else {
            //Line 5602 in StudentDLL
            Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName());

            if (!maybeExternalSessionConfiguration.isPresent()) {
                throw new IllegalStateException("External Session Configuration could not be found for client name " + openExamRequest.getClientName());
            }

            ExternalSessionConfiguration externalSessionConfiguration = maybeExternalSessionConfiguration.get();
            Exam previousExam = maybePreviousExam.isPresent() ? maybePreviousExam.get() : null;
            Optional<ValidationError> maybeOpenNewExam = canCreateNewExam(openExamRequest, previousExam, externalSessionConfiguration);
            if (maybeOpenNewExam.isPresent()) {
                return new Response<Exam>(maybeOpenNewExam.get());
            }

            exam = new Exam.Builder().withId(UUID.randomUUID()).build();
        }

        return new Response<>(exam);
    }

    @Override
    public Response<ExamApproval> getApproval(ExamApprovalRequest examApprovalRequest) {
        final ExamApproval examApproval = new ExamApproval();

        Optional<ValidationError> maybeValidationErrors = verifyExamApprovalRules(examApprovalRequest);
        if (maybeValidationErrors.isPresent()) {
            return new Response<ExamApproval>(maybeValidationErrors.get());
        }

        examApproval.setExamApprovalStatus(ExamApprovalStatus.APPROVED);
        return new Response<>(examApproval);
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

    private Optional<ValidationError> canOpenPreviousExam(Exam previousExam, Session currentSession) {
        //Port of Student.DLL lines 5526-5530
        if (ExamStatusCode.STAGE_CLOSED.equals(previousExam.getStatus().getStage())) {
            return Optional.empty();
        }

        //Port of Student.DLL lines 5531-5551
        //If either session type is null or if they don't match an error is returned
        Optional<Session> maybePreviousSession = sessionService.findSessionById(previousExam.getSessionId());
        if (!maybePreviousSession.isPresent()) {
            return Optional.of(new ValidationError(ValidationErrorCode.SESSION_TYPE_MISMATCH, "current session type and previous session type don't match"));
        }

        Session previousSession = maybePreviousSession.get();
        if (previousSession.getType() != currentSession.getType()) {
            return Optional.of(new ValidationError(ValidationErrorCode.SESSION_TYPE_MISMATCH, "current session type and previous session type don't match"));
        }

        //Port of Student.DLL lines 5555-5560
        if (ExamStatusCode.STAGE_INACTIVE.equals(previousExam.getStatus().getStage())) {
            return Optional.empty();
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
            return Optional.empty();
        }

        //Port of Student.DLL line 5593
        return Optional.of(new ValidationError(ValidationErrorCode.CURRENT_EXAM_OPEN, "Current exam is active"));
    }

    private Optional<ValidationError> canCreateNewExam(OpenExamRequest openExamRequest, Exam previousExam, ExternalSessionConfiguration externalSessionConfiguration) {

        //Lines 5612 - 5618 in StudentDLL
        if (previousExam == null) {
            if (openExamRequest.getMaxAttempts() < 0 && LegacyComparer.notEqual("SIMULATION", externalSessionConfiguration.getEnvironment())) {
                return Optional.of(new ValidationError(ValidationErrorCode.SIMULATION_ENVIRONMENT_REQUIRED, "Environment must be simulation when max attempts less than zero"));
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
                if (LegacyComparer.lessThan(previousExam.getAttempts(), openExamRequest.getMaxAttempts()) &&
                    LegacyComparer.greaterThan(duration.get(DAYS), openExamRequest.getNumberOfDaysToDelay())) {
                    return Optional.empty();
                } else if (LegacyComparer.greaterOrEqual(previousExam.getAttempts(), openExamRequest.getMaxAttempts())) {
                    return Optional.of(new ValidationError(ValidationErrorCode.MAX_OPPORTUNITY_EXCEEDED, "Max number of attempts for exam exceeded"));
                } else {
                    return Optional.of(new ValidationError(ValidationErrorCode.NOT_ENOUGH_DAYS_PASSED, String.format("Next exam cannot be started until %s days pass since last exam", openExamRequest.getNumberOfDaysToDelay())));
                }
            }
        }

        return Optional.empty();
    }

    private Optional<ValidationError> verifyExamApprovalRules(ExamApprovalRequest examApprovalRequest) {
        final String SIMULATION_ENVIRONMENT = "simulation";
        final String DEVELOPMENT_ENVIRONMENT = "development";

        Exam exam = examQueryRepository.getExamById(examApprovalRequest.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("Exam could not be found for id " + examApprovalRequest.getExamId()));

        // RULE:  The browser key for the approval request must match the browser key of the exam.
        if (!exam.getBrowserKey().equals(examApprovalRequest.getBrowserKey())) {
            return Optional.of(new ValidationError(ValidationErrorCode.BROWSER_KEY_MISMATCH, "Access violation: System access denied"));
        }

        // RULE:  Session id for the approval request must match the session id of the exam.
        if (!exam.getSessionId().equals(examApprovalRequest.getSessionId())) {
            return Optional.of(new ValidationError(ValidationErrorCode.SESSION_ID_MISMATCH, "The session keys do not match; please consult your test administrator"));
        }

        Session session = sessionService.findSessionById(examApprovalRequest.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Could not find session for id " + examApprovalRequest.getSessionId()));

        // RULE:  Unless the environment is set to "simulation" or "development", the exam's session must be open.
        ExternalSessionConfiguration externalSessionConfig =
                sessionService.findExternalSessionConfigurationByClientName(examApprovalRequest.getClientName())
                        .orElseThrow(() -> new IllegalStateException("External Session Configuration could not be found for client name " + examApprovalRequest.getClientName()));

        boolean checkSession = (!externalSessionConfig.getEnvironment().toLowerCase().equals(SIMULATION_ENVIRONMENT)
                        && !externalSessionConfig.getEnvironment().toLowerCase().equals(DEVELOPMENT_ENVIRONMENT));
        if (checkSession) {
            if (!session.isOpen()) {
                return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_DENIED, "The session is not available for testing, please check with your test administrator."));
            }
        }

        // RULE:  If the session has no proctor, there is nothing to approve.  This is either a guest session or an
        // otherwise proctorless session.
        if (session.getProctorId() == null) {
            return Optional.empty();
        }

        // RULE:  Student should not be able to start an exam if the TA check-in window has expired.
        TimeLimitConfiguration timeLimitConfig =
                timeLimitConfigurationService.findTimeLimitConfiguration(examApprovalRequest.getClientName(), exam.getAssessmentId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find time limit configuration for client name %s and assessment id %s", examApprovalRequest.getClientName(), exam.getAssessmentId())));

        if (Instant.now().isAfter(session.getDateVisited().plus(timeLimitConfig.getTaCheckinTimeMinutes(), ChronoUnit.MINUTES))) {
            // TODO: Create session audit record
            // TODO: determine correct status to set
            sessionService.pause(session.getId(), "closed");
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_TIMEOUT, "The session is not available for testing, please check with your test administrator."));
        }

        return Optional.empty();
    }
}