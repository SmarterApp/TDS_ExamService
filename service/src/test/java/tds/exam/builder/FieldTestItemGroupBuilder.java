package tds.exam.builder;


import java.time.Instant;
import java.util.UUID;

import tds.exam.models.FieldTestItemGroup;

public class FieldTestItemGroupBuilder {
    private String groupKey = "group-key";
    private String groupId = "group-id";
    private String blockId = "A";
    private String languageCode = "ENU";
    private UUID examId = UUID.randomUUID();
    private int itemCount = 1;
    private String segmentKey = "segment-key";
    private Instant deletedAt;
    private Integer positionAdministered = null;
    private Instant administeredAt;

    public FieldTestItemGroupBuilder(String groupKey) {
        this.groupKey = groupKey;
    }

    public FieldTestItemGroup build() {
        FieldTestItemGroup.Builder builder = new FieldTestItemGroup.Builder()
            .withGroupKey(groupKey)
            .withGroupId(groupId)
            .withBlockId(blockId)
            .withExamId(examId)
            .withLanguageCode(languageCode)
            .withItemCount(itemCount)
            .withSegmentKey(segmentKey)
            .withAdministeredAt(administeredAt)
            .withDeletedAt(deletedAt);

        if(positionAdministered != null) {
            builder.withPosition(positionAdministered);
        }

        return builder.build();
    }

    public FieldTestItemGroupBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public FieldTestItemGroupBuilder withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public FieldTestItemGroupBuilder withItemCount(int itemCount) {
        this.itemCount = itemCount;
        return this;
    }

    public FieldTestItemGroupBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }

    public FieldTestItemGroupBuilder withDeletedAt(final Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public FieldTestItemGroupBuilder withPositionAdministered(final Integer positionAdministered) {
        this.positionAdministered = positionAdministered;
        return this;
    }

    public FieldTestItemGroupBuilder withAdministeredAt(final Instant administeredAt) {
        this.administeredAt = administeredAt;
        return this;
    }
}
