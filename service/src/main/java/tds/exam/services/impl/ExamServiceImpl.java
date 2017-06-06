package tds.exam.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.AssessmentInfo;
import tds.assessment.AssessmentWindow;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.data.legacy.LegacyComparer;
import tds.common.entity.utils.ChangeListener;
import tds.common.web.exceptions.NotFoundException;
import tds.config.ClientSystemFlag;
import tds.config.TimeLimitConfiguration;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamApproval;
import tds.exam.ExamAssessmentMetadata;
import tds.exam.ExamConfiguration;
import tds.exam.ExamInfo;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.ExamineeContext;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamStatusQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamSegmentWrapperService;
import tds.exam.services.ExamService;
import tds.exam.services.ExamineeService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.exam.utils.ExamStatusChangeValidator;
import tds.exam.wrapper.ExamPageWrapper;
import tds.exam.wrapper.ExamSegmentWrapper;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.session.SessionAssessment;
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
import static tds.student.RtsStudentPackageAttribute.BLOCKED_SUBJECT;
import static tds.student.RtsStudentPackageAttribute.ELIGIBLE_ASSESSMENTS;
import static tds.student.RtsStudentPackageAttribute.ENTITY_NAME;
import static tds.student.RtsStudentPackageAttribute.EXTERNAL_ID;

@Service
class ExamServiceImpl implements ExamService {
    private static final int CONTENT_LOAD_TIMEOUT = 120;

    private final ExamQueryRepository examQueryRepository;
    private final ExamCommandRepository examCommandRepository;
    private final ExamPageService examPageService;
    private final SessionService sessionService;
    private final StudentService studentService;
    private final ExamSegmentService examSegmentService;
    private final ExamSegmentWrapperService examSegmentWrapperService;
    private final AssessmentService assessmentService;
    private final TimeLimitConfigurationService timeLimitConfigurationService;
    private final ConfigService configService;
    private final ExamStatusQueryRepository examStatusQueryRepository;
    private final ExamAccommodationService examAccommodationService;
    private final ExamApprovalService examApprovalService;
    private final ExamineeService examineeService;

    // From CommondDLL._IsValidStatusTransition_FN(): a collection of all the statuses that can transition to
    // "paused".  That is, each of these status values has a nested switch statement that contains the "paused"
    // status.
    private static final Set<String> statusesThatCanTransitionToPaused = new HashSet<>(Arrays.asList(ExamStatusCode.STATUS_PAUSED,
        ExamStatusCode.STATUS_PENDING,
        ExamStatusCode.STATUS_SUSPENDED,
        ExamStatusCode.STATUS_STARTED,
        ExamStatusCode.STATUS_APPROVED,
        ExamStatusCode.STATUS_REVIEW,
        ExamStatusCode.STATUS_INITIALIZING));

    private final Collection<ChangeListener<Exam>> examStatusChangeListeners;
    private final Collection<ExamStatusChangeValidator> statusChangeValidators;

    @Autowired
    public ExamServiceImpl(ExamQueryRepository examQueryRepository,
                           SessionService sessionService,
                           StudentService studentService,
                           ExamSegmentService examSegmentService,
                           ExamSegmentWrapperService examSegmentWrapperService,
                           AssessmentService assessmentService,
                           TimeLimitConfigurationService timeLimitConfigurationService,
                           ConfigService configService,
                           ExamCommandRepository examCommandRepository,
                           ExamPageService examPageService,
                           ExamStatusQueryRepository examStatusQueryRepository,
                           ExamAccommodationService examAccommodationService,
                           ExamApprovalService examApprovalService,
                           ExamineeService examineeService,
                           Collection<ChangeListener<Exam>> examStatusChangeListeners,
                           Collection<ExamStatusChangeValidator> statusChangeValidators) {
        this.examQueryRepository = examQueryRepository;
        this.sessionService = sessionService;
        this.studentService = studentService;
        this.examSegmentService = examSegmentService;
        this.examSegmentWrapperService = examSegmentWrapperService;
        this.assessmentService = assessmentService;
        this.timeLimitConfigurationService = timeLimitConfigurationService;
        this.configService = configService;
        this.examCommandRepository = examCommandRepository;
        this.examPageService = examPageService;
        this.examStatusQueryRepository = examStatusQueryRepository;
        this.examAccommodationService = examAccommodationService;
        this.examApprovalService = examApprovalService;
        this.examineeService = examineeService;
        this.examStatusChangeListeners = examStatusChangeListeners;
        this.statusChangeValidators = statusChangeValidators;
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

        if (maybePreviousExam.isPresent()) {
            PreviousExamStatus previousExamStatus = canOpenPreviousExam(maybePreviousExam.get(), currentSession);
            Exam previousExam = maybePreviousExam.get();
            if (previousExamStatus.getValidationError().isPresent()) {
                return new Response<>(previousExamStatus.getValidationError().get());
            } else if(previousExamStatus.isResumable()) {
                return openPreviousExam(currentSession.getClientName(), openExamRequest, previousExam, assessment);
            }
        }

        Exam previousExam = maybePreviousExam.orElse(null);
        Optional<ValidationError> maybeOpenNewExamValidationError = canCreateNewExam(currentSession.getClientName(),
            openExamRequest, previousExam, assessment.getMaxOpportunities());
        if (maybeOpenNewExamValidationError.isPresent()) {
            return new Response<>(maybeOpenNewExamValidationError.get());
        }
        return createExam(currentSession.getClientName(), openExamRequest, currentSession, assessment, externalSessionConfiguration, previousExam, student);
    }

    @Transactional
    @Override
    public Optional<ValidationError> updateExamStatus(final UUID examId, final ExamStatusCode newStatus) {
        return updateExamStatus(examId, newStatus, null);
    }

    @Transactional
    @Override
    public Optional<ValidationError> updateExamStatus(final UUID examId,
                                                      final ExamStatusCode newStatus,
                                                      final String statusChangeReason) {
        return updateExamStatus(examId, newStatus, statusChangeReason, -1);
    }

    private Optional<ValidationError> updateExamStatus(final UUID examId,
                                                       final ExamStatusCode newStatus,
                                                       final String statusChangeReason,
                                                       final int waitingForSegmentPosition) {
        final Exam exam = examQueryRepository.getExamById(examId)
            .orElseThrow(() -> new NotFoundException(String.format("Exam could not be found for id %s", examId)));

        for (ExamStatusChangeValidator validator : statusChangeValidators) {
            Optional<ValidationError> maybeError = validator.validate(exam, newStatus);
            if (maybeError.isPresent()) {
                return maybeError;
            }
        }

        final Exam updatedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(newStatus, org.joda.time.Instant.now())
            .withStatusChangeReason(statusChangeReason)
            .withWaitingForSegmentApprovalPosition(waitingForSegmentPosition)
            .build();

        updateExam(exam, updatedExam);

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
        for (Exam exam : examsInSession) {
            Exam pausedExam = new Exam.Builder().fromExam(exam)
                .withStatus(pausedStatus, org.joda.time.Instant.now())
                .withStatusChangeReason("paused by session")
                .build();

            updateExam(exam, pausedExam);
        }
    }

    @Override
    public Optional<ValidationError> waitForSegmentApproval(final UUID examId, final SegmentApprovalRequest request) {
        Response<ExamApproval> approvalResponse = examApprovalService.getApproval(new ExamInfo(examId, request.getSessionId(), request.getBrowserId()));

        if (approvalResponse.getError().isPresent()) {
            return Optional.of(approvalResponse.getError().get());
        }

        Optional<ValidationError> maybeError;

        /* StudentDLL - T_WaitForSegment_SP lines [1859 - 1864] + 1876*/
        if (request.isEntryApproval()) {
            maybeError = updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_SEGMENT_ENTRY), null,
                request.getSegmentPosition());
        } else {
            maybeError = updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_SEGMENT_EXIT), null,
                request.getSegmentPosition());
        }

        return maybeError;
    }

    @Override
    public Optional<ValidationError> updateExamAccommodationsAndExam(final UUID examId, final ApproveAccommodationsRequest request) {
        /* This method is a port of StudentDLL.T_ApproveAccommodations_SP, starting at line 11429 */
        Optional<Exam> maybeExam = examQueryRepository.getExamById(examId);

        if (!maybeExam.isPresent()) {
            return Optional.of(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("No exam found for id %s", examId)
            ));
        }

        Exam exam = maybeExam.get();

        if (!request.isGuest()) {
            /* StudentDLL line 11441 */
            Optional<ValidationError> maybeError = examApprovalService.verifyAccess(new ExamInfo(examId, request.getSessionId(), exam.getBrowserId()), exam);

            if (maybeError.isPresent()) {
                return maybeError;
            }
        }

        /* StudentDLL lines 11465-11473 */
        Optional<Session> maybeSession = sessionService.findSessionById(exam.getSessionId());

        if (!maybeSession.isPresent()) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_NOT_ENROLLED_IN_SESSION, "The test opportunity is not enrolled in this session"));
        } else if (!maybeSession.get().isProctorless() && request.isGuest()) {
            return Optional.of(new ValidationError(ValidationErrorCode.STUDENT_SELF_APPROVE_UNPROCTORED_SESSION, "Student can only self-approve unproctored sessions"));
        }

        List<ExamAccommodation> updatedAccommodations = examAccommodationService.approveAccommodations(exam, maybeSession.get(), request);
        // Update the "custom" exam flag is a custom exam accommodation is approved
        updateExamWithCustomAccommodations(exam, updatedAccommodations);

        return Optional.empty();
    }

    @Override
    public Response<List<ExamAssessmentMetadata>> findExamAssessmentMetadata(final long studentId, final UUID sessionId, final String grade) {
        /* Port of StudentDLL.T_GetEligibleTests_SP() */
        List<ExamAssessmentMetadata> examAssessmentMetadatas;

        Optional<Session> maybeSession = sessionService.findSessionById(sessionId);

        if (!maybeSession.isPresent()) {
            return new Response<>(new ValidationError(
                ExamStatusCode.STATUS_FAILED, String.format("No session found for session id %s", sessionId)));
        }

        Session session = maybeSession.get();

        if (studentId > 0) {
            examAssessmentMetadatas = findEligibleExamAssessmentsForStudent(studentId, session, grade);
        } else {
            examAssessmentMetadatas = findEligibleExamAssessmentsForGuest(session.getClientName(), grade);
        }

        return new Response<>(examAssessmentMetadatas);
    }

    @Override
    public List<Exam> findAllExamsForStudent(final long studentId) {
        return examQueryRepository.findAllExamsForStudent(studentId);
    }

    private List<ExamAssessmentMetadata> findEligibleExamAssessmentsForGuest(final String clientName, final String grade) {
        final List<ExamAssessmentMetadata> examAssessmentMetadatas = new ArrayList<>();
        List<AssessmentInfo> eligibleAssessments = assessmentService.findAssessmentInfosForGrade(clientName, grade);

        for (AssessmentInfo assessment : eligibleAssessments) {
            examAssessmentMetadatas.add(
                new ExamAssessmentMetadata.Builder()
                    .withAssessmentKey(assessment.getKey())
                    .withAssessmentId(assessment.getId())
                    .withAssessmentLabel(assessment.getLabel())
                    .withMaxAttempts(assessment.getMaxAttempts())
                    .withSubject(assessment.getSubject())
                    .withGrade(grade)
                    .withAttempt(1)
                    .withStatus(ExamStatusCode.STATUS_PENDING)
                    .build()
            );
        }

        return examAssessmentMetadatas;
    }

    @Transactional
    @Override
    public Response<ExamConfiguration> startExam(final UUID examId, final String browserUserAgent) {
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
            Exam initializedExam = initializeExam(exam, assessment, browserUserAgent);
            /* StudentDLL [5367] and TestOppServiceImpl [167] */
            examConfig = initializeDefaultExamConfiguration(initializedExam, assessment, timeLimitConfiguration);
        } else { // Restart or resume the most recent exam
            int resumptions = exam.getResumptions();
            int restartsAndResumptions = exam.getRestartsAndResumptions() + 1; // Increment the restartAndResumption count
            org.joda.time.Instant now = org.joda.time.Instant.now();
            Optional<org.joda.time.Instant> maybeLastActivity = examQueryRepository.findLastStudentActivity(examId);

            /* [178] In the legacy app, if lastActivity = null, then DbComparator.lessThan(null, <not-null>) = false */
            boolean isGracePeriodResume = maybeLastActivity.isPresent()
                && Minutes.minutesBetween(maybeLastActivity.get(), now).getMinutes() < timeLimitConfiguration.getExamRestartWindowMinutes();

            List<ExamSegmentWrapper> examSegmentWrappers = examSegmentWrapperService.findAllExamSegments(examId);
            /* [186 - 191] Move the resume/grace period restart increment and the exam update down in the flow of this code */
            /* Skip TestOpportunityAudit code [193] */
            int startPosition = findExamStartPosition(examSegmentWrappers);
            /* TestOpportunityServiceImpl [204] / StudentDLL [5424]
             * Session type is always 0 (online) in TDS, so skip first conditional on [204]*/
            if (isGracePeriodResume) { // Resume exam
                /* This increment is done in TestOpportunityServiceImpl [179] */
                resumptions++;
            } else if (assessment.shouldDeleteUnansweredItems()) { // Restart exam
                // Mark the exam pages as "deleted"
                examPageService.deletePages(exam.getId());
            }

            Exam restartedExam = new Exam.Builder()
                .fromExam(exam)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), now)
                .withResumptions(resumptions)
                .withRestartsAndResumptions(restartsAndResumptions)
                .withStartedAt(now)
                .withBrowserUserAgent(browserUserAgent)
                .build();

            updateExam(exam, restartedExam);

            updateFinishedPages(examSegmentWrappers, isGracePeriodResume);
            /* Skip restart increment on [209] because we are already incrementing earlier in this method */
            /* [212] No need to call updateUnfinishedResponsePages since we no longer need to keep count of "opportunityrestart" */
            examConfig = getExamConfiguration(exam, assessment, timeLimitConfiguration, startPosition);
        }

        return new Response<>(examConfig);
    }

    /* This method emulates functionality of {@code StudentDLL._UnfinishedResponsePages_SP} @ line 5146 */

    //If all items are answered and not in a grace period resume mark the pages as not visible
    private void updateFinishedPages(final List<ExamSegmentWrapper> examSegmentWrappers, final boolean isGracePeriodResume) {
        if(isGracePeriodResume) {
            return;
        }

        List<ExamPage> examPages = examSegmentWrappers.stream()
            .flatMap(segmentWrapper -> segmentWrapper.getExamPages().stream())
            .filter(pageWrapper ->
                // Find any pages that have items that have not yet been answered.
                pageWrapper.getExamItems().stream()
                    .noneMatch(ExamServiceImpl::isItemUnanswered)
            )
            .map(pageWrapper ->
                ExamPage.Builder
                    .fromExamPage(pageWrapper.getExamPage())
                    .withVisible(false)
                    .build()
            )
            .collect(Collectors.toList());

        if(!examPages.isEmpty()) {
            examPageService.update(examPages.toArray(new ExamPage[examPages.size()]));
        }
    }

    /*
        This method emulated StudentDLL.ResumeItemPosition_FN [5151]
     */
    private int findExamStartPosition(final List<ExamSegmentWrapper> examSegmentWrappers) {
        Optional<ExamItem> maybeResumeExamItem;

        // Find the first segment position where dateExited is not null or that has a satisfied permeable condition
        /* StudentDLL.ResumeItemPosition_FN [5156] */
        maybeResumeExamItem = findExamItemFromResumableSegment(examSegmentWrappers);
        if (maybeResumeExamItem.isPresent()) {
            return maybeResumeExamItem.get().getPosition();
        }

        // flat map all exam pages, items for this assessment/restart number
        Optional<ExamItem> maybeExamItemWithoutResponse = examSegmentWrappers.stream()
            .flatMap(wrapper -> wrapper.getExamPages().stream()
                .filter(pageWrapper -> pageWrapper.getExamPage().isVisible())
                .flatMap(examPage -> examPage.getExamItems().stream()
                    .filter(ExamServiceImpl::isItemUnanswered)
                ))
            .findFirst();

        /* StudentDLL.ResumeItemPosition_FN [5183] */
        // Find the first item in the assessment that is not valid (either because it has no response or because the response is not valid
        if(maybeExamItemWithoutResponse.isPresent()) {
            return maybeExamItemWithoutResponse.get().getPosition();
        }

        /* StudentDLL.ResumeItemPosition_FN [5193] */
        // If all items had a response, find the highest item position regardless of whether it has a response or not
        ExamSegmentWrapper lastSegment = examSegmentWrappers.get(examSegmentWrappers.size() - 1);
        ExamPageWrapper lastPage = lastSegment.getExamPages().get(lastSegment.getExamPages().size() - 1);
        return lastPage.getExamItems().get(lastPage.getExamItems().size() - 1).getPosition();
    }

    private Optional<ExamItem> findExamItemFromResumableSegment(final List<ExamSegmentWrapper> examSegmentWrappers) {
        Optional<ExamSegmentWrapper> maybeResumeExamSegment = examSegmentWrappers.stream()
            .filter(this::isExamSegmentResumable)
            .findFirst();

        if (!maybeResumeExamSegment.isPresent()) {
            return Optional.empty();
        }

        // If there was a segment found with the above query
        ExamSegmentWrapper currentSegment = maybeResumeExamSegment.get();

        // flat map all exam pages, items for this segment/restart number
        List<ExamItem> examItemsInSegment = currentSegment.getExamPages().stream()
            .filter(examPageWrapper -> examPageWrapper.getExamPage().isVisible())
            .flatMap(pageWrapper -> pageWrapper.getExamItems().stream())
            .collect(Collectors.toList());

        if (examItemsInSegment.isEmpty()) {
            return Optional.empty();
        }

        /* StudentDLL.ResumeItemPosition_FN [5164] */
        // Find the first item in this segment that is not valid (either because it has no response or because the response is not valid
        Optional<ExamItem> maybeExamItem = examItemsInSegment.stream()
            .filter(ExamServiceImpl::isItemUnanswered)
            .findFirst();

        if (maybeExamItem.isPresent()) {
            return maybeExamItem;
        }

        /* StudentDLL.ResumeItemPosition_FN [5174] */
        // If all items had a response, find the highest item position regardless of whether it has a response or not
        return Optional.of(examItemsInSegment.get(examItemsInSegment.size() - 1));
    }

    private boolean isExamSegmentResumable(final ExamSegmentWrapper examSegment) {
        return examSegment.getExamSegment().getExitedAt() == null
            || (examSegment.getExamSegment().isPermeable()
            && examSegment.getExamSegment().getRestorePermeableCondition() != null);
    }

    private static boolean isItemUnanswered(final ExamItem examItem) {
        return !examItem.getResponse().isPresent()
            || !examItem.getResponse().get().isValid();
    }

    private Exam initializeExam(final Exam exam, final Assessment assessment, final String browserUserAgent) {
        /* TestOpportunityServiceImpl [435] + [470] */
        int testLength = examSegmentService.initializeExamSegments(exam, assessment);
        org.joda.time.Instant now = org.joda.time.Instant.now();
        /* TODO: Skipping [451-469] until Simulation mode is investigated and implemented
         * The testoppabilityestimate table written to here is only read from in SimDLL.java  */
        Exam initializedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), org.joda.time.Instant.now())
            .withStartedAt(now)
            .withExpiresAt(now)
            .withRestartsAndResumptions(0)
            .withMaxItems(testLength)
            .withWaitingForSegmentApprovalPosition(-1)
            .withBrowserUserAgent(browserUserAgent)
            .build();

        updateExam(exam, initializedExam);

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

        String studentAccommodations = openExamRequest.getGuestAccommodations();
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
                } else if (StringUtils.isEmpty(studentAccommodations) && ACCOMMODATIONS.equals(attribute.getName())) {
                    studentAccommodations = attribute.getValue();
                }
            }
        }

        boolean guestStudent = openExamRequest.getStudentId() < 0;
        //OpenTestServiceImpl lines 317 - 341
        List<AssessmentWindow> assessmentWindows = assessmentService.findAssessmentWindows(
            clientName,
            assessment.getAssessmentId(),
            guestStudent,
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
            .withWaitingForSegmentApprovalPosition(1)
            .withMultiStageBraille(assessment.isMultiStageBraille())
            .build();

        examCommandRepository.insert(exam);

        // OpenTestServiceImpl lines 409 - 410
        if (!openExamRequest.isGuestStudent()) {
            examineeService.insertAttributesAndRelationships(exam, student, ExamineeContext.INITIAL);
        }

        //Lines 412 - 421 OpenTestServiceImpl is not implemented.  After talking with data warehouse and Smarter Balanced
        //The initial student attributes are not used and smarter balance suggested removing them
        List<ExamAccommodation> examAccommodations = examAccommodationService.initializeExamAccommodations(exam, studentAccommodations);

        exam = updateExamWithCustomAccommodations(exam, examAccommodations);

        //Lines OpenTestServiceImpl lines 428-447 not implemented.  Instead exam status is set during insert instead of inserting
        //and then updating status after accommodations

        return new Response<>(exam);
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
        ExamStatusCode status = previousExam.getStartedAt() == null
            ? examStatusQueryRepository.findExamStatusCode(STATUS_PENDING)
            : examStatusQueryRepository.findExamStatusCode(STATUS_SUSPENDED);

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
            .withAbnormalStarts(previousExam.getAbnormalStarts() + abnormalIncrement)
            .build();

        updateExam(previousExam, currentExam);

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

    private PreviousExamStatus canOpenPreviousExam(final Exam previousExam, final Session currentSession) {
        //Port of Student.DLL lines 5526-5530
        if (ExamStatusStage.CLOSED.equals(previousExam.getStatus().getStage())) {
            return new PreviousExamStatus(false);
        }

        //Port of Student.DLL lines 5531-5551
        Optional<Session> maybePreviousSession = sessionService.findSessionById(previousExam.getSessionId());
        if (!maybePreviousSession.isPresent()) {
            throw new IllegalStateException(String.format("Could not find previous session %s for exam %s", previousExam.getSessionId(), previousExam.getId()));
        }

        Session previousSession = maybePreviousSession.get();

        //Port of Student.DLL lines 5555-5560
        if (ExamStatusStage.INACTIVE.equals(previousExam.getStatus().getStage())) {
            return new PreviousExamStatus(true);
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
            return new PreviousExamStatus(true);
        }

        //Port of Student.DLL line 5593
        return new PreviousExamStatus(new ValidationError(
            ValidationErrorCode.CURRENT_EXAM_OPEN,
            configService.getFormattedMessage(currentSession.getClientName(), "_CanOpenTestOpportunity", "Current opportunity is active")
        ));
    }

    private Optional<ValidationError> canCreateNewExam(final String clientName,
                                                       final OpenExamRequest openExamRequest,
                                                       final Exam previousExam,
                                                       final int maxAttempts) {
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

            //Verifies that the new exam does not exceed attempts and there are enough days since the last attempt
            boolean daysSinceLastExamThreshold = previousExam.getCompletedAt() == null ||
                LegacyComparer.greaterThan(Duration.between(convertJodaInstant(previousExam.getCompletedAt()), Instant.now()).toDays(), numberOfDaysToDelay);

            if (LegacyComparer.lessThan(previousExam.getAttempts(), maxAttempts) &&
                daysSinceLastExamThreshold) {
                return Optional.empty();
            } else if (LegacyComparer.greaterOrEqual(previousExam.getAttempts(), maxAttempts)) {
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
        Optional<ExamAccommodation> maybeCustomExamAccommodation = examAccommodations.stream()
            .filter(ExamAccommodation::isCustom)
            .findFirst();

        if (maybeCustomExamAccommodation.isPresent() != exam.isCustomAccommodations()) {
            Exam updatedExam = new Exam.Builder()
                .fromExam(exam)
                .withCustomAccommodation(maybeCustomExamAccommodation.isPresent())
                .build();

            updateExam(exam, updatedExam);
            return updatedExam;
        }

        return exam;
    }

    /**
     * Perform the update to the new {@link tds.exam.Exam} then execute the
     * {@link tds.common.entity.utils.ChangeListener}s to apply any rules/business logic as a result of the update.
     *
     * @param exam        The {@link tds.exam.Exam} in its original state
     * @param updatedExam The {@link tds.exam.Exam} with new values to persist
     */
    private void updateExam(final Exam exam, final Exam updatedExam) {
        examCommandRepository.update(updatedExam);

        examStatusChangeListeners.forEach(listener -> listener.accept(exam, updatedExam));
    }

    private List<ExamAssessmentMetadata> findEligibleExamAssessmentsForStudent(final long studentId, final Session session, final String grade) {
        final String clientName = session.getClientName();
        List<ExamAssessmentMetadata> examAssessmentMetadatas = new ArrayList<>();
        Set<String> sessionAssessmentIds = sessionService.findSessionAssessments(session.getId()).stream()
            .map(SessionAssessment::getAssessmentId)
            .collect(Collectors.toSet());
        /* StudentDLL - 10439 and 10535 - get both student package attributes at once */
        List<RtsStudentPackageAttribute> attributes = studentService.findStudentPackageAttributes(studentId, clientName, ELIGIBLE_ASSESSMENTS, BLOCKED_SUBJECT);
        /* StudentDLL.java - 10451 - for _GetCurrentTests, we need assessment metadata for each eligible assessment */
        List<AssessmentInfo> eligibleAssessments = assessmentService.findAssessmentInfosForAssessments(clientName,
            getEligibleAssessmentKeysFromAttributes(attributes));

        /* StudentDLL [10520] - _GetOpportunityInfo - Get the exam and session data for all exams taken by this student */
        List<Exam> studentExams = examQueryRepository.findAllExamsForStudent(studentId);
        List<UUID> examSessionIds = studentExams.stream().map(Exam::getSessionId).collect(Collectors.toList());
        // SessionId -> Session for quick lookup - these are the sessions associated with each exam
        Map<UUID, Session> examSessions = sessionService.findSessionsByIds(examSessionIds)
            .stream().collect(Collectors.toMap(Session::getId, Function.identity()));
        // A map from assessmentKey -> all exams taken for that exam by this student
        Map<String, List<Exam>> assessmentExams = studentExams.stream().collect(Collectors.groupingBy(Exam::getAssessmentKey));

        for (AssessmentInfo assessment : eligibleAssessments) {
            int currentAttempt = 0;
            String deniedReason = null;
            String status = null;
            Optional<Exam> maybeMostRecentExam = getMostRecentExamForAssessment(assessment.getKey(), assessmentExams);
            boolean isNewOpportunity = false;

            if (!sessionAssessmentIds.contains(assessment.getId())) {
                deniedReason = configService.getFormattedMessage(clientName, "_CanOpenTestOpportunity",
                    "Test not available for this session.");
                status = ExamStatusCode.STATUS_DENIED;
            } else if (!ExamStatusCode.STATUS_DENIED.equals(status)) {
                /* [10549-10561] - check if the subject is blocked according to RTS attributes */
                if (isSubjectBlocked(attributes, assessment.getSubject())) {
                    deniedReason = configService.getFormattedMessage(clientName, "_CanOpenTestOpportunity",
                        "This test is administratively blocked. Please check with your test administrator.");
                    status = ExamStatusCode.STATUS_DENIED;
                } else if (maybeMostRecentExam.isPresent()) {
                    Exam recentExam = maybeMostRecentExam.get();
                    PreviousExamStatus previousExamStatus = canOpenPreviousExam(recentExam, session);

                    if (previousExamStatus.getValidationError().isPresent()) {
                        deniedReason = previousExamStatus.getValidationError().get().getMessage();
                    } else if (previousExamStatus.isResumable()) {
                        currentAttempt = recentExam.getAttempts();
                    } else { // If this branch is hit, we can open the existing exam
                        OpenExamRequest request = new OpenExamRequest.Builder()
                            .withStudentId(studentId)
                            .withAssessmentKey(assessment.getKey())
                            .withSessionId(session.getId())
                            .withBrowserId(recentExam.getBrowserId())
                            .build();

                        Optional<ValidationError> maybeError = canCreateNewExam(clientName, request, recentExam, assessment.getMaxAttempts());
                        isNewOpportunity = !maybeError.isPresent();
                        if (maybeError.isPresent()) {
                            status = ExamStatusCode.STATUS_DENIED;
                            deniedReason = maybeError.get().getMessage();
                        }
                        currentAttempt = isNewOpportunity
                            ? recentExam.getAttempts() + 1
                            : recentExam.getAttempts();
                    }
                } else { // If theres no previous exam, only need to validate the opportunity count
                        /* StudentDLL Lines [10649-10660] */
                    if (assessment.getMaxAttempts() <= 0) {
                        deniedReason = configService.getFormattedMessage(clientName, "_CanOpenTestOpportunity",
                            "No opportunities are available for this test");
                    } else {
                        isNewOpportunity = true;
                        currentAttempt = 1;
                    }
                }
            }

            /* Lines 10615-10622 */
            if (status == null) {
                if (currentAttempt == 0) {
                    status = ExamStatusCode.STATUS_DENIED;
                } else if (!isNewOpportunity) {
                    status = ExamStatusCode.STATUS_SUSPENDED;
                } else {
                    status = ExamStatusCode.STATUS_PENDING;
                }
            }

            examAssessmentMetadatas.add(
                new ExamAssessmentMetadata.Builder()
                    .withAssessmentKey(assessment.getKey())
                    .withAssessmentId(assessment.getId())
                    .withAssessmentLabel(assessment.getLabel())
                    .withMaxAttempts(assessment.getMaxAttempts())
                    .withSubject(assessment.getSubject())
                    .withGrade(grade)
                    .withAttempt(currentAttempt)
                    .withStatus(status)
                    .withDeniedReason(deniedReason)
                    .build()
            );
        }

        return examAssessmentMetadatas;
    }

    private Optional<Exam> getMostRecentExamForAssessment(final String assessmentKey, final Map<String, List<Exam>> assessmentExams) {
        if (!assessmentExams.containsKey(assessmentKey)) {
            return Optional.empty();
        }

        return assessmentExams.get(assessmentKey).stream()
            .sorted(Comparator.comparing(Exam::getCreatedAt).reversed())
            .findFirst();
    }

    private static String[] getEligibleAssessmentKeysFromAttributes(final List<RtsStudentPackageAttribute> attributes) {
        final Optional<String[]> eligibleAssessments = attributes.stream()
            .filter(attribute -> ELIGIBLE_ASSESSMENTS.equals(attribute.getName()))
            .map(RtsStudentPackageAttribute::getValue)
            .map(assessments -> assessments.split(";"))
            .findFirst();

        return eligibleAssessments.orElseGet(() -> new String[0]);

    }

    private static boolean isSubjectBlocked(final List<RtsStudentPackageAttribute> blockedSubjectsAttributes, final String subject) {
        if (blockedSubjectsAttributes == null || blockedSubjectsAttributes.isEmpty()) {
            return false;
        }

        final Optional<Boolean> maybeBlockedSubject = blockedSubjectsAttributes.stream()
            .filter(attribute -> BLOCKED_SUBJECT.equals(attribute.getName()))
            .map(RtsStudentPackageAttribute::getValue)
            .map(blockedSubject -> blockedSubject.contains(subject))
            .findFirst();

        return maybeBlockedSubject.orElse(false);
    }

    private static class PreviousExamStatus {
        private final boolean resumable;
        private final ValidationError validationError;

        PreviousExamStatus(final boolean resumable) {
            this.resumable = resumable;
            validationError = null;
        }

        PreviousExamStatus(final ValidationError validationError) {
            this.validationError = validationError;
            resumable = false;
        }

        boolean isResumable() {
            return resumable;
        }

        Optional<ValidationError> getValidationError() {
            return Optional.ofNullable(validationError);
        }
    }
}