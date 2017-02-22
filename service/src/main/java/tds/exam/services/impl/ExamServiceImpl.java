package tds.exam.services.impl;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.AssessmentWindow;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.data.legacy.LegacyComparer;
import tds.common.web.exceptions.NotFoundException;
import tds.config.ClientSystemFlag;
import tds.config.TimeLimitConfiguration;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamConfiguration;
import tds.exam.ExamInfo;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.ExamineeContext;
import tds.exam.ExpandableExam;
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
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamService;
import tds.exam.services.ExamineeService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.exam.utils.StatusTransitionValidator;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.RtsStudentPackageAttribute;
import tds.student.Student;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparing;
import static tds.common.time.JodaTimeConverter.convertJodaInstant;
import static tds.config.ClientSystemFlag.ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE;
import static tds.config.ClientSystemFlag.RESTORE_ACCOMMODATIONS_TYPE;
import static tds.exam.ExamStatusCode.STATUS_PENDING;
import static tds.exam.ExamStatusCode.STATUS_SUSPENDED;
import static tds.exam.error.ValidationErrorCode.ANONYMOUS_STUDENT_NOT_ALLOWED;
import static tds.exam.error.ValidationErrorCode.NO_OPEN_ASSESSMENT_WINDOW;
import static tds.student.RtsStudentPackageAttribute.ACCOMMODATIONS;
import static tds.student.RtsStudentPackageAttribute.ENTITY_NAME;
import static tds.student.RtsStudentPackageAttribute.EXTERNAL_ID;

@Service
class ExamServiceImpl implements ExamService {
    private static final int CONTENT_LOAD_TIMEOUT = 120;

    private final ExamQueryRepository examQueryRepository;
    private final ExamCommandRepository examCommandRepository;
    private final ExamPageService examPageService;
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
    private final ExamApprovalService examApprovalService;
    private final ExamineeService examineeService;

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
                           ExamPageService examPageService,
                           ExamItemService examItemService,
                           ExamStatusQueryRepository examStatusQueryRepository,
                           ExamAccommodationService examAccommodationService,
                           ExamApprovalService examApprovalService,
                           ExamineeService examineeService) {
        this.examQueryRepository = examQueryRepository;
        this.historyQueryRepository = historyQueryRepository;
        this.sessionService = sessionService;
        this.studentService = studentService;
        this.examSegmentService = examSegmentService;
        this.assessmentService = assessmentService;
        this.timeLimitConfigurationService = timeLimitConfigurationService;
        this.configService = configService;
        this.examCommandRepository = examCommandRepository;
        this.examPageService = examPageService;
        this.examItemService = examItemService;
        this.examStatusQueryRepository = examStatusQueryRepository;
        this.examAccommodationService = examAccommodationService;
        this.examApprovalService = examApprovalService;
        this.examineeService = examineeService;

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
    public Optional<Exam> findExam(final UUID id) {
        return examQueryRepository.getExamById(id);
    }

    @Transactional
    @Override
    public Response<Exam> openExam(final OpenExamRequest openExamRequest) {
        //Different parts of the session are queried throughout the legacy code.  Instead we fetch the entire session object in one call and pass
        //the reference to those parts that require it.
        Optional<Session> maybeSession = sessionService.findSessionById(openExamRequest.getSessionId());
        if (!maybeSession.isPresent()) {
            throw new IllegalArgumentException(String.format("Could not find session for id %s", openExamRequest.getSessionId()));
        }

        Session currentSession = maybeSession.get();

        //Line 5602 in StudentDLL.  This has been moved to earlier in the flow than the original because it is used throughout.  The original
        //fetches the external configuration multiple times in the different layers.
        Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName(currentSession.getClientName());
        if (!maybeExternalSessionConfiguration.isPresent()) {
            throw new IllegalStateException(String.format("External Session Configuration could not be found for client name %s", currentSession.getClientName()));
        }

        ExternalSessionConfiguration externalSessionConfiguration = maybeExternalSessionConfiguration.get();


        //Line OpenTestServiceImp line 126 - 130
        if (!currentSession.isOpen()) {
            return new Response<>(new ValidationError(ValidationErrorCode.SESSION_NOT_OPEN, String.format("Session %s is not open", currentSession.getId())));
        }

        Student student = null;
        if (!openExamRequest.isGuestStudent()) {
            student = studentService.getStudentById(currentSession.getClientName(), openExamRequest.getStudentId()).orElseThrow((Supplier<RuntimeException>) ()
                -> new IllegalArgumentException(String.format("Could not find student for id %s", openExamRequest.getStudentId()))
            );
        } else {
            //OpenTestServiceImpl lines 103 - 104
            if (!allowsGuestStudent(currentSession.getClientName(), externalSessionConfiguration)) {
                return new Response<>(new ValidationError(ANONYMOUS_STUDENT_NOT_ALLOWED, String.format("Anonymous students not allowed for this client %s", currentSession.getClientName())));
            }
        }

        Optional<Assessment> maybeAssessment = assessmentService.findAssessment(currentSession.getClientName(),
            openExamRequest.getAssessmentKey());
        if (!maybeAssessment.isPresent()) {
            throw new IllegalArgumentException(String.format("Assessment information could not be found for assessment key %s", openExamRequest.getAssessmentKey()));
        }

        Assessment assessment = maybeAssessment.get();

        //Previous exam is retrieved in lines 5492 - 5530 and 5605 - 5645 in StudentDLL
        Optional<Exam> maybePreviousExam = examQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), assessment.getAssessmentId(), currentSession.getClientName());

        boolean canOpenPreviousExam = false;
        if (maybePreviousExam.isPresent()) {
            Optional<ValidationError> canOpenPreviousExamError = canOpenPreviousExam(maybePreviousExam.get(), currentSession);

            if (canOpenPreviousExamError.isPresent()) {
                return new Response<>(canOpenPreviousExamError.get());
            }

            canOpenPreviousExam = true;
        }

        if (canOpenPreviousExam) {
            return openPreviousExam(currentSession.getClientName(), openExamRequest, maybePreviousExam.get(), assessment);
        }

        Exam previousExam = maybePreviousExam.isPresent() ? maybePreviousExam.get() : null;
        Optional<ValidationError> maybeOpenNewExamValidationError = canCreateNewExam(currentSession.getClientName(), openExamRequest, previousExam, externalSessionConfiguration);
        if (maybeOpenNewExamValidationError.isPresent()) {
            return new Response<>(maybeOpenNewExamValidationError.get());
        }

        return createExam(currentSession.getClientName(), openExamRequest, currentSession, assessment, externalSessionConfiguration, previousExam, student);
    }

    @Override
    public Optional<Double> getInitialAbility(final Exam exam, final Assessment assessment) {
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

    @Transactional
    @Override
    public Optional<ValidationError> updateExamStatus(final UUID examId, final ExamStatusCode newStatus) {
        return updateExamStatus(examId, newStatus, null);
    }

    @Transactional
    @Override
    public Optional<ValidationError> updateExamStatus(final UUID examId, final ExamStatusCode newStatus, final String statusChangeReason) {
        Exam exam = examQueryRepository.getExamById(examId)
            .orElseThrow(() -> new NotFoundException(String.format("Exam could not be found for id %s", examId)));

        if (!StatusTransitionValidator.isValidTransition(exam.getStatus().getCode(), newStatus.getCode())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE,
                String.format("Transitioning exam status from %s to %s is not allowed", exam.getStatus().getCode(), newStatus.getCode())));
        }

        Exam updatedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(newStatus, org.joda.time.Instant.now())
            .withStatusChangeReason(statusChangeReason)
            .build();

        examCommandRepository.update(updatedExam);

        return Optional.empty();
    }

    @Transactional
    @Override
    public void pauseAllExamsInSession(final UUID sessionId) {
        List<Exam> examsInSession = examQueryRepository.findAllExamsInSessionWithStatus(sessionId,
            statusesThatCanTransitionToPaused);

        if (examsInSession.isEmpty()) {
            return;
        }

        ExamStatusCode pausedStatus = new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE);
        List<Exam> pausedExams = examsInSession.stream()
            .map(e -> new Exam.Builder().fromExam(e)
                .withStatus(pausedStatus, org.joda.time.Instant.now())
                .withStatusChangeReason("paused by session")
                .build())
            .collect(Collectors.toList());

        examCommandRepository.update(pausedExams.toArray(new Exam[pausedExams.size()]));
    }

    @Override
    public Response<List<ExpandableExam>> findExamsBySessionId(final UUID sessionId, final String... expandableParams) {
        final Set<String> params = Sets.newHashSet(expandableParams);
        final Set<String> validExamStatuses = Sets.newHashSet(
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_DENIED
        );

        final List<Exam> exams = examQueryRepository.findAllExamsInSessionWithStatus(sessionId, validExamStatuses);
        final Map<UUID, ExpandableExam.Builder> examBuilders = exams.stream()
            .collect(Collectors.toMap(Exam::getId, exam -> new ExpandableExam.Builder(exam)));
        final UUID[] examIds = examBuilders.keySet().toArray(new UUID[examBuilders.size()]);

        if (params.contains(ExpandableExam.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS)) {
            List<ExamAccommodation> examAccommodations = examAccommodationService.findApprovedAccommodations(examIds);
            mapExamAccommodationsToExams(examBuilders, examAccommodations);
        }

        if (params.contains(ExpandableExam.EXPANDABLE_PARAMS_ITEM_RESPONSE_COUNT)) {
            Map<UUID, Integer> itemResponseCounts = examItemService.getResponseCounts(examIds);
            mapResponseCountsToExams(examBuilders, itemResponseCounts);
        }

        if (params.contains(ExpandableExam.EXPANDABLE_PARAMS_UNFULFILLED_REQUEST_COUNT)) {
            //TODO: fetch count of unfulfilled print/emboss requests for each exam
        }

        // Build each exam
        List<ExpandableExam> expandableExams = examBuilders.values().stream()
            .map(builders -> builders.build())
            .collect(Collectors.toList());

        return new Response<>(expandableExams);
    }

    @Transactional
    @Override
    public Response<ExamConfiguration> startExam(final UUID examId) {
        ExamConfiguration examConfig;
        Optional<Exam> maybeExam = examQueryRepository.getExamById(examId);
        if (!maybeExam.isPresent()) {
            return new Response<>(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("No exam found for id %s", examId)
            ));
        }
        Exam exam = maybeExam.get();

        /* TestOpportunityServiceImpl [155] No need to go any further, so moving before service calls */
        if (!exam.getStatus().getCode().equalsIgnoreCase(ExamStatusCode.STATUS_APPROVED)) {
            return new Response<>(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("Cannot start exam %s: Exam was not approved.", examId)
            ));
        }

        /* TestOpportunityServiceImpl [131] */
        Optional<Session> maybeSession = sessionService.findSessionById(exam.getSessionId());
        if (!maybeSession.isPresent()) {
            return new Response<>(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("No session found for session id %s", exam.getSessionId())));
        }
        Session session = maybeSession.get();

        /* StudentDLL [5269] / TestOpportunityServiceImpl [137] */
        Optional<ValidationError> maybeAccessViolation = examApprovalService.verifyAccess(new ExamInfo(examId, session.getId(),
            exam.getBrowserId()), exam);
        if (maybeAccessViolation.isPresent()) {
            return new Response<>(maybeAccessViolation.get());
        }
        /* TestOpportunityServiceImpl [147] */
        Optional<Assessment> maybeAssessment = assessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey());
        if (!maybeAssessment.isPresent()) {
            return new Response<>(new ValidationError(
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

        if (exam.getStartedAt() == null) { // Start a new exam
            // Initialize the segments in the exam and get the testlength.
            Exam initializedExam = initializeExam(exam, assessment);
            /* StudentDLL [5367] and TestOppServiceImpl [167] */
            examConfig = initializeDefaultExamConfiguration(initializedExam, assessment, timeLimitConfiguration);
        } else { // Restart or resume the most recent exam
            int resumptions = exam.getResumptions();
            int restartsAndResumptions = exam.getRestartsAndResumptions() + 1; // Increment the restartAndResumption count
            org.joda.time.Instant now = org.joda.time.Instant.now();
            Optional<org.joda.time.Instant> maybeLastActivity = examQueryRepository.findLastStudentActivity(examId);

            /* [178] In the legacy app, if lastActivity = null, then DbComparator.lessThan(null, <not-null>) = false */
            boolean isResumable = maybeLastActivity.isPresent()
                && Minutes.minutesBetween(maybeLastActivity.get(), now).getMinutes() < timeLimitConfiguration.getExamRestartWindowMinutes();

            /* [186 - 191] Move the resume/grace period restart increment and the exam update down in the flow of this code */
            /* Skip TestOpportunityAudit code [193] */
            int startPosition = 1;     // Default startPosition to "1" for a restarted exam

            /* TestOpportunityServiceImpl [204] / StudentDlLL [5424]
             * Session type is always 0 (online) in TDS, so skip first conditional on [204]
             * No need to update restart count on line [203] because restart count can be derived from event table, so skip [202-203] */

            if (isResumable) { // Resume exam
                /* TestOpportunityServiceImpl [215] - Only need to get latest position when resuming, else its 1 */
                startPosition = examItemService.getExamPosition(exam.getId());
                /* This increment is done in TestOpportunityServiceImpl [179] */
                resumptions++;
            } else if (assessment.shouldDeleteUnansweredItems()) { // Restart exam
                // Mark the exam pages as "deleted"
                examPageService.deletePages(exam.getId());
            }

            Exam restartedExam = new Exam.Builder()
                .fromExam(exam)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), now)
                .withChangedAt(now)
                .withResumptions(resumptions)
                .withRestartsAndResumptions(restartsAndResumptions)
                .withStartedAt(now)
                .build();

            examCommandRepository.update(restartedExam);
            /* Skip restart increment on [209] because we are already incrementing earlier in this method */
            /* [212] No need to call updateUnfinishedResponsePages since we no longer need to keep count of "opportunityrestart" */
            examConfig = getExamConfiguration(exam, assessment, timeLimitConfiguration, startPosition);
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
            .withStartedAt(now)
            .withChangedAt(now)
            .withExpiresAt(now)
            .withRestartsAndResumptions(0)
            .withMaxItems(testLength)
            .build();

        examCommandRepository.update(initializedExam);

        return initializedExam;
    }

    private static ExamConfiguration initializeDefaultExamConfiguration(final Exam exam, final Assessment assessment, final TimeLimitConfiguration timeLimitConfiguration) {
        /*
            This method mimics the legacy TestConfigHelper.getNew().
            - scoreByTds has been removed as it is unused by the application.
        */
        return getExamConfiguration(exam, assessment, timeLimitConfiguration, 1);
    }

    private static ExamConfiguration getExamConfiguration(final Exam exam, final Assessment assessment, final TimeLimitConfiguration timeLimitConfiguration, int startPosition) {
        return new ExamConfiguration.Builder()
            .withExam(exam)
            .withContentLoadTimeout(CONTENT_LOAD_TIMEOUT)
            .withInterfaceTimeout(timeLimitConfiguration.getInterfaceTimeoutMinutes())
            .withExamRestartWindowMinutes(timeLimitConfiguration.getExamRestartWindowMinutes())
            .withPrefetch(assessment.getPrefetch())
            .withValidateCompleteness(assessment.isValidateCompleteness())
            .withRequestInterfaceTimeout(timeLimitConfiguration.getRequestInterfaceTimeoutMinutes())
            .withStartPosition(startPosition)
            .withStatus(ExamStatusCode.STATUS_STARTED)
            .withTestLength(exam.getMaxItems())
            .build();
    }

    private Response<Exam> createExam(final String clientName,
                                      final OpenExamRequest openExamRequest,
                                      final Session session, Assessment assessment,
                                      final ExternalSessionConfiguration externalSessionConfiguration,
                                      final Exam previousExam,
                                      final Student student) {
        Exam.Builder examBuilder = new Exam.Builder();

        //From OpenTestServiceImpl lines 160 -163
        if (session.getProctorId() == null) {
            examBuilder.withStatus(examStatusQueryRepository.findExamStatusCode(ExamStatusCode.STATUS_APPROVED), org.joda.time.Instant.now());
        } else {
            examBuilder.withStatus(examStatusQueryRepository.findExamStatusCode(ExamStatusCode.STATUS_PENDING), org.joda.time.Instant.now());
        }

        String guestAccommodations = openExamRequest.getGuestAccommodations();
        if (openExamRequest.isGuestStudent()) {
            examBuilder.withStudentName("GUEST");
            examBuilder.withLoginSSID("GUEST");
        } else {
            List<RtsStudentPackageAttribute> attributes = studentService.findStudentPackageAttributes(openExamRequest.getStudentId(), clientName, EXTERNAL_ID, ENTITY_NAME, ACCOMMODATIONS);

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
        List<AssessmentWindow> assessmentWindows = assessmentService.findAssessmentWindows(
            clientName,
            assessment.getAssessmentId(),
            openExamRequest.getStudentId(),
            externalSessionConfiguration
        );

        //OpenTestServiceImpl lines 344 - 365
        Optional<AssessmentWindow> maybeWindow = assessmentWindows.stream()
            .filter(assessmentWindow -> assessmentWindow.getAssessmentKey().equals(openExamRequest.getAssessmentKey()))
            .min(comparing(AssessmentWindow::getStartTime));

        //OpenTestServiceImpl line 367 - 368 validation check.  no window no exam
        if (!maybeWindow.isPresent()) {
            return new Response<>(new ValidationError(
                NO_OPEN_ASSESSMENT_WINDOW,
                configService.getFormattedMessage(
                    clientName,
                    "_OpenNewOpportunity",
                    "There is no active testing window for this student at this time"
                )
            ));
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
            .withJoinedAt(org.joda.time.Instant.now())
            .withAssessmentWindowId(assessmentWindow.getWindowId())
            .withEnvironment(externalSessionConfiguration.getEnvironment())
            .withSubject(assessment.getSubject())
            .build();

        examCommandRepository.insert(exam);

        // OpenTestServiceImpl lines 409 - 410
        if (!openExamRequest.isGuestStudent()) {
            examineeService.insertAttributesAndRelationships(exam, student, ExamineeContext.INITIAL);
        }

        //Lines 412 - 421 OpenTestServiceImpl is not implemented.  After talking with data warehouse and Smarter Balanced
        //The initial student attributes are not used and smarter balance suggested removing them
        List<ExamAccommodation> examAccommodations = examAccommodationService.initializeExamAccommodations(exam);

        exam = updateExamWithCustomAccommodations(exam, examAccommodations);

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
    private Optional<Ability> getMostRecentTestAbilityForSameAssessment(final List<Ability> abilityList, final String assessmentId) {
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
    private Optional<Ability> getMostRecentTestAbilityForDifferentAssessment(final List<Ability> abilityList, final String assessmentId) {
        for (Ability ability : abilityList) {
            if (!assessmentId.equals(ability.getAssessmentId())) {
                /* NOTE: The query that retrieves the list of abilities is sorted by the "date_scored" of the exam in
                   descending order. Therefore we can assume the first match is the most recent */
                return Optional.of(ability);
            }
        }

        return Optional.empty();
    }

    private Response<Exam> openPreviousExam(final String clientName,
                                            final OpenExamRequest openExamRequest,
                                            final Exam previousExam,
                                            final Assessment assessment) {
        /*
         Represents StudentDLL._OpenExistingOpportunity_SP in the legacy code.  The beginning of the method
         goes and fetches statuses and previous exams which we do not need to do here since that is done earlier
         in the flow.
         */

        //StudentDLL - around line 6793
        //If the student already has started the exam then the exam starts in a suspended state otherwise
        //the new exam being opened is treated as a fresh one which is pending state waiting for the proctor to approve
        ExamStatusCode status = examStatusQueryRepository.findExamStatusCode(STATUS_PENDING);
        if (previousExam.getStartedAt() != null) {
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
            .withSessionId(openExamRequest.getSessionId())
            .withChangedAt(org.joda.time.Instant.now())
            .withAbnormalStarts(previousExam.getAbnormalStarts() + abnormalIncrement)
            .build();

        examCommandRepository.update(currentExam);

        //The next block replaces OpenTestServiceImpl lines 194-202 fetching the guest accommodations if not a guest student
        //Fetches the client system flag for restoring accommodations StudentDLL._RestoreRTSAccommodations_FN
        String guestAccommodations = openExamRequest.getGuestAccommodations();
        Optional<ClientSystemFlag> maybeRestoreAccommodations = configService.findClientSystemFlag(clientName, RESTORE_ACCOMMODATIONS_TYPE);
        boolean restoreAccommodations = maybeRestoreAccommodations.isPresent() && maybeRestoreAccommodations.get().isEnabled();
        if (restoreAccommodations && !openExamRequest.isGuestStudent()) {
            List<RtsStudentPackageAttribute> attributes = studentService.findStudentPackageAttributes(openExamRequest.getStudentId(), clientName, ACCOMMODATIONS);

            if (!attributes.isEmpty()) {
                //If there are any attributes returned it should only be the one for restore accommodations
                RtsStudentPackageAttribute restoreAccommodationAttribute = attributes.get(0);
                guestAccommodations = restoreAccommodationAttribute.getValue();
            }
        }

        List<ExamAccommodation> examAccommodations = examAccommodationService.initializeAccommodationsOnPreviousExam(previousExam, assessment, 0, restoreAccommodations, guestAccommodations);

        currentExam = updateExamWithCustomAccommodations(currentExam, examAccommodations);
        return new Response<>(currentExam);
    }

    private Optional<ValidationError> canOpenPreviousExam(final Exam previousExam,
                                                          final Session currentSession) {
        //Port of Student.DLL lines 5526-5530
        if (ExamStatusStage.CLOSED.equals(previousExam.getStatus().getStage())) {
            return Optional.empty();
        }

        //Port of Student.DLL lines 5531-5551
        Optional<Session> maybePreviousSession = sessionService.findSessionById(previousExam.getSessionId());
        if (!maybePreviousSession.isPresent()) {
            return Optional.of(new ValidationError(ValidationErrorCode.PREVIOUS_SESSION_NOT_FOUND, "Exam's previous session could not be found"));
        }

        Session previousSession = maybePreviousSession.get();

        //Port of Student.DLL lines 5555-5560
        if (ExamStatusStage.INACTIVE.equals(previousExam.getStatus().getStage())) {
            return Optional.empty();
        }

        /*
        The below code is a straight port of the legacy StudentDLL._CanOpenExistingOpportunity_SP.  It was a little hard to follow the application
        reasons for this logic. StudentDLL lines 5569 - 5589
         */
        boolean daysSinceLastChange = false;
        if (previousExam.getChangedAt() != null) {
            daysSinceLastChange = DAYS.between(Instant.ofEpochMilli(previousExam.getChangedAt().getMillis()), Instant.now()) >= 1;
        }

        if (daysSinceLastChange ||
            LegacyComparer.isEqual(previousSession.getId(), currentSession.getId()) ||
            LegacyComparer.isEqual("closed", previousSession.getStatus()) ||
            LegacyComparer.greaterThan(Instant.now(), convertJodaInstant(previousSession.getDateEnd()))) {
            return Optional.empty();
        }

        //Port of Student.DLL line 5593
        return Optional.of(new ValidationError(
            ValidationErrorCode.CURRENT_EXAM_OPEN,
            configService.getFormattedMessage(currentSession.getClientName(), "_CanOpenTestOpportunity", "Current opportunity is active")
        ));
    }

    private Optional<ValidationError> canCreateNewExam(final String clientName,
                                                       final OpenExamRequest openExamRequest,
                                                       final Exam previousExam,
                                                       final ExternalSessionConfiguration externalSessionConfiguration) {
        //Lines 5610 - 5618 in StudentDLL was not implemented.  The reason is that the max opportunities is always
        //3 via the loader scripts.  So the the conditional in the StudentDLL code will always allow one to open a new
        //Exam if previous exam is null (0 ocnt in the legacy code)

        //Get timelmits for delay days  Line 516-525 OpenTestServiceImpl
        int numberOfDaysToDelay;
        Optional<TimeLimitConfiguration> maybeTimeLimitConfiguration = timeLimitConfigurationService.findTimeLimitConfiguration(clientName, openExamRequest.getAssessmentKey());
        if (maybeTimeLimitConfiguration.isPresent()) {
            numberOfDaysToDelay = maybeTimeLimitConfiguration.get().getExamDelayDays();
        } else {
            throw new IllegalStateException(String.format("Time limit configuration could not be found for client %s or assessment %s", clientName, openExamRequest.getAssessmentKey()));
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
            boolean daysSinceLastExamThreshold = previousExam.getCompletedAt() == null ||
                LegacyComparer.greaterThan(Duration.between(convertJodaInstant(previousExam.getCompletedAt()), Instant.now()).get(DAYS), numberOfDaysToDelay);

            if (LegacyComparer.lessThan(previousExam.getAttempts(), openExamRequest.getMaxAttempts()) &&
                daysSinceLastExamThreshold) {
                return Optional.empty();
            } else if (LegacyComparer.greaterOrEqual(previousExam.getAttempts(), openExamRequest.getMaxAttempts())) {
                return Optional.of(new ValidationError(
                    ValidationErrorCode.MAX_OPPORTUNITY_EXCEEDED,
                    configService.getFormattedMessage(clientName, "_CanOpenTestOpportunity", "All opportunities have been used for this test")
                ));
            } else {
                Instant examCompletedAt = convertJodaInstant(previousExam.getCompletedAt());
                Instant nextAvailableDate = examCompletedAt.plus(numberOfDaysToDelay, ChronoUnit.DAYS);

                return Optional.of(new ValidationError(
                    ValidationErrorCode.NOT_ENOUGH_DAYS_PASSED,
                    configService.getFormattedMessage(clientName, "_CanOpenTestOpportunity", "Your next test opportunity is not yet available.", nextAvailableDate)
                ));
            }
        }

        return Optional.empty();
    }

    private boolean allowsGuestStudent(final String clientName,
                                       final ExternalSessionConfiguration externalSessionConfiguration) {
        if (externalSessionConfiguration.isInSimulationEnvironment()) {
            return true;
        }

        Optional<ClientSystemFlag> maybeAllowGuestAccessFlag = configService.findClientSystemFlag(clientName, ALLOW_ANONYMOUS_STUDENT_FLAG_TYPE);

        return maybeAllowGuestAccessFlag.isPresent() && maybeAllowGuestAccessFlag.get().isEnabled();
    }

    private Exam updateExamWithCustomAccommodations(final Exam exam,
                                                    final List<ExamAccommodation> examAccommodations) {
        //Pulled from CommonDLL lines 2669 - 2670.  If any of the exam accommodations are custom then we need to flag the exam
        Optional<ExamAccommodation> maybeExamAccommodation = examAccommodations.stream()
            .filter(ExamAccommodation::isCustom)
            .findFirst();

        if (maybeExamAccommodation.isPresent() != exam.isCustomAccommodations()) {
            Exam updatedExam = new Exam.Builder()
                .fromExam(exam)
                .withCustomAccommodation(maybeExamAccommodation.isPresent())
                .build();

            examCommandRepository.update(updatedExam);
            return updatedExam;
        }

        return exam;
    }


    private static void mapResponseCountsToExams(Map<UUID, ExpandableExam.Builder> examBuilders, Map<UUID, Integer> itemResponseCounts) {
        itemResponseCounts.forEach((examId, responseCount) -> {
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withItemsResponseCount(responseCount);
        });
    }

    private static void mapExamAccommodationsToExams(Map<UUID, ExpandableExam.Builder> examBuilders, List<ExamAccommodation> examAccommodations) {
        // list exam accoms grouped by the examId
        Map<UUID, List<ExamAccommodation>> sortedAccommodations = examAccommodations.stream()
            .collect(Collectors.groupingBy(ExamAccommodation::getExamId));

        // Assign each sub-list of exam accommodations to their respective exam ids
        sortedAccommodations.forEach((examId, sortedExamAccommodations) -> {
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withExamAccommodations(sortedExamAccommodations);
        });
    }
}