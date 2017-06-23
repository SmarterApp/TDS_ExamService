/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam;

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
    private UUID examId;
    private String response;
    private int sequence;
    private boolean valid;
    private boolean selected;
    private ExamItemResponseScore score;
    private Instant createdAt;
    private boolean markedForReview;

    /**
     * Private constructor for frameworks
     */
    private ExamItemResponse() {
    }

    private ExamItemResponse(Builder builder) {
        id = builder.id;
        examItemId = checkNotNull(builder.examItemId);
        examId = checkNotNull(builder.examId);
        response = checkNotNull(builder.response);
        sequence = builder.sequence;
        valid = builder.valid;
        selected = builder.selected;
        score = builder.score;
        createdAt = builder.createdAt;
        markedForReview = builder.markedForReview;
    }

    public static final class Builder {
        private long id;
        private UUID examItemId;
        private UUID examId;
        private String response;
        private int sequence;
        private boolean valid;
        private boolean selected;
        private ExamItemResponseScore score;
        private Instant createdAt;
        private boolean markedForReview;

        public static Builder fromExamItemResponse(final ExamItemResponse examItemResponse) {
            return new Builder()
                .withId(examItemResponse.id)
                .withExamItemId(examItemResponse.examItemId)
                .withExamId(examItemResponse.examId)
                .withResponse(examItemResponse.response)
                .withSequence(examItemResponse.sequence)
                .withValid(examItemResponse.valid)
                .withSelected(examItemResponse.selected)
                .withScore(examItemResponse.score)
                .withMarkedForReview(examItemResponse.markedForReview)
                .withCreatedAt(examItemResponse.createdAt);
        }


        public Builder withId(final long id) {
            this.id = id;
            return this;
        }

        public Builder withExamItemId(final UUID examItemId) {
            this.examItemId = examItemId;
            return this;
        }

        public Builder withExamId(final UUID examId) {
            this.examId = examId;
            return this;
        }

        public Builder withResponse(final String response) {
            this.response = checkNotNull(response, "Response cannot be null");
            return this;
        }

        public Builder withSequence(final int sequence) {
            if (sequence < 1) {
                throw new IllegalArgumentException("Sequence cannot be less than 1");
            }

            this.sequence = sequence;
            return this;
        }

        public Builder withValid(final boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder withSelected(final boolean selected) {
            this.selected = selected;
            return this;
        }

        public Builder withScore(final ExamItemResponseScore score) {
            this.score = score;
            return this;
        }

        public Builder withCreatedAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withMarkedForReview(final boolean markedForReview) {
            this.markedForReview = markedForReview;
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
     * @return The id of the {@link tds.exam.Exam} this {@link tds.exam.ExamItemResponse} corresponds to
     */
    public UUID getExamId() {
        return examId;
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
    public Optional<ExamItemResponseScore> getScore() {
        return Optional.fromNullable(score);
    }

    /**
     * @return The date the exam item response was created at
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return Flag indicating whether the item is marked for review
     */
    public boolean isMarkedForReview() {
        return markedForReview;
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
        if (isMarkedForReview() != that.isMarkedForReview()) return false;
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
        result = 31 * result + (isMarkedForReview() ? 1 : 0);

        return result;
    }
}
