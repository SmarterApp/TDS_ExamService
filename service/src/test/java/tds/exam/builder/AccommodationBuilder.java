package tds.exam.builder;

import java.time.Instant;
import java.util.UUID;

import tds.exam.Accommodation;

/**
 * Build an {@link tds.exam.Accommodation} populated with test data
 */
public class AccommodationBuilder {
    public static class SampleData {
        public static final UUID DEFAULT_EXAM_ID = UUID.fromString("6b824c7d-0215-4229-ba95-99f1dae5ef04");
        public static final String DEFAULT_SEGMENT_ID = "segment-1";
        public static final String DEFAULT_ACCOMMODATION_TYPE = "language";
        public static final String DEFAULT_ACCOMMODATION_CODE = "ENU";
    }

    private long id = 0L;
    private UUID examId = SampleData.DEFAULT_EXAM_ID;
    private String segmentId = SampleData.DEFAULT_SEGMENT_ID;
    private String type = SampleData.DEFAULT_ACCOMMODATION_TYPE;
    private String code = SampleData.DEFAULT_ACCOMMODATION_CODE;
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
