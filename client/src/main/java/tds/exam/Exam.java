package tds.exam;

import org.joda.time.Instant;

import java.util.UUID;

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
    private ExamStatusCode status;
    private String statusChangeReason;
    private String clientName;
    private String subject;
    private Instant dateStarted;
    private Instant dateChanged;
    private Instant dateDeleted;
    private Instant dateScored;
    private Instant dateCompleted;
    private Instant createdAt;

    public static class Builder {
        private UUID id;
        private UUID sessionId;
        private UUID browserId;
        private String assessmentId;
        private long studentId;
        private int attempts;
        private ExamStatusCode status = new ExamStatusCode.Builder().build();
        private String statusChangeReason;
        private String clientName;
        private String subject;
        private Instant dateStarted;
        private Instant dateChanged;
        private Instant dateDeleted;
        private Instant createdAt;
        private Instant dateScored;
        private Instant dateCompleted;

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

        public Builder withStatus(ExamStatusCode newStatus){
            status = newStatus;
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
        status = builder.status;
        statusChangeReason = builder.statusChangeReason;
        subject = builder.subject;
        clientName = builder.clientName;
        dateStarted = builder.dateStarted;
        dateChanged = builder.dateChanged;
        dateDeleted = builder.dateDeleted;
        dateScored = builder.dateScored;
        createdAt = builder.createdAt;
        dateCompleted = builder.dateCompleted;
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
     *     "Browser information" refers to IP address, user-agent etc, from another table.
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
     * @return The current status of the exam
     */
    public ExamStatusCode getStatus() {
        return status;
    }

    /**
     * @return Text describing the reason for the most recent status change.
     * <p>
     *     Sources for this value include restarting an exam or when a Proctor denies approval to start an exam.
     * </p>
     */
    public String getStatusChangeReason() {
        return statusChangeReason;
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
}
