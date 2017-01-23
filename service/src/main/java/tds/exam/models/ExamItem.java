package tds.exam.models;

import org.joda.time.Instant;

import tds.assessment.Item;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represent the item on a page of an exam
 */
public class ExamItem {
    private long id;
    private long examPageId;
    private String itemKey;
    private int position;
    private boolean selected;
    private boolean markedForReview;
    private boolean fieldTest;
    private ExamItemResponse response;
    private Item assessmentItem;

    /**
     * For frameworks
     */
    private ExamItem() {}

    public ExamItem(final long id) {
        this.id = id;
    }

    public ExamItem(Builder builder) {
        id = builder.id;
        examPageId = builder.examPageId;
        itemKey = builder.itemKey;
        position = builder.position;
        selected = builder.selected;
        markedForReview = builder.markedForReview;
        fieldTest = builder.fieldTest;
        response = builder.response;
        assessmentItem = builder.assessmentItem;
    }

    public static final class Builder {
        private long id;
        private long examPageId;
        private String itemKey;
        private int position;
        private boolean selected;
        private boolean markedForReview;
        private boolean fieldTest;
        private ExamItemResponse response;
        private Item assessmentItem;

        public Builder fromExamItem(ExamItem examItem) {
            id = examItem.id;
            examPageId = examItem.examPageId;
            itemKey = examItem.itemKey;
            position = examItem.position;
            selected = examItem.selected;
            markedForReview = examItem.markedForReview;
            fieldTest = examItem.fieldTest;
            response = examItem.response;
            assessmentItem = examItem.assessmentItem;

            return this;
        }

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

        public Builder withPosition(int position) {
            this.position = position;
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

        public Builder withAssessmentItem(Item assessmentItem) {
            this.assessmentItem = checkNotNull(assessmentItem, "Assessment Item cannot be null");
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
     * @return The id of the {@link tds.exam.models.ExamPage} that owns this item
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
     * @return The position of the item in the exam
     */
    public int getPosition() {
        return position;
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
     * @return The most recent {@link tds.exam.models.ExamItemResponse} for this item
     */
    public ExamItemResponse getResponse() {
        return response;
    }

    /**
     * @return The {@link tds.assessment.Item} that this item is built from // TODO better description here
     */
    public Item getAssessmentItem() {
        return assessmentItem;
    }

    /**
     * @return The bank key of the {@link tds.assessment.Item} this item represents
     */
    public long getAssessmentBankKey() {
        // Key convention for an item: [bank key]-[item key], e.g. 187-2602
        return Long.parseLong(assessmentItem.getId().split("-")[0]);
    }

    /**
     * @return The item key of the {@link tds.assessment.Item} this item represents
     */
    public long getAssessmentItemKey() {
        // Key convention for an item: [bank key]-[item key], e.g. 187-2602
        return Long.parseLong(assessmentItem.getId().split("-")[1]);
    }

    /**
     * @return The path to the XML file for the {@link tds.assessment.Item} this item represents
     */
    public String getAssessmentItemFilePath() {
        return assessmentItem.getItemFilePath();
    }

    /**
     * @return The path to the XML file describing the stimulus that accompanies this item.
     */
    public String getAssessmentItemStimulusPath() {
        // While it should always have an item XML file, an assessment item might not have an accompanying stimulus XML
        // file
        return assessmentItem.getStimulusFilePath() == null
            ? ""
            : assessmentItem.getStimulusFilePath();
    }

    /**
     * @return The item type (also referred to as format) of the {@link tds.assessment.Item} this item represents
     */
    public String getAssessmentItemType() {
        return assessmentItem.getItemType();
    }

    /**
     * @return True if the {@link tds.assessment.Item} is marked as required; otherwise false
     */
    public boolean getAssessmentItemIsRequired() {
        return assessmentItem.isRequired();
    }

    /**
     * @return Return the text of the response as provided by the student, if one is provided
     */
    public String getResponseText() {
        if (response == null) {
            return "";
        }

        return response.getResponse() == null
            ? ""
            : response.getResponse();
    }

    /**
     * @return The length of the response, if one is provided
     */
    public int getResponseLength() {
        if (response == null) {
            return 0;
        }

        return response.getResponse() == null
            ? 0
            : response.getResponse().length();
    }

    /**
     * @return The time when the most recent response to this item was created, if one is available
     */
    public Instant getRespondedAt() {
        if (response == null) {
            return null;
        }

        return response.getCreatedAt();
    }
}
