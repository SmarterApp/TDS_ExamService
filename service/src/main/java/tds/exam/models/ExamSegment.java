package tds.exam.models;

import tds.exam.Exam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Represents the segment of an exam.
 *
 */
public class ExamSegment {
    private UUID examId;
    private String assessmentSegmentKey;
    private String assessmentSegmentId;
    private int segmentPosition;
    private String formKey;
    private String formId;
    private String algorithm;
    private int examItemCount;
    private int fieldTestItemCount;
    private List<String> fieldTestItems;
    private boolean isPermeable;
    private String restorePermeableCondition;
    private String formCohort;
    private boolean isSatisfied;
    private Instant dateExited;
    private List<String> itemPool;
    private int poolCount;
    private Instant createdAt;

    public ExamSegment(Builder builder) {
        this.examId = builder.examId;
        this.assessmentSegmentKey = builder.assessmentSegmentKey;
        this.assessmentSegmentId = builder.assessmentSegmentId;
        this.segmentPosition = builder.segmentPosition;
        this.formKey = builder.formKey;
        this.formId = builder.formId;
        this.algorithm = builder.algorithm;
        this.examItemCount = builder.examItemCount;
        this.fieldTestItemCount = builder.fieldTestItemCount;
        this.fieldTestItems = builder.fieldTestItems;
        this.isPermeable = builder.isPermeable;
        this.restorePermeableCondition = builder.restorePermeableCondition;
        this.formCohort = builder.formCohort;
        this.isSatisfied = builder.isSatisfied;
        this.dateExited = builder.dateExited;
        this.itemPool = builder.itemPool;
        this.poolCount = builder.poolCount;
        this.createdAt = builder.createdAt;
    }

    public static class Builder {
        private UUID examId;
        private String assessmentSegmentKey;
        private String assessmentSegmentId;
        private int segmentPosition;
        private String formKey;
        private String formId;
        private String algorithm;
        private int examItemCount;
        private int fieldTestItemCount;
        private List<String> fieldTestItems;
        private boolean isPermeable;
        private String restorePermeableCondition;
        private String formCohort;
        private boolean isSatisfied;
        private Instant dateExited;
        private List<String> itemPool;
        private int poolCount;
        private Instant createdAt;

        public Builder withExamId(UUID newExamId) {
            this.examId = newExamId;
            return this;
        }

        public Builder withAssessmentSegmentKey(String newAssessmentSegmentKey) {
            this.assessmentSegmentKey = newAssessmentSegmentKey;
            return this;
        }

        public Builder withAssessmentSegmentId(String newAssessmentSegmentId) {
            this.assessmentSegmentId = newAssessmentSegmentId;
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

        public Builder withAlgorithm(String newAlgorithm) {
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

        public Builder withFieldTestItems(List<String> newFieldTestItems) {
            this.fieldTestItems = newFieldTestItems;
            return this;
        }

        public Builder withIsPermeable(boolean newIsPermeable) {
            this.isPermeable = newIsPermeable;
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

        public Builder withIsSatisfied(boolean newIsSatisfied) {
            this.isSatisfied = newIsSatisfied;
            return this;
        }

        public Builder withDateExited(Instant newDateExited) {
            this.dateExited = newDateExited;
            return this;
        }

        public Builder withItemPool(List<String> newItemPool) {
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
     * @return The position of the segment in the {@link tds.assessment.Assessment}
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
    public String getAssessmentSegmentKey() {
        return assessmentSegmentKey;
    }

    /**
     * @return the id of the assessment segment
     */
    public String getAssessmentSegmentId() {
        return assessmentSegmentId;
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
    public String getAlgorithm() {
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
     * @return the list of field test item ids, comma delimited.
     */
    public List<String> getFieldTestItems() {
        return fieldTestItems;
    }

    /**
     * @return states whether a segment can be permeated (viewed by a student)
     */
    public boolean isPermeable() {
        return isPermeable;
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
        return isSatisfied;
    }

    /**
     * @return the {@link Instant} the segment was exited
     */
    public Instant getDateExited() {
        return dateExited;
    }

    /**
     * @return the list of item ids in the item pool, comma delimited
     */
    public List<String> getItemPool() {
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
