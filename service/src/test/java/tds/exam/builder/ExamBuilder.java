package tds.exam.builder;

import org.joda.time.Instant;

import java.util.UUID;

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
    private String clientName = "clientName";
    private Instant dateDeleted = null;
    private Instant dateScored = null;
    private Instant dateChanged = null;
    private Instant dateStarted = null;
    private Instant dateCompleted = null;
    private ExamStatusCode status = new ExamStatusCode(STATUS_PENDING, ExamStatusStage.INUSE);
    private String subject = "ELA";
    private String studentKey = "ADV001";
    private String studentName = "Darth";
    private Instant dateJoined = null;
    private String assessmentWindowId = "ANNUAL";
    private String assessmentAlgorithm = "fixedForm";
    private String assessmentKey = "(SBAC_PT)SBAC-IRP-CAT-ELA-3-Summer-2015-2016";
    private String environment = "Development";
    private boolean segmented = false;
    private int abnormalStarts = 1;
    private boolean waitingForSegmentApproval = false;
    private int currentSegmentPosition = 1;

    public Exam build() {
        return new Exam.Builder()
            .withId(examId)
            .withSessionId(sessionId)
            .withBrowserId(browserId)
            .withAssessmentId(assessmentId)
            .withStudentId(studentId)
            .withAttempts(attempts)
            .withClientName(clientName)
            .withDateDeleted(dateDeleted)
            .withDateScored(dateScored)
            .withDateChanged(dateChanged)
            .withDateStarted(dateStarted)
            .withDateCompleted(dateCompleted)
            .withStatus(status)
            .withSubject(subject)
            .withLoginSSID(studentKey)
            .withDateJoined(dateJoined)
            .withAssessmentWindowId(assessmentWindowId)
            .withStudentName(studentName)
            .withAssessmentAlgorithm(assessmentAlgorithm)
            .withAssessmentKey(assessmentKey)
            .withEnvironment(environment)
            .withSegmented(segmented)
            .withAbnormalStarts(abnormalStarts)
            .withWaitingForSegmentApproval(waitingForSegmentApproval)
            .withCurrentSegmentPosition(currentSegmentPosition)
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

    public ExamBuilder withDateJoined(Instant dateJoined) {
        this.dateJoined = dateJoined;
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

    public ExamBuilder withDateDeleted(Instant dateDeleted) {
        this.dateDeleted = dateDeleted;
        return this;
    }

    public ExamBuilder withDateScored(Instant dateScored) {
        this.dateScored = dateScored;
        return this;
    }

    public ExamBuilder withDateChanged(Instant dateChanged) {
        this.dateChanged = dateChanged;
        return this;
    }

    public ExamBuilder withDateStarted(Instant dateStarted) {
        this.dateStarted = dateStarted;
        return this;
    }

    public ExamBuilder withDateCompleted(Instant dateCompleted) {
        this.dateCompleted = dateCompleted;
        return this;
    }

    public ExamBuilder withStatus(ExamStatusCode status) {
        this.status = status;
        return this;
    }

    public ExamBuilder withSubject(String subject) {
        this.subject = subject;
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
}
