package tds.exam;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.Instant;

import java.util.UUID;

import static tds.common.util.Preconditions.checkNotNull;

/**
 * An accommodation that is approved for use during an {@link Exam}.
 */
@JsonIgnoreProperties(value="approved", allowGetters=true)
public class ExamAccommodation {
    private UUID id;
    private UUID examId;
    private String segmentKey;
    private int segmentPosition;
    private String type;
    private String code;
    private String value;
    private String description;
    private boolean selectable;
    private boolean allowChange;
    private Instant deniedAt;
    private Instant createdAt;
    private Instant deletedAt;
    private int totalTypeCount;
    private boolean custom;

    /**
     * Private constructor for frameworks
     */
    private ExamAccommodation() {
    }

    public static class Builder {
        private UUID id;
        private UUID examId;
        private String segmentKey;
        private String type;
        private String code;
        private String description;
        private Instant deniedAt;
        private Instant createdAt;
        private Instant deletedAt;
        private boolean selectable;
        private boolean allowChange;
        private String value;
        private int segmentPosition = 1;
        private int totalTypeCount;
        private boolean custom;

        public Builder(UUID id) {
            this.id = id;
        }

        public Builder withExamId(UUID examId) {
            this.examId = checkNotNull(examId, "exam id cannot be null");
            return this;
        }

        public Builder withSegmentKey(String segmentKey) {
            this.segmentKey = checkNotNull(segmentKey, "segmentKey cannot be null");
            return this;
        }

        public Builder withType(String type) {
            this.type = checkNotNull(type);
            return this;
        }

        public Builder withCode(String code) {
            this.code = checkNotNull(code, "code cannot be null");
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
            this.createdAt = createdAt;
            return this;
        }

        public Builder withDeletedAt(Instant deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Builder withSelectable(boolean selectable) {
            this.selectable = selectable;
            return this;
        }

        public Builder withAllowChange(boolean allowChange) {
            this.allowChange = allowChange;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withSegmentPosition(int segmentPosition) {
            this.segmentPosition = segmentPosition;
            return this;
        }

        public Builder withTotalTypeCount(int totalTypeCount) {
            this.totalTypeCount = totalTypeCount;
            return this;
        }

        public Builder withCustom(boolean custom) {
            this.custom = custom;
            return this;
        }

        public static Builder fromExamAccommodation(final ExamAccommodation accommodation) {
            return new Builder(accommodation.getId())
                .withExamId(accommodation.getExamId())
                .withSegmentKey(accommodation.getSegmentKey())
                .withType(accommodation.getType())
                .withCode(accommodation.getCode())
                .withDescription(accommodation.getDescription())
                .withDeniedAt(accommodation.getDeniedAt())
                .withCreatedAt(accommodation.getCreatedAt())
                .withDeletedAt(accommodation.getDeletedAt())
                .withSelectable(accommodation.isSelectable())
                .withAllowChange(accommodation.isAllowChange())
                .withValue(accommodation.getValue())
                .withCustom(accommodation.isCustom());
        }

        public ExamAccommodation build() {
            return new ExamAccommodation(this);
        }
    }

    private ExamAccommodation(Builder builder) {
        id = builder.id;
        examId = builder.examId;
        segmentKey = builder.segmentKey;
        type = builder.type;
        code = builder.code;
        description = builder.description;
        deniedAt = builder.deniedAt;
        createdAt = builder.createdAt;
        deletedAt = builder.deletedAt;
        selectable = builder.selectable;
        allowChange = builder.allowChange;
        value = builder.value;
        segmentPosition = builder.segmentPosition;
        totalTypeCount = builder.totalTypeCount;
        custom = builder.custom;
    }

    /**
     * @return The unique identifier of the {@link ExamAccommodation} record
     */
    public UUID getId() {
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
    public String getSegmentKey() {
        return segmentKey;
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

    /**
     * Delete date
     *
     * @return the delete date
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * @return {@code true} if the accommodation can be selected
     */
    public boolean isSelectable() {
        return selectable;
    }

    /**
     * @return {@code true} if the exam accommodation can be changed
     */
    public boolean isAllowChange() {
        return allowChange;
    }

    /**
     * @return the value of the accommodation
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the segment position associated with this exam accommodation
     */
    public int getSegmentPosition() {
        return segmentPosition;
    }

    /**
     * @return the total number of like types
     */
    public int getTotalTypeCount() {
        return totalTypeCount;
    }

    /**
     * @return {@code true} if the accommodation is not a default
     */
    public boolean isCustom() {
        return custom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExamAccommodation that = (ExamAccommodation) o;

        if (segmentPosition != that.segmentPosition) return false;
        if (!examId.equals(that.examId)) return false;
        if (!type.equals(that.type)) return false;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        int result = examId.hashCode();
        result = 31 * result + segmentPosition;
        result = 31 * result + type.hashCode();
        result = 31 * result + code.hashCode();
        return result;
    }
}
