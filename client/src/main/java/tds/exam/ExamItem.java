package tds.exam;

import com.google.common.base.Optional;
import org.joda.time.Instant;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represent the item on a page of an exam
 */
public class ExamItem {
    private UUID id;
    private UUID examPageId;
    private String itemKey;
    private long assessmentItemBankKey;
    private long assessmentItemKey;
    private String itemType;
    private int position;
    private boolean required;
    private boolean fieldTest;
    private String itemFilePath;
    private String stimulusFilePath;
    private ExamItemResponse response;
    private Instant createdAt;

    /**
     * Private constructor for frameworks
     */
    private ExamItem() {
    }


    private ExamItem(Builder builder) {
        id = checkNotNull(builder.id);
        examPageId = checkNotNull(builder.examPageId);
        itemKey = checkNotNull(builder.itemKey);
        assessmentItemBankKey = checkNotNull(builder.assessmentItemBankKey);
        assessmentItemKey = checkNotNull(builder.assessmentItemKey);
        itemType = checkNotNull(builder.itemType);
        position = builder.position;
        required = builder.required;
        fieldTest = builder.fieldTest;
        itemFilePath = checkNotNull(builder.itemFilePath);
        stimulusFilePath = builder.stimulusFilePath;
        response = builder.response;
        createdAt = builder.createdAt;
    }

    public static final class Builder {
        private UUID id;
        private UUID examPageId;
        private String itemKey;
        private long assessmentItemBankKey;
        private long assessmentItemKey;
        private String itemType;
        private int position;
        private boolean required;
        private boolean fieldTest;
        private String itemFilePath;
        private String stimulusFilePath;
        private ExamItemResponse response;
        private Instant createdAt;

        public Builder(final UUID id) {
            this.id = id;
        }

        public Builder withExamPageId(final UUID examPageId) {
            this.examPageId = examPageId;
            return this;
        }

        public Builder withItemKey(final String itemKey) {
            this.itemKey = itemKey;
            return this;
        }

        public Builder withAssessmentItemBankKey(final long assessmentItemBankKey) {
            this.assessmentItemBankKey = assessmentItemBankKey;
            return this;
        }

        public Builder withAssessmentItemKey(final long assessmentItemKey) {
            this.assessmentItemKey = assessmentItemKey;
            return this;
        }

        public Builder withItemType(final String itemType) {
            this.itemType = itemType;
            return this;
        }

        public Builder withPosition(final int position) {
            if (position < 1) {
                throw new IllegalArgumentException("Item position must be greater than 0");
            }

            this.position = position;
            return this;
        }

        public Builder withRequired(final boolean required) {
            this.required = required;
            return this;
        }

        public Builder withFieldTest(final boolean fieldTest) {
            this.fieldTest = fieldTest;
            return this;
        }

        public Builder withResponse(final ExamItemResponse response) {
            this.response = response;
            return this;
        }

        public Builder withItemFilePath(final String itemFilePath) {
            this.itemFilePath = checkNotNull(itemFilePath, "Item file path cannot be null");
            return this;
        }

        public Builder withStimulusFilePath(final String stimulusFilePath) {
            this.stimulusFilePath = stimulusFilePath;
            return this;
        }

        public Builder withCreatedAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExamItem build() {
            return new ExamItem(this);
        }
    }

    /**
     * @return The id of this {@link tds.exam.ExamItem}
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return The id of the {@link tds.exam.ExamPage} that owns this {@link tds.exam.ExamItem}
     */
    public UUID getExamPageId() {
        return examPageId;
    }

    /**
     * @return The item key for this {@link tds.exam.ExamItem}
     */
    public String getItemKey() {
        return itemKey;
    }

    /**
     * @return The bank key of this {@link tds.exam.ExamItem} as represented in the itembank database
     */
    public long getAssessmentItemBankKey() {
        return assessmentItemBankKey;
    }

    /**
     * @return The item key of this {@link tds.exam.ExamItem} represented in the itembank database
     */
    public long getAssessmentItemKey() {
        return assessmentItemKey;
    }

    /**
     * @return The code representing the type of this {@link tds.exam.ExamItem} (e.g. "ER" for Extended Response, "MS"
     * for Multi-Select)
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * @return The position of this {@link tds.exam.ExamItem} in the exam
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return True if this {@link tds.exam.ExamItem} is marked as required; otherwise false
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return Flag indicating whether this is a field test {@link tds.exam.ExamItem}
     */
    public boolean isFieldTest() {
        return fieldTest;
    }

    /**
     * @return The path to this {@link tds.exam.ExamItem}'s XML file
     */
    public String getItemFilePath() {
        return itemFilePath;
    }

    /**
     * @return The path to this {@link tds.exam.ExamItem}'s stimulus file, if one is present
     */
    public Optional<String> getStimulusFilePath() {
        return Optional.fromNullable(stimulusFilePath);
    }

    /**
     * @return The most recent {@link tds.exam.ExamItemResponse} for this {@link tds.exam.ExamItem}, if there is one
     */
    public Optional<ExamItemResponse> getResponse() {
        return Optional.fromNullable(response);
    }

    /**
     * @return The date/time when this {@link tds.exam.ExamItem} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
