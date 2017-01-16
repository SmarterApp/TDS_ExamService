package tds.exam.builder;


import java.util.UUID;

import tds.exam.models.FieldTestItemGroup;

public class FieldTestItemGroupBuilder {
    private String groupKey = "group-key";
    private String groupId = "group-id";
    private String blockId = "A";
    private String languageCode = "ENU";
    private UUID examId = UUID.randomUUID();
    private int numItems = 1;
    private String segmentKey = "segment-key";

    public FieldTestItemGroupBuilder(String groupKey) {
        this.groupKey = groupKey;
    }

    public FieldTestItemGroup build() {
        return new FieldTestItemGroup.Builder()
            .withGroupKey(groupKey)
            .withGroupId(groupId)
            .withBlockId(blockId)
            .withExamId(examId)
            .withLanguageCode(languageCode)
            .withItemCount(numItems)
            .withSegmentKey(segmentKey)
            .build();
    }

    public FieldTestItemGroupBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public FieldTestItemGroupBuilder withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public FieldTestItemGroupBuilder withNumItems(int numItems) {
        this.numItems = numItems;
        return this;
    }

    public FieldTestItemGroupBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }
}
