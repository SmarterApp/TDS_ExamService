package tds.exam;

import org.joda.time.Instant;

import java.util.UUID;

/**
 * Represents a relationship from the student package
 */
public class ExamineeRelationship {
    private long id;
    private UUID examId;
    private String name;
    private String value;
    private String type;
    private ExamineeContext context;
    private Instant createdAt;

    /**
     * Private constructor for frameworks
     */
    private ExamineeRelationship() {
    }

    public ExamineeRelationship(Builder builder) {
        this.id = builder.id;
        this.examId = builder.examId;
        this.name = builder.name;
        this.value = builder.value;
        this.type = builder.type;
        this.context = builder.context;
        this.createdAt = builder.createdAt;
    }

    public static class Builder {
        private long id;
        private UUID examId;
        private String name;
        private String value;
        private String type;
        private ExamineeContext context;
        private Instant createdAt;

        public ExamineeRelationship build() {
            return new ExamineeRelationship(this);
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withExamId(UUID examId) {
            this.examId = examId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withContext(ExamineeContext context) {
            this.context = context;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
    }

    /**
     * @return The unique identifier of this {@link tds.exam.ExamineeRelationship}
     */
    public long getId() {
        return id;
    }

    /**
     * @return The identifier of the {@link tds.exam.Exam} to which this {@link tds.exam.ExamineeRelationship} belongs
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The name (referred to as "tds_id" in the legacy student application) of this
     * {@link tds.exam.ExamineeRelationship}
     */
    public String getName() {
        return name;
    }

    /**
     * @return The value for this {@link tds.exam.ExamineeRelationship}
     */
    public String getValue() {
        return value;
    }

    /**
     * @return The relationship type for this {@link tds.exam.ExamineeRelationship}
     */
    public String getType() {
        return type;
    }

    /**
     * @return The {@link tds.exam.ExamineeContext} describing when this {@link tds.exam.ExamineeRelationship} was
     * captured
     */
    public ExamineeContext getContext() {
        return context;
    }

    /**
     * @return The date and time when this {@link tds.exam.ExamineeRelationship} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
