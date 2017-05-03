package tds.exam;

import org.joda.time.Instant;

import java.util.UUID;

import static tds.common.util.Preconditions.checkNotNull;

/**
 * Represents the page of an exam
 */

public class ExamPage {
    private UUID id;
    private int pagePosition;
    private String segmentKey;
    private String itemGroupKey;
    private boolean groupItemsRequired;
    private UUID examId;
    private Instant createdAt;
    private Instant deletedAt;
    private Instant startedAt;
    private long duration;

    /**
     * Private constructor for frameworks
     */
    private ExamPage() {
    }

    private ExamPage(Builder builder) {
        id = checkNotNull(builder.id);
        pagePosition = builder.pagePosition;
        segmentKey = checkNotNull(builder.segmentKey);
        itemGroupKey = checkNotNull(builder.itemGroupKey);
        groupItemsRequired = builder.groupItemsRequired;
        examId = checkNotNull(builder.examId);
        createdAt = builder.createdAt;
        deletedAt = builder.deletedAt;
        startedAt = builder.startedAt;
    }

    public static final class Builder {
        private UUID id;
        private int pagePosition;
        private String segmentKey;
        private String itemGroupKey;
        private boolean groupItemsRequired;
        private UUID examId;
        private Instant createdAt;
        private Instant deletedAt;
        private Instant startedAt;
        private long duration;

        public Builder withPagePosition(int pagePosition) {
            this.pagePosition = pagePosition;
            return this;
        }

        public Builder withSegmentKey(String segmentKey) {
            this.segmentKey = segmentKey;
            return this;
        }

        public Builder withItemGroupKey(String itemGroupKey) {
            this.itemGroupKey = itemGroupKey;
            return this;
        }

        public Builder withGroupItemsRequired(boolean groupItemsRequired) {
            this.groupItemsRequired = groupItemsRequired;
            return this;
        }

        public Builder withExamId(UUID examId) {
            this.examId = examId;
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

        public Builder withStartedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public ExamPage build() {
            return new ExamPage(this);
        }

        public static Builder fromExamPage(ExamPage examPage) {
            return new ExamPage.Builder()
                .withId(examPage.getId())
                .withPagePosition(examPage.getPagePosition())
                .withSegmentKey(examPage.getSegmentKey())
                .withItemGroupKey(examPage.getItemGroupKey())
                .withGroupItemsRequired(examPage.isGroupItemsRequired())
                .withExamId(examPage.getExamId())
                .withCreatedAt(examPage.getCreatedAt())
                .withDeletedAt(examPage.getDeletedAt())
                .withStartedAt(examPage.getStartedAt())
                .withDuration(examPage.getDuration());
        }
    }

    /**
     * @return The id of the {@link ExamPage} record
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * @return The position of the page in the exam - 1 based
     */
    public int getPagePosition() {
        return pagePosition;
    }

    /**
     * @return The key of which segment owns this page
     */
    public String getSegmentKey() {
        return segmentKey;
    }

    /**
     * @return The item group key of the page
     */
    public String getItemGroupKey() {
        return itemGroupKey;
    }

    /**
     * @return True if all items in this page's item group are required; otherwise false
     */
    public boolean isGroupItemsRequired() {
        // This value is only ever 0 or -1 in the database.  When the value is -1 all items are required (from comment
        // in https://github.com/SmarterApp/TDS_TestDeliverySystemDataAccess/blob/cebc3996ab000dc604b539da51235eaf20039d0d/database%20scripts%20-%20mysql/Session/User-Defined%20Functions/iscomplete.sql#L46)

        // In legacy, this value is stored in the session.testeeresponse table.  Since it pertains to an item group
        // (which is a super-set of items), it seems appropriate to store it at the page level.  From here, it can be
        // mapped to the legacy OpportunityItem.
        return groupItemsRequired;
    }

    /**
     * @return The id of the exam this exam page belongs to
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The {@link Instant} for when the {@link ExamPage} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The {@link Instant} for when the {@link ExamPage} was deleted
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * @return The {@link Instant} for when the {@link ExamPage} was rendered to the student
     */
    public Instant getStartedAt() {
        return startedAt;
    }

    /**
     * @return the amount of time spent on a page
     */
    public long getDuration() {
        return duration;
    }
}
