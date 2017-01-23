package tds.exam.builder;

import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tds.exam.models.ExamItem;
import tds.exam.models.ExamPage;

/**
 * Build a {@link tds.exam.models.ExamPage} with test data
 */
public class ExamPageBuilder {
    public static final long DEFAULT_ID = 99999L;

    private long id = DEFAULT_ID;
    private int pagePosition = 1;
    private String segmentKey = "segment-key-1";
    private String segmentId = "segment-id-1";
    private int segmentPosition = 1;
    private String itemGroupKey = "item-group-key";
    private int groupItemsRequired = -1;
    private UUID examId = UUID.randomUUID();
    private List<ExamItem> examItems = new ArrayList<>();
    private Instant createdAt = Instant.now();
    private Instant deletedAt;
    private Instant startedAt;

    public ExamPage build() {
        return new ExamPage.Builder()
            .withId(id)
            .withPagePosition(pagePosition)
            .withSegmentKey(segmentKey)
            .withSegmentId(segmentId)
            .withSegmentPosition(segmentPosition)
            .withItemGroupKey(itemGroupKey)
            .withGroupItemsRequired(groupItemsRequired)
            .withExamId(examId)
            .withExamItems(examItems)
            .withCreatedAt(createdAt)
            .withDeletedAt(deletedAt)
            .withStartedAt(startedAt)
            .build();
    }

    public ExamPageBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public ExamPageBuilder withPagePosition(int pagePosition) {
        this.pagePosition = pagePosition;
        return this;
    }

    public ExamPageBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }

    public ExamPageBuilder withSegmentId(String segmentId) {
        this.segmentId =segmentId;
        return this;
    }

    public ExamPageBuilder withSegmentPosition(int segmentPosition) {
        this.segmentPosition = segmentPosition;
        return this;
    }

    public ExamPageBuilder withItemGroupKey(String itemGroupKey) {
        this.itemGroupKey = itemGroupKey;
        return this;
    }

    public ExamPageBuilder withGroupItemsRequired(int groupItemsRequired) {
        this.groupItemsRequired = groupItemsRequired;
        return this;
    }

    public ExamPageBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public ExamPageBuilder withExamItems(List<ExamItem> examItems) {
        this.examItems = examItems;
        return this;
    }

    public ExamPageBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ExamPageBuilder withDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public ExamPageBuilder withStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
        return this;
    }
}
