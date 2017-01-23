package tds.exam.models;

import org.joda.time.Instant;

import java.util.List;
import java.util.UUID;

import tds.exam.Exam;

/**
 * Represents the page of an exam
 */
public class ExamPage {
    private long id;
    private int pagePosition;
    private String segmentKey;
    private String segmentId;
    private int segmentPosition;
    private String itemGroupKey;
    private int groupItemsRequired;
    private UUID examId;
    private List<ExamItem> examItems;
    private Instant createdAt;
    private Instant deletedAt;
    private Instant startedAt;

    /**
     * For frameworks
     */
    private ExamPage() {}

    public ExamPage(Builder builder) {
        id = builder.id;
        pagePosition = builder.pagePosition;
        segmentKey = builder.segmentKey;
        segmentId = builder.segmentId;
        segmentPosition = builder.segmentPosition;
        itemGroupKey = builder.itemGroupKey;
        groupItemsRequired = builder.groupItemsRequired;
        examId = builder.examId;
        examItems = builder.examItems;
        createdAt = builder.createdAt;
        deletedAt = builder.deletedAt;
        startedAt = builder.startedAt;
    }

    public static final class Builder {
        private long id;
        private int pagePosition;
        private String segmentKey;
        private String segmentId;
        private int segmentPosition;
        private String itemGroupKey;
        private int groupItemsRequired;
        private UUID examId;
        private List<ExamItem> examItems;
        private Instant createdAt;
        private Instant deletedAt;
        private Instant startedAt;

        public Builder fromExamPage(ExamPage examPage) {
            id = examPage.id;
            pagePosition = examPage.pagePosition;
            segmentKey = examPage.segmentKey;
            segmentId = examPage.segmentId;
            segmentPosition = examPage.segmentPosition;
            itemGroupKey = examPage.itemGroupKey;
            groupItemsRequired = examPage.groupItemsRequired;
            examId = examPage.examId;
            examItems = examPage.examItems;
            createdAt = examPage.createdAt;
            deletedAt = examPage.deletedAt;
            startedAt = examPage.startedAt;
            return this;
        }

        public Builder withPagePosition(int pagePosition) {
            this.pagePosition = pagePosition;
            return this;
        }

        public Builder withSegmentKey(String segmentKey) {
            this.segmentKey = segmentKey;
            return this;
        }

        public Builder withSegmentId(String segmentId) {
            this.segmentId = segmentId;
            return this;
        }

        public Builder withSegmentPosition(int segmentPosition) {
            this.segmentPosition = segmentPosition;
            return this;
        }

        public Builder withItemGroupKey(String itemGroupKey) {
            this.itemGroupKey = itemGroupKey;
            return this;
        }

        public Builder withGroupItemsRequired(int groupItemsRequired) {
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

        public Builder withExamItems(List<ExamItem> examItems) {
            this.examItems = examItems;
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

        public Builder withId(long id) {
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
    public long getId() {
        return this.id;
    }
    /**
     * @return The position of the page in the exam - 1 based
     */
    public int getPagePosition() {
        return pagePosition;
    }

    /**
     * @return The key of which {@link tds.exam.models.ExamSegment} owns this page
     */
    public String getSegmentKey() {
        return segmentKey;
    }

    /**
     * @return The id of which {@link tds.exam.models.ExamSegment} owns this page
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * @return The position of the {@link tds.exam.models.ExamSegment} owns this page
     */
    public int getSegmentPosition() {
        return segmentPosition;
    }

    /**
     * @return The item group key of the page
     */
    public String getItemGroupKey() {
        return itemGroupKey;
    }

    /**
     * @return Determine if all group items are required
     */
    public int getGroupItemsRequired() {
        // This value is only ever 0 or -1 in the database.  When the value is -1 all items are required (from comment
        // in https://github.com/SmarterApp/TDS_TestDeliverySystemDataAccess/blob/cebc3996ab000dc604b539da51235eaf20039d0d/database%20scripts%20-%20mysql/Session/User-Defined%20Functions/iscomplete.sql#L46

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
     * @return The collection of {@link tds.exam.models.ExamItem}s for this page
     */
    public List<ExamItem> getExamItems() {
        return examItems;
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
