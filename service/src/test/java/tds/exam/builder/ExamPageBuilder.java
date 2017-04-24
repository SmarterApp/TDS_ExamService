package tds.exam.builder;

import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tds.exam.ExamItem;
import tds.exam.ExamPage;

/**
 * Build a {@link tds.exam.ExamPage} with test data
 */
public class ExamPageBuilder {
    public static final UUID DEFAULT_ID = UUID.fromString("b868561f-8264-42b1-80ce-e812f2bad7f7");

    private UUID id = DEFAULT_ID;
    private int pagePosition = 1;
    private String segmentKey = "segment-key-1";
    private String segmentId = "segment-id-1";
    private int segmentPosition = 1;
    private String itemGroupKey = "item-group-key";
    private boolean groupItemsRequired = true;
    private UUID examId = UUID.randomUUID();
    private List<ExamItem> examItems = new ArrayList<>();
    private Instant createdAt = Instant.now();
    private Instant deletedAt;
    private Instant startedAt;
    private long duration = 100;

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
            .withDuration(duration)
            .build();
    }

    public ExamPageBuilder withId(UUID id) {
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
        this.segmentId = segmentId;
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

    public ExamPageBuilder withGroupItemsRequired(boolean groupItemsRequired) {
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

    public ExamPageBuilder withDuration(long duration) {
        this.duration = duration;
        return this;
    }
}
