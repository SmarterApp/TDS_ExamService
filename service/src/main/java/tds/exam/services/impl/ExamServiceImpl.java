package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.SetOfAdminSubject;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.data.legacy.LegacyComparer;
import tds.config.TimeLimitConfiguration;
import tds.config.ClientTestProperty;
import tds.exam.Exam;
import tds.exam.OpenExamRequest;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalRequest;
import tds.exam.ExamStatusCode;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.Student;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
class ExamServiceImpl implements ExamService {
    private static final Logger LOG = LoggerFactory.getLogger(ExamServiceImpl.class);

    private final ExamQueryRepository examQueryRepository;
    private final HistoryQueryRepository historyQueryRepository;
    private final SessionService sessionService;
    private final StudentService studentService;
    private final AssessmentService assessmentService;
    private final TimeLimitConfigurationService timeLimitConfigurationService;

    @Autowired
    public ExamServiceImpl(ExamQueryRepository examQueryRepository,
                           HistoryQueryRepository historyQueryRepository,
                           SessionService sessionService,
                           StudentService studentService,
                           AssessmentService assessmentService,
                           TimeLimitConfigurationService timeLimitConfigurationService) {
        this.examQueryRepository = examQueryRepository;
        this.historyQueryRepository = historyQueryRepository;
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
        Exam exam = examQueryRepository.getExamById(examApprovalRequest.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("Exam could not be found for id " + examApprovalRequest.getExamId()));

        Optional<ValidationError> maybeValidationError = verifyExamApprovalRules(examApprovalRequest, exam);

        return maybeValidationError.isPresent()
                ? new Response<ExamApproval>(maybeValidationError.get())
                : new Response<>(new ExamApproval(examApprovalRequest.getExamId(), exam.getStatus(), exam.getStatusChangeReason()));
    }

    /**
     * @inheritDoc
     */
    @Override
    public Optional<Double> getInitialAbility(Exam exam, ClientTestProperty property) {
        Optional<Double> ability = Optional.empty();
        Double slope = property.getAbilitySlope();
        Double intercept = property.getAbilityIntercept();
        List<Ability> testAbilities = examQueryRepository.findAbilities(exam.getId(), exam.getClientName(),
                property.getSubjectName(), exam.getStudentId());

        // Attempt to retrieve the most recent ability for the current subject and assessment
        Optional<Ability> initialAbility = getMostRecentTestAbilityForSameAssessment(testAbilities, exam.getAssessmentId());
        if (initialAbility.isPresent()) {
            ability = Optional.of(initialAbility.get().getScore());
        } else if (property.getInitialAbilityBySubject()) {
            // if no ability for a similar assessment was retrieved above, attempt to get the initial ability for another
            // assessment of the same subject
            initialAbility = getMostRecentTestAbilityForDifferentAssessment(testAbilities, exam.getAssessmentId());
            if (initialAbility.isPresent()) {
                ability = Optional.of(initialAbility.get().getScore());
            } else {
                // if no value was returned from the previous call, get the initial ability from the previous year
                Optional<Double> initialAbilityFromHistory = historyQueryRepository.findAbilityFromHistoryForSubjectAndStudent(
                        exam.getClientName(), exam.getSubject(), exam.getStudentId());

                if (initialAbilityFromHistory.isPresent() && slope != null && intercept != null) {
                    ability = Optional.of(initialAbilityFromHistory.get() * slope + intercept);
                } else if (initialAbilityFromHistory.isPresent()) {
                    // If no slope/intercept is provided, store base value
                    ability = initialAbilityFromHistory;
                }
            }
        }

        // If the ability was not retrieved from any of the exam tables, query the assessment service
        if (!ability.isPresent()) {
            Optional<SetOfAdminSubject> subjectOptional = assessmentService.findSetOfAdminSubjectByKey(exam.getAssessmentId());
            if (subjectOptional.isPresent()) {
                ability = Optional.of((double) subjectOptional.get().getStartAbility());
            } else {
                LOG.warn("Could not set the ability for exam ID " + exam.getId());
            }
        }

        return ability;
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

    /**
     * Gets the most recent {@link Ability} based on the dateScored value for the same assessment.
     *
     * @param abilityList the list of {@link Ability}s to iterate through
     * @param assessmentId  The test key
     * @return
     */
    private Optional<Ability> getMostRecentTestAbilityForSameAssessment(List<Ability> abilityList, String assessmentId) {
        for (Ability ability : abilityList) {
            if (assessmentId.equals(ability.getAssessmentId())) {
                /* NOTE: The query that retrieves the list of abilities is sorted by the "date_scored" of the exam in
                   descending order. Therefore we can assume the first match is the most recent */
                return Optional.of(ability);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the most recent {@link Ability} based on the dateScored value for a different assessment.
     *
     * @param abilityList the list of {@link Ability}s to iterate through
     * @param assessmentId  The test key
     * @return
     */
    private Optional<Ability> getMostRecentTestAbilityForDifferentAssessment(List<Ability> abilityList, String assessmentId) {
        for (Ability ability : abilityList) {

            if (!assessmentId.equals(ability.getAssessmentId())) {
                /* NOTE: The query that retrieves the list of abilities is sorted by the "date_scored" of the exam in
                   descending order. Therefore we can assume the first match is the most recent */
                return Optional.of(ability);
            }
        }

        return Optional.empty();
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

    /**
     * Verify all the rules for approving a request to start an {@link Exam} are satisfied.
     * <p>
     *     The rules are:
     *     <ul>
     *         <li>The browser key of the approval request must match the browser key of the {@link Exam}.</li>
     *         <li>The session id of the approval request must match the session id of the {@link Exam}.</li>
     *         <li>The {@link Session} must be open (unless the environment is set to "simulation" or "development")</li>
     *         <li>The TA Check-In time window cannot be passed</li>
     *     </ul>
     *     <strong>NOTE:</strong>  If the {@link Session} has no Proctor (because the {@link Session} is a guest session
     *     or is otherwise proctor-less), approval is granted as long as the {@link Session} is open.
     * </p>
     *
     * @param examApprovalRequest The {@link ExamApprovalRequest} being evaluated
*      @param exam The {@link Exam} for which approval is being requested
     * @return An empty optional if the approval rules are satisfied; otherwise an optional containing a
     * {@link ValidationError} describing the rule that was not satisfied
     */
    private Optional<ValidationError> verifyExamApprovalRules(ExamApprovalRequest examApprovalRequest, Exam exam) {
        final String SIMULATION_ENVIRONMENT = "simulation";
        final String DEVELOPMENT_ENVIRONMENT = "development";

        // RULE:  The browser key for the approval request must match the browser key of the exam.
        if (!exam.getBrowserId().equals(examApprovalRequest.getBrowserId())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH, "Access violation: System access denied"));
        }

        // RULE:  Session id for the approval request must match the session id of the exam.
        if (!exam.getSessionId().equals(examApprovalRequest.getSessionId())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH, "The session keys do not match; please consult your test administrator"));
        }

        Session session = sessionService.findSessionById(examApprovalRequest.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Could not find session for id " + examApprovalRequest.getSessionId()));

        ExternalSessionConfiguration externalSessionConfig =
                sessionService.findExternalSessionConfigurationByClientName(examApprovalRequest.getClientName())
                        .orElseThrow(() -> new IllegalStateException(String.format("External Session Configuration could not be found for client name %s", examApprovalRequest.getClientName())));

        // RULE:  Unless the environment is set to "simulation" or "development", the exam's session must be open.
        boolean checkSession = (!externalSessionConfig.getEnvironment().toLowerCase().equals(SIMULATION_ENVIRONMENT)
                        && !externalSessionConfig.getEnvironment().toLowerCase().equals(DEVELOPMENT_ENVIRONMENT));
        if (checkSession && !session.isOpen()) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED, "The session is not available for testing, please check with your test administrator."));
        }

        // RULE:  If the session has no proctor, there is nothing to approve.  This is either a guest session or an
        // otherwise proctor-less session.
        if (session.getProctorId() == null) {
            return Optional.empty();
        }

        // RULE:  Student should not be able to start an exam if the TA check-in window has expired.
        TimeLimitConfiguration timeLimitConfig =
                timeLimitConfigurationService.findTimeLimitConfiguration(examApprovalRequest.getClientName(), exam.getAssessmentId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find time limit configuration for client name %s and assessment id %s", examApprovalRequest.getClientName(), exam.getAssessmentId())));

        if (Instant.now().isAfter(session.getDateVisited().plus(timeLimitConfig.getTaCheckinTimeMinutes(), ChronoUnit.MINUTES))) {
            // TODO: Create session audit record
            sessionService.pause(session.getId(), "closed");
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_TA_CHECKIN_TIMEOUT, "The session is not available for testing, please check with your test administrator."));
        }

        return Optional.empty();
    }
}