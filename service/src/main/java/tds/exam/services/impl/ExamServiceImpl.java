package tds.exam.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import tds.assessment.Assessment;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.data.legacy.LegacyComparer;
import tds.common.web.exceptions.NotFoundException;
import tds.config.AssessmentWindow;
import tds.config.ClientSystemFlag;
import tds.config.TimeLimitConfiguration;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamStatusQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.RtsStudentPackageAttribute;

import static java.time.temporal.ChronoUnit.DAYS;
import static tds.common.time.JodaTimeConverter.convertJodaInstant;
import static tds.config.ClientSystemFlag.ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE;
import static tds.exam.ExamStatusCode.STATUS_PENDING;
import static tds.exam.ExamStatusCode.STATUS_SUSPENDED;
import static tds.exam.error.ValidationErrorCode.ANONYMOUS_STUDENT_NOT_ALLOWED;
import static tds.exam.error.ValidationErrorCode.NO_OPEN_ASSESSMENT_WINDOW;
import static tds.student.RtsStudentPackageAttribute.ACCOMMODATIONS;
import static tds.student.RtsStudentPackageAttribute.ENTITY_NAME;
import static tds.student.RtsStudentPackageAttribute.EXTERNAL_ID;

@Service
class ExamServiceImpl implements ExamService {
    private static final Logger LOG = LoggerFactory.getLogger(ExamServiceImpl.class);
    private static final int CONTENT_LOAD_TIMEOUT = 120;

    private final ExamQueryRepository examQueryRepository;
    private final ExamCommandRepository examCommandRepository;
    private final ExamItemService examItemService;
    private final HistoryQueryRepository historyQueryRepository;
    private final SessionService sessionService;
    private final StudentService studentService;
    private final ExamSegmentService examSegmentService;
    private final AssessmentService assessmentService;
    private final TimeLimitConfigurationService timeLimitConfigurationService;
    private final ConfigService configService;
    private final ExamStatusQueryRepository examStatusQueryRepository;
    private final ExamAccommodationService examAccommodationService;

    private final Set<String> statusesThatCanTransitionToPaused;

    @Autowired
    public ExamServiceImpl(ExamQueryRepository examQueryRepository,
                           HistoryQueryRepository historyQueryRepository,
                           SessionService sessionService,
                           StudentService studentService,
                           ExamSegmentService examSegmentService,
                           AssessmentService assessmentService,
                           TimeLimitConfigurationService timeLimitConfigurationService,
                           ConfigService configService,
                           ExamCommandRepository examCommandRepository,
                           ExamItemService examItemService,
                           ExamStatusQueryRepository examStatusQueryRepository,
                           ExamAccommodationService examAccommodationService) {
        this.examQueryRepository = examQueryRepository;
        this.historyQueryRepository = historyQueryRepository;
        this.sessionService = sessionService;
        this.studentService = studentService;
        this.examSegmentService = examSegmentService;
        this.assessmentService = assessmentService;
        this.timeLimitConfigurationService = timeLimitConfigurationService;
        this.configService = configService;
        this.examCommandRepository = examCommandRepository;
        this.examItemService = examItemService;
        this.examStatusQueryRepository = examStatusQueryRepository;
        this.examAccommodationService = examAccommodationService;

        // From CommondDLL._IsValidStatusTransition_FN(): a collection of all the statuses that can transition to
        // "paused".  That is, each of these status values has a nested switch statement that contains the "paused"
        // status.
        statusesThatCanTransitionToPaused = new HashSet<>(Arrays.asList(ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_INITIALIZING));
    }

    @Override
    public Optional<Exam> findExam(UUID id) {
        return examQueryRepository.getExamById(id);
    }

    @Override
    public Response<Exam> openExam(OpenExamRequest openExamRequest) {
        //Line 5602 in StudentDLL.  This has been moved to earlier in the flow than the original because it is used throughout.  The original
        //fetches the external configuration multiple times in the different layers.
        Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName());
        if (!maybeExternalSessionConfiguration.isPresent()) {
            throw new IllegalStateException(String.format("External Session Configuration could not be found for client name %s", openExamRequest.getClientName()));
        }

        ExternalSessionConfiguration externalSessionConfiguration = maybeExternalSessionConfiguration.get();

        //Different parts of the session are queried throughout the legacy code.  Instead we fetch the entire session object in one call and pass
        //the reference to those parts that require it.
        Optional<Session> maybeSession = sessionService.findSessionById(openExamRequest.getSessionId());
        if (!maybeSession.isPresent()) {
            throw new IllegalArgumentException(String.format("Could not find session for id %s", openExamRequest.getSessionId()));
        }

        Session currentSession = maybeSession.get();

        //Line OpenTestServiceImp line 126 - 130
        if (!currentSession.isOpen()) {
            return new Response<Exam>(new ValidationError(ValidationErrorCode.SESSION_NOT_OPEN, String.format("Session %s is not open", currentSession.getId())));
        }

        if (!openExamRequest.isGuestStudent()) {
            studentService.getStudentById(openExamRequest.getStudentId()).orElseThrow((Supplier<RuntimeException>) ()
                -> new IllegalArgumentException(String.format("Could not find student for id %s", openExamRequest.getStudentId()))
            );
        } else {
            //OpenTestServiceImpl lines 103 - 104
            if (!allowsGuestStudent(openExamRequest.getClientName(), externalSessionConfiguration)) {
                return new Response<Exam>(new ValidationError(ANONYMOUS_STUDENT_NOT_ALLOWED, String.format("Anonymous students not allowed for this client %s", openExamRequest.getClientName())));
            }
        }

        Optional<Assessment> maybeAssessment = assessmentService.findAssessment(openExamRequest.getClientName(),
            openExamRequest.getAssessmentKey());
        if (!maybeAssessment.isPresent()) {
            throw new IllegalArgumentException(String.format("Assessment information could not be found for assessment key %s", openExamRequest.getAssessmentKey()));
        }

        Assessment assessment = maybeAssessment.get();

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
            return openPreviousExam(openExamRequest, externalSessionConfiguration, maybePreviousExam.get(), assessment);
        }

        Exam previousExam = maybePreviousExam.isPresent() ? maybePreviousExam.get() : null;
        Optional<ValidationError> maybeOpenNewExamValidationError = canCreateNewExam(openExamRequest, previousExam, externalSessionConfiguration);
        if (maybeOpenNewExamValidationError.isPresent()) {
            return new Response<Exam>(maybeOpenNewExamValidationError.get());
        }

        return createExam(openExamRequest, currentSession, assessment, externalSessionConfiguration, previousExam);
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

    @Override
    public Optional<Double> getInitialAbility(Exam exam, Assessment assessment) {
        Optional<Double> ability = Optional.empty();
        float slope = assessment.getAbilitySlope();
        float intercept = assessment.getAbilityIntercept();
        List<Ability> testAbilities = examQueryRepository.findAbilities(exam.getId(), exam.getClientName(),
            assessment.getSubject(), exam.getStudentId());

        // Attempt to retrieve the most recent ability for the current subject and assessment
        Optional<Ability> initialAbility = getMostRecentTestAbilityForSameAssessment(testAbilities, exam.getAssessmentId());
        if (initialAbility.isPresent()) {
            ability = Optional.of(initialAbility.get().getScore());
        } else if (assessment.isInitialAbilityBySubject()) {
            // if no ability for a similar assessment was retrieved above, attempt to get the initial ability for another
            // assessment of the same subject
            initialAbility = getMostRecentTestAbilityForDifferentAssessment(testAbilities, exam.getAssessmentId());
            if (initialAbility.isPresent()) {
                ability = Optional.of(initialAbility.get().getScore());
            } else {
                // if no value was returned from the previous call, get the initial ability from the previous year
                Optional<Double> initialAbilityFromHistory = historyQueryRepository.findAbilityFromHistoryForSubjectAndStudent(
                    exam.getClientName(), exam.getSubject(), exam.getStudentId());

                if (initialAbilityFromHistory.isPresent()) {
                    ability = Optional.of(initialAbilityFromHistory.get() * slope + intercept);
                }
            }
        }

        // If the ability was not retrieved from any of the exam tables, query the assessment service
        if (!ability.isPresent()) {
            ability = Optional.of((double) assessment.getStartAbility());
        }

        return ability;
    }

    @Override
    public Optional<ValidationError> pauseExam(UUID examId) {
        Exam exam = examQueryRepository.getExamById(examId)
            .orElseThrow(() -> new NotFoundException(String.format("Exam could not be found for id %s", examId)));

        if (!statusesThatCanTransitionToPaused.contains(exam.getStatus().getStatus())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE,
                String.format("Bad status transition from %s to %s", exam.getStatus().getStatus(), ExamStatusCode.STATUS_PAUSED)));
        }

        // A status change reason is not required for pausing an exam.
        Exam pausedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), org.joda.time.Instant.now())
            .build();

        examCommandRepository.update(pausedExam);

        return Optional.empty();
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

        Instant sessionDateVisited = Instant.ofEpochMilli(session.getDateVisited().getMillis());
        if (Instant.now().isAfter(sessionDateVisited.plus(timeLimitConfig.getTaCheckinTimeMinutes(), ChronoUnit.MINUTES))) {
            // Legacy code creates an audit record here.  Immutability should provide an audit trail; a new session record
            // will be inserted to represent the change in status.
            sessionService.pause(session.getId(), "closed");
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_TA_CHECKIN_TIMEOUT, "The session is not available for testing, please check with your test administrator."));
        }

        return Optional.empty();
    }

    @Override
    public Response<ExamConfiguration> startExam(final UUID examId) {
        ExamConfiguration examConfig;
        Optional<Exam> maybeExam = examQueryRepository.getExamById(examId);
        if (!maybeExam.isPresent()) {
            return new Response<ExamConfiguration>(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("No exam found for id %s", examId)
            ));
        }
        Exam exam = maybeExam.get();

        /* TestOpportunityServiceImpl [155] No need to go any further, so moving before service calls */
        if (!exam.getStatus().getStatus().equalsIgnoreCase(ExamStatusCode.STATUS_APPROVED)) {
            return new Response<ExamConfiguration>(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("Cannot start exam %s: Exam was not approved.", examId)
            ));
        }

        /* TestOpportunityServiceImpl [131] */
        Optional<Session> maybeSession = sessionService.findSessionById(exam.getSessionId());
        if (!maybeSession.isPresent()) {
            return new Response<ExamConfiguration>(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("No session found for session id %s", exam.getSessionId())));
        }
        Session session = maybeSession.get();

        /* StudentDLL [5269] / TestOpportunityServiceImpl [137] */
        Optional<ValidationError> maybeAccessViolation = verifyAccess(new ApprovalRequest(examId, session.getId(),
            exam.getBrowserId(), exam.getClientName()), exam);
        if (maybeAccessViolation.isPresent()) {
            return new Response<ExamConfiguration>(maybeAccessViolation.get());
        }
        /* TestOpportunityServiceImpl [147] */
        Optional<Assessment> maybeAssessment = assessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey());
        if (!maybeAssessment.isPresent()) {
            return new Response<ExamConfiguration>(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("No assessment found for assessment key '%s'.", exam.getAssessmentKey())
            ));
        }
        Assessment assessment = maybeAssessment.get();

        TimeLimitConfiguration timeLimitConfiguration =
            timeLimitConfigurationService.findTimeLimitConfiguration(exam.getClientName(), assessment.getAssessmentId())
                .orElseThrow(() ->
                    new IllegalStateException(String.format("No time limit configurations found for clientName '%s' and assessment id '%s'.",
                        exam.getClientName(), assessment.getAssessmentId())));

        /* StudentDLL [5344] Skipping getInitialAbility() call here - the ability is retrieved in legacy but never set on TestConfig */

        if (exam.getDateStarted() == null) { // Start a new exam
            // Initialize the segments in the exam and get the testlength.
            Exam initializedExam = initializeExam(exam, assessment);
            /* StudentDLL [5367] and TestOppServiceImpl [167] */
            examConfig = initializeDefaultExamConfiguration(initializedExam, assessment, timeLimitConfiguration);
        } else { //Restart or resume the most recent exam
            int resumptions = exam.getResumptions();
            int restartsAndResumptions = exam.getRestartAndResumptions() + 1; // Increment the restart count
            org.joda.time.Instant now = org.joda.time.Instant.now();
            Optional<org.joda.time.Instant> maybeLastActivity = examQueryRepository.getLastStudentActivityInstant(examId);

            /* [174] In the legacy app, if lastActiviy = null, then DbComparator.lessThan(null, <not-null>) = false */
            boolean isTimeDifferenceLessThanDelay = maybeLastActivity.isPresent()
                && Minutes.minutesBetween(maybeLastActivity.get(), now).getMinutes() < timeLimitConfiguration.getExamRestartWindowMinutes();

            if (isTimeDifferenceLessThanDelay) {
                resumptions++;
            }

            Exam restartedExam = new Exam.Builder()
                .fromExam(exam)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), now)
                .withDateChanged(now)
                .withResumptions(resumptions)
                .withRestartsAndResumptions(restartsAndResumptions)
                .withDateStarted(now)
                .build();

            examCommandRepository.update(restartedExam);
            /* Skip TestOpportunityAudit code [189] */

            // Default startPosition to "1" for a restarted exam
            int startPosition = 1;

            /* TestOpportunityServiceImpl [199] / StudentDlLL [5424]
             * Session type is always 1 (online) in TDS, so skip first conditional on [200]
             * No need to update restart count on line [203] because restart count can be derived from event table, so skip [202-203] */
            if (!isTimeDifferenceLessThanDelay && assessment.getDeleteUnansweredItems()) { // Restart exam
                // Mark the exam pages as "deleted"
                examItemService.deletePages(exam.getId());
            }

            if (isTimeDifferenceLessThanDelay) { // Resume exam
                /* TestOpportunityServiceImpl [215] - Only need to get latest position when resuming, else its 1 */
                startPosition = examItemService.getExamPosition(exam.getId());
            }

            /* Skip restart increment on [209] because we are already incrementing earlier in this method */
            /* [212] No need to call updateUnifnishedResponsePages since we no longer need to keep count of "opportunityrestart" */
            examConfig = getRestartedExamConfiguration(exam, assessment, timeLimitConfiguration, restartsAndResumptions, startPosition);
        }

        return new Response<>(examConfig);
    }

    private Exam initializeExam(final Exam exam, final Assessment assessment) {
        /* TestOpportunityServiceImpl [435] + [470] */
        int testLength = examSegmentService.initializeExamSegments(exam, assessment);
        org.joda.time.Instant now = org.joda.time.Instant.now();
        /* TODO: Skipping [451-469] until Simulation mode is investigated and implemented
         * The testoppabilityestimate table written to here is only read from in SimDLL.java  */
        Exam initializedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), org.joda.time.Instant.now())
            .withDateStarted(now)
            .withDateChanged(now)
            .withExpireFrom(now)
            .withMaxItems(testLength)
            .build();

        examCommandRepository.update(initializedExam);

        return initializedExam;
    }

    /*
        This method mimics the legacy TestConfigHelper.getNew().
        - scoreByTds has been removed as it is unused by the application.
    */
    private static ExamConfiguration initializeDefaultExamConfiguration(Exam exam, Assessment assessment, TimeLimitConfiguration timeLimitConfiguration) {
        return new ExamConfiguration.Builder()
            .withExam(exam)
            .withContentLoadTimeout(CONTENT_LOAD_TIMEOUT)
            .withInterfaceTimeout(timeLimitConfiguration.getInterfaceTimeoutMinutes())
            .withExamRestartWindowMinutes(timeLimitConfiguration.getExamRestartWindowMinutes())
            .withPrefetch(assessment.getPrefetch())
            .withValidateCompleteness(assessment.isValidateCompleteness())
            .withRequestInterfaceTimeout(timeLimitConfiguration.getRequestInterfaceTimeoutMinutes())
            .withRestartsAndResumptions(0)
            .withStartPosition(1)
            .withStatus(ExamStatusCode.STATUS_STARTED)
            .withTestLength(exam.getMaxItems())
            .build();
    }

    private static ExamConfiguration getRestartedExamConfiguration(Exam exam, Assessment assessment, TimeLimitConfiguration timeLimitConfiguration,
                                                                   int restartAndResumptions, int startPosition) {
        return new ExamConfiguration.Builder()
            .withExam(exam)
            .withContentLoadTimeout(CONTENT_LOAD_TIMEOUT)
            .withInterfaceTimeout(timeLimitConfiguration.getInterfaceTimeoutMinutes())
            .withExamRestartWindowMinutes(timeLimitConfiguration.getExamRestartWindowMinutes())
            .withPrefetch(assessment.getPrefetch())
            .withValidateCompleteness(assessment.isValidateCompleteness())
            .withRequestInterfaceTimeout(timeLimitConfiguration.getRequestInterfaceTimeoutMinutes())
            .withRestartsAndResumptions(restartAndResumptions)
            .withStartPosition(startPosition)
            .withStatus(ExamStatusCode.STATUS_STARTED)
            .withTestLength(exam.getMaxItems())
            .build();
    }

    private Response<Exam> createExam(OpenExamRequest openExamRequest, Session session, Assessment assessment, ExternalSessionConfiguration externalSessionConfiguration, Exam previousExam) {
        Exam.Builder examBuilder = new Exam.Builder();

        //From OpenTestServiceImpl lines 160 -163
        if (openExamRequest.getProctorId() == null) {
            examBuilder.withStatus(examStatusQueryRepository.findExamStatusCode(ExamStatusCode.STATUS_APPROVED), org.joda.time.Instant.now());
        } else {
            examBuilder.withStatus(examStatusQueryRepository.findExamStatusCode(ExamStatusCode.STATUS_PENDING), org.joda.time.Instant.now());
        }

        String guestAccommodations = openExamRequest.getGuestAccommodations();
        if (openExamRequest.isGuestStudent()) {
            examBuilder.withStudentName("GUEST");
            examBuilder.withLoginSSID("GUEST");
        } else {
            List<RtsStudentPackageAttribute> attributes = studentService.findStudentPackageAttributes(openExamRequest.getStudentId(), openExamRequest.getClientName(), EXTERNAL_ID, ENTITY_NAME, ACCOMMODATIONS);

            for (RtsStudentPackageAttribute attribute : attributes) {
                if (EXTERNAL_ID.equals(attribute.getName())) {
                    examBuilder.withLoginSSID(attribute.getValue());
                } else if (ENTITY_NAME.equals(attribute.getName())) {
                    examBuilder.withStudentName(attribute.getValue());
                } else if (StringUtils.isEmpty(guestAccommodations) && ACCOMMODATIONS.equals(attribute.getName())) {
                    guestAccommodations = attribute.getValue();
                }
            }
        }

        //OpenTestServiceImpl lines 317 - 341
        List<AssessmentWindow> assessmentWindows = configService.findAssessmentWindows(
            openExamRequest.getClientName(),
            assessment.getAssessmentId(),
            session.getType(),
            openExamRequest.getStudentId(),
            externalSessionConfiguration
        );

        //OpenTestServiceImpl lines 344 - 365
        Optional<AssessmentWindow> maybeWindow = assessmentWindows.stream()
            .filter(assessmentWindow -> assessmentWindow.getAssessmentKey().equals(openExamRequest.getAssessmentKey()))
            .min((o1, o2) -> o1.getStartTime().compareTo(o2.getStartTime()));

        //OpenTestServiceImpl line 367 - 368 validation check.  no window no exam
        if (!maybeWindow.isPresent()) {
            return new Response<Exam>(new ValidationError(NO_OPEN_ASSESSMENT_WINDOW, "Could not find an open assessment window"));
        }

        AssessmentWindow assessmentWindow = maybeWindow.get();

        //OpenTestServiceImpl lines 381 0 395 were not implemented because "_version" is never used in the application.  It is
        //inserted and updated throughout the flow but is not included in the TRT nor is used for any logic within the code.

        Exam exam = examBuilder
            .withId(UUID.randomUUID())
            .withClientName(externalSessionConfiguration.getClientName())
            .withStudentId(openExamRequest.getStudentId())
            .withSessionId(session.getId())
            .withBrowserId(openExamRequest.getBrowserId())
            .withAssessmentId(assessment.getAssessmentId())
            .withAssessmentKey(assessment.getKey())
            .withAttempts(previousExam == null ? 1 : previousExam.getAttempts() + 1)
            .withAssessmentAlgorithm(assessment.getSelectionAlgorithm().getType())
            .withSegmented(assessment.isSegmented())
            .withDateJoined(org.joda.time.Instant.now())
            .withAssessmentWindowId(assessmentWindow.getWindowId())
            .withEnvironment(externalSessionConfiguration.getEnvironment())
            .withSubject(assessment.getSubject())
            .build();

        examCommandRepository.insert(exam);

        //Lines 412 - 421 OpenTestServiceImpl is not implemented.  After talking with data warehouse and Smarter Balanced
        //The initial student attributes are not used and smarter balance suggested removing them
        examAccommodationService.initializeExamAccommodations(exam);

        //Lines OpenTestServiceImpl lines 428-447 not implemented.  Instead exam status is set during insert instead of inserting
        //and then updating status after accommodations

        return new Response<>(exam);
    }

    /**
     * Gets the most recent {@link tds.exam.models.Ability} based on the dateScored value for the same assessment.
     *
     * @param abilityList  the list of {@link tds.exam.models.Ability}s to iterate through
     * @param assessmentId The test key
     * @return the {@link tds.exam.models.Ability} that lines up with the assessment id
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
     * Gets the most recent {@link tds.exam.models.Ability} based on the dateScored value for a different assessment.
     *
     * @param abilityList  the list of {@link tds.exam.models.Ability}s to iterate through
     * @param assessmentId The test key
     * @return the {@link tds.exam.models.Ability} that lines up with the assessment id
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

    private Response<Exam> openPreviousExam(OpenExamRequest openExamRequest, ExternalSessionConfiguration externalSessionConfiguration, Exam previousExam, Assessment assessment) {
        /*
         Represents StudentDLL._OpenExistingOpportunity_SP in the legacy code.  The beginning of the method
         goes and fetches statuses and previous exams which we do not need to do here since that is done earlier
         in the flow.
         */

        //StudentDLL - around line 6793
        //If the student already has started the exam then the exam starts in a suspended state otherwise
        //the new exam being opened is treated as a fresh one which is pending state waiting for the proctor to approve
        ExamStatusCode status = examStatusQueryRepository.findExamStatusCode(STATUS_PENDING);
        if (previousExam.getDateStarted() != null) {
            status = examStatusQueryRepository.findExamStatusCode(STATUS_SUSPENDED);
        }

        //Student DLL - around line 6804
        //If for some reason the previous exam is in an inuse stage then we still allow the prevous exam to
        //be opened but we mark it as an abnormal start.  This should not happen very often as it most likely
        //is some type of bug in the legacy Web app and its processing of requests.
        int abnormalIncrement = ExamStatusStage.IN_USE.equals(previousExam.getStatus().getStage()) ? 1 : 0;

        Exam currentExam = new Exam.Builder()
            .fromExam(previousExam)
            .withStatus(status, org.joda.time.Instant.now())
            .withBrowserId(openExamRequest.getBrowserId())
            .withDateChanged(org.joda.time.Instant.now())
            .withAbnormalStarts(previousExam.getAbnormalStarts() + abnormalIncrement)
            .build();

        examCommandRepository.update(currentExam);

        return new Response<>(currentExam);
    }

    private Optional<ValidationError> canOpenPreviousExam(Exam previousExam, Session currentSession) {
        //Port of Student.DLL lines 5526-5530
        if (ExamStatusStage.CLOSED.equals(previousExam.getStatus().getStage())) {
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
        if (ExamStatusStage.INACTIVE.equals(previousExam.getStatus().getStage())) {
            return Optional.empty();
        }

        /*
        The below code is a straight port of the legacy StudentDLL._CanOpenExistingOpportunity_SP.  It was a little hard to follow the application
        reasons for this logic. StudentDLL lines 5569 - 5589
         */
        boolean daysSinceLastChange = false;
        if (previousExam.getDateChanged() != null) {
            daysSinceLastChange = DAYS.between(Instant.ofEpochMilli(previousExam.getDateChanged().getMillis()), Instant.now()) >= 1;
        }

        if (daysSinceLastChange ||
            LegacyComparer.isEqual(previousSession.getId(), currentSession.getId()) ||
            LegacyComparer.isEqual("closed", previousSession.getStatus()) ||
            LegacyComparer.greaterThan(Instant.now(), convertJodaInstant(previousSession.getDateEnd()))) {
            return Optional.empty();
        }

        //Port of Student.DLL line 5593
        return Optional.of(new ValidationError(ValidationErrorCode.CURRENT_EXAM_OPEN, "Current exam is active"));
    }

    private Optional<ValidationError> canCreateNewExam(OpenExamRequest openExamRequest, Exam previousExam, ExternalSessionConfiguration externalSessionConfiguration) {
        //Lines 5610 - 5618 in StudentDLL was not implemented.  The reason is that the max opportunities is always
        //3 via the loader scripts.  So the the conditional in the StudentDLL code will always allow one to open a new
        //Exam if previous exam is null (0 ocnt in the legacy code)

        //Get timelmits for delay days  Line 516-525 OpenTestServiceImpl
        Integer numberOfDaysToDelay = null;
        Optional<TimeLimitConfiguration> maybeTimeLimitConfiguration = timeLimitConfigurationService.findTimeLimitConfiguration(openExamRequest.getClientName(), openExamRequest.getAssessmentKey());
        if (maybeTimeLimitConfiguration.isPresent()) {
            numberOfDaysToDelay = maybeTimeLimitConfiguration.get().getExamDelayDays();
        }

        //Lines 5645 - 5673 in StudentDLL
        if (previousExam != null) {
            //This is done with a query in the legacy application but we can just check the status of the previous exam fetched in a previous step.
            if (!ExamStatusStage.CLOSED.equals(previousExam.getStatus().getStage())) {
                return Optional.of(new ValidationError(ValidationErrorCode.PREVIOUS_EXAM_NOT_CLOSED, "Previous exam is not closed"));
            }

            //Lines 5646 - 5649
            if (externalSessionConfiguration.isInSimulationEnvironment()) {
                return Optional.empty();
            }

            //Verifies that the new exam does not exceed attempts and there are enough days since the last attempt
            boolean daysSinceLastExamThreshold = previousExam.getDateCompleted() == null ||
                LegacyComparer.greaterThan(Duration.between(convertJodaInstant(previousExam.getDateCompleted()), Instant.now()).get(DAYS), numberOfDaysToDelay);

            if (LegacyComparer.lessThan(previousExam.getAttempts(), openExamRequest.getMaxAttempts()) &&
                daysSinceLastExamThreshold) {
                return Optional.empty();
            } else if (LegacyComparer.greaterOrEqual(previousExam.getAttempts(), openExamRequest.getMaxAttempts())) {
                return Optional.of(new ValidationError(ValidationErrorCode.MAX_OPPORTUNITY_EXCEEDED, "Max number of attempts for exam exceeded"));
            } else {
                return Optional.of(new ValidationError(ValidationErrorCode.NOT_ENOUGH_DAYS_PASSED, String.format("Next exam cannot be started until %s days pass since last exam", numberOfDaysToDelay)));
            }
        }

        return Optional.empty();
    }

    //Should have long term cache
    private boolean allowsGuestStudent(String clientName, ExternalSessionConfiguration externalSessionConfiguration) {
        if (externalSessionConfiguration.isInSimulationEnvironment()) {
            return true;
        }

        Optional<ClientSystemFlag> maybeAllowGuestAccessFlag = configService.findClientSystemFlag(clientName, ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE);

        return maybeAllowGuestAccessFlag.isPresent() && maybeAllowGuestAccessFlag.get().isEnabled();
    }
}