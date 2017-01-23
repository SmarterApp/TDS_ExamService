package tds.exam.builder;

import tds.assessment.Item;
import tds.exam.models.ExamItem;
import tds.exam.models.ExamItemResponse;

/**
 * Build an {@link tds.exam.models.ExamItem} with test data
 */
public class ExamItemBuilder {
    public static final long EXAM_ITEM_DEFAULT_ID = 999L;

    private long id = EXAM_ITEM_DEFAULT_ID;
    private long examPageId = ExamPageBuilder.DEFAULT_ID;
    private String itemKey = "item-key-1";
    private int position = 1;
    private boolean selected;
    private boolean markedForReview;
    private boolean fieldTest;
    private ExamItemResponse response;
    private Item assessmentItem = new Item(itemKey);

    public ExamItem build() {
        return new ExamItem.Builder()
            .withId(id)
            .withExamPageId(examPageId)
            .withItemKey(itemKey)
            .withPosition(position)
            .withSelected(selected)
            .withMarkedForReview(markedForReview)
            .withFieldTest(fieldTest)
            .withResponse(response)
            .withAssessmentItem(assessmentItem)
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

    public ExamItemBuilder withPosition(int position) {
        this.position = position;
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

    public ExamItemBuilder withAssessmentItem(Item assessmentItem) {
        this.assessmentItem = assessmentItem;
        return this;
    }
}
