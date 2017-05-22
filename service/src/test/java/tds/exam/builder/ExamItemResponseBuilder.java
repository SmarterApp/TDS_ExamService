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
    private UUID examId = UUID.randomUUID();
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
            .withExamId(examId)
            .withResponse(response)
            .withSequence(sequence)
            .withSelected(selected)
            .withValid(valid)
            .withScore(score)
            .withCreatedAt(createdAt)
            .build();
    }

    public ExamItemResponseBuilder withId(final long id) {
        this.id = id;
        return this;
    }

    public ExamItemResponseBuilder withExamItemId(final UUID examItemId) {
        this.examItemId = examItemId;
        return this;
    }

    public ExamItemResponseBuilder withExamId(final UUID examId) {
        this.examId = examId;
        return this;
    }

    public ExamItemResponseBuilder withResponse(final String response) {
        this.response = response;
        return this;
    }

    public ExamItemResponseBuilder withSequence(final int sequence) {
        this.sequence = sequence;
        return this;
    }

    public ExamItemResponseBuilder withSelected(final boolean selected) {
        this.selected = selected;
        return this;
    }

    public ExamItemResponseBuilder withValid(final boolean valid) {
        this.valid = valid;
        return this;
    }

    public ExamItemResponseBuilder withScore(final ExamItemResponseScore score) {
        this.score = score;
        return this;
    }

    public ExamItemResponseBuilder withCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
