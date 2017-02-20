package tds.exam;

import org.joda.time.Instant;

import java.util.Set;
import java.util.UUID;

import tds.common.Algorithm;

/**
 * Represents the segment of an exam.
 *
 */
public class ExamSegment {
    private UUID examId;
    private String segmentKey;
    private String segmentId;
    private int segmentPosition;
    private String formKey;
    private String formId;
    private Algorithm algorithm;
    private int examItemCount;
    private int fieldTestItemCount;
    private boolean permeable;
    private String restorePermeableCondition;
    private String formCohort;
    private boolean satisfied;
    private Instant exitedAt;
    private Set<String> itemPool;
    private int poolCount;
    private Instant createdAt;
    
    private ExamSegment() {}

    public ExamSegment(Builder builder) {
        this.examId = builder.examId;
        this.segmentKey = builder.segmentKey;
        this.segmentId = builder.segmentId;
        this.segmentPosition = builder.segmentPosition;
        this.formKey = builder.formKey;
        this.formId = builder.formId;
        this.algorithm = builder.algorithm;
        this.examItemCount = builder.examItemCount;
        this.fieldTestItemCount = builder.fieldTestItemCount;
        this.permeable = builder.permeable;
        this.restorePermeableCondition = builder.restorePermeableCondition;
        this.formCohort = builder.formCohort;
        this.satisfied = builder.satisfied;
        this.exitedAt = builder.exitedAt;
        this.itemPool = builder.itemPool;
        this.poolCount = builder.poolCount;
        this.createdAt = builder.createdAt;
    }

    public static class Builder {
        private UUID examId;
        private String segmentKey;
        private String segmentId;
        private int segmentPosition;
        private String formKey;
        private String formId;
        private Algorithm algorithm;
        private int examItemCount;
        private int fieldTestItemCount;
        private boolean permeable;
        private String restorePermeableCondition;
        private String formCohort;
        private boolean satisfied;
        private Instant exitedAt;
        private Set<String> itemPool;
        private int poolCount;
        private Instant createdAt;

        public Builder withExamId(UUID newExamId) {
            this.examId = newExamId;
            return this;
        }

        public Builder withSegmentKey(String segmentKey) {
            this.segmentKey = segmentKey;
            return this;
        }

        public Builder withSegmentId(String segmentId) {
            this.segmentId = segmentId;
            return this;
        }

        public Builder withSegmentPosition(int newSegmentPosition) {
            this.segmentPosition = newSegmentPosition;
            return this;
        }

        public Builder withFormKey(String newFormKey) {
            this.formKey = newFormKey;
            return this;
        }

        public Builder withFormId(String newFormId) {
            this.formId = newFormId;
            return this;
        }

        public Builder withAlgorithm(Algorithm newAlgorithm) {
            this.algorithm = newAlgorithm;
            return this;
        }

        public Builder withExamItemCount(int newExamItemCount) {
            this.examItemCount = newExamItemCount;
            return this;
        }

        public Builder withFieldTestItemCount(int newFieldTestItemCount) {
            this.fieldTestItemCount = newFieldTestItemCount;
            return this;
        }

        public Builder withPermeable(boolean newPermeable) {
            this.permeable = newPermeable;
            return this;
        }

        public Builder withRestorePermeableCondition(String restorePermeableCondition) {
            this.restorePermeableCondition = restorePermeableCondition;
            return this;
        }

        public Builder withFormCohort(String newFormCohort) {
            this.formCohort = newFormCohort;
            return this;
        }

        public Builder withSatisfied(boolean newIsSatisfied) {
            this.satisfied = newIsSatisfied;
            return this;
        }

        public Builder withExitedAt(Instant exitedAt) {
            this.exitedAt = exitedAt;
            return this;
        }

        public Builder withItemPool(Set<String> newItemPool) {
            this.itemPool = newItemPool;
            return this;
        }

        public Builder withPoolCount(int newPoolCount) {
            this.poolCount = newPoolCount;
            return this;
        }

        public Builder withCreatedAt(Instant newCreatedAt) {
            this.createdAt = newCreatedAt;
            return this;
        }

        public ExamSegment build() {
            return new ExamSegment(this);
        }
    }

    /**
     * @return The position of the segment in the assessment
     */
    public int getSegmentPosition() {
        return segmentPosition;
    }

    /**
     * @return The id to the {@link Exam} associated with this exam segment.
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return the key of the assessment segment
     */
    public String getSegmentKey() {
        return segmentKey;
    }

    /**
     * @return the id of the assessment segment
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * @return the key of the form this segment belongs to (fixed form only)
     */
    public String getFormKey() {
        return formKey;
    }

    /**
     * @return the id of the form this segment belongs to (fixed form only)
     */
    public String getFormId() {
        return formId;
    }

    /**
     * @return the algorithm used by the segment for item selection
     */
    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * @return  the number of total items for this exam.
     */
    public int getExamItemCount() {
        return examItemCount;
    }

    /**
     * @return the number of total field test items for this exam
     */
    public int getFieldTestItemCount() {
        return fieldTestItemCount;
    }

    /**
     * @return states whether a segment can be permeated (viewed by a student)
     */
    public boolean isPermeable() {
        return permeable;
    }

    /**
     * @return the condition for which permeability is restored for
     */
    public String getRestorePermeableCondition() {
        return restorePermeableCondition;
    }

    /**
     * @return the form cohort (fixed form only)
     */
    public String getFormCohort() {
        return formCohort;
    }

    /**
     * @return states whether an exam segment has been "satisfied" (completed) by the student
     */
    public boolean isSatisfied() {
        return satisfied;
    }

    /**
     * @return the {@link Instant} the segment was exited
     */
    public Instant getExitedAt() {
        return exitedAt;
    }

    /**
     * @return the list of item ids in the item pool, comma delimited
     */
    public Set<String> getItemPool() {
        return itemPool;
    }

    /**
     * @return the total number of items in the item pool
     */
    public int getPoolCount() {
        return poolCount;
    }

    /**
     * @return the {@link Instant} for when the {@link ExamSegment} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
