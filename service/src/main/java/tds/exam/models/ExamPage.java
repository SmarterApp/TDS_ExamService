package tds.exam.models;

import org.joda.time.Instant;

import java.util.UUID;

/**
 * Represents the page of an exam
 */
public class ExamPage {
    private UUID id;
    private int pagePosition;
    private String itemGroupKey;
    private UUID examId;
    private Instant createdAt;
    private Instant deletedAt;
    private Instant startedAt;

    private ExamPage() {}

    public ExamPage(Builder builder) {
        this.id = builder.id;
        this.pagePosition = builder.pagePosition;
        this.itemGroupKey = builder.itemGroupKey;
        this.examId = builder.examId;
        this.createdAt = builder.createdAt;
        this.deletedAt = builder.deletedAt;
        this.startedAt = builder.startedAt;
    }

    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private int pagePosition;
        private String itemGroupKey;
        private UUID examId;
        private Instant createdAt;
        private Instant deletedAt;
        private Instant startedAt;

        public Builder withPagePosition(int pagePosition) {
            this.pagePosition = pagePosition;
            return this;
        }

        public Builder withItemGroupKey(String itemGroupKey) {
            this.itemGroupKey = itemGroupKey;
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

        public ExamPage build() {
            return new ExamPage(this);
        }
    }

    /**
     * @return The id of the {@link tds.exam.models.ExamPage} record
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
     * @return The item group key of the page
     */
    public String getItemGroupKey() {
        return itemGroupKey;
    }

    /**
     * @return The id of the exam this exam page belongs to
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The {@link Instant} for when the {@link tds.exam.models.ExamPage} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The {@link Instant} for when the {@link tds.exam.models.ExamPage} was deleted
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * @return The {@link Instant} for when the {@link tds.exam.models.ExamPage} was rendered to the student
     */
    public Instant getStartedAt() {
        return startedAt;
    }
}
