package tds.exam;

import java.time.Instant;
import java.util.UUID;

/**
 * An accommodation that is approved for use during an {@link Exam}.
 */
public class ExamAccommodation {
    private long id;
    private UUID examId;
    private String segmentId;
    private String type;
    private String code;
    private String description;
    private Instant deniedAt;
    private Instant createdAt;

    public static class Builder {
        private long id;
        private UUID examId;
        private String segmentId;
        private String type;
        private String code;
        private String description;
        private Instant deniedAt;
        private Instant createdAt;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withExamId(UUID examId) {
            if (examId == null) {
                throw new IllegalArgumentException("examId cannot be null");
            }

            this.examId = examId;
            return this;
        }

        public Builder withSegmentId(String segmentId) {
            if (segmentId == null) {
                throw new IllegalArgumentException("segmentId cannot be null");
            }

            this.segmentId = segmentId;
            return this;
        }

        public Builder withType(String type) {
            if (type == null) {
                throw new IllegalArgumentException("type cannot be null");
            }

            this.type = type;
            return this;
        }

        public Builder withCode(String code) {
            if (code == null) {
                throw new IllegalArgumentException("code cannot be null");
            }

            this.code = code;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withDeniedAt(Instant deniedAt) {
            this.deniedAt = deniedAt;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            if (createdAt == null) {
                throw new IllegalArgumentException("createdAt cannot be null");
            }
            this.createdAt = createdAt;
            return this;
        }

        public ExamAccommodation build() {
            return new ExamAccommodation(this);
        }
    }

    private ExamAccommodation(Builder builder) {
        id = builder.id;
        examId = builder.examId;
        segmentId = builder.segmentId;
        type = builder.type;
        code = builder.code;
        description = builder.description;
        deniedAt = builder.deniedAt;
        createdAt = builder.createdAt;
    }

    /**
     * @return The unique identifier of the {@link ExamAccommodation} record
     */
    public long getId() {
        return id;
    }

    /**
     * @return The id of the {@link Exam} to which this {@link ExamAccommodation} belongs
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The segment of the Assessment in which this {@link ExamAccommodation} can be used
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * @return The type of this {@link ExamAccommodation}
     */
    public String getType() {
        return type;
    }

    /**
     * @return The code for this {@link ExamAccommodation}
     */
    public String getCode() {
        return code;
    }

    /**
     * @return A description of what feature the {@link ExamAccommodation} provides
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The time at which this {@link ExamAccommodation} was denied (e.g. by a Proctor)
     */
    public Instant getDeniedAt() {
        return deniedAt;
    }

    /**
     * @return The time at which this {@link ExamAccommodation} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Determine if this {@link ExamAccommodation} is approved or not.
     *
     * @return True if this {@link ExamAccommodation} is approved; otherwise false
     */
    public boolean isApproved() {
        return deniedAt == null;
    }
}
