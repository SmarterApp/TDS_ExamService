package tds.exam;

import org.joda.time.Instant;

import java.util.UUID;

import static tds.common.util.Preconditions.checkNotNull;

/**
 * Represents a response to an {@link tds.exam.ExamItem} of an {@link tds.exam.Exam}.
 */
public class ExamItemResponse {
    private long id;
    private UUID examItemId;
    private String response;
    private int sequence;
    private boolean valid;
    private Instant createdAt;

    private ExamItemResponse() {
    }

    public ExamItemResponse(Builder builder) {
        id = builder.id;
        examItemId = builder.examItemId;
        response = builder.response;
        sequence = builder.sequence;
        valid = builder.valid;
        createdAt = builder.createdAt;
    }

    public static final class Builder {
        private long id;
        private UUID examItemId;
        private String response;
        private int sequence;
        private boolean valid;
        private Instant createdAt;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withExamItemId(UUID examItemId) {
            this.examItemId = examItemId;
            return this;
        }

        public Builder withResponse(String response) {
            this.response = checkNotNull(response, "Response cannot be null");
            return this;
        }

        public Builder withSequence(int sequence) {
            if (sequence < 1) {
                throw new IllegalArgumentException("Sequence cannot be less than 1");
            }

            this.sequence = sequence;
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
    public UUID getExamItemId() {
        return examItemId;
    }

    /**
     * @return The response submitted by the student
     */
    public String getResponse() {
        return response;
    }

    /**
     * @return The sequence in which the {@link tds.exam.ExamItem} was responded to (e.g. the third item might be
     * responded to first)
     * <p>
     *     Response sequence is 1-based.  If a student has never responded to an item, there will not be a record in the
     *     {@code exam_item_response} table.  In legacy, if a student has never responded to an item, the
     *     {@code testeeresponse.responsesequence} value will be 0.
     * </p>
     */
    public int getSequence() {
        return sequence;
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
