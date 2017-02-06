package tds.exam.builder;

import org.joda.time.Instant;

import java.util.UUID;

import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;

/**
 * Build an {@link tds.exam.ExamItemResponse} with sample data
 */
public class ExamItemResponseBuilder {
    private static final long DEFAULT_ID = 999L;

    private long id = DEFAULT_ID;
    private UUID examItemId = UUID.randomUUID();
    private String response = "response text";
    private int sequence = 1;
    private boolean selected;
    private boolean valid = true;
    private ExamItemResponseScore score = new ExamItemResponseScoreBuilder().build();
    private Instant createdAt;

    public ExamItemResponse build() {
        return new ExamItemResponse.Builder()
            .withId(id)
            .withExamItemId(examItemId)
            .withResponse(response)
            .withSequence(sequence)
            .withSelected(selected)
            .withValid(valid)
            .withScore(score)
            .withCreatedAt(createdAt)
            .build();
    }

    public ExamItemResponseBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public ExamItemResponseBuilder withExamItemId(UUID examItemId) {
        this.examItemId = examItemId;
        return this;
    }

    public ExamItemResponseBuilder withResponse(String response) {
        this.response = response;
        return this;
    }

    public ExamItemResponseBuilder withSequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    public ExamItemResponseBuilder withSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public ExamItemResponseBuilder withValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    public ExamItemResponseBuilder withScore(ExamItemResponseScore score) {
        this.score = score;
        return this;
    }

    public ExamItemResponseBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
