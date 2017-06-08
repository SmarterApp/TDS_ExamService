package tds.exam.builder;

import org.joda.time.Instant;

import java.util.UUID;

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;

/**
 * Build an {@link tds.exam.ExamItem} with test data
 */
public class ExamItemBuilder {
    private static final UUID EXAM_ITEM_DEFAULT_ID = UUID.fromString("be1205f4-3cea-4b9b-9d91-86b566c3ada3");

    private UUID id = EXAM_ITEM_DEFAULT_ID;
    private UUID examPageId = ExamPageBuilder.DEFAULT_ID;
    private String itemKey = "187-1234";
    private long assessmentItemBankKey = 187L;
    private long assessmentItemKey = 1234L;
    private String itemType = "MS";
    private int position = 1;
    private boolean required;
    private boolean fieldTest;
    private String itemFilePath = "/path/to/item/187-1234.xml";
    private String stimulusFilePath;
    private ExamItemResponse response;
    private Instant createdAt = Instant.now();
    private String groupId = "I-187-1234";

    public ExamItem build() {
        return new ExamItem.Builder(id)
            .withExamPageId(examPageId)
            .withItemKey(itemKey)
            .withAssessmentItemBankKey(assessmentItemBankKey)
            .withAssessmentItemKey(assessmentItemKey)
            .withItemType(itemType)
            .withPosition(position)
            .withRequired(required)
            .withFieldTest(fieldTest)
            .withItemFilePath(itemFilePath)
            .withStimulusFilePath(stimulusFilePath)
            .withResponse(response)
            .withCreatedAt(createdAt)
            .withGroupId(groupId)
            .build();
    }

    public ExamItemBuilder withId(final UUID id) {
        this.id = id;
        return this;
    }

    public ExamItemBuilder withExamPageId(final UUID examPageId) {
        this.examPageId = examPageId;
        return this;
    }

    public ExamItemBuilder withItemKey(final String itemKey) {
        this.itemKey = itemKey;
        return this;
    }

    public ExamItemBuilder withAssessmentItemBankKey(final long assessmentItemBankKey) {
        this.assessmentItemBankKey = assessmentItemBankKey;
        return this;
    }

    public ExamItemBuilder withAssessmentItemKey(final long assessmentItemKey) {
        this.assessmentItemKey = assessmentItemKey;
        return this;
    }

    public ExamItemBuilder withItemType(final String itemType) {
        this.itemType = itemType;
        return this;
    }

    public ExamItemBuilder withPosition(final int position) {
        this.position = position;
        return this;
    }

    public ExamItemBuilder withRequired(final boolean required) {
        this.required = required;
        return this;
    }

    public ExamItemBuilder withFieldTest(final boolean fieldTest) {
        this.fieldTest = fieldTest;
        return this;
    }

    public ExamItemBuilder withResponse(final ExamItemResponse response) {
        this.response = response;
        return this;
    }

    public ExamItemBuilder withItemFilePath(final String itemFilePath) {
        this.itemFilePath = itemFilePath;
        return this;
    }

    public ExamItemBuilder withStimulusFilePath(final String stimulusFilePath) {
        this.stimulusFilePath = stimulusFilePath;
        return this;
    }

    public ExamItemBuilder withCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ExamItemBuilder withGroupId(final String groupId) {
        this.groupId = groupId;
        return this;
    }
}
