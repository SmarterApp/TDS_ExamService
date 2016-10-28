package tds.exam.builder;

import java.time.Instant;
import java.util.UUID;

import tds.exam.Accommodation;

/**
 * Build an {@link tds.exam.Accommodation} populated with test data
 */
public class AccommodationBuilder {
    public static final UUID FIRST_EXAM_ID = UUID.randomUUID();
    public static final UUID SECOND_EXAM_ID = UUID.randomUUID();
    public static final String FIRST_SEGMENT_ID = "segment-1";
    public static final String SECOND_SEGMENT_ID = "segment-2";
    public static final String THIRD_SEGMENT_ID = "segment-3";

    private long id = 0L;
    private UUID examId = FIRST_EXAM_ID;
    private String segmentId = FIRST_SEGMENT_ID;
    private String type = "type";
    private String code = "code";
    private String description = "description";
    private Instant deniedAt = null;
    private Instant createdAt = Instant.now();

    public Accommodation build() {
        return new Accommodation.Builder()
            .withId(id)
            .withExamId(examId)
            .withSegmentId(segmentId)
            .withType(type)
            .withCode(code)
            .withDescription(description)
            .withDeniedAt(deniedAt)
            .withCreatedAt(createdAt)
            .build();
    }

    public AccommodationBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public AccommodationBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public AccommodationBuilder withSegmentId(String segmentId) {
        this.segmentId = segmentId;
        return this;
    }

    public AccommodationBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public AccommodationBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public AccommodationBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public AccommodationBuilder withDeniedAt(Instant deniedAt) {
        this.deniedAt = deniedAt;
        return this;
    }

    public AccommodationBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
