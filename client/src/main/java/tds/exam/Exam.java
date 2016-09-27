package tds.exam;

import java.time.Instant;
import java.util.UUID;

/**
 * Class representing an exam
 */
public class Exam {
    private UUID id;
    private UUID sessionId;
    private String assessmentId;
    private long studentId;
    private int attempts;
    private ExamStatusCode status;
    private String clientName;
    private Instant dateStarted;
    private Instant dateChanged;
    private Instant dateDeleted;
    private Instant dateCompleted;
    private Instant createdAt;

    public static class Builder {
        private UUID id;
        private UUID sessionId;
        private String assessmentId;
        private long studentId;
        private int attempts;
        private ExamStatusCode status = new ExamStatusCode.Builder().build();
        private String clientName;
        private Instant dateStarted;
        private Instant dateChanged;
        private Instant dateDeleted;
        private Instant createdAt;
        private Instant dateCompleted;

        public Builder withId(UUID newId) {
            id = newId;
            return this;
        }

        public Builder withSessionId(UUID newSessionId) {
            sessionId = newSessionId;
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

        public Builder withClientName(String newClientName) {
            clientName = newClientName;
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
        assessmentId = builder.assessmentId;
        studentId = builder.studentId;
        attempts = builder.attempts;
        status = builder.status;
        clientName = builder.clientName;
        dateStarted = builder.dateStarted;
        dateChanged = builder.dateChanged;
        dateDeleted = builder.dateDeleted;
        createdAt = builder.createdAt;
        dateCompleted = builder.dateCompleted;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public long getStudentId() {
        return studentId;
    }

    public int getAttempts() {
        return attempts;
    }

    public ExamStatusCode getStatus() {
        return status;
    }

    public String getClientName() {
        return clientName;
    }

    public Instant getDateStarted() {
        return dateStarted;
    }

    public Instant getDateChanged() {
        return dateChanged;
    }

    public Instant getDateDeleted() {
        return dateDeleted;
    }

    public Instant getDateCompleted() {
        return dateCompleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
