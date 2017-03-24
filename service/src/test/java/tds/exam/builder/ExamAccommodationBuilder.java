package tds.exam.builder;

import org.joda.time.Instant;

import java.util.UUID;

import tds.exam.ExamAccommodation;

/**
 * Build an {@link tds.exam.ExamAccommodation} populated with test data
 */
public class ExamAccommodationBuilder {
    public static class SampleData {
        public static final UUID DEFAULT_EXAM_ID = UUID.fromString("6b824c7d-0215-4229-ba95-99f1dae5ef04");
        public static final String DEFAULT_SEGMENT_KEY = "segment-1";
        public static final String DEFAULT_ACCOMMODATION_TYPE = "language";
        public static final String DEFAULT_ACCOMMODATION_CODE = "ENU";
        public static final String DEFAULT_ACCOMMODATION_VALUE = "English";
    }

    private UUID id = UUID.randomUUID();
    private UUID examId = SampleData.DEFAULT_EXAM_ID;
    private String segmentKey = SampleData.DEFAULT_SEGMENT_KEY;
    private String type = SampleData.DEFAULT_ACCOMMODATION_TYPE;
    private String code = SampleData.DEFAULT_ACCOMMODATION_CODE;
    private String description = "description";
    private Instant deniedAt = null;
    private Instant createdAt = Instant.now();
    private Instant deletedAt = null;
    private boolean selectable = false;
    private boolean allowChange = false;
    private boolean allowCombine = false;
    private boolean visible = true;
    private boolean disabledOnGuestSession = false;
    private boolean studentControlled = false;
    private boolean defaultAccommodation = true;
    private String dependsOn = null;
    private String value = SampleData.DEFAULT_ACCOMMODATION_VALUE;
    private int segmentPosition = 1;
    private int totalTypeCount = 1;
    private boolean custom;
    private boolean functional = false;

    public ExamAccommodation build() {
        return new ExamAccommodation.Builder(id)
            .withExamId(examId)
            .withSegmentKey(segmentKey)
            .withType(type)
            .withCode(code)
            .withDescription(description)
            .withDeniedAt(deniedAt)
            .withCreatedAt(createdAt)
            .withDeletedAt(deletedAt)
            .withSelectable(selectable)
            .withAllowChange(allowChange)
            .withValue(value)
            .withSegmentPosition(segmentPosition)
            .withTotalTypeCount(totalTypeCount)
            .withCustom(custom)
            .withAllowCombine(allowCombine)
            .withDefaultAccommodation(defaultAccommodation)
            .withVisible(visible)
            .withDisabledOnGuestSession(disabledOnGuestSession)
            .withStudentControlled(studentControlled)
            .withDependsOn(dependsOn)
            .withFunctional(functional)
            .build();
    }

    public ExamAccommodationBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public ExamAccommodationBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public ExamAccommodationBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }

    public ExamAccommodationBuilder withCustom(boolean custom) {
        this.custom = custom;
        return this;
    }

    public ExamAccommodationBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public ExamAccommodationBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public ExamAccommodationBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ExamAccommodationBuilder withDeniedAt(Instant deniedAt) {
        this.deniedAt = deniedAt;
        return this;
    }

    public ExamAccommodationBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ExamAccommodationBuilder withDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public ExamAccommodationBuilder withSelectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    public ExamAccommodationBuilder withAllowChange(boolean allowChange) {
        this.allowChange = allowChange;
        return this;
    }

    public ExamAccommodationBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public ExamAccommodationBuilder withVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public ExamAccommodationBuilder withDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }

    public ExamAccommodationBuilder withStudentControlled(boolean studentControlled) {
        this.studentControlled = studentControlled;
        return this;
    }

    public ExamAccommodationBuilder withDisabledOnGuestSession(boolean disabledOnGuestSession) {
        this.disabledOnGuestSession = disabledOnGuestSession;
        return this;
    }

    public ExamAccommodationBuilder withDefaultAccommodation(boolean defaultAccommodation) {
        this.defaultAccommodation = defaultAccommodation;
        return this;
    }

    public ExamAccommodationBuilder withAllowCombine(boolean allowCombine) {
        this.allowCombine = allowCombine;
        return this;
    }

    public ExamAccommodationBuilder withSegmentPosition(int segmentPosition) {
        this.segmentPosition = segmentPosition;
        return this;
    }

    public ExamAccommodationBuilder withTotalTypeCount(int totalTypeCount) {
        this.totalTypeCount = totalTypeCount;
        return this;
    }

    public ExamAccommodationBuilder withFunctional(boolean functional) {
        this.functional = functional;
        return this;
    }
}
