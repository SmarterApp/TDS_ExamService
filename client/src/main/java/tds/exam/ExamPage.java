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
    private int groupItemsRequired;
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
        private int groupItemsRequired;
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

        public Builder withGroupItemsRequired(final int groupItemsRequired) {
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
                .withGroupItemsRequired(examPage.getGroupItemsRequired())
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
     * @return the number of items required for the group.  -1 means that the all the items in the
     * group are required.
     */
    public int getGroupItemsRequired() {
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
