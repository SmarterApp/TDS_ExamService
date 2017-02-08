package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.config.TimeLimitConfiguration;
import tds.exam.ExamInfo;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.SessionService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;

@Service
public class ExamApprovalServiceImpl implements ExamApprovalService {
    private final ExamQueryRepository examQueryRepository;
    private final SessionService sessionService;
    private final TimeLimitConfigurationService timeLimitConfigurationService;

    @Autowired
    public ExamApprovalServiceImpl(ExamQueryRepository examQueryRepository,
                                   SessionService sessionService,
                                   TimeLimitConfigurationService timeLimitConfigurationService) {
        this.examQueryRepository = examQueryRepository;
        this.sessionService = sessionService;
        this.timeLimitConfigurationService = timeLimitConfigurationService;
    }

    @Override
    public Response<ExamApproval> getApproval(final ExamInfo examInfo) {
        Exam exam = examQueryRepository.getExamById(examInfo.getExamId())
            .orElseThrow(() -> new IllegalArgumentException(String.format("Exam could not be found for id %s", examInfo.getExamId())));

        Optional<ValidationError> maybeAccessViolation = verifyAccess(examInfo, exam);

        return maybeAccessViolation.isPresent()
            ? new Response<>(maybeAccessViolation.get())
            : new Response<>(new ExamApproval(examInfo.getExamId(), exam.getStatus(), exam.getStatusChangeReason()));
    }

    @Override
    public List<Exam> getExamsPendingApproval(UUID sessionId) {
        return examQueryRepository.getExamsPendingApproval(sessionId);
    }

    @Override
    public Optional<ValidationError> verifyAccess(final ExamInfo examInfo, final Exam exam) {
        // RULE:  The browser key for the approval request must match the browser key of the exam.
        if (!exam.getBrowserId().equals(examInfo.getBrowserId())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH, "Access violation: System access denied"));
        }

        // RULE:  Session id for the approval request must match the session id of the exam.
        if (!exam.getSessionId().equals(examInfo.getSessionId())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH, "The session keys do not match; please consult your test administrator"));
        }

        ExternalSessionConfiguration externalSessionConfig =
            sessionService.findExternalSessionConfigurationByClientName(exam.getClientName())
                .orElseThrow(() -> new IllegalStateException(String.format("External Session Configuration could not be found for client name %s", exam.getClientName())));

        // RULE:  If the environment is set to "simulation" or "development", there is no need to check anything else.
        if (externalSessionConfig.isInSimulationEnvironment()
            || externalSessionConfig.isInDevelopmentEnvironment()) {
            return Optional.empty();
        }

        Session session = sessionService.findSessionById(examInfo.getSessionId())
            .orElseThrow(() -> new IllegalArgumentException("Could not find session for id " + examInfo.getSessionId()));

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
            timeLimitConfigurationService.findTimeLimitConfiguration(exam.getClientName(), exam.getAssessmentId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find time limit configuration for client name %s and assessment id %s", exam.getClientName(), exam.getAssessmentId())));

        // The Proctor application periodically polls to indicate the session is still "alive" and the Proctor is still
        // hosting the session.  If the Proctor does not respond or otherwise maintain this keep-alive/"heartbeat",
        // assume the session has been abandoned, which means the session should be closed.
        Instant sessionDateVisited = Instant.ofEpochMilli(session.getDateVisited().getMillis());
        if (Instant.now().isAfter(sessionDateVisited.plus(timeLimitConfig.getTaCheckinTimeMinutes(), ChronoUnit.MINUTES))) {
            // Legacy code creates an audit record here.  Immutability should provide an audit trail; a new session record
            // will be inserted to represent the change in status.
            sessionService.pause(session.getId(), "closed");
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_TA_CHECKIN_TIMEOUT, "The session is not available for testing, please check with your test administrator."));
        }

        return Optional.empty();
    }
}
