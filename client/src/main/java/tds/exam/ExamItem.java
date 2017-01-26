package tds.exam;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represent the item on a page of an exam
 */
// TODO:  do we need isValid?
public class ExamItem {
    private long id;
    private long examPageId;
    private String itemKey;
    private long assessmentItemBankKey;
    private long assessmentItemKey;
    private String itemType;
    private int position;
    private boolean required;
    private boolean selected;
    private boolean markedForReview;
    private boolean fieldTest;
    private String itemFilePath;

    private String stimulusFilePath;
    private ExamItemResponse response;

    /**
     * For frameworks
     */
    private ExamItem() {
    }

    public ExamItem(final long id) {
        this.id = id;
    }

    public ExamItem(Builder builder) {
        id = builder.id;
        examPageId = builder.examPageId;
        itemKey = builder.itemKey;
        assessmentItemBankKey = builder.assessmentItemBankKey;
        assessmentItemKey = builder.assessmentItemKey;
        itemType = builder.itemType;
        position = builder.position;
        required = builder.required;
        selected = builder.selected;
        markedForReview = builder.markedForReview;
        fieldTest = builder.fieldTest;
        itemFilePath = builder.itemFilePath;
        stimulusFilePath = builder.stimulusFilePath;
        response = builder.response;
    }

    public static final class Builder {
        private long id;
        private long examPageId;
        private String itemKey;
        private long assessmentItemBankKey;
        private long assessmentItemKey;
        private String itemType;
        private int position;
        private boolean required;
        private boolean selected;
        private boolean markedForReview;
        private boolean fieldTest;
        private String itemFilePath;
        private String stimulusFilePath;
        private ExamItemResponse response;

        public Builder withId(long newId) {
            this.id = newId;
            return this;
        }

        public Builder withExamPageId(long examPageId) {
            this.examPageId = examPageId;
            return this;
        }

        public Builder withItemKey(String itemKey) {
            this.itemKey = itemKey;
            return this;
        }

        public Builder withAssessmentItemBankKey(long assessmentItemBankKey) {
            this.assessmentItemBankKey = assessmentItemBankKey;
            return this;
        }

        public Builder withAssessmentItemKey(long assessmentItemKey) {
            this.assessmentItemKey = assessmentItemKey;
            return this;
        }

        public Builder withItemType(String itemType) {
            this.itemType = itemType;
            return this;
        }

        public Builder withPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder withSelected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public Builder withMarkedForReview(boolean markedForReview) {
            this.markedForReview = markedForReview;
            return this;
        }

        public Builder withFieldTest(boolean fieldTest) {
            this.fieldTest = fieldTest;
            return this;
        }

        public Builder withResponse(ExamItemResponse response) {
            this.response = response;
            return this;
        }

        public Builder withItemFilePath(String itemFilePath) {
            this.itemFilePath = checkNotNull(itemFilePath, "Item file path cannot be null");
            return this;
        }

        public Builder withStimulusFilePath(String stimulusFilePath) {
            this.stimulusFilePath = stimulusFilePath;
            return this;
        }

        public ExamItem build() {
            return new ExamItem(this);
        }
    }

    /**
     * @return The id of the exam item
     */
    public long getId() {
        return id;
    }

    /**
     * @return The id of the {@link tds.exam.ExamPage} that owns this item
     */
    public long getExamPageId() {
        return examPageId;
    }

    /**
     * @return The item key for this item
     */
    public String getItemKey() {
        return itemKey;
    }

    /**
     * @return The bank key of this item as represented in the itembank database
     */
    public long getAssessmentItemBankKey() {
        return assessmentItemBankKey;
    }

    /**
     * @return The item key of this item represented in the itembank database
     */
    public long getAssessmentItemKey() {
        return assessmentItemKey;
    }

    /**
     * @return The code representing the type of item (e.g. "ER" for Extended Response, "MS" for Multi-Select)
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * @return The position of the item in the exam
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return True if this item is marked as required; otherwise false
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return True if the item has been selected; otherwise false
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @return Indicate if the student has marked this item for review
     */
    public boolean isMarkedForReview() {
        return markedForReview;
    }

    /**
     * @return Flag indicating whether this is a field test exam item
     */
    public boolean isFieldTest() {
        return fieldTest;
    }

    /**
     * @return The path to this item's XML file
     */
    public String getItemFilePath() {
        return itemFilePath;
    }

    /**
     * @return The path to this item's stimulus file, if one is present
     */
    public Optional<String> getStimulusFilePath() {
        return Optional.fromNullable(stimulusFilePath);
    }

    /**
     * @return The most recent {@link tds.exam.ExamItemResponse} for this item, if there is one
     */
    public Optional<ExamItemResponse> getResponse() {
        return Optional.fromNullable(response);
    }
}
