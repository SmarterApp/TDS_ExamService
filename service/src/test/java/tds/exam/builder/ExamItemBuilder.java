package tds.exam.builder;

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;

/**
 * Build an {@link tds.exam.ExamItem} with test data
 */
public class ExamItemBuilder {
    public static final long EXAM_ITEM_DEFAULT_ID = 999L;

    private long id = EXAM_ITEM_DEFAULT_ID;
    private long examPageId = ExamPageBuilder.DEFAULT_ID;
    private String itemKey = "187-1234";
    private long assessmentItemBankKey = 187L;
    private long assessmentItemKey = 1234L;
    private String itemType = "MS";
    private int position = 1;
    private boolean selected;
    private boolean required;
    private boolean markedForReview;
    private boolean fieldTest;
    private String itemFilePath = "/path/to/item/187-1234.xml";
    private String stimulusFilePath;
    private ExamItemResponse response;

    public ExamItem build() {
        return new ExamItem.Builder()
            .withId(id)
            .withExamPageId(examPageId)
            .withItemKey(itemKey)
            .withAssessmentItemBankKey(assessmentItemBankKey)
            .withAssessmentItemKey(assessmentItemKey)
            .withItemType(itemType)
            .withPosition(position)
            .withRequired(required)
            .withSelected(selected)
            .withMarkedForReview(markedForReview)
            .withFieldTest(fieldTest)
            .withItemFilePath(itemFilePath)
            .withStimulusFilePath(stimulusFilePath)
            .withResponse(response)
            .build();
    }

    public ExamItemBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public ExamItemBuilder withExamPageId(long examPageId) {
        this.examPageId = examPageId;
        return this;
    }

    public ExamItemBuilder withItemKey(String itemKey) {
        this.itemKey = itemKey;
        return this;
    }

    public ExamItemBuilder withAssessmentItemBankKey(long assessmentItemBankKey) {
        this.assessmentItemBankKey = assessmentItemBankKey;
        return this;
    }

    public ExamItemBuilder withAssessmentItemKey(long assessmentItemKey) {
        this.assessmentItemKey = assessmentItemKey;
        return this;
    }

    public ExamItemBuilder withItemType(String itemType) {
        this.itemType = itemType;
        return this;
    }

    public ExamItemBuilder withPosition(int position) {
        this.position = position;
        return this;
    }

    public ExamItemBuilder withRequired(boolean required) {
        this.required = required;
        return this;
    }

    public ExamItemBuilder withSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public ExamItemBuilder withMarkedForReview(boolean markedForReview) {
        this.markedForReview = markedForReview;
        return this;
    }

    public ExamItemBuilder withFieldTest(boolean fieldTest) {
        this.fieldTest = fieldTest;
        return this;
    }

    public ExamItemBuilder withResponse(ExamItemResponse response) {
        this.response = response;
        return this;
    }

    public ExamItemBuilder withItemFilePath(String itemFilePath) {
        this.itemFilePath = itemFilePath;
        return this;
    }

    public ExamItemBuilder withStimulusFilePath(String stimulusFilePath) {
        this.stimulusFilePath = stimulusFilePath;
        return this;
    }
}
