package tds.exam.builder;

import org.joda.time.Instant;

import java.util.UUID;

import tds.common.util.Preconditions;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;

import static tds.exam.ExamStatusCode.STATUS_PENDING;

/**
 * Build an {@link Exam} populated with test data.
 */
public class ExamBuilder {
    private UUID examId = UUID.randomUUID();
    private UUID sessionId = UUID.randomUUID();
    private UUID browserId = UUID.randomUUID();
    private String assessmentId = "assessmentId";
    private long studentId = 1L;
    private int attempts = 0;
    private int maxItems = 6;
    private String clientName = "clientName";
    private Instant deletedAt = null;
    private Instant scoredAt = null;
    private Instant changedAt = null;
    private Instant startedAt = null;
    private Instant completedAt = null;
    private Instant expiresAt = null;
    private ExamStatusCode status = new ExamStatusCode(STATUS_PENDING);
    private Instant statusChangeDate = Instant.now();
    private String subject = "ELA";
    private String studentKey = "ADV001";
    private String studentName = "Darth";
    private Instant joinedAt = null;
    private String assessmentWindowId = "ANNUAL";
    private String assessmentAlgorithm = "fixedForm";
    private String assessmentKey = "(SBAC_PT)SBAC-IRP-CAT-ELA-3-Summer-2015-2016";
    private String environment = "Development";
    private String languageCode = "ENU";
    private boolean segmented = false;
    private int abnormalStarts = 1;
    private boolean waitingForSegmentApproval = false;
    private int currentSegmentPosition = 1;
    private boolean customAccommodations = true;
    private String language = "English";
    private int resumptions = 5;
    private int restartsAndResumptions = 7;

    public Exam build() {
        return new Exam.Builder()
            .withId(examId)
            .withSessionId(sessionId)
            .withBrowserId(browserId)
            .withAssessmentId(assessmentId)
            .withStudentId(studentId)
            .withAttempts(attempts)
            .withClientName(clientName)
            .withDeletedAt(deletedAt)
            .withScoredAt(scoredAt)
            .withChangedAt(changedAt)
            .withStartedAt(startedAt)
            .withCompletedAt(completedAt)
            .withStatus(status, statusChangeDate)
            .withSubject(subject)
            .withLoginSSID(studentKey)
            .withJoinedAt(joinedAt)
            .withMaxItems(maxItems)
            .withAssessmentWindowId(assessmentWindowId)
            .withStudentName(studentName)
            .withAssessmentAlgorithm(assessmentAlgorithm)
            .withAssessmentKey(assessmentKey)
            .withEnvironment(environment)
            .withSegmented(segmented)
            .withAbnormalStarts(abnormalStarts)
            .withLanguageCode(languageCode)
            .withExpiresAt(expiresAt)
            .withWaitingForSegmentApproval(waitingForSegmentApproval)
            .withCurrentSegmentPosition(currentSegmentPosition)
            .withCustomAccommodation(customAccommodations)
            .withResumptions(resumptions)
            .withRestartsAndResumptions(restartsAndResumptions)
            .build();
    }

    public ExamBuilder withStudentKey(String studentKey) {
        this.studentKey = studentKey;
        return this;
    }

    public ExamBuilder withStudentName(String studentName) {
        this.studentName = studentName;
        return this;
    }

    public ExamBuilder withJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
        return this;
    }

    public ExamBuilder withAssessmentWindowId(String assessmentWindowId) {
        this.assessmentWindowId = assessmentWindowId;
        return this;
    }

    public ExamBuilder withAssessmentAlgorithm(String assessmentAlgorithm) {
        this.assessmentAlgorithm = assessmentAlgorithm;
        return this;
    }

    public ExamBuilder withAssessmentKey(String assessmentKey) {
        this.assessmentKey = assessmentKey;
        return this;
    }

    public ExamBuilder withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public ExamBuilder withSegmented(boolean segmented) {
        this.segmented = segmented;
        return this;
    }

    public ExamBuilder withId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public ExamBuilder withSessionId(UUID sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ExamBuilder withBrowserId(UUID browserId) {
        this.browserId = browserId;
        return this;
    }

    public ExamBuilder withAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
        return this;
    }

    public ExamBuilder withStudentId(long studentId) {
        this.studentId = studentId;
        return this;
    }

    public ExamBuilder withAttempts(int attempts) {
        this.attempts = attempts;
        return this;
    }

    public ExamBuilder withClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public ExamBuilder withDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public ExamBuilder withScoredAt(Instant scoredAt) {
        this.scoredAt = scoredAt;
        return this;
    }

    public ExamBuilder withChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
        return this;
    }

    public ExamBuilder withStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
        return this;
    }

    public ExamBuilder withCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    public ExamBuilder withExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public ExamBuilder withStatus(ExamStatusCode status, Instant statusChangeDate) {
        Preconditions.checkNotNull(status, "status cannot be null");
        Preconditions.checkNotNull(statusChangeDate, "status change date cannot be null");

        this.status = status;
        this.statusChangeDate = statusChangeDate;
        return this;
    }

    public ExamBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public ExamBuilder withLanguageCode(String languageCode) {
        this.languageCode = languageCode;
        return this;
    }

    public ExamBuilder withAbnormalStarts(int abnormalStarts) {
        this.abnormalStarts = abnormalStarts;
        return this;
    }

    public ExamBuilder withWaitingForSegmentApproval(boolean waitingForSegmentApproval) {
        this.waitingForSegmentApproval = waitingForSegmentApproval;
        return this;
    }

    public ExamBuilder withCurrentSegmentPosition(int currentSegmentPosition) {
        this.currentSegmentPosition = currentSegmentPosition;
        return this;
    }

    public ExamBuilder withCustomAccommodations(boolean customAccommodations) {
        this.customAccommodations = customAccommodations;
        return this;
    }

    public ExamBuilder withLanguage(String language) {
        this.language = language;
        return this;
    }

    public ExamBuilder withResumptions(int resumptions) {
        this.resumptions = resumptions;
        return this;
    }

    public ExamBuilder withRestartsAndResumptions(int restartsAndResumptions) {
        this.restartsAndResumptions = restartsAndResumptions;
        return this;
    }

    public ExamBuilder withMaxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

}
