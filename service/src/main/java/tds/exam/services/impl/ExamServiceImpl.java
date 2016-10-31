package tds.exam.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.SetOfAdminSubject;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.data.legacy.LegacyComparer;
import tds.config.AssessmentWindow;
import tds.config.ClientSystemFlag;
import tds.config.ClientTestProperty;
import tds.config.TimeLimitConfiguration;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExamRequest;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamQueryRepository;
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

import static java.time.temporal.ChronoUnit.DAYS;
import static tds.config.ClientSystemFlag.ANONYMOUS_STUDENT_AUDIT_OBJECT;
import static tds.exam.error.ValidationErrorCode.ANONYMOUS_STUDENT_NOT_ALLOWED;
import static tds.exam.error.ValidationErrorCode.NO_OPEN_ASSESSMENT_WINDOW;
import static tds.student.RtsStudentPackageAttribute.ACCOMMODATIONS;
import static tds.student.RtsStudentPackageAttribute.ENTITY_NAME;
import static tds.student.RtsStudentPackageAttribute.EXTERNAL_ID;

@Service
class ExamServiceImpl implements ExamService {
    private static final Logger LOG = LoggerFactory.getLogger(ExamServiceImpl.class);

    private final ExamQueryRepository examQueryRepository;
    private final ExamCommandRepository examCommandRepository;
    private final HistoryQueryRepository historyQueryRepository;
    private final SessionService sessionService;
    private final StudentService studentService;
    private final AssessmentService assessmentService;
    private final TimeLimitConfigurationService timeLimitConfigurationService;
    private final ConfigService configService;

    @Autowired
    public ExamServiceImpl(ExamQueryRepository examQueryRepository,
                           HistoryQueryRepository historyQueryRepository,
                           SessionService sessionService,
                           StudentService studentService,
                           AssessmentService assessmentService,
                           TimeLimitConfigurationService timeLimitConfigurationService,
                           ConfigService configService,
                           ExamCommandRepository examCommandRepository) {
        this.examQueryRepository = examQueryRepository;
        this.historyQueryRepository = historyQueryRepository;
        this.sessionService = sessionService;
        this.studentService = studentService;
        this.assessmentService = assessmentService;
        this.timeLimitConfigurationService = timeLimitConfigurationService;
        this.configService = configService;
        this.examCommandRepository = examCommandRepository;
    }

    @Override
    public Optional<Exam> getExam(UUID id) {
        return examQueryRepository.getExamById(id);
    }

    @Override
    public Response<Exam> openExam(OpenExamRequest openExamRequest) {
        //Line 5602 in StudentDLL
        Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName());

        if (!maybeExternalSessionConfiguration.isPresent()) {
            throw new IllegalStateException(String.format("External Session Configuration could not be found for client name %s", openExamRequest.getClientName()));
        }

        ExternalSessionConfiguration externalSessionConfiguration = maybeExternalSessionConfiguration.get();

        Optional<Session> maybeSession = sessionService.findSessionById(openExamRequest.getSessionId());
        if (!maybeSession.isPresent()) {
            throw new IllegalArgumentException(String.format("Could not find session for id %s", openExamRequest.getSessionId()));
        }

        Student currentStudent = null;
        if (!openExamRequest.isGuestStudent()) {
            Optional<Student> maybeStudent = studentService.getStudentById(openExamRequest.getStudentId());
            if (!maybeStudent.isPresent()) {
                throw new IllegalArgumentException(String.format("Could not find student for id %s", openExamRequest.getStudentId()));
            } else {
                currentStudent = maybeStudent.get();
            }
        } else {
            //OpenTestServiceImpl lines 103 - 104
            if (!allowsGuestStudent(openExamRequest.getClientName(), externalSessionConfiguration)) {
                return new Response<Exam>(new ValidationError(ANONYMOUS_STUDENT_NOT_ALLOWED, String.format("Anonymous students not allowed for this client %s", openExamRequest.getClientName())));
            }
        }

        Session currentSession = maybeSession.get();

        Optional<SetOfAdminSubject> maybeSetOfAdminSubject = assessmentService.findSetOfAdminSubjectByKey(openExamRequest.getAssessmentKey());
        if (!maybeSetOfAdminSubject.isPresent()) {
            throw new IllegalArgumentException(String.format("Assessment information could not be found for assessment key %s", openExamRequest.getAssessmentKey()));
        }

        SetOfAdminSubject assessment = maybeSetOfAdminSubject.get();

        //Previous exam is retrieved in lines 5492 - 5530 and 5605 - 5645 in StudentDLL
        Optional<Exam> maybePreviousExam = examQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), openExamRequest.getClientName());

        boolean canOpenPreviousExam = false;
        if (maybePreviousExam.isPresent()) {
            Optional<ValidationError> canOpenPreviousExamError = canOpenPreviousExam(maybePreviousExam.get(), currentSession);

            if (canOpenPreviousExamError.isPresent()) {
                return new Response<Exam>(canOpenPreviousExamError.get());
            }

            canOpenPreviousExam = true;
        }

        if (canOpenPreviousExam) {
            //Open previous exam
            LOG.debug("Can open previous exam");
            return new Response<>(new Exam.Builder().withId(maybePreviousExam.get().getId()).build());
        }


        Exam previousExam = maybePreviousExam.isPresent() ? maybePreviousExam.get() : null;
        Optional<ValidationError> maybeOpenNewExamValidationError = canCreateNewExam(openExamRequest, previousExam, externalSessionConfiguration);
        if (maybeOpenNewExamValidationError.isPresent()) {
            return new Response<Exam>(maybeOpenNewExamValidationError.get());
        }

        return createExam(openExamRequest, currentSession, assessment, externalSessionConfiguration);
    }

    @Override
    public Response<ExamApproval> getApproval(ApprovalRequest approvalRequest) {
        Exam exam = examQueryRepository.getExamById(approvalRequest.getExamId())
            .orElseThrow(() -> new IllegalArgumentException(String.format("Exam could not be found for id %s", approvalRequest.getExamId())));

        Optional<ValidationError> maybeAccessViolation = verifyAccess(approvalRequest, exam);

        return maybeAccessViolation.isPresent()
            ? new Response<ExamApproval>(maybeAccessViolation.get())
            : new Response<>(new ExamApproval(approvalRequest.getExamId(), exam.getStatus(), exam.getStatusChangeReason()));
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

    @Override
    public Optional<ValidationError> verifyAccess(ApprovalRequest approvalRequest, Exam exam) {
        // RULE:  The browser key for the approval request must match the browser key of the exam.
        if (!exam.getBrowserId().equals(approvalRequest.getBrowserId())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH, "Access violation: System access denied"));
        }

        // RULE:  Session id for the approval request must match the session id of the exam.
        if (!exam.getSessionId().equals(approvalRequest.getSessionId())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH, "The session keys do not match; please consult your test administrator"));
        }

        ExternalSessionConfiguration externalSessionConfig =
            sessionService.findExternalSessionConfigurationByClientName(approvalRequest.getClientName())
                .orElseThrow(() -> new IllegalStateException(String.format("External Session Configuration could not be found for client name %s", approvalRequest.getClientName())));

        // RULE:  If the environment is set to "simulation" or "development", there is no need to check anything else.
        if (externalSessionConfig.isInSimulationEnvironment()
            || externalSessionConfig.isInDevelopmentEnvironment()) {
            return Optional.empty();
        }

        Session session = sessionService.findSessionById(approvalRequest.getSessionId())
            .orElseThrow(() -> new IllegalArgumentException("Could not find session for id " + approvalRequest.getSessionId()));

        // RULE:  the exam's session must be open.
        if (!session.isOpen()) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED, "The session is not available for testing, please check with your test administrator."));
        }

        // RULE:  If the session has no proctor, there is nothing to approve.  This is either a guest session or an
        // otherwise proctor-less session.
        if (session.isProctorless()) {
            return Optional.empty();
        }

        // RULE:  Student should not be able to start an exam if the TA check-in window has expired.
        TimeLimitConfiguration timeLimitConfig =
            timeLimitConfigurationService.findTimeLimitConfiguration(approvalRequest.getClientName(), exam.getAssessmentId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find time limit configuration for client name %s and assessment id %s", approvalRequest.getClientName(), exam.getAssessmentId())));

        if (Instant.now().isAfter(session.getDateVisited().plus(timeLimitConfig.getTaCheckinTimeMinutes(), ChronoUnit.MINUTES))) {
            // Legacy code creates an audit record here.  Immutability should provide an audit trail; a new session record
            // will be inserted to represent the change in status.
            sessionService.pause(session.getId(), "closed");
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_TA_CHECKIN_TIMEOUT, "The session is not available for testing, please check with your test administrator."));
        }

        return Optional.empty();
    }

    private Response<Exam> createExam(OpenExamRequest openExamRequest, Session session, SetOfAdminSubject assessment, ExternalSessionConfiguration externalSessionConfiguration) {
        Exam.Builder examBuilder = new Exam.Builder();

        //From OpenTestServiceImpl lines 160 -163
        if (openExamRequest.getProctorId() == null) {
            examBuilder.withStatus(new ExamStatusCode.Builder().withStatus(ExamStatusCode.STATUS_APPROVED).build());
        } else {
            examBuilder.withStatus(new ExamStatusCode.Builder().withStatus(ExamStatusCode.STATUS_PENDING).build());
        }

        String guestAccommodations = openExamRequest.getGuestAccommodations();
        if (openExamRequest.isGuestStudent()) {
            examBuilder.withStudentName("GUEST");
            examBuilder.withStudentKey("GUEST");
        } else {
            List<RtsStudentPackageAttribute> attributes = studentService.findStudentPackageAttributes(openExamRequest.getStudentId(), openExamRequest.getClientName(), EXTERNAL_ID, ENTITY_NAME, ACCOMMODATIONS);

            for (RtsStudentPackageAttribute attribute : attributes) {
                if (EXTERNAL_ID.equals(attribute.getName())) {
                    examBuilder.withStudentKey(attribute.getValue());
                } else if (ENTITY_NAME.equals(attribute.getName())) {
                    examBuilder.withStudentName(attribute.getValue());
                } else if (StringUtils.isEmpty(guestAccommodations) && ACCOMMODATIONS.equals(attribute.getName())) {
                    guestAccommodations = attribute.getValue();
                }
            }
        }

        //OpenTestServiceImpl lines 317 - 341
        AssessmentWindow[] assessmentWindows = configService.findAssessmentWindows(
            openExamRequest.getClientName(),
            assessment.getAssessmentId(),
            session.getType(),
            openExamRequest.getStudentId(),
            externalSessionConfiguration
        );

        //OpenTestServiceImpl lines 344 - 365
        Optional<AssessmentWindow> maybeWindow = Arrays.stream(assessmentWindows)
            .filter(assessmentWindow -> assessmentWindow.getAssessmentKey().equals(openExamRequest.getAssessmentKey()))
            .min((o1, o2) -> o1.getStartTime().compareTo(o2.getStartTime()));

        if (!maybeWindow.isPresent()) {
            return new Response<Exam>(new ValidationError(NO_OPEN_ASSESSMENT_WINDOW, "Could not find an open assessment window"));
        }

        Exam exam = examBuilder
            .withId(UUID.randomUUID())
            .withClientName(externalSessionConfiguration.getClientName())
            .withStudentId(openExamRequest.getStudentId())
            .withSessionId(session.getId())
            .withBrowserId(openExamRequest.getBrowserId())
            .withAssessmentId(assessment.getAssessmentId())
            .withAssessmentKey(assessment.getKey())
            .withAttempts(0)
            .withAssessmentAlgorithm(assessment.getSelectionAlgorithm())
            .withSegmented(assessment.isSegmented())
            .withDateJoined(Instant.now())
            .withAssessmentWindowId(maybeWindow.get().getWindowId())
            .withEnvironment(externalSessionConfiguration.getEnvironment())
            .withSubject(assessment.getSubjectName())
            .build();

        examCommandRepository.save(exam);

        return new Response<>(exam);
    }

    /**
     * Gets the most recent {@link Ability} based on the dateScored value for the same assessment.
     *
     * @param abilityList  the list of {@link Ability}s to iterate through
     * @param assessmentId The test key
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
     * @param abilityList  the list of {@link Ability}s to iterate through
     * @param assessmentId The test key
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
            if (openExamRequest.getMaxAttempts() < 0 && !externalSessionConfiguration.isInSimulationEnvironment()) {
                return Optional.of(new ValidationError(ValidationErrorCode.SIMULATION_ENVIRONMENT_REQUIRED, "Environment must be simulation when max attempts less than zero"));
            }

            return Optional.empty();
        }

        //Lines 5645 - 5673 in StudentDLL
        if (ExamStatusCode.STAGE_CLOSED.equals(previousExam.getStatus().getStage())) {
            if (externalSessionConfiguration.isInSimulationEnvironment()) {
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

    //Should have long term cache
    private boolean allowsGuestStudent(String clientName, ExternalSessionConfiguration externalSessionConfiguration) {
        if(externalSessionConfiguration.isInSimulationEnvironment()) {
            return true;
        }

        Optional<ClientSystemFlag> maybeSystemFlag = configService.findClientSystemFlag(clientName, ANONYMOUS_STUDENT_AUDIT_OBJECT);

        return maybeSystemFlag.isPresent() && maybeSystemFlag.get().getIsOn();
    }
}
