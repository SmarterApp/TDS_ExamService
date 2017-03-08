package tds.exam.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a field test item group
 */
public class FieldTestItemGroup {
    private long id;
    private String segmentKey;
    private UUID examId;
    private int position;
    private int itemCount;
    private String groupId;
    private String groupKey;
    private String blockId;
    private String segmentId;
    private UUID sessionId;
    private String languageCode;
    private Instant createdAt;
    private Instant deletedAt;
    private Integer positionAdministered;
    private Instant administeredAt;

    //Exists for frameworks
    private FieldTestItemGroup() {}

    public FieldTestItemGroup(Builder builder) {
        this.id = builder.id;
        this.segmentKey = builder.segmentKey;
        this.examId = builder.examId;
        this.position = builder.position;
        this.itemCount = builder.itemCount;
        this.groupId = builder.groupId;
        this.groupKey = builder.groupKey;
        this.blockId = builder.blockId;
        this.segmentId = builder.segmentId;
        this.sessionId = builder.sessionId;
        this.languageCode = builder.languageCode;
        this.administeredAt = builder.administeredAt;
        this.createdAt = builder.createdAt;
        this.positionAdministered = builder.positionAdministered;
        this.deletedAt = builder.deletedAt;
    }

    /**
     * @return The id of the {@link tds.exam.models.FieldTestItemGroup}
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return The group id of the {@link tds.exam.models.FieldTestItemGroup}
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return The key of the {@link tds.assessment.Segment} the {@link tds.exam.models.FieldTestItemGroup} is a part of
     */
    public String getSegmentKey() {
        return segmentKey;
    }

    /**
     * @return The id of the {@link tds.assessment.Segment} the {@link tds.exam.models.FieldTestItemGroup} is a part of
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * @return The id of the {@link tds.session.Session} the {@link tds.exam.models.FieldTestItemGroup} is a part of
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @return The id of the {@link tds.exam.Exam} this {@link tds.exam.models.FieldTestItemGroup} is a part of
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The position of the {@link tds.exam.models.FieldTestItemGroup} in the {@link tds.exam.Exam}
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return The number of field test {@link tds.assessment.Item}s in the {@link tds.exam.models.FieldTestItemGroup}
     */
    public int getItemCount() {
        return itemCount;
    }

    /**
     * @return The group key of the {@link tds.exam.models.FieldTestItemGroup}. Usually in the form of "groupid_blockid".
     */
    public String getGroupKey() {
        return groupKey;
    }

    /**
     * @return The block id of the {@link tds.exam.models.FieldTestItemGroup}
     */
    public String getBlockId() {
        return blockId;
    }

    /**
     * @return The language code of the {@link tds.exam.Exam}
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * @return The {@link java.time.Instant} the {@link tds.exam.models.FieldTestItemGroup} was assigned at
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The {@link java.time.Instant}
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * @return A flag that determines whether the {@link tds.exam.models.FieldTestItemGroup} is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * @return The position in the exam the {@link tds.exam.models.FieldTestItemGroup} was actually administered at
     */
    public Integer getPositionAdministered() {
        return positionAdministered;
    }

    /**
     * @return The {@link java.time.Instant} the item was administered at
     */
    public Instant getAdministeredAt() {
        return administeredAt;
    }

    public static class Builder {
        private long id;
        private String segmentKey;
        private UUID examId;
        private int position;
        private int itemCount;
        private String groupId;
        private String groupKey;
        private String blockId;
        private String segmentId;
        private UUID sessionId;
        private String languageCode;
        private Instant createdAt;
        private Instant deletedAt;
        private Integer positionAdministered;
        private Instant administeredAt;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withSegmentKey(String segmentKey) {
            this.segmentKey = segmentKey;
            return this;
        }

        public Builder withExamId(UUID examId) {
            this.examId = examId;
            return this;
        }

        public Builder withPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder withItemCount(int itemCount) {
            this.itemCount = itemCount;
            return this;
        }

        public Builder withGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder withGroupKey(String groupKey) {
            this.groupKey = groupKey;
            return this;
        }

        public Builder withBlockId(String blockId) {
            this.blockId = blockId;
            return this;
        }

        public Builder withSegmentId(String segmentId) {
            this.segmentId = segmentId;
            return this;
        }

        public Builder withSessionId(UUID sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder withLanguageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withDeletedAt(Instant deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Builder withPositionAdministered(Integer positionAdministered) {
            this.positionAdministered = positionAdministered;
            return this;
        }

        public Builder withAdministeredAt(Instant administeredAt) {
            this.administeredAt = administeredAt;
            return this;
        }

        public Builder fromFieldTestItemGroup(final FieldTestItemGroup fieldTestItemGroup) {
            this.id = fieldTestItemGroup.id;
            this.segmentKey = fieldTestItemGroup.segmentKey;
            this.examId = fieldTestItemGroup.examId;
            this.position = fieldTestItemGroup.position;
            this.itemCount = fieldTestItemGroup.itemCount;
            this.groupId = fieldTestItemGroup.groupId;
            this.groupKey = fieldTestItemGroup.groupKey;
            this.blockId = fieldTestItemGroup.blockId;
            this.segmentId = fieldTestItemGroup.segmentId;
            this.sessionId = fieldTestItemGroup.sessionId;
            this.languageCode = fieldTestItemGroup.languageCode;
            this.administeredAt = fieldTestItemGroup.administeredAt;
            this.createdAt = fieldTestItemGroup.createdAt;
            this.positionAdministered = fieldTestItemGroup.positionAdministered;
            this.deletedAt = fieldTestItemGroup.deletedAt;
            return this;
        }

        public FieldTestItemGroup build() {
            return new FieldTestItemGroup(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldTestItemGroup that = (FieldTestItemGroup) o;

        if (!examId.equals(that.examId)) return false;
        if (!languageCode.equals(that.languageCode)) return false;
        if (!segmentKey.equals(that.segmentKey)) return false;
        return groupKey.equals(that.groupKey);
    }

    @Override
    public int hashCode() {
        int result = examId.hashCode();
        result = 31 * result + languageCode.hashCode();
        result = 31 * result + segmentKey.hashCode();
        result = 31 * result + groupKey.hashCode();
        return result;
    }
}
