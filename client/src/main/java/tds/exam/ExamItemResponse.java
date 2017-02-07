package tds.exam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
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
    private boolean selected;
    private ExamItemResponseScore score;
    private Instant createdAt;

    /**
     * Private constructor for frameworks
     */
    private ExamItemResponse() {
    }

    private ExamItemResponse(Builder builder) {
        id = builder.id;
        examItemId = builder.examItemId;
        response = builder.response;
        sequence = builder.sequence;
        valid = builder.valid;
        selected = builder.selected;
        score = builder.score;
        createdAt = builder.createdAt;
    }

    public static final class Builder {
        private long id;
        private UUID examItemId;
        private String response;
        private int sequence;
        private boolean valid;
        private boolean selected;
        private ExamItemResponseScore score;
        private Instant createdAt;

        public Builder fromExamItemResponse(ExamItemResponse examItemResponse) {
            id = examItemResponse.id;
            examItemId = examItemResponse.examItemId;
            response = examItemResponse.response;
            sequence = examItemResponse.sequence;
            valid = examItemResponse.valid;
            selected = examItemResponse.selected;
            score = examItemResponse.score;
            createdAt = examItemResponse.createdAt;
            return this;
        }


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

        public Builder withSelected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public Builder withScore(ExamItemResponseScore score) {
            this.score = score;
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
     * Response sequence is 1-based.  If a student has never responded to an item, there will not be a record in the
     * {@code exam_item_response} table.  In legacy, if a student has never responded to an item, the
     * {@code testeeresponse.responsesequence} value will be 0.
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
     * @return True if this {@link tds.exam.ExamItem} has been selected; otherwise false
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @return The {@link tds.exam.ExamItemResponseScore} for this {@link tds.exam.ExamItemResponse}, if one has been
     * provided
     */
    @JsonIgnore
    public Optional<ExamItemResponseScore> getScore() {
        // NOTE:  Scores should not be exposed outside of the server thus should not be returned to the caller.  There
        // is a debug setting in the legacy Student application that allows for returning the scores to the client.
        // Since we are not porting the debugging behavior to the microservice(s), that feature will not be implemented.
        return Optional.fromNullable(score);
    }

    /**
     * @return The date the exam item response was created at
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamItemResponse)) return false;

        ExamItemResponse that = (ExamItemResponse) o;

        if (getId() != that.getId()) return false;
        if (getSequence() != that.getSequence()) return false;
        if (isValid() != that.isValid()) return false;
        if (isSelected() != that.isSelected()) return false;
        if (!getExamItemId().equals(that.getExamItemId())) return false;
        if (!getResponse().equals(that.getResponse())) return false;
        if (!getScore().equals(that.getScore())) return false;
        return getCreatedAt().equals(that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + getExamItemId().hashCode();
        result = 31 * result + getResponse().hashCode();
        result = 31 * result + getSequence();
        result = 31 * result + (isValid() ? 1 : 0);
        result = 31 * result + (isSelected() ? 1 : 0);

        return result;
    }
}
