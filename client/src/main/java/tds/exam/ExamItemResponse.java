package tds.exam;

import org.joda.time.Instant;

/**
 * Represents a response to an {@link tds.exam.ExamItem} of an {@link tds.exam.Exam}.
 */
public class ExamItemResponse {
    private long id;
    private long examItemId;
    private String response;
    private boolean valid;
    private Instant createdAt;

    private ExamItemResponse() {
    }

    public ExamItemResponse(Builder builder) {
        this.id = builder.id;
        this.examItemId = builder.examItemId;
        this.response = builder.response;
        this.valid = builder.valid;
        this.createdAt = builder.createdAt;
    }

    public static final class Builder {
        private long id;
        private long examItemId;
        private String response;
        private boolean valid;
        private Instant createdAt;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withExamItemId(long examItemId) {
            this.examItemId = examItemId;
            return this;
        }

        public Builder withResponse(String response) {
            this.response = response;
            return this;
        }

        public Builder withValid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExamItemResponse build() {
            return new ExamItemResponse(this);
        }
    }

    /**
     * @return the id of the {@link ExamItemResponse}
     */
    public long getId() {
        return id;
    }

    /**
     * @return The id of the {@link tds.exam.ExamItem} the {@link ExamItemResponse} corresponds to
     */
    public long getExamItemId() {
        return examItemId;
    }

    /**
     * @return The response submitted by the student
     */
    public String getResponse() {
        return response;
    }

    /**
     * @return True if this response is valid for the {@link tds.exam.ExamItem}'s format; otherwise false
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return The date the exam item response was created at
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
