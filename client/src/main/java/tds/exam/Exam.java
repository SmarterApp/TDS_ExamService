package tds.exam;

import org.joda.time.Instant;

import java.util.UUID;

import tds.common.util.Preconditions;

/**
 * Class representing an exam
 */
public class Exam {
    private UUID id;
    private UUID sessionId;
    private UUID browserId;
    private String assessmentId;
    private long studentId;
    private int attempts;
    private int maxItems;
    private ExamStatusCode status;
    private String statusChangeReason;
    private Instant statusChangeDate;
    private String clientName;
    private String subject;
    private Instant dateStarted;
    private Instant dateChanged;
    private Instant dateDeleted;
    private Instant dateScored;
    private Instant dateCompleted;
    private Instant createdAt;
    private Instant expireFrom;
    private String loginSSID;
    private String studentName;
    private Instant dateJoined;
    private String assessmentWindowId;
    private String assessmentAlgorithm;
    private String assessmentKey;
    private String environment;
    private String languageCode;
    private boolean segmented;
    private int abnormalStarts;
    private boolean waitingForSegmentApproval;
    private int currentSegmentPosition;

    public static class Builder {
        private UUID id;
        private UUID sessionId;
        private UUID browserId;
        private String assessmentId;
        private long studentId;
        private int attempts;
        private int maxItems;
        private ExamStatusCode status;
        private String statusChangeReason;
        private Instant statusChangeDate;
        private String clientName;
        private String subject;
        private Instant dateStarted;
        private Instant dateChanged;
        private Instant dateDeleted;
        private Instant createdAt;
        private Instant dateScored;
        private Instant dateCompleted;
        private Instant expireFrom;
        private String loginSSID;
        private String studentName;
        private Instant dateJoined;
        private String assessmentWindowId;
        private String assessmentAlgorithm;
        private String assessmentKey;
        private String environment;
        private String languageCode;
        private boolean segmented;
        private int abnormalStarts;
        private boolean waitingForSegmentApproval;
        private int currentSegmentPosition;


        public Builder withSegmented(boolean segmented) {
            this.segmented = segmented;
            return this;
        }

        public Builder withLoginSSID(String loginSSID) {
            this.loginSSID = loginSSID;
            return this;
        }

        public Builder withStudentName(String studentName) {
            this.studentName = studentName;
            return this;
        }

        public Builder withDateJoined(Instant dateJoined) {
            this.dateJoined = dateJoined;
            return this;
        }

        public Builder withAssessmentWindowId(String windowId) {
            this.assessmentWindowId = windowId;
            return this;
        }

        public Builder withAssessmentAlgorithm(String assessmentAlgorithm) {
            this.assessmentAlgorithm = assessmentAlgorithm;
            return this;
        }

        public Builder withAssessmentKey(String assessmentKey) {
            this.assessmentKey = assessmentKey;
            return this;
        }

        public Builder withEnvironment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder withId(UUID newId) {
            id = newId;
            return this;
        }

        public Builder withSessionId(UUID newSessionId) {
            sessionId = newSessionId;
            return this;
        }

        public Builder withBrowserId(UUID newBrowserId) {
            browserId = newBrowserId;
            return this;
        }

        public Builder withAssessmentId(String newAssessmentId) {
            assessmentId = newAssessmentId;
            return this;
        }

        public Builder withStudentId(long newStudentId) {
            studentId = newStudentId;
            return this;
        }

        public Builder withAttempts(int attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder withMaxItems(int maxItems) {
            this.maxItems = maxItems;
            return this;
        }

        public Builder withStatus(ExamStatusCode newStatus, Instant newStatusChangeDate) {
            Preconditions.checkNotNull(newStatus, "status cannot be null");
            Preconditions.checkNotNull(newStatusChangeDate, "status change date cannot be null");

            status = newStatus;
            statusChangeDate = newStatusChangeDate;
            return this;
        }

        public Builder withStatusChangeReason(String newStatusChangeReason) {
            statusChangeReason = newStatusChangeReason;
            return this;
        }

        public Builder withSubject(String newSubject) {
            subject = newSubject;
            return this;
        }

        public Builder withClientName(String newClientName) {
            clientName = newClientName;
            return this;
        }

        public Builder withDateScored(Instant newDateScored) {
            dateScored = newDateScored;
            return this;
        }

        public Builder withDateStarted(Instant newDateStarted) {
            dateStarted = newDateStarted;
            return this;
        }

        public Builder withDateChanged(Instant newDateChanged) {
            dateChanged = newDateChanged;
            return this;
        }

        public Builder withDateDeleted(Instant newDateDeleted) {
            dateDeleted = newDateDeleted;
            return this;
        }

        public Builder withCreatedAt(Instant newCreatedAt) {
            createdAt = newCreatedAt;
            return this;
        }

        public Builder withDateCompleted(Instant dateCompleted) {
            this.dateCompleted = dateCompleted;
            return this;
        }

        public Builder withExpireFrom(Instant expireFrom) {
            this.expireFrom = expireFrom;
            return this;
        }

        public Builder withAbnormalStarts(int abnormalStarts) {
            this.abnormalStarts = abnormalStarts;
            return this;
        }

        public Builder withWaitingForSegmentApproval(boolean waitingForSegmentApproval) {
            this.waitingForSegmentApproval = waitingForSegmentApproval;
            return this;
        }

        public Builder withCurrentSegmentPosition(int currentSegmentPosition) {
            this.currentSegmentPosition = currentSegmentPosition;
            return this;
        }

        public Builder withLanguageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        public Builder fromExam(Exam exam) {
            id = exam.id;
            sessionId = exam.sessionId;
            browserId = exam.browserId;
            assessmentId = exam.assessmentId;
            studentId = exam.studentId;
            attempts = exam.attempts;
            maxItems = exam.maxItems;
            status = exam.status;
            statusChangeReason = exam.statusChangeReason;
            statusChangeDate = exam.statusChangeDate;
            subject = exam.subject;
            clientName = exam.clientName;
            dateStarted = exam.dateStarted;
            dateChanged = exam.dateChanged;
            dateDeleted = exam.dateDeleted;
            dateScored = exam.dateScored;
            createdAt = exam.createdAt;
            dateCompleted = exam.dateCompleted;
            expireFrom = exam.expireFrom;
            loginSSID = exam.loginSSID;
            studentName = exam.studentName;
            dateJoined = exam.dateJoined;
            assessmentWindowId = exam.assessmentWindowId;
            assessmentAlgorithm = exam.assessmentAlgorithm;
            assessmentKey = exam.assessmentKey;
            environment = exam.environment;
            segmented = exam.segmented;
            abnormalStarts = exam.abnormalStarts;
            waitingForSegmentApproval = exam.waitingForSegmentApproval;
            currentSegmentPosition = exam.currentSegmentPosition;
            languageCode = exam.languageCode;
            return this;
        }

        public Exam build() {
            return new Exam(this);
        }
    }

    private Exam(Builder builder) {
        id = builder.id;
        sessionId = builder.sessionId;
        browserId = builder.browserId;
        assessmentId = builder.assessmentId;
        studentId = builder.studentId;
        attempts = builder.attempts;
        maxItems = builder.maxItems;
        status = builder.status;
        statusChangeReason = builder.statusChangeReason;
        statusChangeDate = builder.statusChangeDate;
        subject = builder.subject;
        clientName = builder.clientName;
        dateStarted = builder.dateStarted;
        dateChanged = builder.dateChanged;
        dateDeleted = builder.dateDeleted;
        dateScored = builder.dateScored;
        createdAt = builder.createdAt;
        dateCompleted = builder.dateCompleted;
        expireFrom = builder.expireFrom;
        loginSSID = builder.loginSSID;
        studentName = builder.studentName;
        dateJoined = builder.dateJoined;
        assessmentWindowId = builder.assessmentWindowId;
        assessmentAlgorithm = builder.assessmentAlgorithm;
        assessmentKey = builder.assessmentKey;
        environment = builder.environment;
        segmented = builder.segmented;
        abnormalStarts = builder.abnormalStarts;
        waitingForSegmentApproval = builder.waitingForSegmentApproval;
        currentSegmentPosition = builder.currentSegmentPosition;
        languageCode = builder.languageCode;
    }

    /**
     * @return The unique identifier of the exam
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return The identifier of the session that hosts the exam
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @return The identifier of the browser of the browser information for thie eaxm
     * <p>
     * "Browser information" refers to IP address, user-agent etc, from another table.
     * </p>
     */
    public UUID getBrowserId() {
        return browserId;
    }

    /**
     * @return The id of the assessment this exam represents
     */
    public String getAssessmentId() {
        return assessmentId;
    }

    /**
     * @return The identifier of the student taking the exam
     */
    public long getStudentId() {
        return studentId;
    }

    /**
     * @return The number of times this exam has been attempted
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * @return The maximum number of items in the exam
     */
    public int getMaxItems() {
        return maxItems;
    }

    /**
     * @return The current status of the exam
     */
    public ExamStatusCode getStatus() {
        return status;
    }

    /**
     * @return Text describing the reason for the most recent status change.
     * <p>
     * Sources for this value include restarting an exam or when a Proctor denies approval to start an exam.
     * </p>
     */
    public String getStatusChangeReason() {
        return statusChangeReason;
    }

    /**
     * @return The date when the {@link tds.exam.Exam}'s status changed.
     */
    public Instant getStatusChangeDate() {
        return statusChangeDate;
    }

    /**
     * @return The client name that owns the exam
     */
    public String getClientName() {
        return clientName;
    }


    /**
     * @return The subject of the exam
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return The date and time when the exam was started
     */
    public Instant getDateStarted() {
        return dateStarted;
    }

    /**
     * @return The most recent date and time at which the exam was changed
     */
    public Instant getDateChanged() {
        return dateChanged;
    }

    /**
     * @return The date and time when the exam was deleted
     */
    public Instant getDateDeleted() {
        return dateDeleted;
    }

    /**
     * @return The date and time when the exam was completed
     */
    public Instant getDateCompleted() {
        return dateCompleted;
    }

    /**
     * @return The date and time when the exam record was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The date and time when the exam was scored
     */
    public Instant getDateScored() {
        return dateScored;
    }

    /**
     * @return The date and time of when the exam expires from.
     */
    public Instant getExpireFrom() {
        return expireFrom;
    }

    /**
     * @return the student key of the student taking the exam
     */
    public String getLoginSSID() {
        return loginSSID;
    }

    /**
     * @return the name of the student taking the exam
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * @return the time when the student joined the session
     */
    public Instant getDateJoined() {
        return dateJoined;
    }

    /**
     * @return the assessment window id
     */
    public String getAssessmentWindowId() {
        return assessmentWindowId;
    }

    /**
     * @return the algorithm associated with the assessment
     */
    public String getAssessmentAlgorithm() {
        return assessmentAlgorithm;
    }

    /**
     * @return the unique key for the assessment
     */
    public String getAssessmentKey() {
        return assessmentKey;
    }

    /**
     * @return the environment.
     */
    public String getEnvironment() {
        return environment;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * @return {@code true} associated assessment is segmented
     */
    public boolean isSegmented() {
        return segmented;
    }

    /**
     * @return the number of times an exam was started under abnormal instances.  For example,
     * restarting an exam that was already in the process of being started.
     */
    public int getAbnormalStarts() {
        return abnormalStarts;
    }

    /**
     * @return {@code true} when approval is required before the student can move onto the next segment
     */
    public boolean isWaitingForSegmentApproval() {
        return waitingForSegmentApproval;
    }

    /**
     * @return the current segment position in multi segmented exams
     */
    public int getCurrentSegmentPosition() {
        return currentSegmentPosition;
    }
}
